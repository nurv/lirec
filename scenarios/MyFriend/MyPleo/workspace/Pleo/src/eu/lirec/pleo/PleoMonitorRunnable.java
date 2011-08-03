/** 
 * PleoMonitorRunnable.java - Runnable used to set and load properties from the robot's monitor
 * interface. It loads to, and from, the need's xml. It verifies if commands have been correctly
 * executed, and if not, tries to overcome the detected problem.
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class PleoMonitorRunnable implements Runnable {

	private static final String LOG_TAG = "PleoMonitorRunnable";
	private static final String PLEO_STATE_FILE_NAME = "needs.xml";

	private static final int CONFIRMATION_CHECK_INTERVAL_MILLISECONDS = 500;
	private static final int RESEND_INTERVAL_MILLISECONDS = 1000;

	private Vector<Pattern> _expectedMonitorLinePatterns = new Vector<Pattern>();
	private Vector<Pattern> _commandPatterns = new Vector<Pattern>();
	private Pattern _propertyLoadPattern = null;
	private Pattern _propertyLoadPatternSplit = null;
	private Pattern _monitorOffPattern = null;
	private Pattern _shutdownPattern = null;
	private Pattern _propertyNoisePattern = null;

	private PleoConnectionService _pleoConnectionService;
	private BluetoothSocket _pleoBluetoothSocket;
	private LinkedBlockingQueue<String> _commandQueue;
	private InputStream _pleoBluetoothSocketInputStream = null;
	private OutputStream _pleoBluetoothSocketOutputStream = null;
	private MyPleoNeedsVerifiable _myPleoNeedsVerifiable = null;

	private class NoiseException extends Exception {
		private static final long serialVersionUID = -6684867531225138247L;

		NoiseException(String s) {
			super(s);
		}
	}

	private class MonitorOffException extends Exception {
		private static final long serialVersionUID = 8952275498806168066L;

		MonitorOffException(String s) {
			super(s);
		}
	}

	private class ShutdownException extends Exception {
		private static final long serialVersionUID = 3037989529958519107L;

		ShutdownException(String s) {
			super(s);
		}
	}

	public PleoMonitorRunnable(PleoConnectionService pleoConnectionService,
			BluetoothSocket pleoBluetoothSocket,
			LinkedBlockingQueue<String> commandQueue) {
		_pleoConnectionService = pleoConnectionService;
		_pleoBluetoothSocket = pleoBluetoothSocket;
		_commandQueue = commandQueue;
	}

	public void run() {
		try {
			setupMyPleoNeedsLoading();
			setupPatterns();
			setupSocketStreams();
			executeCommands();
			updateMyPleoNeedsXML();

		} catch (IOException exception) {
			Log.e(LOG_TAG, exception.getMessage());
		} catch (InterruptedException e) {
			Log.e(LOG_TAG, "Interrupted while waiting.");
		} catch (MonitorOffException exception) {
			Log.e(LOG_TAG, exception.getMessage());
			_pleoConnectionService.announceDisablingMonitor();
		} catch (ShutdownException exception) {
			Log.e(LOG_TAG, exception.getMessage());
		}

		_pleoConnectionService.disconnect();
		Log.d(LOG_TAG, "Exiting Monitor Runnable.");
	}

	private void updateMyPleoNeedsXML() {
		if (_myPleoNeedsVerifiable != null) {
			if (_myPleoNeedsVerifiable.isFinishedLoading()) {
				File needsFile = new File(_pleoConnectionService
						.getExternalFilesDir(null), PLEO_STATE_FILE_NAME);
				MyPleoNeedsXMLUpdater myPleoNeedsXMLUpdater = new MyPleoNeedsXMLUpdater(
						needsFile, _myPleoNeedsVerifiable);
				myPleoNeedsXMLUpdater.loadMyPleoNeedsToXML();
			}
			_myPleoNeedsVerifiable = null;
		}
	}

	private void setupMyPleoNeedsLoading() {
		_myPleoNeedsVerifiable = new MyPleoNeedsVerifiable();
	}

	private void executeCommands() throws InterruptedException,
			MonitorOffException, ShutdownException {
		while (!_commandQueue.isEmpty()) {
			String command = _commandQueue.poll();
			executeCommand(command);
		}
	}

	private void setupPatterns() {
		// TODO: Confirmation not being used
		_commandPatterns.add(Pattern.compile("property set 20484 0"));
		_expectedMonitorLinePatterns.add(Pattern
				.compile("BEHAVIOR: empty close"));

		_commandPatterns.add(Pattern.compile("property set 20484 1"));
		_expectedMonitorLinePatterns.add(Pattern
				.compile("BEHAVIOR: empty init"));

		_commandPatterns.add(Pattern.compile("property set [0-9]+ [0-9]+"));
		_expectedMonitorLinePatterns.add(Pattern
				.compile("property set [0-9]+ [0-9]+"));

		_commandPatterns.add(Pattern.compile("clear"));
		_expectedMonitorLinePatterns.add(Pattern.compile("clear"));

		_commandPatterns.add(Pattern.compile("property show"));
		_expectedMonitorLinePatterns.add(Pattern
				.compile("property: ID=34, name=[a-z_0-9]+, value=[0-9]+"));

		_propertyLoadPattern = Pattern
				.compile("property: ID=[0-9]+, name=[a-z_0-9]+, value=[0-9]+");

		_propertyLoadPatternSplit = Pattern
				.compile("property: ID=|, name=|, value=");

		_monitorOffPattern = Pattern
				.compile(".*WARNING: DISABLING MONITOR DUE TO NOISY RX LINE.*");
		_shutdownPattern = Pattern.compile("LL SHUTDOWN: BAT_LEVEL = [0-9]!");

		_propertyNoisePattern = Pattern.compile("prop.*");
	}

	private void setupSocketStreams() throws IOException {
		try {
			_pleoBluetoothSocketInputStream = _pleoBluetoothSocket
					.getInputStream();
			_pleoBluetoothSocketOutputStream = _pleoBluetoothSocket
					.getOutputStream();
		} catch (IOException e) {
			throw new IOException("Unable to setup socket streams. "
					+ e.getMessage());
		}
	}

	private void executeCommand(String command) throws InterruptedException,
			MonitorOffException, ShutdownException {
		while (true) {
			try {
				write(command);
				Log.i(LOG_TAG, "Command <" + command
						+ "> sent to Pleo monitor.");
				confirmExecution(command);
				break;

				// TODO: Break in this case?
			} catch (IOException exception) {
				Log.d(LOG_TAG, "Unable to sucessfully execute <" + command
						+ ">." + exception.getMessage());
				Log.d(LOG_TAG, "Command will be resent to Pleo monitor in "
						+ RESEND_INTERVAL_MILLISECONDS + " miliseconds.");
				Thread.sleep(RESEND_INTERVAL_MILLISECONDS);
			} catch (NoiseException exception) {
				Log.d(LOG_TAG, "Command will be resent to Pleo monitor in "
						+ RESEND_INTERVAL_MILLISECONDS + "miliseconds.");
				Thread.sleep(RESEND_INTERVAL_MILLISECONDS);
			}
		}
	}

	private void write(String command) throws IOException {
		try {
			String endLine = "\r\n";
			String commandWithEndLines = endLine + command + endLine;

			_pleoBluetoothSocketOutputStream.write(endLine.getBytes());
			_pleoBluetoothSocketOutputStream.flush();

			_pleoBluetoothSocketOutputStream.write(commandWithEndLines
					.getBytes());
			_pleoBluetoothSocketOutputStream.flush();

			_pleoBluetoothSocketOutputStream.write(endLine.getBytes());
			_pleoBluetoothSocketOutputStream.flush();
		} catch (IOException exception) {
			throw new IOException("Unable to send command to Pleo monitor."
					+ exception.getMessage());
		}
	}

	private void confirmExecution(String command) throws IOException,
			InterruptedException, ShutdownException, MonitorOffException,
			NoiseException {
		boolean isCommandConfirmed = false;
		while (!isCommandConfirmed) {
			String line = readLine();
			isCommandConfirmed = confirmExecutionWithLine(command, line);
		}
	}

	private String readLine() throws IOException, InterruptedException {
		boolean lineComplete = false;
		String line = "";
		while (!lineComplete) {
			if (_pleoBluetoothSocketInputStream.available() > 0) {
				int streamByteInt = _pleoBluetoothSocketInputStream.read();
				char streamChar = (char) streamByteInt;
				line = line + streamChar;

				if (streamChar == '\n') {
					lineComplete = true;
					Log.i(LOG_TAG, "Monitor:" + line);
				}
			} else {
				try {
					Log.d(LOG_TAG,
							"Waiting for command confirmation character "
									+ CONFIRMATION_CHECK_INTERVAL_MILLISECONDS
									+ " miliseconds. line(" + line + ")");
					Thread.sleep(CONFIRMATION_CHECK_INTERVAL_MILLISECONDS);
				} catch (InterruptedException exception) {
					throw new InterruptedException(
							"Interrupted while waiting for character"
									+ exception.getMessage());
				}
			}
		}
		return line;
	}

	private boolean confirmExecutionWithLine(String command, String line)
			throws IOException, ShutdownException, MonitorOffException,
			NoiseException {
		boolean isCommandConfirmed = false;
		if (line.startsWith(">"))
			line = line.substring(1);
		line = line.trim();
		isCommandConfirmed = confirmExecutionWithTrimmedLine(command, line);
		return isCommandConfirmed;
	}

	// TODO: check for isLineRecognized
	private boolean confirmExecutionWithTrimmedLine(String command, String line)
			throws ShutdownException, MonitorOffException, NoiseException {
		boolean isCommandConfirmed = false;
		boolean isLineRecognized = false;
		
		isLineRecognized = detectExpectedLine(line);
		
		isCommandConfirmed = matchLineToExpectedLine(command, line);

		Matcher lineMatcher = _propertyLoadPattern.matcher(line);
		if (lineMatcher.matches()) {
			Log.d(LOG_TAG, "PROPERTY LOAD DETECTED");
			updateMyPleoNeedsVerifiable(line);
			isLineRecognized = true;
		}

		checkForMonitorProblems(line, isLineRecognized);

		return isCommandConfirmed;
	}

	private boolean matchLineToExpectedLine(String command, String line) {
		boolean isCommandConfirmed = false;
		boolean isCommandRecognized = false;
		for (int iCommand = 0; iCommand < _commandPatterns.size() && !isCommandRecognized; iCommand++) {
			Pattern commandPattern = _commandPatterns.get(iCommand);
			Matcher commandMatcher = commandPattern.matcher(command);
			
			if(commandMatcher.matches()){
				Pattern linePattern = _expectedMonitorLinePatterns.get(iCommand);
				Matcher lineMatcher = linePattern.matcher(line);
				
				if(lineMatcher.matches()){
					isCommandConfirmed = true;
					if (iCommand == 0)
						_pleoConnectionService.announceMigratedToPleo();
				}
				
				isCommandRecognized = true;
			}
		}
		return isCommandConfirmed;
	}

	private boolean detectExpectedLine(String line) {
		boolean isLineRecognized = false;
		
		for (int iExpectedLine = 0; iExpectedLine < _expectedMonitorLinePatterns
				.size()
				&& !isLineRecognized; iExpectedLine++) {
			Pattern linePattern = _expectedMonitorLinePatterns.get(iExpectedLine);
			Matcher lineMatcher = linePattern.matcher(line);

			if (lineMatcher.matches())
				isLineRecognized = true;
		}
		
		return isLineRecognized;
	}

	private void checkForMonitorProblems(String line, boolean isLineRecognized)
			throws ShutdownException, MonitorOffException, NoiseException {
		Matcher lineMatcher;
		lineMatcher = _shutdownPattern.matcher(line);
		if (lineMatcher.matches()) {
			isLineRecognized = true;
			throw new ShutdownException("Behavior shutdown due to low battery.");
		}

		lineMatcher = _monitorOffPattern.matcher(line);
		if (lineMatcher.matches()) {
			isLineRecognized = true;
			throw new MonitorOffException("Monitor turned off due to noise.");
		}

		if (!isLineRecognized) {
			lineMatcher = _propertyNoisePattern.matcher(line);
			if (lineMatcher.matches()) {
				isLineRecognized = true;
				throw new NoiseException("Noise detected.");
			}
		}

		// TODO: Add Additional noise
		// (line.matches("ï.*"))
		// (line.matches("!É.*"))
		// (line.matches("É.*"))
	}

	private void updateMyPleoNeedsVerifiable(String line) {
		if (_myPleoNeedsVerifiable != null) {
			String[] splitLine = _propertyLoadPatternSplit.split(line);
			int propertyID = Integer.parseInt(splitLine[1]);
			int propertyValue = Integer.parseInt(splitLine[3]);
			switch (propertyID) {
			case MyPleoNeeds.CLEANLINESS_ID:
				_myPleoNeedsVerifiable.setNeedCleanliness(propertyValue);
				break;
			case MyPleoNeeds.ENERGY_ID:
				_myPleoNeedsVerifiable.setNeedEnergy(propertyValue);
				break;
			case MyPleoNeeds.PETTING_ID:
				_myPleoNeedsVerifiable.setNeedPetting(propertyValue);
				break;
			case MyPleoNeeds.SKILLS_ID:
				_myPleoNeedsVerifiable.setNeedSkills(propertyValue);
				break;
			case MyPleoNeeds.WATER_ID:
				_myPleoNeedsVerifiable.setNeedWater(propertyValue);
				break;
			case MyPleoNeeds.NEED_END_ID:
				_myPleoNeedsVerifiable.finishLoading();
				break;
			}
		}
	}

}
