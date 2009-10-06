package lirec.storage;

import ion.Meta.Event;

/** This event is raised whenever a property value has been set through a
 *  RequestSetProperty. Also raised when the property did not exist before and was 
 *  created.*/
public class EventPropertyChanged extends Event {

	/** the name of the property whose value has changed*/
	private String propertyName;	

	/** the value the property was set to */
	private Object propertyValue;
	
	/** the container to which the changed property belongs */
	private LirecStorageContainer parentContainer;

	/** create a new event that a property of given name to given value*/
	public EventPropertyChanged(String propertyName, Object propertyValue, LirecStorageContainer parentContainer)
	{
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		this.parentContainer = parentContainer;
	}

	/** return the name of the property that was changed */
	public String getPropertyName()
	{
		return propertyName;
	}

	/** return the value the property was set to */
	public Object getPropertyValue()
	{
		return propertyValue;
	}	
	
	/** return the container which the changed property belongs to */
	public LirecStorageContainer getParentContainer()
	{
		return parentContainer;
	}
	
}
