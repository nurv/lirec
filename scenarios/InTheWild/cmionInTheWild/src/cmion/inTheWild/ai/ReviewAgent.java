package cmion.inTheWild.ai;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cmion.architecture.IArchitecture;
import cmion.level2.migration.Migrating;
import cmion.level2.migration.MigrationAware;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

public class ReviewAgent extends AgentMindConnector implements Migrating, MigrationAware {

	private boolean agentSleeping;
	
	private String currentEmotion;
	
	public ReviewAgent(IArchitecture architecture) 
	{
		super(architecture);	
	}

	@Override
	protected void architectureReady() 
	{
		// sendSleeping();
		currentEmotion = "neutral";
		sendTalk("Hello, how can I help you?",currentEmotion); 
	}

	@Override
	public void awakeMind() {}

	@Override
	public boolean isMindSleeping() {
		return false;
	}

	@Override
	protected void processActionFailure(MindAction a) 
	{
		// if an action fails, try it again
		this.newAction(a);
	}

	@Override
	protected void processActionSuccess(MindAction a) 
	{
		if (a.getSubject()=="Sarah" && a.getName().equals("talk")
		   && (a.getParameters()!= null) && (a.getParameters().size()>0))
		{	
			String utterance = a.getParameters().get(0);
			if (utterance.toLowerCase().contains("migrat")
				&& utterance.toLowerCase().contains("phone"))
			{
				sendMigrate("Android");
				sendSleeping();	
			}
				
		}
	}

	@Override
	protected void processEntityAdded(String entityName) 
	{
		
	}

	@Override
	protected void processEntityRemoved(String entityName) {}

	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue) 
	{
		if (entityName.equals("User") && propertyName.equals("isPresent")
			&& propertyValue.equals("True"))
		{
			if (agentSleeping)
			{	
				sendTalk("Hello, how can I help you?","neutral"); 
				agentSleeping = false;
			}
		}
		if (entityName.equals("User") && propertyName.equals("isPresent")
				&& propertyValue.equals("False"))
		{
			if (!agentSleeping) sendSleeping();
		}
		
		if (entityName.equals("User") && propertyName.equals("position"))
		{
			if (!agentSleeping) sendGaze(propertyValue);
		}
	}


	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {}

	@Override
	protected void processRemoteAction(MindAction remoteAction) 
	{
		// process incoming sms
		if (remoteAction.getName().equals("sms") && remoteAction.getParameters().size()>0)
		{
			String incomingText = remoteAction.getParameters().get(0).toLowerCase();
			if (incomingText.contains("ugly"))
			{
				currentEmotion = "sadness";
				sendTalk("That was not very nice of you.",currentEmotion);
			}
			else if (incomingText.contains("thank"))
			{
				currentEmotion = "joy";
				sendTalk("You are welcome","joy");
			}
			else if (incomingText.contains("bye"))
			{
				sendTalk("Goodbye. Hope to see you again soon.",currentEmotion);	
			}
			else if (incomingText.contains("wake"))
			{
				currentEmotion = "neutral";
				sendTalk("Hello",currentEmotion);
			}
			else if (incomingText.contains("where"))
			{
				sendTalk("She is on a business trip.",currentEmotion);
			}
			else if (incomingText.contains("meet"))
			{
				sendTalk("Do you want me to ask her when she can meet you?",currentEmotion);
			}
			else if (incomingText.contains("no"))
			{
				sendTalk("Alright, let me know if you change your mind.",currentEmotion);
			}
			else if (incomingText.contains("yes"))
			{
				sendTalk("I will migrate to her phone and ask her.",currentEmotion);
			}

		}	
	}

	@Override
	public void sendMindToSleep() {}
	
	private void sendSleeping() 
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add("sleeping");
		MindAction mindAction = new MindAction("Sarah", "emotion", parameters);
		newAction(mindAction);	
		agentSleeping = true;
	}

	private void sendTalk(String utterance, String emotion) 
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(utterance);
		parameters.add(emotion);
		MindAction mindAction = new MindAction("Sarah", "talk", parameters);
		newAction(mindAction);		
	}
	
	private void sendGaze(String direction) 
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(direction);
		MindAction mindAction = new MindAction("Sarah", "gaze", parameters);
		newAction(mindAction);	
	}
	
	private void sendMigrate(String target) 
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(target);
		MindAction mindAction = new MindAction("Sarah", "Migration", parameters);
		newAction(mindAction);	
	}

	@Override
	public String getMessageTag() 
	{
		return "simpleMind";
	}

	@Override
	public void restoreState(Element message) 
	{
		// read emotion
		currentEmotion = message.getElementsByTagName("emotion").item(0).getChildNodes().item(0).getNodeValue();
		
		String reply = null;
		// check if we got a reply and read it if we got one
		if (message.getElementsByTagName("reply").getLength() > 0)
			reply = message.getElementsByTagName("reply").item(0).getChildNodes().item(0).getNodeValue();
		
		// no reply, so we came from the robot
		if (reply==null)
		{
			
		}
		else
		{
			if (reply.trim().equals("") || reply.trim().equals("I can't say now"))
				sendTalk("I'm sorry, I couldnt get an answer.",currentEmotion);
			else 
				sendTalk("Your supervisor can meet you "+reply,currentEmotion);
		}	
	}

	@Override
	public Element saveState(Document doc) 
	{
		Element parent = doc.createElement(getMessageTag());
		
		Element emotion = doc.createElement("emotion");
		Node emotionNode = doc.createTextNode(currentEmotion);
		emotion.appendChild(emotionNode);
		parent.appendChild(emotion);
		
		return parent;		
	}

	@Override
	public void onMigrationFailure() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMigrationIn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMigrationOut() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMigrationSuccess() {
		// TODO Auto-generated method stub
		
	}

}
