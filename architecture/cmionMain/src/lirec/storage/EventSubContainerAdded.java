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

/** an event informing that a new subcontainer has been added to a LirecStorageContainer
 *  This will be raised by the owner container*/
public class EventSubContainerAdded extends Event{

/** the sub container that has been added */
private LirecStorageContainer subContainer;

/** the parent container that has added the sub container*/
private LirecStorageContainer parentContainer;


/** create a new event sub container added */
public EventSubContainerAdded(LirecStorageContainer parentContainer, LirecStorageContainer subContainer)
{
	this.parentContainer = parentContainer;
	this.subContainer =  subContainer;
}

/** returns the sub container that has been added */
public LirecStorageContainer getSubContainer()
{
	return subContainer;
}

/** returns the container that has added the sub container */
public LirecStorageContainer getParentContainer()
{
	return parentContainer;
}


}
