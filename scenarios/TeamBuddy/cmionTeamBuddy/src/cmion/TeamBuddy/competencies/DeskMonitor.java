package cmion.TeamBuddy.competencies;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;
import cmion.storage.CmionStorageContainer;

public class DeskMonitor extends Competency {

	private String sUserName;
	int port;
	boolean bExit;//to check if user has exited
	//boolean bStarted;//to check if the module was connected
	boolean bUsrStatus; //present 1 or absent 0
	boolean bFirstUserSeen;//to check if the user was seen atleast once
	long StartTime;
	int iBreakTime;
	int iExitTime;
	ServerSocket serverSocket;
	Socket s = null;

	public DeskMonitor(IArchitecture architecture, String UserName, String portNumber) {
		super(architecture);
		// TODO Auto-generated constructor stub

		//name and type of the competence
		this.sUserName = UserName;
		this.port = Integer.parseInt(portNumber);
		this.bUsrStatus = false;
		//this.bStarted = false;
		this.bExit = false;
		this.iBreakTime = 20; //break time offset in minutes
		this.iExitTime = 480; //exit time offset in minutes
		this.bFirstUserSeen=false;//set the user seen first time flag to false

		this.competencyName = "DeskMonitor";
		this.competencyType = "DeskMonitor";
		

		try {
			serverSocket = new ServerSocket(this.port);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onRead() {
		// TODO Auto-generated method stub
		//bUsrStatus = false;
		
		String Data = "";
		try {
			//if(serverSocket.isBound())			 
			//set timeout for waiting for server to connect
			serverSocket.setSoTimeout(1000 * 60 * 3);
			s = serverSocket.accept();
			if (s.isBound()) {
				BufferedReader stdIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
				Data = stdIn.readLine();
				// System.out.println(Data);
				if (!Data.isEmpty()) {
					//bStarted = true;//set true once user is seen
					bUsrStatus = true;//set true once user is seen
					//if user is seen for the first time, start the clock
					if(!bFirstUserSeen)
					{
						this.StartTime = System.currentTimeMillis();
						this.bFirstUserSeen=true;
						
					}	
					
				}
			}

		} catch (IOException e) {
			System.err.println("connection error with " + e);
		}

		Calendar TimeNow = new GregorianCalendar();

		int day = TimeNow.get(Calendar.DAY_OF_MONTH);
		int hour = TimeNow.get(Calendar.HOUR_OF_DAY);
		int min = TimeNow.get(Calendar.MINUTE);

		String filename = sUserName;

		//for logging all the user information
		try {
			// Create file 
			FileWriter fstream = new FileWriter(filename, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(sUserName + " " + Data + " " + day + ":" + hour + ":" + min);
			out.newLine();

			//Close the output stream
			out.close();
		} catch (Exception e) {//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		//create variables on blackboard and write on it
		architecture.getBlackBoard().setRTProperty(sUserName, bUsrStatus);
		//check if world model has a container for the user
		CmionStorageContainer userContainer = architecture.getWorldModel().getAgent(sUserName);
		if (userContainer != null) {
			if (bUsrStatus)
				userContainer.requestSetProperty("present", "True");
			else
				userContainer.requestSetProperty("present", "False");
		}
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {

		while (true) {
			onRead();
			String log = "";
			//System.out.println("inside competency code");
			Calendar TimeNow = new GregorianCalendar();

			// long milliseconds1 = StartTime.getTimeInMillis();
			// long milliseconds2 = TimeNow.getTimeInMillis();
			long diff = System.currentTimeMillis() - this.StartTime;
			//long diffSeconds = diff / 1000;
			long diffMinutes = diff / (60 * 1000);
			int day = TimeNow.get(Calendar.DAY_OF_MONTH);
			int hour = TimeNow.get(Calendar.HOUR_OF_DAY);
			int min = TimeNow.get(Calendar.MINUTE);

			//System.out.println(" sUserName, time diff, user status " + sUserName + " " + diffMinutes + " " + this.bUsrStatus);
			//if user is seen
			if (this.bUsrStatus && this.bFirstUserSeen) {
				
				this.StartTime = System.currentTimeMillis();
				this.bExit = false;//set this to false once the user is seen 
				//System.out.println(" time diff " + diffMinutes);
				//System.out.println(" time now " + Calendar.getInstance());
				
				if (diffMinutes >= iBreakTime && diffMinutes < iExitTime) {
					System.out.println("break " + hour + " time diff " + diffMinutes);
					log = sUserName + " " + "Break" + " " + day + ":" + hour + ":" + min + " " + diffMinutes;
					// raise an event remote action
					ArrayList<String> actionParameters = new ArrayList<String>();
					// if you have any parameters add them below
					// actionParameters.add();
					MindAction ma = new MindAction(sUserName, "Break", actionParameters);
					this.raise(new EventRemoteAction(ma));
					
				} else if (diffMinutes >= iExitTime) {

					// raise an event remote action
					ArrayList<String> actionParameters = new ArrayList<String>();
					// if you have any parameters add them below
					// actionParameters.add();
					MindAction ma = new MindAction(sUserName, "Enter", actionParameters);
					this.raise(new EventRemoteAction(ma));
					System.out.println("user entered " + hour + " time diff " + diffMinutes);
					log = sUserName + " " + "Enter" + " " + day + ":" + hour + ":" + min + " " + diffMinutes;
				}
				this.bUsrStatus = false;
			
			} else if ((diffMinutes >= iExitTime) && !this.bExit && this.bFirstUserSeen) {
				// raise an event remote action
				ArrayList<String> actionParameters = new ArrayList<String>();
				// if you have any parameters add them below
				// actionParameters.add();
				MindAction ma = new MindAction(sUserName, "Exit", actionParameters);
				this.raise(new EventRemoteAction(ma));
				System.out.println("user exited " + hour + " time diff " + diffMinutes);
				log = sUserName + " " + "Exit" + " " + day + ":" + hour + ":" + min + " " + diffMinutes;
				//this.bStarted = false;
				this.bExit = true;
			}

			//sleep for 5 seconds
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (!log.equals("")) {
				System.out.println(log);

				try {
					// Create file 
					FileWriter fstream = new FileWriter("Log.txt", true);
					BufferedWriter out = new BufferedWriter(fstream);
					out.write(log);
					out.newLine();

					//Close the output stream
					out.close();
				} catch (Exception e) {//Catch exception if any
					System.err.println("Error: " + e.getMessage());
				}

			}

		}//end while

	}

	@Override
	public boolean runsInBackground() {
		return true;
	}

	@Override
	public void initialize() {
		this.available = true;

	}

}
