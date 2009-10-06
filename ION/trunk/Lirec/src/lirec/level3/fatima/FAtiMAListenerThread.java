package lirec.level3.fatima;

import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

import lirec.storage.WorldModel;
import lirec.util.SocketListener;

/** In this thread we communicate with the FAtiMA mind through a remote connection */
public class FAtiMAListenerThread extends SocketListener {

	/** a reference to the connector */
	private FAtiMAConnector connector;
	
	/** counts the incoming messages */
	private long messageCounter;
	
	/** the name of the agent */
	private String agentName;

	/** the display name of the agent (sort of short/nick name, not used for identifying) */
	private String displayName;
	
	/** the role of the agent (corresponds to the name of the agent personality definition file in the
	 * FAtiMA data folder) */
	private String agentRole;
	
	
	/** create a new FAtiMAListenerThread */
	public FAtiMAListenerThread(Socket socket, FAtiMAConnector connector)
	{
		super(socket);
		this.connector = connector;
		messageCounter = 0;	
	}
	
	
	/** this method processes messages that FAtiMA has sent */
	@Override
	protected synchronized void processMessage(String msg) {

		// increase the message counter
		messageCounter++;
		
		
		// dissect the message from FAtiMA
		StringTokenizer st = new StringTokenizer(msg," ");
		String type = st.nextToken();
		
		if (messageCounter==1)
		{
			// 1st message from FAtiMA agent, this one has a special format, agent tells us its
			// name, role displayName and properties
			
			agentName = type;
			agentRole = st.nextToken();
			displayName = st.nextToken();
			HashMap<String,Object> properties = new HashMap<String,Object>(); 
			
			StringTokenizer st2;
			while (st.hasMoreTokens())
			{
				st2 = new StringTokenizer(st.nextToken(),":");
	        	properties.put(st2.nextToken(),st2.nextToken());
			}
			
			connector.notifyAgentConnected(agentName,properties);
			
			this.send("OK");
			
		}
		else if(type.startsWith("<EmotionalState")) 
		{
			// FAtiMA agent updates us about its current emotional state

			// for now we don't process this
		}
		else if (type.startsWith("<Relations"))
		{
			// FAtiMA agent updates us about relations with other agents

			// for now we don't process this, not likely to be relevant in a single agent environment
		}
		else if (type.startsWith("PROPERTY-CHANGED"))
		{
			// ignore this for now as well, normally the mind should not change properties by itself anyway
			// they should be changed outside in the world simulation, i.e. in this application here
		} 
		// FAtiMA agent wants to look at something, i.e. requests information about the properties of a certain object or another agent
		else if (type.equals("look-at")) {
			String target = st.nextToken();
			
			//System.out.println(_name + " looks at " + target);
			String response = "LOOK-AT " + target;
			
			WorldModel worldModel = connector.getArchitecture().getWorldModel();
			
			if (worldModel.hasAgent(target))
				response += FAtiMAutils.getPropertiesString(worldModel.getAgent(target));
			else if (worldModel.hasObject(target))
				response += FAtiMAutils.getPropertiesString(worldModel.getObject(target));
									
			this.send(response);
		}
		else {
			//Corresponds to an action
			connector.newAction(FAtiMAutils.fatimaMessageToMindAction(agentName,msg));		
		}
		
	}
	
	/** returns the name of the FAtiMA agent that is connected to this thread */
	public String getAgentName()
	{
		return this.agentName;
	}

	/** returns the display name of the FAtiMA agent that is connected to this thread */
	public String getDisplayName()
	{
		return this.displayName;
	}
	
	/** returns the role (personality profile) of the FAtiMA agent that is connected to this thread */
	public String getRole()
	{
		return this.agentRole;
	}
	
}
