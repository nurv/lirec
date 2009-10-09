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

package lirec.architecture;

import ion.Meta.Element;

/** parent class for all Lirec architecture components */
public abstract class LirecComponent extends Element {
	
/** reference to the architecture, through which references to other components can be obtained */
protected Architecture architecture;

/** create a new Lirec Component */
protected LirecComponent(Architecture architecture)
{
	this.architecture = architecture;
}

/** returns a reference to the Lire architecture object */
public Architecture getArchitecture() {
	return architecture;
}

/** every lirec component has to implement this method and register its event and
 * 	request handlers in here */
public abstract void registerHandlers();

/** Lirec Compoenents that do require a socket connection to an external program, such
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

}
