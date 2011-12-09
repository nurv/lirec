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

import cmion.architecture.CmionEvent;

/** This event is raised whenever a property value has been set through a
 *  RequestSetProperty. Also raised when the property did not exist before and was 
 *  created.*/
public class EventPropertyChanged extends CmionEvent {

	/** the name of the property whose value has changed*/
	private String propertyName;	

	/** the value the property was set to */
	private Object propertyValue;
	
	/** whether the property is persistent or not */
	private boolean persistent;	
	
	/** the container to which the changed property belongs */
	private CmionStorageContainer parentContainer;

	/** create a new event that a property of given name was set to given value
	 * @param persistent */
	public EventPropertyChanged(String propertyName, Object propertyValue, boolean persistent, CmionStorageContainer parentContainer)
	{
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
		this.parentContainer = parentContainer;
		this.persistent = persistent;
	}

	/** return the name of the property that was changed */
	public String getPropertyName()
	{
		return propertyName;
	}

	/** return whether the property is persistent */
	public boolean isPersistent()
	{
		return persistent;
	}	
	
	/** return the value the property was set to */
	public Object getPropertyValue()
	{
		return propertyValue;
	}	
	
	/** return the container which the changed property belongs to */
	public CmionStorageContainer getParentContainer()
	{
		return parentContainer;
	}
	
	/** displays information about this event */
	@Override
	public String toString()
	{
		String evtString =  "Property changed of " + parentContainer.getContainerName() +
		                    ": "  + propertyName +" = " + propertyValue ;
		return evtString;		
	}
	
}
