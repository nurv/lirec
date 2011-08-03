/** 
 * PleoConnectionService.java - Android service responsible for the communication with the PhyPleo.
 * When it receives a request, and is able to connect to PhyPleo, it launches a thread
 * responsible for either setting properties or loading them.
 *  
 * Copyright (C) 2011 GAIPS/INESC-ID 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * Company: GAIPS/INESC-ID
 * Project: Pleo Scenario
 * @author: Paulo F. Gomes
 * Email to: pgomes@gaips.inesc-id.pt
 */

package eu.lirec.pleo;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class PleoConnectionService extends Service {
	private static final String LOG_TAG = "PleoConnectionService";
	private static final UUID SPP_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final int CONNECTION_CREATION_INTERVAL = 2000;
	private static final String PLEO_STATE_FILE_NAME = "needs.xml";
	private static final String CONNECTION_CONFIGURATION_FILE_NAME = "configuration.xml";

	public static final String START_ACTION = "eu.lirec.pleo.PleoConnectionService.action.START";
	public static final String UNLOAD_PLEO_BEHAVIOR_ACTION = "eu.lirec.pleo.PleoConnectionService.action.UNLOAD_PLEO_BEHAVIOR";
	// LOAD_PLEO_BEHAVIOR_ACTION is currently not being used in requests to this service.
	public static final String LOAD_PLEO_BEHAVIOR_ACTION = "eu.lirec.pleo.PleoConnectionService.action.LOAD_PLEO_BEHAVIOR";
	public static final String LOAD_PLEO_BEHAVIOR_TRY_ACTION = "eu.lirec.pleo.PleoConnectionService.action.LOAD_PLEO_BEHAVIOR_TRY";
	public static final String LOAD_PLEO_BEHAVIOR_SIMPLE_ACTION = "eu.lirec.pleo.PleoConnectionService.action.LOAD_PLEO_BEHAVIOR_SIMPLE";
	
	public static final String DISABLING_MONITOR_WARNING_INTENT_ACTION = "eu.lirec.pleo.PleoConnectionService.action.DISABLING_MONITOR_WARNING";
	public static final String CONNECTED_INTENT_ACTION = "eu.lirec.pleo.PleoConnectionService.action.CONNECTED";
	public static final String SHUTDOWN_WARNING_INTENT_ACTION = "eu.lirec.pleo.PleoConnectionService.action.SHUTDOWN_WARNING";
	public static final String SERVICE_ON_INTENT_ACTION = "eu.lirec.pleo.PleoConnectionService.action.SERVICE_ON";
	public static final String MIGRATING_TO_PLEO_INTENT_ACTION = "eu.lirec.pleo.PleoConnectionService.action.MIGRATING_TO_PLEO";
	public static final String MIGRATED_TO_PLEO_INTENT_ACTION = "eu.lirec.pleo.PleoConnectionService.action.MIGRATED_TO_PLEO";

	private BluetoothAdapter _bluetoothAdapter = null;
	private BluetoothDevice _pleoBluetoothDevice = null;
	private BluetoothSocket _pleoBluetoothSocket = null;
	private final ReentrantLock _isConnectedToPleoLock = new ReentrantLock();
	private boolean _isConnectedToPleo = false;
	private String _pleoBluetoothDongleMACAddress;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(LOG_TAG, "++ ON CREATE ++");

		try {
			_pleoBluetoothDongleMACAddress = loadPleoConnectionConfiguration();
			announceServiceOn();
		} catch (ConfigurationXMLParserException exception) {
			Log.e(LOG_TAG, "Unable to load Pleo Connection Configuration"
					+ exception.getMessage());
			stopSelf();
		}
	}

	private void loadMyPleoNeeds(MyPleoNeeds myPleoNeeds)
			throws MyPleoNeedsParserException {
		File needsFile = new File(getExternalFilesDir(null),
				PLEO_STATE_FILE_NAME);
		try {
			SAXParserFactory needsFileParserFactory = SAXParserFactory
					.newInstance();
			SAXParser needsFileParser = needsFileParserFactory.newSAXParser();
			MyPleoNeedsParserHandler myPleoNeedsParserHandler = new MyPleoNeedsParserHandler(
					myPleoNeeds);
			needsFileParser.parse(needsFile, myPleoNeedsParserHandler);
		} catch (IOException e) {
			throw new MyPleoNeedsParserException("Error reading " + needsFile
					+ " from directory " + getExternalFilesDir(null), e);
		} catch (SAXException e) {
			throw new MyPleoNeedsParserException("Error parsing " + needsFile
					+ " from directory " + getExternalFilesDir(null), e);
		} catch (ParserConfigurationException e) {
			throw new MyPleoNeedsParserException("Error parsing " + needsFile
					+ " from directory " + getExternalFilesDir(null), e);
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String intentAction = intent.getAction();
		
		if(intentAction.compareTo(LOAD_PLEO_BEHAVIOR_TRY_ACTION) == 0 ||
		   intentAction.compareTo(PleoConnectionService.START_ACTION) == 0){
			connectWeak();
		}else
			connect();
			
		_isConnectedToPleoLock.lock();
		try {
			if (intentAction.compareTo(UNLOAD_PLEO_BEHAVIOR_ACTION) == 0
					&& _isConnectedToPleo) {
				unloadPleoBehavior();
			} else if ((intentAction.compareTo(LOAD_PLEO_BEHAVIOR_ACTION) == 0  || intentAction.compareTo(LOAD_PLEO_BEHAVIOR_TRY_ACTION) == 0)
					&& _isConnectedToPleo) {
				announceMigratingToPleo();
				loadPleoBehavior();
			} else if(intentAction.compareTo(LOAD_PLEO_BEHAVIOR_SIMPLE_ACTION) == 0){
				loadPleoBehaviorSimple();
			}
		} catch (InterruptedException e) {
			Log.e(LOG_TAG, "Interrupted while sending command to Pleo.");
		} finally {
			_isConnectedToPleoLock.unlock();
		}

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(LOG_TAG, "-- ON DESTROY --");

		_isConnectedToPleoLock.lock();
		try {
			if (_isConnectedToPleo)
				cleanSocket();
		} finally {
			_isConnectedToPleoLock.unlock();
		}

	}

	// Connects to Pleo and remains connected if already connected.
	public void connect() {
		_isConnectedToPleoLock.lock();
		try {
			if (!_isConnectedToPleo) {
				_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				_pleoBluetoothDevice = _bluetoothAdapter
						.getRemoteDevice(_pleoBluetoothDongleMACAddress);

				tryConnectingToPleo();
				_isConnectedToPleo = true;
				announceConnected();
			}
		} catch (BluetoothDongleNotBondedException exception) {
			Log.e(LOG_TAG, "Bluetooth dongle (" + exception.getMessage()
					+ ") not bonded.");
		} finally {
			_isConnectedToPleoLock.unlock();
		}

		return;
	}
	
	public void connectWeak() {
		_isConnectedToPleoLock.lock();
		try {
			if (!_isConnectedToPleo) {
				_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				_pleoBluetoothDevice = _bluetoothAdapter
						.getRemoteDevice(_pleoBluetoothDongleMACAddress);

				_isConnectedToPleo = tryConnectingToPleoWeak();
				if(_isConnectedToPleo)
					announceConnected();
			}
		} catch (BluetoothDongleNotBondedException exception) {
			Log.e(LOG_TAG, "Bluetooth dongle (" + exception.getMessage()
					+ ") not bonded.");
		} finally {
			_isConnectedToPleoLock.unlock();
		}

		return;
	}

	private String loadPleoConnectionConfiguration()
			throws ConfigurationXMLParserException {

		File pleoConnectionConfigurationFile = new File(
				getExternalFilesDir(null), CONNECTION_CONFIGURATION_FILE_NAME);
		String pleoBluetoothDongleMACAddress = "";

		try {
			ConfigurationXMLParserHandler configurationXMLParserHandler = new ConfigurationXMLParserHandler();
			SAXParserFactory pleoConnectionConfigurationFileParserFactory = SAXParserFactory
					.newInstance();
			SAXParser pleoConnectionConfigurationFileParser = pleoConnectionConfigurationFileParserFactory
					.newSAXParser();
			pleoConnectionConfigurationFileParser.parse(
					pleoConnectionConfigurationFile,
					configurationXMLParserHandler);

			pleoBluetoothDongleMACAddress = configurationXMLParserHandler
					.getPleoBluetoothDongleMACAddress();
		} catch (IOException e) {
			throw new ConfigurationXMLParserException(
					"Error reading " + pleoConnectionConfigurationFile
							+ " from directory " + getExternalFilesDir(null), e);
		} catch (SAXException e) {
			throw new ConfigurationXMLParserException(
					"Error parsing " + pleoConnectionConfigurationFile
							+ " from directory " + getExternalFilesDir(null), e);
		} catch (ParserConfigurationException e) {
			throw new ConfigurationXMLParserException(
					"Error parsing " + pleoConnectionConfigurationFile
							+ " from directory " + getExternalFilesDir(null), e);
		}

		return pleoBluetoothDongleMACAddress;
	}

	private void unloadPleoBehavior() throws InterruptedException {
		LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<String>();

		commandQueue.put("clear");
		commandQueue.put("property set 20484 1");
		commandQueue.put("property show");

		Runnable monitorRunnable = new PleoMonitorRunnable(this,_pleoBluetoothSocket,commandQueue);
		Thread monitorThread = new Thread(monitorRunnable,"monitorUnloadPleoBehaviourThread");
		monitorThread.setDaemon(true);
		monitorThread.start();
	}

	private void loadPleoBehavior() throws InterruptedException {
		LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<String>();

		try {
			MyPleoNeeds myPleoNeeds = new MyPleoNeeds();
			loadMyPleoNeeds(myPleoNeeds);

			commandQueue.put("clear");
			commandQueue.put("property set 20480 "
					+ myPleoNeeds.getNeedCleanliness());
			commandQueue.put("property set 6 " + myPleoNeeds.getNeedEnergy());
			commandQueue.put("property set 20481 "
					+ myPleoNeeds.getNeedPetting());
			commandQueue.put("property set 20482 "
					+ myPleoNeeds.getNeedSkills());
			commandQueue
					.put("property set 20483 " + myPleoNeeds.getNeedWater());

		} catch (MyPleoNeedsParserException e) {
			Log.w(LOG_TAG, e.getMessage(), e);
		}

		commandQueue.put("property set 20484 0");

		Runnable monitorRunnable = new PleoMonitorRunnable(this,_pleoBluetoothSocket,commandQueue);
		Thread monitorThread = new Thread(monitorRunnable,"monitorLoadPleoBehaviourThread");
		monitorThread.setDaemon(true);
		monitorThread.start();
	}
	
	private void loadPleoBehaviorSimple() throws InterruptedException {
		LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<String>();

		commandQueue.put("property set 20484 0");

		Runnable monitorRunnable = new PleoMonitorRunnable(this,_pleoBluetoothSocket,commandQueue);
		Thread monitorThread = new Thread(monitorRunnable,"monitorLoadPleoBehaviourThread");
		monitorThread.setDaemon(true);
		monitorThread.start();
	}

	private class BluetoothDongleNotBondedException extends Exception {
		private static final long serialVersionUID = -2481455177765725098L;

		BluetoothDongleNotBondedException(String s) {
			super(s);
		}
	}

	private void tryConnectingToPleo() throws BluetoothDongleNotBondedException {

		while (_pleoBluetoothSocket == null) {

			try {
				connectToPleo();
				Log.i(LOG_TAG, "Connected to Pleo.");
			} catch (IOException e) {
				Log.w(LOG_TAG, "Unable to connect to Pleo.");
				cleanSocket();

				try {
					Thread.sleep(CONNECTION_CREATION_INTERVAL);
				} catch (InterruptedException e1) {
					Log.e(LOG_TAG,
							"Unable to sleep when trying to connect to Pleo.");
				}
			} catch (BluetoothDongleNotBondedException e) {
				throw e;
			}
		}
	}
	
	private boolean tryConnectingToPleoWeak() throws BluetoothDongleNotBondedException {

		boolean success = false;
			try {
				connectToPleo();
				success = true;
				Log.i(LOG_TAG, "Connected to Pleo.");
			} catch (IOException e) {
				Log.w(LOG_TAG, "Unable to connect to Pleo.");
				cleanSocket();

			} catch (BluetoothDongleNotBondedException e) {
				throw e;
			}
		return success;
	}

	private void cleanSocket() {
		try {
			if (_pleoBluetoothSocket != null) {
				_pleoBluetoothSocket.close();
				_isConnectedToPleo = false;
				_pleoBluetoothSocket = null;
			}
		} catch (IOException e1) {
			Log.w(LOG_TAG, "Unable to clean socket.");
		}
	}

	public void disconnect() {
		_isConnectedToPleoLock.lock();
		try {
			cleanSocket();
			Log.d(LOG_TAG, "Disconnecting from Pleo");
		} finally {
			_isConnectedToPleoLock.unlock();
		}
	}

	public void announceDisablingMonitor() {
		Intent intent = new Intent(DISABLING_MONITOR_WARNING_INTENT_ACTION);
		sendBroadcast(intent);
	}

	public void announceConnected() {
		Intent intent = new Intent(CONNECTED_INTENT_ACTION);
		sendBroadcast(intent);
	}
	
	public void announceServiceOn() {
		Intent intent = new Intent(SERVICE_ON_INTENT_ACTION);
		sendBroadcast(intent);
	}
	
	public void announceMigratingToPleo(){
		Intent intent = new Intent(MIGRATING_TO_PLEO_INTENT_ACTION);
		sendBroadcast(intent);
	}
	
	public void announceMigratedToPleo(){
		Intent intent = new Intent(MIGRATED_TO_PLEO_INTENT_ACTION);
		sendBroadcast(intent);
	}

	private void connectToPleo() throws BluetoothDongleNotBondedException,
			IOException {

		if (_pleoBluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE)
			throw new BluetoothDongleNotBondedException(_pleoBluetoothDevice
					.getAddress());

		_pleoBluetoothSocket = _pleoBluetoothDevice
				.createRfcommSocketToServiceRecord(SPP_UUID);
		_bluetoothAdapter.cancelDiscovery();
		_pleoBluetoothSocket.connect();
	}
}