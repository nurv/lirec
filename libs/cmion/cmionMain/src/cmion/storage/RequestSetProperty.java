/*	
    CMION
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  09/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  Renamed to CMION
  ---  
*/

package cmion.storage;

import ion.Meta.Request;

/** Request to set the given property to the given value. Value can be any object
 *  If a property of that name already existed the old value will be replaced. 
 *  In any case this will result in a property changed event being raised */
public class RequestSetProperty extends Request {

/** the name of the property to set*/
private String propertyName;	

/** the value to set the property to */
private Object propertyValue;

/** is this property persistent or not, if this value is null do not make any changes
 *  to the persistence */
private Boolean persistent;

/** create a new request to set a property of given name to given value
 *  creating the property if it did not exist before
 *  If the property did not exist before it will be created as non-persistent
 *  If it existed before no changes to its persistent state are made*/
public RequestSetProperty(String propertyName, Object propertyValue)
{
	this(propertyName,propertyValue,null);
}

/** create a new request to set a property of given name to given value
 *  creating the property if it did not exist before
 *  @param persistent Is the property persistent or not */
public RequestSetProperty(String propertyName, Object propertyValue, Boolean persistent)
{
	this.propertyName = propertyName;
	this.propertyValue = propertyValue;
	this.persistent = persistent;
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

/** return whether to set the property to persistent (true), 
 *  non-persistent (false) or leave the persistent state as it was (NULL) */
public Boolean getPersistent()
{
	return persistent;
}
	
}
