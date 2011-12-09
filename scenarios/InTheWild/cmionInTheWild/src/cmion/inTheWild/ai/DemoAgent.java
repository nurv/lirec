package cmion.inTheWild.ai;

import java.util.ArrayList;
import java.util.Random;

import cmion.architecture.IArchitecture;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

public class DemoAgent extends AgentMindConnector
{

	private long lastTimeSent;
	
	public DemoAgent(IArchitecture architecture) {
		super(architecture);
		lastTimeSent = System.currentTimeMillis();
	}
	
	private synchronized long getTimeElapsed()
	{
		return System.currentTimeMillis() - lastTimeSent;
	}	
	
	@Override
	public void sendMindToSleep() {
	}

	@Override
	public boolean isMindSleeping() {
		return false;
	}

	@Override
	public void awakeMind() {
	}
	
	private synchronized void sendAction(MindAction action)
	{
		if (getTimeElapsed()>8000)
		{	
			this.newAction(action);
			lastTimeSent = System.currentTimeMillis();
		}
	}

	@Override
	protected synchronized void processRemoteAction(MindAction remoteAction) {
		if (remoteAction.getName().equalsIgnoreCase("comeClose"))
		{
			String s[] = { "Anger4", "Anger5" , "Fear4", "Fear5", "Surprise3", "Surprise4", "Surprise5" }; 
			Random r = new Random();
			int i = r.nextInt(s.length);
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add(s[i]);
			MindAction ma = new MindAction("Emys","wozEmotion",parameters);
			this.newAction(ma);
			lastTimeSent = System.currentTimeMillis();			
		}
	}

	@Override
	protected void processActionSuccess(MindAction a) {
	}

	@Override
	protected void processActionFailure(MindAction a) {
	}

	@Override
	protected void processActionCancellation(MindAction a) {
	}

	@Override
	protected void processEntityAdded(String entityName) {
	}

	@Override
	protected void processEntityRemoved(String entityName) {
	}

	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue, boolean persistent) {
	}

	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {
	}

	@Override
	protected void architectureReady() 
	{
		RandomActionThread t = new RandomActionThread();
		t.start();
	}

	private class RandomActionThread extends Thread
	{
		
		ArrayList<MindAction> actions;
		
		public RandomActionThread()
		{
			actions = new ArrayList<MindAction>();
		}
		
		private MindAction getRandomAction()
		{
			Random r = new Random();
			int i = r.nextInt(actions.size());
			return actions.get(i);
		}
		
		
		@Override
		public void run()
		{
			if (actions.size() > 0)
			{
			while (true)
			{
				MindAction ma = getRandomAction();
				sendAction(ma);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {}
			}
			}
		}
	}

	@Override
	protected void processRawMessage(String message) {
		// TODO Auto-generated method stub
		
	}

	
	
}
