package lirec.storage;

import ion.Meta.Event;

/** This event is raised by a container whenever a property is removed from this
 * container. The event is not raised if the property is deleted because the container
 * itself or its parent grandparent etc is removed. 
 */
public class EventPropertyRemoved extends Event {

	/** the name of the property whose value has changed*/
	private String propertyName;	

	/** the container from which the property was removed */
	private LirecStorageContainer parentContainer;
	
	/** create a new event that a property of given name was removed*/
	public EventPropertyRemoved(String propertyName, LirecStorageContainer parentContainer)
	{
		this.propertyName = propertyName;
		this.parentContainer = parentContainer;
	}

	/** return the name of the property that was removed */
	public String getPropertyName()
	{
		return propertyName;
	}
	
	/** returns a reference to the container from which the property was removed  */
	public LirecStorageContainer getParentContainer()
	{
		return parentContainer;
	}
	
}