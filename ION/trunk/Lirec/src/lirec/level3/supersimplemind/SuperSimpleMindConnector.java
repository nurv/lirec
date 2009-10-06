package lirec.level3.supersimplemind;

import lirec.architecture.Architecture;
import lirec.level3.AgentMindConnector;
import lirec.level3.MindAction;

/** The connector to a SuperSimpleMind (example implementation of a simple mind interface) */
public class SuperSimpleMindConnector extends AgentMindConnector {

	
	/** the mind itself */
	private SuperSimpleMind mind;
	
	/** creates a new connector for a SuperSimpleMind */ 
	public SuperSimpleMindConnector(Architecture architecture)
	{	
		super(architecture);
		// create the mind, initially sleeping
		mind = new SuperSimpleMind(this);
	}
	
	
	@Override
	public synchronized void awakeMind() {
		mind.sendAwake();
	}

	@Override
	public synchronized boolean isMindSleeping() {
		return mind.isSleeping();
	}

	@Override
	public synchronized void processActionFailure(MindAction a) {
		mind.sendFailure(a);
	}

	@Override
	public synchronized void processActionSuccess(MindAction a) {
		mind.sendSuccess(a);
	}

	/** pause the mind */
	@Override
	public synchronized void  sendMindToSleep() {
		mind.sendSleep();
	}

	@Override
	public synchronized void processRemoteAction(MindAction remoteAction) {
		mind.sendRemoteAction(remoteAction);		
	}


	@Override
	protected void processEntityAdded(String entityName) {
		mind.sendEntityAdded(entityName);
	}


	@Override
	protected void processEntityRemoved(String entityName) {
		mind.sendEntityRemoved(entityName);
	}


	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue) {
		mind.sendPropertyChanged(entityName,propertyName,propertyValue);
	}


	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {
		mind.sendPropertyRemoved(entityName,propertyName);
	}

}
