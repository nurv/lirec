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

import java.util.HashMap;

import cmion.architecture.IArchitecture;

/** The BlackBoard  is a storage container for competencies to share information
 *  with each other. For time critical applications, it has some additional 
 *  functions to store "real-time" properties, those are properties that are not written
 *  and read through ION requests and events but directly. The advantage of this is faster
 *  reaction times (not tied to the ION simulation loop), the disadvantage, is that there
 *  are no event listeners for changes to those properties available and access is less 
 *  controlled */
public class BlackBoard extends CmionStorageContainer 
{
	
	/** this is where real time properties are stored */
	private HashMap<String,Object> rtproperties;
	
	/** create a new black board with the specified name */
	public BlackBoard(IArchitecture architecture, String name) {
		// this container is of the type "BlackBoard"
		super(architecture,name, "BlackBoard", null);
		rtproperties = new HashMap<String,Object>();
	}	

	/** returns the value of the real time property with the given name */
	public synchronized Object getRTPropertyValue(String propertyName)
	{
		return rtproperties.get(propertyName);
	}
	
	/** sets the value of the real time property with the given name */
	public synchronized void setRTProperty(String propertyName, Object propertyValue)
	{
		rtproperties.put(propertyName,propertyValue);
	}
	
	/** sets the value of the real time property with the given name */
	public synchronized void removeRTProperty(String propertyName)
	{
		rtproperties.remove(propertyName);
	}

	/** sets the value of the real time property with the given name */
	public synchronized boolean hasRTProperty(String propertyName)
	{
		return rtproperties.containsKey(propertyName);
	}

}
