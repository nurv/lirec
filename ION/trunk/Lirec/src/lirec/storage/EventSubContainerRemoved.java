package lirec.storage;

import ion.Meta.Event;

/** an event informing that a new sub container has been removed from a LirecStorageContainer
 *  This will be raised by the owner container
 *  Note that this will only be raised if removal has been requested for the container itself,
 *  if this container was removed because its parent, grandparent, etc, was removed, the event
 *  will not be raised  */
public class EventSubContainerRemoved extends Event 
{
	/** the name of the container that was removed */
	private String removedContainerName;
	
	/** the type of the container that was removed */
	private String removedContainerType;	
	
	/** the container from which the sub container was removed */
	private LirecStorageContainer parentContainer;
	
	/** create an event informing of the removal of a sub container*/
	public EventSubContainerRemoved(String removedContainerName, String removedContainerType, LirecStorageContainer parentContainer)
	{
		this.removedContainerName = removedContainerName;
		this.removedContainerType = removedContainerType;
		this.parentContainer = parentContainer;
	}
	
	/** returns the name of the removed container */
	public String getRemovedContainerName()
	{
		return removedContainerName;
	}
	
	/** returns the type of the removed container */
	public String getRemovedContainerType()
	{
		return removedContainerType;
	}
	
	/** returns a reference to the container from which the sub container was removed  */
	public LirecStorageContainer getParentContainer()
	{
		return parentContainer;
	}
	
}
