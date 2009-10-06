package lirec.storage;

import ion.Meta.Request;

/** Request to set the given property to the given value. Value can be any object
 *  If a property of that name already existed the old value will be replaced. 
 *  In any case this will result in a property changed event being raised */
public class RequestSetProperty extends Request {

/** the name of the property to set*/
private String propertyName;	

/** the value to set the property to */
private Object propertyValue;

/** create a new request to set a property of given name to given value*/
public RequestSetProperty(String propertyName, Object propertyValue)
{
	this.propertyName = propertyName;
	this.propertyValue = propertyValue;
}

/** return the name of the property to set */
public String getPropertyName()
{
	return propertyName;
}

/** return the value to set the property to */
public Object getPropertyValue()
{
	return propertyValue;
}

	
}
