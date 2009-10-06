package lirec.storage;

import ion.Meta.Request;

/** Request a container to remove property of the given name. If such a property 
 *  existed will result in the container raising an EventPropertyRemoved. */
public class RequestRemoveProperty extends Request {

/** the name of the property to remove*/
private String propertyName;	

/** create a new request to remove a property of given name */
public RequestRemoveProperty(String propertyName)
{
	this.propertyName = propertyName;
}

/** return the name of the removed property */
public String getPropertyName()
{
	return propertyName;
}
	
}