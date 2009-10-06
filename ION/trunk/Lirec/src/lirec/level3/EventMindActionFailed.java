package lirec.level3;

import ion.Meta.Event;

/** this type of event is raised by the competency manager when a mind action has failed 
 *  the agent mind connector listens for those events and sends them to the agent mind*/
public class EventMindActionFailed extends Event 
{

EventMindActionFailed(MindAction mindAction)
{
	super();
	this.mindAction = mindAction;
}

/** the mind action that has failed */
private MindAction mindAction;

/** returns the mind action that this event refers to */
public MindAction getMindAction()
{
	return mindAction;
}
}

