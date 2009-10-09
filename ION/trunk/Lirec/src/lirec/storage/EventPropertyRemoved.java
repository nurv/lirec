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