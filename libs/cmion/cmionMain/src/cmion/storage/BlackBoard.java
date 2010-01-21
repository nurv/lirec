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
  02/12/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  ---  
*/

package cmion.storage;

import cmion.architecture.IArchitecture;

/** The BlackBoard  is a storage container for competencies to share information
 *  with each other */
public class BlackBoard extends CmionStorageContainer 
{

	/** create a new black board with the specified name */
	public BlackBoard(IArchitecture architecture, String name) {
		// this container is of the type "WorldModel"
		super(architecture,name, "BlackBoard", null);
	}	
	
}