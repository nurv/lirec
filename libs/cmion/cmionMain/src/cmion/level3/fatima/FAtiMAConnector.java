/*	
    CMION
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  09/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  Renamed to CMION
  ---  
*/

package cmion.level3.fatima;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import cmion.architecture.IArchitecture;
import cmion.level2.migration.Migrating;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;


public class FAtiMAConnector extends AgentMindConnector implements Migrating {

	/** the thread that runs the agent mind */
	private FAtiMAListenerThread mindThread;
	
	/** keeps track of whether we have established a connection to the remote
	 *  mind yet */
	private boolean fatimaConnected;
	
	/** keeps track of whether the connected mind is currently active or not*/
	private boolean sleeping;
	
	/** is the connected FAtiMA capable of migrating, i.e. can it send and receive
	 *  its agent state through the socket connection with this connector 
	 *  (this feature was added to FAtiMA on 15/04/10)*/
	private boolean canMigrate;
	
	/** the current state that was received from fatima */
	private String currentFatimaState;
	
	/** remembers whether we have received a state from fatima or not*/
	private boolean hasCurrentFatimaState;
	
	/** create a new FAtiMA connector that connects to an old version of 
	 *  FAtiMA that cannot migrate (kept for backwards compatibility) */
	public FAtiMAConnector(IArchitecture architecture) 
	{
		super(architecture);
		fatimaConnected = false;
		sleeping = false;
		mindThread = null;
		canMigrate = false;
		new ListenForConnectionThread().start();
	}

	/** create a new FAtiMA connector */
	public FAtiMAConnector(IArchitecture architecture, String migrating) 
	{
		super(architecture);
		fatimaConnected = false;
		sleeping = false;
		mindThread = null;
		if (migrating.toLowerCase().trim().equals("true"))
			canMigrate = true;
		else
			canMigrate = false;
		new ListenForConnectionThread().start();
	}

	
	/** send a message to FAtiMA telling the mind to resume from a paused state */	
	@Override
	public void awakeMind() {
		if (mindThread!=null)
		{
			mindThread.send("CMD Start");
			sleeping = false;
		}
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
		if (mindThread!=null) mindThread.send(msg);	
	}

    /** report the success of an action to FAtiMA */	
	@Override
	protected void processActionSuccess(MindAction a) {
		String msg = "ACTION-FINISHED " + a.getSubject() + " "
        +  FAtiMAutils.mindActiontoFatimaMessage(a);	
		if (mindThread!=null) mindThread.send(msg);	
	}

	@Override
	protected void processRemoteAction(MindAction remoteAction) {
		String msg = "ACTION-FINISHED " + remoteAction.getSubject() + " "
        +  FAtiMAutils.mindActiontoFatimaMessage(remoteAction);	
		if (mindThread!=null) mindThread.send(msg);	
	}

	/** send a message to FAtiMA telling the mind to pause */
	@Override
	public void sendMindToSleep() {
		if (mindThread!=null) 
		{
			mindThread.send("CMD Stop");
			sleeping = true;
		}
	}

	@Override
	protected void processEntityAdded(String entityName) {
		if (mindThread!=null) mindThread.send("ENTITY-ADDED "+entityName);
		
	}

	@Override
	protected void processEntityRemoved(String entityName) {
		if (mindThread!=null) mindThread.send("ENTITY-REMOVED "+entityName);	
	}

	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue) {
		if (mindThread!=null) mindThread.send("PROPERTY-CHANGED "+entityName+ "(" + propertyName+ ") " + propertyValue);		
	}

	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {
		if (mindThread!=null) mindThread.send("PROPERTY-REMOVED "+entityName+ " " + propertyName);		
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

	@Override
	protected void architectureReady() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMessageTag() 
	{
		return "fatimaconnector";
	}

	@Override
	public void onMigrationFailure() {
		System.out.println("MIND-Migration Failed.");	
	}

	@Override
	public void onMigrationIn() {
		System.out.println("MIND-Receiving a migration.");	
	}

	@Override
	public void onMigrationOut() {
		System.out.println("MIND-Going to migrate.");
	}

	@Override
	public void onMigrationSuccess() {
		System.out.println("MIND-Migration Success.");
	}

	@Override
	public void restoreState(Element message) 
	{
		if (canMigrate)
		{
			if (message.hasChildNodes())
			{
				String state = message.getChildNodes().item(0).getNodeValue();
				System.out.println("fatima state: " + state);
			}
			
		} 
		else	
			System.out.println("this version of fatima cannot load the incoming state");
	}

	@Override
	public Element saveState(Document doc) 
	{
		Element parent = doc.createElement(getMessageTag());
		if (canMigrate)
		{			
			currentFatimaState = null;
			hasCurrentFatimaState = false;
			
			// obtain state from fatima
			mindThread.send("CMD GET-STATE");
			
			// now wait until the mind thread has received the state
			while (!this.hasCurrentFatimaState)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}

			System.out.println("fatima state: " + currentFatimaState);
			Node state = doc.createTextNode(currentFatimaState);
			parent.appendChild(state);
		} 
		else
		{
			System.out.println("this version of fatima cannot save its state");
		}
		return parent;
	}

	/** this message is called by the connected fatima listener, when it receives 
	 *  a state from fatima */
	public void notifyState(String state) 
	{
		currentFatimaState = state;
		hasCurrentFatimaState = true;
	}





}
