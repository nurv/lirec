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


/** an event informing that a new sub container has been removed from a CMION StorageContainer
 *  This will be raised by the owner container
 *  Note that this will only be raised if removal has been requested for the container itself,
 *  if this container was removed because its parent, grandparent, etc, was removed, the event
 *  will not be raised  */
public class EventSubContainerRemoved extends CmionEvent 
{
	/** the name of the container that was removed */
	private String removedContainerName;
	
	/** the type of the container that was removed */
	private String removedContainerType;	
	
	/** the container from which the sub container was removed */
	private CmionStorageContainer parentContainer;
	
	/** create an event informing of the removal of a sub container*/
	public EventSubContainerRemoved(String removedContainerName, String removedContainerType, CmionStorageContainer parentContainer)
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
	public CmionStorageContainer getParentContainer()
	{
		return parentContainer;
	}
	
	/** displays information about this event */
	@Override
	public String toString()
	{
		String evtString =  "Sub Container removed from " + parentContainer.getContainerName() +
		                    ": " + removedContainerName;
		return evtString;		
	}
	
}
