/*	
        Lirec Architecture
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
  ---  
*/

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
