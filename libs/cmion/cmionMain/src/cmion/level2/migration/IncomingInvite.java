package cmion.level2.migration;

import ion.Meta.Event;

/** This event signals that the invite listener has received an invite. The event is raised
 *  by the invite listener class and handled by the migration class and should not be used 
 *  in a different context. */ 
public class IncomingInvite extends Event {
	
	/** the device name of the inviter */
	private String inviter;
	
	/** construct a new incoming invite event
	 * @param inviter the name of the device that is inviting us */
	public IncomingInvite(String inviter)
	{
		this.inviter = inviter;
	}

	/** return the device name of the inviter */
	public String getInviter()
	{
		return inviter;
	}
}
