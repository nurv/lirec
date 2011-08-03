/** 
 * PleoMainActivity.java - Android activity responsible for launching the connection service
 * with PhyPleo and displaying the migration options available.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PleoMainActivity extends Activity {
	private static final String LOG_TAG = "PleoMainActivity";
	private static final String PLEO_STATE_FILE_NAME = "needs.xml";
	private static final String CONNECTION_CONFIGURATION_FILE_NAME = "configuration.xml";
	private static final int XML_READING_BUFFER_SIZE = 1024;
	private static final int FIRST_TIMER_DURATION_MINUTES = 1;
	private static final int SECONDS_PER_MINUTE = 60;
	private static final int MILISECONDS_PER_SECOND = 1000;

	// Intent request codes
	private static final int REQUEST_ENABLE_BLUETOOTH = 1;

	private boolean _pleoConnectionServiceCreated = false;
	private BluetoothAdapter _bluetoothAdapter = null;
	BroadcastReceiver _pleoConnectionServiceReceiver;
	private State _state = State.BOTH_INACTIVE;
	
	public enum State {
	    BOTH_INACTIVE, VIPLEO, VIPLEOTOPHYPLEO, PHYPLEO, PHYPLEOTOVIPLEO 
	}
	
	private OnClickListener _viStartButtonListener = new OnClickListener() {
		
		public void onClick(View v) {
			if(_state == State.PHYPLEO){
				//TODO: Use the intermediate PHYPLEOTOVIPLEO state
				_state = State.VIPLEO;
				unloadPleoBehavior();
			}
			else{
				_state = State.VIPLEO;
				disableAutoMigrationButtons();
			}
			
			startMyPleoActivity();
		}
	};
	
	private OnClickListener _phyStartButtonListener = new OnClickListener() {
		
		public void onClick(View v) {
			_state = State.PHYPLEO;
			disableAutoMigrationButtons();
			turnOnPleoSimple();
		}
	};
	
	private OnClickListener _viStartMigrateButtonListener = new OnClickListener() {
		
		public void onClick(View v) {
			hideButtons();
			File configurationFile = new File(getExternalFilesDir(null), CONNECTION_CONFIGURATION_FILE_NAME);
			ConfigurationXMLUpdater configurationXMLUpdater = new ConfigurationXMLUpdater(configurationFile,true);
			configurationXMLUpdater.update();
			_state = State.VIPLEO;
			startMyPleoActivity();
		}
	};
	
	private OnClickListener _phyStartMigrateButtonListener = new OnClickListener() {
		
		public void onClick(View v) {
			hideButtons();
			File configurationFile = new File(getExternalFilesDir(null), CONNECTION_CONFIGURATION_FILE_NAME);
			ConfigurationXMLUpdater configurationXMLUpdater = new ConfigurationXMLUpdater(configurationFile,false);
			configurationXMLUpdater.update();
			_state = State.PHYPLEO;
			activateTimer();
			turnOnPleoSimple();
		}
	};
	
	private void hideButtons() {
		Button viStartButton = (Button) findViewById(R.id.ViStartButton);
		viStartButton.setVisibility(View.INVISIBLE);
		Button phyStartButton = (Button) findViewById(R.id.PhyStartButton);
		phyStartButton.setVisibility(View.INVISIBLE);
		Button viStartMigrateButton = (Button) findViewById(R.id.ViStartMigrateButton);
		viStartMigrateButton.setVisibility(View.INVISIBLE);
		Button phyStartMigrateButton = (Button) findViewById(R.id.PhyStartMigrateButton);
		phyStartMigrateButton.setVisibility(View.INVISIBLE);
	}
	
	private Runnable _timerRunnable = new Runnable() {

		public void run() {
			int firstTimerDurationMinutes = FIRST_TIMER_DURATION_MINUTES;
			long firstTimerDurationMiliSeconds = firstTimerDurationMinutes * SECONDS_PER_MINUTE * MILISECONDS_PER_SECOND;
						
			try {
				Thread.sleep(firstTimerDurationMiliSeconds);
				//TODO: Use the intermediate PHYPLEOTOVIPLEO state
				_state = State.VIPLEO;
				unloadPleoBehavior();
				startMyPleoActivity();				
			} catch (InterruptedException e) {
				Log.e(LOG_TAG,"Timer interrupted while sleeping");
			}
		}
	};
	
	private void startMyPleoActivity() {
		Intent gameActivityIntent = new Intent(PleoMainActivity.this,
				MyPleo.class);
		startActivity(gameActivityIntent);
	}

	private void unloadPleoBehavior() {
		Intent pleoConnectionServiceIntent = new Intent(
				PleoMainActivity.this, PleoConnectionService.class);
		pleoConnectionServiceIntent
				.setAction(PleoConnectionService.UNLOAD_PLEO_BEHAVIOR_ACTION);
		startService(pleoConnectionServiceIntent);
	}
	
	private void turnOnPleoSimple() {
		Intent pleoConnectionServiceIntent = new Intent(
				PleoMainActivity.this, PleoConnectionService.class);
		pleoConnectionServiceIntent
				.setAction(PleoConnectionService.LOAD_PLEO_BEHAVIOR_SIMPLE_ACTION);
		startService(pleoConnectionServiceIntent);
	}
	
	private void disableAutoMigrationButtons() {
		Button viStartMigrateButton = (Button) findViewById(R.id.ViStartMigrateButton);
		viStartMigrateButton.setEnabled(false);
		Button phyStartMigrateButton = (Button) findViewById(R.id.PhyStartMigrateButton);
		phyStartMigrateButton.setEnabled(false);
	}
	
	public class PleoConnectionServiceReceiver extends BroadcastReceiver
    {
		@Override
		public void onReceive(Context context, Intent intent) {
			Button phyStartButton = (Button) findViewById(R.id.PhyStartButton);
			Button viStartButton = (Button) findViewById(R.id.ViStartButton);
			
			if(intent.getAction().compareTo(PleoConnectionService.CONNECTED_INTENT_ACTION) == 0){
				Button phyStartMigrateButton = (Button) findViewById(R.id.PhyStartMigrateButton);
				Button viStartMigrateButton = (Button) findViewById(R.id.ViStartMigrateButton);
				
				phyStartButton.setEnabled(true);
				phyStartMigrateButton.setEnabled(true);
				viStartMigrateButton.setEnabled(true);
				
			}else if(intent.getAction().compareTo(PleoConnectionService.MIGRATING_TO_PLEO_INTENT_ACTION) == 0){
				_state = State.VIPLEOTOPHYPLEO;
				phyStartButton.setEnabled(false);
				viStartButton.setEnabled(false);
				
			}else if(intent.getAction().compareTo(PleoConnectionService.MIGRATED_TO_PLEO_INTENT_ACTION) == 0){
				_state = State.PHYPLEO;
				phyStartButton.setEnabled(false);
				viStartButton.setEnabled(true);
				
			}else if(intent.getAction().compareTo(PleoConnectionService.DISABLING_MONITOR_WARNING_INTENT_ACTION) == 0){
				TextView disablingMonitorText = (TextView) findViewById(R.id.DisablingMonitorTextView);
				disablingMonitorText.setVisibility(View.VISIBLE);
				phyStartButton.setEnabled(false);
				
			}else if(intent.getAction().compareTo(PleoConnectionService.SHUTDOWN_WARNING_INTENT_ACTION) == 0){
				TextView shutdownText = (TextView) findViewById(R.id.ShutdownTextView);
				shutdownText.setVisibility(View.VISIBLE);
				phyStartButton.setEnabled(false);
				
			}else if(intent.getAction().compareTo(PleoConnectionService.SERVICE_ON_INTENT_ACTION) == 0){
				_pleoConnectionServiceCreated = true;
			}
		}
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(LOG_TAG, "+++ ON CREATE +++");

		_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (_bluetoothAdapter == null) {
			Log.e(LOG_TAG, "Bluetooth is not available");
			finish();
			return;
		}

		setupGUI();
		setupFilesFolder();
		setupPleoConnectionServiceReceiver();
	}

	private void activateTimer() {
		Thread timerThread = new Thread(_timerRunnable, "TimerThread");
		timerThread.setDaemon(true);
		timerThread.start();
	}

	private void setupGUI() {
		setContentView(R.layout.connection);
		
		Button viStartButton = (Button) findViewById(R.id.ViStartButton);
		viStartButton.setOnClickListener(_viStartButtonListener);
		viStartButton.setEnabled(true);
		
		Button phyStartButton = (Button) findViewById(R.id.PhyStartButton);
		phyStartButton.setOnClickListener(_phyStartButtonListener);
		phyStartButton.setEnabled(false);
		
		Button viStartMigrateButton = (Button) findViewById(R.id.ViStartMigrateButton);
		viStartMigrateButton.setOnClickListener(_viStartMigrateButtonListener);
		viStartMigrateButton.setEnabled(false);
		
		Button phyStartMigrateButton = (Button) findViewById(R.id.PhyStartMigrateButton);
		phyStartMigrateButton.setOnClickListener(_phyStartMigrateButtonListener);
		phyStartMigrateButton.setEnabled(false);
	}

	private void setupFilesFolder() {
		File filesFolder = getExternalFilesDir(null);
		
		File needsFile = new File(filesFolder, PLEO_STATE_FILE_NAME);
		loadFileFromAPK(needsFile, PLEO_STATE_FILE_NAME);
		
		File pleoConnectionConfigurationFile = new File(filesFolder, CONNECTION_CONFIGURATION_FILE_NAME);
		loadFileFromAPK(pleoConnectionConfigurationFile, CONNECTION_CONFIGURATION_FILE_NAME);
	}

	private void loadFileFromAPK(File file,String fileName) {
		try {
			InputStream apkXMLInputStream = getAssets().open(fileName);
			if (apkXMLInputStream != null) {
				FileOutputStream xmlOutputStream = new FileOutputStream(file);

				byte xmlReadingBuffer[] = new byte[XML_READING_BUFFER_SIZE];
				while (apkXMLInputStream.available() > 0) {
					int readingLength = (apkXMLInputStream.available() > XML_READING_BUFFER_SIZE) ? XML_READING_BUFFER_SIZE : (int) apkXMLInputStream.available();
					apkXMLInputStream.read(xmlReadingBuffer, 0, readingLength);
					xmlOutputStream.write(xmlReadingBuffer, 0, readingLength);
				}
				apkXMLInputStream.close();
				xmlOutputStream.close();

			}
			else{
				Log.e(LOG_TAG,"Unable to get XML from apk");
			}

		} catch (IOException e) {
			Log.e(LOG_TAG,"I/O problem writing XML");
		}
	}

	@Override
	public synchronized void onRestart() {
		super.onRestart();
		Log.i(LOG_TAG, "++ ON RESTART ++");
	}

	@Override
	public synchronized void onStart() {
		super.onStart();
		Log.i(LOG_TAG, "++ ON START ++");

		if (!_bluetoothAdapter.isEnabled()) {
			Intent enableBluetoothIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetoothIntent,
					REQUEST_ENABLE_BLUETOOTH);
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		Log.i(LOG_TAG, "++ ON RESUME ++");

		if (_bluetoothAdapter.isEnabled()) {
			if (!_pleoConnectionServiceCreated) {
				Intent pleoConnectionServiceIntent = new Intent(this,
						PleoConnectionService.class);
				pleoConnectionServiceIntent
						.setAction(PleoConnectionService.START_ACTION);
				startService(pleoConnectionServiceIntent);
			}
		}
	}

	private void setupPleoConnectionServiceReceiver() {
		IntentFilter pleoConnectionServiceFilter;
		pleoConnectionServiceFilter = new IntentFilter(PleoConnectionService.DISABLING_MONITOR_WARNING_INTENT_ACTION);
		pleoConnectionServiceFilter.addAction(PleoConnectionService.CONNECTED_INTENT_ACTION);
		pleoConnectionServiceFilter.addAction(PleoConnectionService.SHUTDOWN_WARNING_INTENT_ACTION);
		pleoConnectionServiceFilter.addAction(PleoConnectionService.SERVICE_ON_INTENT_ACTION);
		pleoConnectionServiceFilter.addAction(PleoConnectionService.MIGRATING_TO_PLEO_INTENT_ACTION);
		pleoConnectionServiceFilter.addAction(PleoConnectionService.MIGRATED_TO_PLEO_INTENT_ACTION);
		
		_pleoConnectionServiceReceiver = new PleoConnectionServiceReceiver();
        registerReceiver(_pleoConnectionServiceReceiver, pleoConnectionServiceFilter);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(LOG_TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_ENABLE_BLUETOOTH:
			if (resultCode == Activity.RESULT_OK) {
				Log.i(LOG_TAG, "BlueTooth enabled");
				displayBluetoothSuccess();
			} else {
				Log.e(LOG_TAG, "BlueTooth not enabled");
				displayBluetoothWarning();
				finish();
			}
		}
	}

	private void displayBluetoothSuccess() {
		Context applicationContext = getApplicationContext();
		CharSequence warningText = this.getString(R.string.bluetooth_enabled);
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(applicationContext, warningText, duration);
		toast.show();
	}

	private void displayBluetoothWarning() {
		Context applicationContext = getApplicationContext();
		CharSequence warningText = this.getString(R.string.bluetooth_not_enabled);
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(applicationContext, warningText, duration);
		toast.show();
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		Log.i(LOG_TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.i(LOG_TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(LOG_TAG, "--- ON DESTROY ---");
		unregisterReceiver(_pleoConnectionServiceReceiver);
		
		if (_pleoConnectionServiceCreated) {
			Intent pleoConnectionServiceIntent = new Intent(
					PleoMainActivity.this, PleoConnectionService.class);
			stopService(pleoConnectionServiceIntent);
		}
	}
}