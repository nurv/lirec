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

package cmion.level3.fatima;

import java.util.ArrayList;
import java.util.StringTokenizer;

import cmion.level3.MindAction;
import cmion.storage.CmionStorageContainer;


/** a collection of utility methods for handling the communication via the FAtiMA
 *  protocol */
public class FAtiMAutils {
	
	/** returns a String listing all properties of a storage container in the format
	 * specified by the FAtiMA protocol, which is for example "prop1:value1 prop2:value2 prop3:value3" */
	public static String getPropertiesString(CmionStorageContainer entityContainer) {
		String propString="";
		for (String propName : entityContainer.getPropertyNames())
		{
			propString += " " + propName + ":" + entityContainer.getPropertyValue(propName);
		}
		return propString;
	}
	
	/** returns a FAtiMA message that represents the provided mind action */
	public static String mindActiontoFatimaMessage(MindAction mindAction)
	{
		String result = mindAction.getName();
		for (String parameter : mindAction.getParameters()) result += " " + parameter;
		return result;
	}
	
	/** creates a new mindAction out of a FAtiMA string describing that action */
	public static MindAction fatimaMessageToMindAction(String subject,String msg)
	{
		if (msg.trim().equals("")) return null;
		StringTokenizer st = new StringTokenizer(msg);
		String name = st.nextToken();
		ArrayList<String> parameters = new ArrayList<String>();
		while (st.hasMoreTokens()) parameters.add(st.nextToken());	
		return new MindAction(subject,name,parameters);
	}
	

}
