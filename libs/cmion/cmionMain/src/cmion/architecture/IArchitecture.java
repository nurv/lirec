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

import cmion.level2.CompetencyExecution;
import cmion.level2.CompetencyLibrary;
import cmion.level3.CompetencyManager;
import cmion.storage.CmionStorageContainer;
import cmion.storage.WorldModel;

/** specifies an interface for an architecture class that constructs the cmion components and
 *  stores references to them */
public interface IArchitecture {

	/** in this method the architecture must return a reference to the World Model component */
	public WorldModel getWorldModel();

	/** in this method the architecture must return a reference to the Black Board component */
	public CmionStorageContainer getBlackBoard();
	
	/** in this method the architecture must return a reference to the competency execution component */	
	public CompetencyExecution getCompetencyExecution();

	/** in this method the architecture must return a reference to the competency manager component */		
	public CompetencyManager getCompetencyManager();

	/** in this method the architecture must return a reference to the competency library component */			
	public CompetencyLibrary getCompetencyLibrary();
	
}
