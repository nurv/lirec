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
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import cmion.architecture.IArchitecture;
import cmion.level2.migration.Migrating;
import cmion.level2.migration.MigrationAware;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;
import cmion.storage.CmionStorageContainer;
import cmion.storage.WorldModel;


public class FAtiMAConnector extends AgentMindConnector implements Migrating, MigrationAware {

	/** the thread that runs the agent mind */
	private FAtiMAListenerThread mindThread;
	
	/** keeps track of whether we have established a connection to the remote
	 *  mind yet */
	private boolean fatimaConnected;
	
	/** keeps track of whether the connected mind is currently active or not*/
	private boolean sleeping;
	
	/** keeps track of whether the connected mind is the new modular version of Fatima or not*/
	private boolean modular;
	
	/** keeps track of whether the connected mind is the new version of Fatima that distinguishes persistent properties or not*/
	private boolean distinguishPersistent;	
	
	/** is the connected FAtiMA capable of migrating, i.e. can it send and receive
	 *  its agent state through the socket connection with this connector 
	 *  (this feature was added to FAtiMA on 15/04/10)*/
	private boolean canMigrate;
	
	/** the current state that was received from fatima */
	private String currentFatimaState;
	
	/** remembers whether we have received a state from fatima or not*/
	private boolean hasCurrentFatimaState;
	
	/** indicates whether fatima has finished setting/loading a state that we sent to it 
	 * (because of incoming migration) */
	private boolean fatimaStateSet;
	
	/** remembers the last action that was sent for execution */
	private MindAction lastAction;
	
	
	/** create a new FAtiMA connector that connects to an old version of 
	 *  FAtiMA that cannot migrate (kept for backwards compatibility) */
	public FAtiMAConnector(IArchitecture architecture) 
	{
		super(architecture);
		fatimaConnected = false;
		sleeping = false;
		mindThread = null;
		canMigrate = false;
		modular = false;
		distinguishPersistent = false;;
		new ListenForConnectionThread().start();
	}

	/** create a new FAtiMA connector, with an additional options parameter
	 *  this can at the moment contain any of the following substrings in any order
	 *   - "migrating" : indicates that the fatima connecting here supports migration
	 *    				 (see boolean canMigrate) 
	 *   - "sleeping"  : immediately send this mind to sleep once it is connected, this
	 *   				 is useful, if this platform is awaiting incoming migration and 
	 *   				 currently inactive
	 *   - "modular"  :  indicates the fatima connecting is expected to be the recent (2010+) modular version
	 *   				 or an older one
	 *    				 (see boolean modular) 
	 *   - "persistent": indicates that the fatima connecting is expected to be the recent (15/11/2011+) version
	 *   				 that can disinguish between persistent and non persistent knowledge
	 *    				 (see boolean distinguishPersistent) 	 
	 *    */
	public FAtiMAConnector(IArchitecture architecture, String options) 
	{
		super(architecture);
		fatimaConnected = false;
		mindThread = null;
		String optionsStr = options.toLowerCase().trim();
		
		if (optionsStr.contains("migrating"))
			canMigrate = true;
		else
			canMigrate = false;
		
		if (optionsStr.contains("sleeping"))
			sleeping = true;
		else
			sleeping = false;

		if (optionsStr.contains("modular"))
			modular = true;
		else
			modular = false;
		
		if (optionsStr.contains("persistent"))
			distinguishPersistent = true;
		else
			distinguishPersistent = false;

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
	
	/** FAtiMA is currently not receiving any cancellation feedback messages,
	 *  so we don't need to do anything in here */
	@Override
	protected void processActionCancellation(MindAction a) {}
	
	@Override
	protected void processRemoteAction(MindAction remoteAction) {
		String msg = "ACTION-FINISHED " + remoteAction.getSubject() + " "
        +  FAtiMAutils.mindActiontoFatimaMessage(remoteAction);	
		//if (mindThread!=null) mindThread.send(msg);	
		mindThread.send("CMD GET-PROPERTIES");
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
			String propertyName, String propertyValue, boolean persistent) {
		if (mindThread!=null) 
		{					
			if (modular) // the modular fatima version receives property change messages in a different format (because of ToM)
			{	
				String cmdName = "PROPERTY-CHANGED";
				if  (distinguishPersistent)
				{
					if (persistent)
						cmdName = "PROPERTY-CHANGED-PERSISTENT";
					else
						cmdName = "PROPERTY-CHANGED-NONPERSISTENT";
				}
				//updated as " SELF " does not update the value for prperty
				//mindThread.send(cmdName+" SELF "+ entityName+ "(" + propertyName+ ") " + propertyValue);		
				mindThread.send(cmdName+" * "+ entityName+ "(" + propertyName+ ") " + propertyValue);
			}
			else
				mindThread.send("PROPERTY-CHANGED "+ entityName+ "(" + propertyName+ ") " + propertyValue);		
		}
	}

	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {
		if (mindThread!=null) mindThread.send("PROPERTY-REMOVED "+entityName+ " " + propertyName);		
	}
	
	
	/** this is called once FAtiMA has established a connection */
	public void notifyAgentConnected(String agentName,
			HashMap<String, Object> properties) {
		architecture.getWorldModel().requestAddAgent(agentName, properties);
		architecture.getBlackBoard().requestSetProperty("agentName", agentName);
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
		public ListenForConnectionThread()
		{
			super("ListenForFatimaConnection");
		}
		
		
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
		if (canMigrate  &&  (mindThread!=null))
		{
			if (message.hasChildNodes())
			{
				fatimaStateSet = false;
				
				// read state
				String state = message.getElementsByTagName("state").item(0).getChildNodes().item(0).getNodeValue();
				
				// read success msg
				String successMsg = null;
				if (message.getElementsByTagName("successMsg").getLength()==1) 
					successMsg = message.getElementsByTagName("successMsg").item(0).getChildNodes().item(0).getNodeValue();
				
				// send state to the mind
				mindThread.send("CMD SET-STATE "+state);
			
				// wait until the mind has finished processing this state
				while (!fatimaStateSet)
				{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}			
				
				// now that the state is set awake the mind
				awakeMind();
				
				// finally, send the received success message to the mind
				mindThread.send(successMsg);
				
				// and force the mind to look at all the agents and objects in the new world model
				// that we have migrated to
				
				// first agents
				for (String agentName: architecture.getWorldModel().getAgentNames())
					mindThread.sendLookAtPerception(agentName);
				
				// and now objects
				for (String objectName: architecture.getWorldModel().getObjectNames())
					mindThread.sendLookAtPerception(objectName);
				
				// now that we have done this, update world model with fatima properties
				mindThread.send("CMD GET-PROPERTIES");				
			}
			
		} 
		else	
			System.out.println("this version of fatima cannot load the incoming state");
	}

	public void saveStateToDisk()
	{
		if (mindThread!=null)
		{
			// send save command to FAtiMA
			mindThread.send("CMD SAVE");
		}
	}
	
	
	@Override
	public Element saveState(Document doc) 
	{
		Element parent = doc.createElement(getMessageTag());
		
		if (canMigrate &&  (mindThread!=null))
		{			
			currentFatimaState = null;
			hasCurrentFatimaState = false;
			
			// in order for the mind to serialize the state, it needs to be paused besides 
			// now that we are migrating away from here, the mind should be paused anyway
			
			// send the pause command
			this.sendMindToSleep();
			
			// wait a while to make sure current fatima cycle is complete
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {}
			
			// obtain state from fatima
			mindThread.send("CMD GET-STATE");
			
			// now wait until the mind thread has received the state
			while (!this.hasCurrentFatimaState)
			{
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}

			Element stateElement = doc.createElement("state");
			Node state = doc.createTextNode(currentFatimaState);
			stateElement.appendChild(state);
			parent.appendChild(stateElement);
			
			currentFatimaState = null;
			
			// also add information about the current fatima mind action (which initiated
			// this migration, so that a success message can be sent on the receiving end)
			if (lastAction!=null)
			{
				Element successMsgElement = doc.createElement("successMsg");
				String msg = "ACTION-FINISHED " + lastAction.getSubject() + " "
				+  FAtiMAutils.mindActiontoFatimaMessage(lastAction);	
				Node msgNode = doc.createTextNode(msg);
				successMsgElement.appendChild(msgNode);
				parent.appendChild(successMsgElement);
			}
		} 
		else
		{
			System.out.println("this version of fatima cannot save its state");
		}
		return parent;
	}

	/** this message is called by the connected fatima listener, when it receives 
	 *  a state from fatima */
	public void notifyGetState(String state) 
	{
		currentFatimaState = state;
		hasCurrentFatimaState = true;
	}

	/** this message is called by the connected fatima listener, when it receives 
	 *  a state from fatima */
	public void notifySetState() 
	{
		fatimaStateSet = true;
	}

	/** executes a new action sent by the mind */
	public void execute(MindAction mindAction) 
	{
		// remember this action
		this.lastAction=mindAction;
		// and send it for execution
		this.newAction(mindAction);
	}
	
	/** returns whether the connected Fatima is expected to distinguish between persistent
	 * and non persistent properties (true) or not (false) */
	public boolean distinguishesPersistent()
	{
		return distinguishPersistent;
	}

	@Override
	protected void processRawMessage(String message) {
		mindThread.send(message);
	}

	/** parse the xml message containing the semantic memory of the connected agent,
	 *  use it to update the world model */
	public void parseSemanticMemory(String msg)
	{
		final class Container
		{
			public HashMap<String,Object> properties;
			public HashSet<String> persistentProperties;
		
			public Container()
			{
				properties = new HashMap<String,Object>();
				persistentProperties = new HashSet<String>();
			}
		
		}
		
		try
		{
			HashMap<String,Container> containers = new HashMap<String,Container>(); 
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(msg));
			Document doc = db.parse(is);
			NodeList nodes = doc.getElementsByTagName("Property");
			for (int i=0; i<nodes.getLength(); i++)
			{
				NamedNodeMap attribs = nodes.item(i).getAttributes();
				boolean persistent = Boolean.parseBoolean(attribs.getNamedItem("persistent").getNodeValue());
				String fullName = attribs.getNamedItem("name").getNodeValue();
				String propValue = attribs.getNamedItem("value").getNodeValue();
				
				// there are certain properties we are not interested in, filter those out
				if (!fullName.equals("ActionContext()"))
				{

					// extract propOwner and Name
					String propOwner = fullName.substring(0,fullName.indexOf("("));
					String propName = fullName.substring(fullName.indexOf("(")+1,fullName.length()-1);

					// filter out some more, we don't need the ProbBias stuff
					if (!propOwner.equals("ProbBias"))
					{
						// change SELF to agent's name
						if (propOwner.equals("SELF")) propOwner = mindThread.getAgentName();
						
						// write all properties into temporary container structure
						Container container = containers.get(propOwner);
						if (container==null)
						{	
							container = new Container();
							containers.put(propOwner, container);
						}
						container.properties.put(propName, propValue);
						if (persistent)	container.persistentProperties.add(propName);
					}
				}
			}
		
			WorldModel wm = architecture.getWorldModel();
			// now write the values for all containers to the world model
			for (String containerName : containers.keySet())
			{
				Container container = containers.get(containerName);
				// if the container already exists, just add the properties to it, 
				// we dont need to worry about the agent/object distinction
				if (wm.hasSubContainer(containerName)) 
				{
					CmionStorageContainer csc = wm.getSubContainer(containerName);
					for (String propName: container.properties.keySet())
					{
						csc.requestSetProperty(propName, 
											   container.properties.get(propName), 
											   container.persistentProperties.contains(propName));
					}	
				} else // this is the trickier case, we dont know if this is an agent or object
					// for now use this policy: make it an agent unless it has a property called isObject set to true
				{
					String type = WorldModel.AGENT_TYPE_NAME;
					if (container.properties.containsKey("isObject")) 
					{
						boolean isObject = Boolean.parseBoolean(container.properties.get("isObject").toString());
						if (isObject) type = WorldModel.OBJECT_TYPE_NAME;
					}
					wm.requestAddSubContainer(containerName, type, container.properties, container.persistentProperties);			
				}		
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
