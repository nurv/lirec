package lirec.level3;

import ion.Meta.Request;

/** this type of event is raised by the agent mind connector when the mind sends a new action for execution 
 *  the competency manager listens for those events to plan their execution */
public class RequestNewMindAction extends Request 
{

	/** creates a new event */
	RequestNewMindAction(MindAction mindAction)
	{
		super();
		this.mindAction = mindAction;
	}

	/** the mind action that has been sent */
	private MindAction mindAction;

	/** returns the mind action that this event refers to */
	public MindAction getMindAction()
	{
		return mindAction;
	}
}