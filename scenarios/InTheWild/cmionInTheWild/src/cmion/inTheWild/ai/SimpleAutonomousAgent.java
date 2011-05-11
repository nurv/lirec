package cmion.inTheWild.ai;

import java.util.ArrayList;

import cmion.architecture.IArchitecture;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

public class SimpleAutonomousAgent extends AgentMindConnector{

	private boolean agentSleeping;
	
	public SimpleAutonomousAgent(IArchitecture architecture) 
	{
		super(architecture);	
	}

	@Override
	protected void architectureReady() 
	{
		sendSleeping();
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
				sendTalk("That was not very nice of you.","anger");
			else if (incomingText.contains("lecture"))
				sendTalk("Your next lecture is in 15 minutes in room B2","neutral");
			else if (incomingText.contains("thank"))
				sendTalk("You are welcome","joy");
			else if (incomingText.contains("bye"))
				sendTalk("Goodbye. Hope to see you again soon.","neutral");	
		}	
	}

	@Override
	public void sendMindToSleep() {}
	
	private void sendSleeping() 
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add("sleeping");
		MindAction mindAction = new MindAction("Greta", "emotion", parameters);
		newAction(mindAction);	
		agentSleeping = true;
	}

	private void sendTalk(String utterance, String emotion) 
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(utterance);
		parameters.add(emotion);
		MindAction mindAction = new MindAction("Greta", "talk", parameters);
		newAction(mindAction);		
	}
	
	private void sendGaze(String direction) 
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(direction);
		MindAction mindAction = new MindAction("Greta", "gaze", parameters);
		newAction(mindAction);	
	}

	@Override
	protected void processActionCancellation(MindAction a) {
		// TODO Auto-generated method stub
		
	}

}
