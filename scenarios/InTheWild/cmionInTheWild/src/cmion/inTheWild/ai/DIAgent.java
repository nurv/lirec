package cmion.inTheWild.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import uk.ac.hw.lirec.dialogsystem.DialogInterface;
import uk.ac.hw.lirec.dialogsystem.DialogSystem;
import cmion.architecture.IArchitecture;
import cmion.level3.AgentMindConnector;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;

public class DIAgent extends AgentMindConnector {

	private DialogSystem dialogSystem;
	private MyDialogInterface dialogInterface;
	
	private MindAction currentAction;
	
	private MigrationServer server;
	
	private static Object logLock = new Object();
	
	private boolean busy = false;
	
	public DIAgent(IArchitecture architecture, String scriptFile) {
		super(architecture);
		dialogInterface = new MyDialogInterface();
		dialogSystem = new DialogSystem(dialogInterface);
		try {
			dialogSystem.initSystem(new BufferedReader(new FileReader(scriptFile)));
		} catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		server = new MigrationServer(5228);
		server.start();
	}

	@Override
	public void sendMindToSleep() {}

	@Override
	public boolean isMindSleeping() {
		return false;
	}

	@Override
	public void awakeMind() {}

	@Override
	protected void processRemoteAction(MindAction remoteAction) 
	{
		if (remoteAction.getName().equals("comeClose"))
		{
			if (!busy) evaluate("startInteraction()");
		}
		else if (remoteAction.getName().equals("answer"))
		{
			dialogInterface.provideAnswerAndNotify(remoteAction.getParameters().get(1));	
		}
		else if (remoteAction.getName().equals("migrationIn"))
		{
			evaluate("migrationIn()");	
		}
		else 
		{
			if (!dialogInterface.isBusy()) evaluate(remoteAction.getName()+"()");
		}
	}	
	
	@Override
	protected void processActionSuccess(MindAction a) 
	{
		if (a.equals(currentAction)) dialogInterface.stopWaitingAndNotify();
	}

	@Override
	protected void processActionFailure(MindAction a) 
	{
		if (a.equals(currentAction)) dialogInterface.stopWaitingAndNotify();		
	}

	@Override
	protected void processActionCancellation(MindAction a) {}

	@Override
	protected void processEntityAdded(String entityName) {}

	@Override
	protected void processEntityRemoved(String entityName) {}

	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue, boolean persistent) {}

	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {}

	@Override
	protected void processRawMessage(String message) {}

	@Override
	protected void architectureReady() 
	{
		//evaluate("startScreenMigrateTest()");
	}
	
	private void executeAction(MindAction action)
	{
		currentAction = action;
		newAction(currentAction);
	}

	private void evaluate(final String command)
	{
		// evaluate event in a new thread
		new Thread(
	            new Runnable() {
	                public void run() 
	                {
	                	busy = true;
	                	dialogSystem.evaluateEvent(command);
	                	busy = false;
	                }
	            }).start();

	}
	
	private void migrationOut() 
	{
		dialogSystem.evaluateEvent("migrationOut()");
	}
	
	private class MigrationServer extends Thread
	{	
		private static final String COMMAND_INVITE = "INVITE";
		private static final String COMMAND_MIGRATEIN = "MIGRATEIN";		
		
		private int port;
		
		private HashMap<String, String> dataToMigrate;
		
		public MigrationServer(int port)
		{
			this.port = port;
		}
		
		
		@Override
		public void run()
		{
			ServerSocket serverSocket  = null;
			Socket clientSocket = null;
			while(true)
			try 
			{
				serverSocket  = new ServerSocket(port);
				clientSocket = serverSocket.accept();
				ObjectOutputStream out_object = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in_object = new ObjectInputStream(clientSocket.getInputStream());
				// read the first line (containing the command)

				Object command = in_object.readObject();
				System.out.println("read command: " + command);
				if (command.equals(COMMAND_INVITE))
				{
					System.out.println("command invite");
					dataToMigrate = null;
					migrationOut();
					if (dataToMigrate != null)
					{	
						out_object.writeObject(dataToMigrate);
						out_object.flush();
						System.out.println("sent data");
					} else
					{
						out_object.writeObject("DENIED");
						out_object.flush();
					}
				}	
				else if (command.equals(COMMAND_MIGRATEIN))
				{
					System.out.println("command migratein");
					// this command signifies an incoming migration, following is the serialized hashmap
					// containing the migration data
					@SuppressWarnings("unchecked")
					HashMap<String,String> migrateDataIn = (HashMap<String, String>) in_object.readObject();
					System.out.println("received hashmap");
					
					// push the migration in data into the dialog system
					dialogSystem.interruptDialogEvent();
					dialogSystem.migrateDataIn(migrateDataIn);
					
					// raise an event relating to the incoming migration
					DIAgent.this.raise(new EventRemoteAction(new MindAction("DI","migrationIn",null)));
					
				}	
				else 
				{
					out_object.writeObject("Unknown command");
					out_object.flush();
				}
				//out_object.close();
				//in_object.close();
				//System.out.println("Streams closed");
			}
			catch (StreamCorruptedException e)
			{	
				e.printStackTrace();
				// do not print stack trace for this as this happens very often (port is exposed on internet)
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			} 
			catch (ClassNotFoundException e) 
			{
				e.printStackTrace();
			}					
			finally
			{
				try {
					clientSocket.shutdownOutput();
					//clientSocket.close();
					serverSocket.close();
					System.out.println("Sockets closed");
				} catch (IOException e) {}
			}
		}


		public synchronized void setMigrationData(HashMap<String, String> dataToMigrate) {
			this.dataToMigrate = dataToMigrate;		
		}
		
	}
	
	
	public class MyDialogInterface extends DialogInterface
	{

		private boolean mInterrupted = false;
		private boolean mWaiting = false;
		private boolean mTimedOut = false;
		private String answer;
		
		public synchronized boolean isBusy()
		{
			return mWaiting;
		}
		
		@Override
		public synchronized void interruptDialog() {
			mInterrupted = true;
			this.notify();
		}

		@Override
		public synchronized void resetDi() {
			mWaiting = false;
			mInterrupted = false;
			mTimedOut = false;
		}

		public void setEmysInvisible()
		{
			executeAction(new MindAction("DIAgent","wozSetInvisible",null));
			waitForCallback();				
		}

		public void setEmysVisible()
		{
			executeAction(new MindAction("DIAgent","wozSetVisible",null));
			waitForCallback();							
		}		
		
		@Override
		public void speakText(String text) {
			if (mInterrupted || mTimedOut)
				return;			
			
			ArrayList<String> parameters = new ArrayList<String>(1);
			parameters.add(text);
			executeAction(new MindAction("DIAgent","wozTalk",parameters));
			waitForCallbackOrInterrupt();	
		}
		
		public void log(String message)
		{    	
			if (mInterrupted || mTimedOut)
				return;			
			
			synchronized(logLock)
			{			
				try
				{
					File file = new File("diagent-log.txt");

					//if file doesnt exists, then create it
					if(!file.exists()){
						file.createNewFile();
					}

					//true = append file
					FileWriter fileWriter = new FileWriter(file.getName(),true);
					PrintWriter writer = new PrintWriter(fileWriter);
					writer.print(message);
					writer.flush();
					writer.close();
					fileWriter.close();
				}
				catch(IOException e){
					e.printStackTrace();
				}
			}
		}


		private void removeMultiChoice() {
			executeAction(new MindAction("DIAgent","wozCancelQuestion",null));
			waitForCallback();	
		}		
		
		@Override
		public void getResponse(String infoText) 
		{
			String args[] = {infoText};
			multipleChoiceQuestion(1, args);	
		}

		@Override
		public String multipleChoiceQuestion(Integer numChoices,
				String[] options) 
		{
			if (mInterrupted || mTimedOut)
				return "";			
			if (numChoices < 5)
			{
				List<String> parameters = Arrays.asList(options); ;
				newAction(new MindAction("DIAgent","wozQuestion",parameters));
				String optionsStr = "";
				for (String option : options) optionsStr += (" " + option);
				waitForAnswer();

				// if we were interrupted or timed out, remove the questions display
				if (mInterrupted || mTimedOut)
					removeMultiChoice();
				else
				{
					return answer;
				}
			}
			return "";
			
		}

		@Override
		public void setMood(Moods mood) {}

		private String expressionName(Expression exp) {
			switch (exp)  {
			case ANGER : return "Anger4";
			case SURPRISE : return "Surprise4";
			case SADNESS : return "Sadness4";
			case JOY : return "Joy4";
			case FEAR : return "Fear4";
			default : 	System.out.println("WARNING unknown mapping from expression: "+exp+"!");
						return "Idle4";
			}
		}
		
		@Override
		public void showExpression(Expression expression) 
		{
			if (mInterrupted || mTimedOut)
				return;			
			
			ArrayList<String> parameters = new ArrayList<String>(1);
			parameters.add(expressionName(expression));
			executeAction(new MindAction("DIAgent","wozEmotion",parameters));
			
			waitForCallbackOrInterrupt();			
		}
		
		private synchronized void waitForCallbackOrInterrupt() {
			mWaiting = true;
			while (mWaiting && !mInterrupted) {
				try {
					this.wait();
				} catch (InterruptedException e) {}
			}
		}
		
		private synchronized void waitForCallback() {
			mWaiting = true;
			while (mWaiting) {
				try {
					this.wait();
				} catch (InterruptedException e) {}
			}
		}	
		
		private synchronized void waitForAnswer() {
			answer = null;
			mWaiting = true;
			Thread timeOutThread = new AnswerTimeOutThread();
			timeOutThread.start();
			while ((answer==null) && !mInterrupted && !mTimedOut) {
				try {
					this.wait();
				} catch (InterruptedException e) 
				{}
			}
			// if we have not timed out, stop the time out counter by interrupting it
			if (mTimedOut) timeOutThread.interrupt();	
		}
		
		public synchronized void provideAnswerAndNotify(String answer)
		{
			this.answer = answer;
			stopWaiting();
			this.notify();			
		}

		public void stopWaiting() { mWaiting = false;}
		
		public synchronized void stopWaitingAndNotify() {
			stopWaiting();
			this.notify();
		}

		public synchronized void timeOutAndNotify() {
			stopWaiting();
			mTimedOut = true;
			this.notify();
		}	
		
		@Override
		public synchronized boolean migrateDataOut(String migrateTo,
				HashMap<String, String> dataToMigrate) {
			server.setMigrationData(dataToMigrate);
			return true;
		}
		
		private class AnswerTimeOutThread extends Thread
		{
			// duration of time out in ms
			private long timeOutDuration = 200000;
			
			@Override
			public void run()
			{
				//sleep
				try {
					Thread.sleep(timeOutDuration);
				} catch (InterruptedException e) 
				{
					return;
				}
				// if we reach this point we should signal time out
				timeOutAndNotify();
			}
		}

		@Override
		public boolean inviteMigrate(String migrateFrom) 
		{
			return false;
		}
		
	}

	
	
}
