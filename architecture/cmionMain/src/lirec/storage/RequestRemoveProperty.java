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