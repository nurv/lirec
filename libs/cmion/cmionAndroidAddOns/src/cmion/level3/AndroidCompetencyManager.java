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

package cmion.level3;

import java.io.InputStream;

import android.content.Context;
import cmion.architecture.IArchitecture;

/** the competency manager receives actions from the mind for execution and decomposes 
 *  them into a plan of competencies for execution */
public class AndroidCompetencyManager extends CompetencyManager 
{
	public AndroidCompetencyManager(IArchitecture architecture, String competencyManagerRulesFileName) throws Exception
	{
		super(architecture, competencyManagerRulesFileName);
	}
	
	@Override
	protected InputStream openRulesFile(String competencyManagerRulesFileName) throws Exception {
		Context context = (Context) architecture.getSystemContext();
		int configFileId = context.getResources().getIdentifier(competencyManagerRulesFileName, "raw", context.getPackageName());
		
		// check if file exists
		if(configFileId == 0){
			throw new Exception("cannot locate competency manager rules file " + competencyManagerRulesFileName );
		}
		
		return context.getResources().openRawResource(configFileId);
	}
	
}