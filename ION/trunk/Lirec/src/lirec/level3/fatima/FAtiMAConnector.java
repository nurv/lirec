package lirec.level3.fatima;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import lirec.architecture.Architecture;
import lirec.level3.AgentMindConnector;
import lirec.level3.MindAction;

public class FAtiMAConnector extends AgentMindConnector {

	/** the thread that runs the agent mind */
	private FAtiMAListenerThread mindThread;
	
	/** keeps track of whether we have established a connection to the remote
	 *  mind yet */
	private boolean fatimaConnected;
	
	/** keeps track of whether the connected mind is currently active or not*/
	private boolean sleeping;
	
	/** create a new FAtiMA connector */
	public FAtiMAConnector(Architecture architecture) 
	{
		super(architecture);
		fatimaConnected = false;
		sleeping = false;
		new ListenForConnectionThread().start();
	}

	/** send a message to FAtiMA telling the mind to resume from a paused state */	
	@Override
	public void awakeMind() {
		mindThread.send("CMD Start");
		sleeping = false;
	}

	/** returns whether FAtiMA is sleeping/paused or not, this function
	 * should be called in conjunction with isConnected, because its return
	 * value makes no sense if no mind is connected */
	@Override
	public boolean isMindSleeping() {
		return sleeping;
	}

    /** report the failure of an action to FAtiMA */
	@Override
	protected void processActionFailure(MindAction a) 
	{
		String msg = "ACTION-FAILED " + a.getSubject() + " "
        +  FAtiMAutils.mindActiontoFatimaMessage(a);	
		mindThread.send(msg);	
	}

    /** report the success of an action to FAtiMA */	
	@Override
	protected void processActionSuccess(MindAction a) {
		String msg = "ACTION-FINISHED " + a.getSubject() + " "
        +  FAtiMAutils.mindActiontoFatimaMessage(a);	
		mindThread.send(msg);	
	}

	@Override
	protected void processRemoteAction(MindAction remoteAction) {
		String msg = "ACTION-FINISHED " + remoteAction.getSubject() + " "
        +  FAtiMAutils.mindActiontoFatimaMessage(remoteAction);	
		mindThread.send(msg);	
	}

	/** send a message to FAtiMA telling the mind to pause */
	@Override
	public void sendMindToSleep() {
		mindThread.send("CMD Stop");
		sleeping = true;
	}

	@Override
	protected void processEntityAdded(String entityName) {
		mindThread.send("ENTITY-ADDED "+entityName);
		
	}

	@Override
	protected void processEntityRemoved(String entityName) {
		mindThread.send("ENTITY-REMOVED "+entityName);	
	}

	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue) {
		mindThread.send("PROPERTY-CHANGED "+entityName+ "(" + propertyName+ ") " + propertyValue);		
	}

	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {
		mindThread.send("PROPERTY-REMOVED "+entityName+ " " + propertyName);		
	}
	
	
	/** this is called once FAtiMA has established a connection */
	public void notifyAgentConnected(String agentName,
			HashMap<String, Object> properties) {
		architecture.getWorldModel().requestAddAgent(agentName, properties);
		fatimaConnected = true;
	}
	
	/** returns whether we have a remote mind connected through a socket */
	@Override
	public boolean isConnected()
	{
		return fatimaConnected;
	}
	
	/** thread that waits for a FAtiMA connecting */
	private class ListenForConnectionThread extends Thread
	{
		
		/** thread main method */
		@Override
		public void run()
		{
			try {
				// open a server socket and wait until we have an incoming connection
				Socket s = new ServerSocket(46874).accept();
				// once we have a connection to a FAtiMA mind, connect to it in a separate thread
				mindThread = new FAtiMAListenerThread(s,FAtiMAConnector.this);
				mindThread.initialize();
				mindThread.start();
			} catch (IOException e) {}				
			
		}
		
		
	}





}
