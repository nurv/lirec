package lirec.level3;

import ion.Meta.Event;

	/** this type of event is raised by the competency manager when a mind action has succeeded 
	 *  the agent mind connector listens for those events and sends them to the agent mind*/
public class EventMindActionSucceeded extends Event 
{

	EventMindActionSucceeded(MindAction mindAction)
	{
		super();
		this.mindAction = mindAction;
	}
	
	/** the mind action that has succeeded */
	private MindAction mindAction;
	
	/** returns the mind action that this event refers to */
	public MindAction getMindAction()
	{
		return mindAction;
	}
}
