package lirec.level3;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;
import lirec.architecture.Architecture;
import lirec.architecture.LirecComponent;
import lirec.storage.EventPropertyChanged;
import lirec.storage.EventPropertyRemoved;
import lirec.storage.EventSubContainerAdded;
import lirec.storage.EventSubContainerRemoved;
import lirec.storage.WorldModel;

/** This abstract class describes the functionality an agent mind interface should provide. 
 * A concrete implementation of an AgentMind connector has to implement the abstract methods 
 * of this class. The FAtiMA connector subclasses this class but other subclasses for different 
 * agent minds are possible. The class SuperSimpleMind exemplifies how another mind could be 
 * integrated.
 * 
 * */
public abstract class AgentMindConnector extends LirecComponent 
{
	
/** create a new Agent Mind Connector */	
public AgentMindConnector(Architecture architecture)
{
	super(architecture);
}
	
	
/** sends a command to the connected mind, making it pause any decision making processes and
 * action execution,  while the mind is paused it should not send any action */
public abstract void sendMindToSleep();

/** returns whether the mind is sleeping or not */
public abstract boolean isMindSleeping(); 
	
/** sends a command to the mind telling it to resume decision making and action execution */
public abstract void awakeMind();

/** sends a remoteAction (an action that another agent/user has performed) to the mind */
protected abstract void processRemoteAction(MindAction remoteAction);

/** informs mind of the success of a recently executed action */
protected abstract void processActionSuccess(MindAction a);

/** informs mind of the failure of a recently executed action */
protected abstract void processActionFailure(MindAction a);

/** informs the mind that a new entity (agent or object) has been added to the world model*/
protected abstract void processEntityAdded(String entityName);

/** informs the mind that an entity (agent or object) has been removed from the world model*/
protected abstract void processEntityRemoved(String entityName);

/** informs the mind that a property of an agent or object in the world model has changed*/
protected abstract void processPropertyChanged(String entityName,String propertyName,String propertyValue);

/** informs the mind that a property of an agent or object in the world model has been removed*/
protected abstract void processPropertyRemoved(String entityName,String propertyName);



/** in this method the mind connector registers its request and event handlers with ION*/
@Override
public final void registerHandlers()
{
	// register event handlers 
	
	// remote actions could come from several components, so register the handler with the
	// whole simulation
	Simulation.instance.getEventHandlers().add(new HandleRemoteAction());

	// succeeded or failed action events are always raised by the competency manager
	// so register those handlers directly with the competency manager
	architecture.getCompetencyManager().getEventHandlers().add(new HandleActionSucceeded());
	architecture.getCompetencyManager().getEventHandlers().add(new HandleActionFailed());

	// register handlers with the world model for listening to changes to it
	// for now we are interested in entities added and removed from the world model
	architecture.getWorldModel().getEventHandlers().add(new HandleSubContainerAdded());
	architecture.getWorldModel().getEventHandlers().add(new HandleSubContainerRemoved());
	
	// and properties changed and removed from entities in the world model
	architecture.getWorldModel().registerEventHandlerWithSubContainers(new HandlePropertyChanged());
	architecture.getWorldModel().registerEventHandlerWithSubContainers(new HandlePropertyRemoved());	
}


/** call this if the mind decides to execute a new action, this will schedule an ION request
 *  with the competency manager to execute the action */
public final void newAction(MindAction mindAction)
{
	architecture.getCompetencyManager().schedule(new RequestNewMindAction(mindAction));
}

/** abstract method overridden from ION.meta.element */
@Override
public void onDestroy() {
	// TODO Auto-generated method stub
}

/** internal event handler class for listening to remote action events */
private class HandleRemoteAction extends EventHandler {

    public HandleRemoteAction() {
        super(EventRemoteAction.class);
    }

    @Override
    public void invoke(IEvent evt) {
        // since this is an event handler only for type EventPerception the following cast always works
    	MindAction remoteAction = ((EventRemoteAction)evt).getRemoteAction();
    	processRemoteAction(remoteAction);
    }
}

/** internal event handler class for listening to action succeeded events */
private class HandleActionSucceeded extends EventHandler {

    public HandleActionSucceeded() {
        super(EventMindActionSucceeded.class);
    }

    @Override
    public void invoke(IEvent evt) {
        // since this is an event handler only for type EventMindActionSucceeded the following cast always works
    	MindAction mA = ((EventMindActionSucceeded)evt).getMindAction();
    	processActionSuccess(mA);
    }
}

/** internal event handler class for listening to action failed events */
private class HandleActionFailed extends EventHandler {

    public HandleActionFailed() {
        super(EventMindActionFailed.class);
    }

    @Override
    public void invoke(IEvent evt) {
        // since this is an event handler only for type EventMindActionFailed the following cast always works
    	MindAction mA = ((EventMindActionFailed)evt).getMindAction();
    	processActionFailure(mA);
    }
}

/** internal event handler class for listening to sub container added events */
private class HandleSubContainerAdded extends EventHandler {

    public HandleSubContainerAdded() {
        super(EventSubContainerAdded.class);
    }

    @Override
    public void invoke(IEvent evt) {
        // since this is an event handler only for type EventSubContainerAdded the following cast always works
    	EventSubContainerAdded evtSpecific = (EventSubContainerAdded)evt;
    	
    	// check if the subContainer was added to the World Model
    	if (evtSpecific.getParentContainer().equals(architecture.getWorldModel()))
    		// check if the added Container is an agent or object
    		if (evtSpecific.getSubContainer().getContainerType().equals(WorldModel.AGENT_TYPE_NAME)
    		||	evtSpecific.getSubContainer().getContainerType().equals(WorldModel.OBJECT_TYPE_NAME))
    		{
    			processEntityAdded(evtSpecific.getSubContainer().getContainerName());
    		}  	
    }
}


/** internal event handler class for listening to sub container removed events */
private class HandleSubContainerRemoved extends EventHandler {

    public HandleSubContainerRemoved() {
        super(EventSubContainerRemoved.class);
    }

    @Override
    public void invoke(IEvent evt) {
        // since this is an event handler only for type EventSubContainerRemoved the following cast always works
    	EventSubContainerRemoved evtSpecific = (EventSubContainerRemoved)evt;
    	
    	// check if the subContainer was removed from the World Model
    	if (evtSpecific.getParentContainer().equals(architecture.getWorldModel()))
    		// check if the removed Container is an agent or object
    		if (evtSpecific.getRemovedContainerType().equals(WorldModel.AGENT_TYPE_NAME)
    		||	evtSpecific.getRemovedContainerType().equals(WorldModel.OBJECT_TYPE_NAME))
    		{
    			processEntityRemoved(evtSpecific.getRemovedContainerName());
    		}  	
    }
}

/** internal event handler class for listening to property changed events */
private class HandlePropertyChanged extends EventHandler {

    public HandlePropertyChanged() {
        super(EventPropertyChanged.class);
    }

    @Override
    public void invoke(IEvent evt) {
        // since this is an event handler only for type EventPropertyChanged the following cast always works
    	EventPropertyChanged evtSpecific = (EventPropertyChanged)evt;
    	
   		// check if the property belongs to an agent or object
    	if (evtSpecific.getParentContainer().getContainerType().equals(WorldModel.AGENT_TYPE_NAME)
    	||	evtSpecific.getParentContainer().getContainerType().equals(WorldModel.OBJECT_TYPE_NAME))
    	{
    		processPropertyChanged(evtSpecific.getParentContainer().getContainerName(),
    								evtSpecific.getPropertyName(),
    								evtSpecific.getPropertyValue().toString());
    	}  	
    }
}

/** internal event handler class for listening to property removed events */
private class HandlePropertyRemoved extends EventHandler {

    public HandlePropertyRemoved() {
        super(EventPropertyRemoved.class);
    }

    @Override
    public void invoke(IEvent evt) {
        // since this is an event handler only for type EventPropertyRemoved the following cast always works
    	EventPropertyRemoved evtSpecific = (EventPropertyRemoved)evt;
    	
   		// check if the property belonged to an agent or object
    	if (evtSpecific.getParentContainer().getContainerType().equals(WorldModel.AGENT_TYPE_NAME)
    	||	evtSpecific.getParentContainer().getContainerType().equals(WorldModel.OBJECT_TYPE_NAME))
    	{
    		processPropertyRemoved(evtSpecific.getParentContainer().getContainerName(),
    								evtSpecific.getPropertyName());
    	}  	
    }
}


}
