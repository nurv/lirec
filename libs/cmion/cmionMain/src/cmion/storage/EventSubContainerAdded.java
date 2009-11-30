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

/** an event informing that a new subcontainer has been added to a CMION StorageContainer
 *  This will be raised by the owner container*/
public class EventSubContainerAdded extends CmionEvent{

/** the sub container that has been added */
private CmionStorageContainer subContainer;

/** the parent container that has added the sub container*/
private CmionStorageContainer parentContainer;


/** create a new event sub container added */
public EventSubContainerAdded(CmionStorageContainer parentContainer, CmionStorageContainer subContainer)
{
	this.parentContainer = parentContainer;
	this.subContainer =  subContainer;
}

/** returns the sub container that has been added */
public CmionStorageContainer getSubContainer()
{
	return subContainer;
}

/** returns the container that has added the sub container */
public CmionStorageContainer getParentContainer()
{
	return parentContainer;
}

/** displays information about this event */
@Override
public String toString()
{
	String evtString =  "Sub Container added to " + parentContainer.getContainerName() +
	                    ": " + subContainer.getContainerName();
	return evtString;		
}


}
