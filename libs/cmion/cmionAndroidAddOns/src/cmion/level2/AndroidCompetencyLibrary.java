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

package cmion.level2;

import java.io.InputStream;

import android.content.Context;
import cmion.architecture.IArchitecture;


/** the android competency library extends the competency library, overriding unsupported methods in the android platform.  */
public class AndroidCompetencyLibrary extends CompetencyLibrary {
	
	/** Create a new competency library */
	public AndroidCompetencyLibrary(IArchitecture architecture, String competencyLibraryFile) throws Exception 
	{
		super(architecture, competencyLibraryFile);
	}
	
	@Override
	protected InputStream openConfigurationFile(String competencyLibraryFile) throws Exception{
		Context context = (Context) architecture.getSystemContext();
		int configFileId = context.getResources().getIdentifier(competencyLibraryFile, "raw", context.getPackageName());
		if(configFileId == 0){
			throw new Exception("Could not locate competency library configuration file " + competencyLibraryFile);
		}
		
		return context.getResources().openRawResource(configFileId);
	}

}
