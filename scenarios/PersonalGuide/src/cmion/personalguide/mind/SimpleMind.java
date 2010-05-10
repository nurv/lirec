package cmion.personalguide.mind;


import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import cmion.architecture.IArchitecture;
import cmion.level2.migration.Migrating;
import cmion.level2.migration.MigrationAware;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

public class SimpleMind extends AgentMindConnector implements Migrating, MigrationAware 
{

	private String emotionalState;
	private String replyToMigrate;
	
	// lock actions until they fail or succeed
	private MindAction lockAction;
	
	
	public SimpleMind(IArchitecture architecture) 
	{
		super(architecture);
		emotionalState = "neutral";
		replyToMigrate = "";
		lockAction = null;
	}

	@Override
	protected void architectureReady() 
	{
		executeAction(getEmotionAction("sleep"));
		//executeAction(getTalkAction("Hello!"));
	}

	@Override
	public void awakeMind() {}

	@Override
	public boolean isMindSleeping() 
	{
		return false;
	}

	@Override
	protected void processActionFailure(MindAction a) 
	{
		//unlock
		if (a.equals(lockAction)) lockAction=null;
	}

	@Override
	protected void processActionSuccess(MindAction a) 
	{
		//unlock
		if (a.equals(lockAction)) lockAction=null;

		if (a.getSubject().equals("Sarah")
			&& (a.getName().equals("Talk"))
			&& (a.getParameters() != null)
		    && (a.getParameters().size() > 0))
		{
			String utterance = a.getParameters().get(0);
			if (utterance.toLowerCase().contains("hello"))
				executeAction(getTalkAction("Can I ask you a question?"));
			else if (utterance.toLowerCase().contains("sorry"))
				executeAction(getTalkAction("Migrating back, have a nice day."));
			else if (utterance.toLowerCase().contains("thank you"))
				executeAction(getTalkAction("Migrating back, have a nice day."));
			else if (utterance.toLowerCase().contains("migrating"))
			{
				executeAction(getEmotionAction("sleep"));
				executeAction(getMigrationAction("Screen"));
			}
		}
	}

	@Override
	protected void processEntityAdded(String entityName) 
	{
	}

	@Override
	protected void processEntityRemoved(String entityName) 
	{
	}

	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue) 
	{
	}

	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) 
	{
	}

	@Override
	protected void processRemoteAction(MindAction remoteAction) 
	{
		if (remoteAction.getSubject().equals("User")
		 && remoteAction.getName().equals("Reply")
		 && (remoteAction.getParameters() != null)
		 && (remoteAction.getParameters().size() > 0))
		{
			String userChoice = remoteAction.getParameters().get(0);
			if (userChoice.toLowerCase().equals("yes"))
			{
				emotionalState = "joy";
				executeAction(getEmotionAction(emotionalState));
				executeAction(getTalkAction("When can you meet your student?"));
			}
			else if (userChoice.toLowerCase().equals("no"))
			{
				replyToMigrate = userChoice;
				emotionalState = "sadness";
				executeAction(getEmotionAction(emotionalState));
				executeAction(getTalkAction("Ok, sorry to bother you."));
			}
			else 
			{ //this was an answer to the question of when to meet the student
				replyToMigrate = userChoice;
				executeAction(getTalkAction("Thank you, I will let him know."));				
			}
		}
	}

	@Override
	public void sendMindToSleep() 
	{
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
		emotionalState = message.getElementsByTagName("emotion").item(0).getChildNodes().item(0).getNodeValue();
		// perform the emotion
		executeAction(getEmotionAction(emotionalState));
		executeAction(getTalkAction("Hello!"));
	}

	@Override
	public Element saveState(Document doc) 
	{
		Element parent = doc.createElement(getMessageTag());
		
		Element emotion = doc.createElement("emotion");
		Node emotionNode = doc.createTextNode(emotionalState);
		emotion.appendChild(emotionNode);
		parent.appendChild(emotion);

		Element reply = doc.createElement("reply");
		Node replyNode = doc.createTextNode(replyToMigrate);
		reply.appendChild(replyNode);
		parent.appendChild(reply);
		
		return parent; 
	}

	@Override
	public void onMigrationFailure() {}

	@Override
	public void onMigrationIn() {}

	@Override
	public void onMigrationOut() {}

	@Override
	public void onMigrationSuccess() {}

	private MindAction getEmotionAction(String emotion)
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(emotion);
		return new MindAction("Sarah","Emotion",parameters);
	}
	
	private MindAction getTalkAction(String utterance)
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(utterance);
		return new MindAction("Sarah","Talk",parameters);
	}
	
	private MindAction getMigrationAction(String targetPlatform)
	{
		ArrayList<String> parameters = new ArrayList<String>();
		parameters.add(targetPlatform);
		return new MindAction("Sarah","Migration",parameters);
	}
	
	private void executeAction(MindAction ma)
	{
		new ExecuterThread(ma).start();
		Thread.yield();
	}
	
	private class ExecuterThread extends Thread
	{
		private MindAction actionToExecute;
		
		public ExecuterThread(MindAction actionToExecute)
		{
			this.actionToExecute = actionToExecute;
		}
		
		@Override
		public void run()
		{
			while (lockAction!=null)
			{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
			lockAction = actionToExecute;
			newAction(actionToExecute);
		}
	}
	
}
