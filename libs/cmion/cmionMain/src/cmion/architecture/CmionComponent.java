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

package cmion.architecture;

import ion.Meta.Element;
import ion.Meta.Event;

/** parent class for all cmion components */
public abstract class CmionComponent extends Element {
	
/** reference to the architecture, through which references to other components can be obtained */
protected IArchitecture architecture;

/** create a new cmion Component */
protected CmionComponent(IArchitecture architecture)
{
	this.architecture = architecture;
}

/** returns a reference to the architecture object, through which references to other components can be obtained */
public IArchitecture getArchitecture() {
	return architecture;
}

/** every cmion component has to implement this method and register its event and
 * 	request handlers in here */
public abstract void registerHandlers();

/** Cmion compoenents that do require a socket connection to an external program, such
 *  as a fatima mind, various competencies, etc.  can override this method and in it
 *  return the connection status */
public boolean isConnected()
{
	return true;
}

@Override
public void onDestroy() {
	// TODO Auto-generated method stub
	
}

@Override
public void raise(Event evt) 
{
	// if the event is a cmion event, register ourselves as originator
	if (evt instanceof CmionEvent)
		((CmionEvent) evt).setOriginator(this);

	super.raise(evt);
}
	
}
