package lirec.level3;
import ion.Meta.Event;

/** this type of event can be raised by any sensing competency. It represents someone
 *  other than the companion (e.g. a user) performing an action. The action is represented
 *  as an agent mind action. The agent mind connector listens for those events and sends 
 *  them to the agent mind*/
public class EventRemoteAction extends Event 
{
	/** create a new event remote action*/
	EventRemoteAction(MindAction remoteAction)
	{
		super();
		this.remoteAction = remoteAction;
	}
	
	/** the remote action that has been registered */
	private MindAction remoteAction;
	
	/** returns the remote action that this event refers to */
	public MindAction getRemoteAction()
	{
		return remoteAction;
	}
}
