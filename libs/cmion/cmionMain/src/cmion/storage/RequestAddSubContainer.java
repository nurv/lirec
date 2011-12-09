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

import ion.Meta.Request;

import java.util.HashMap;
import java.util.HashSet;

/** a request that can be made to any CMION Storage Container for adding a new
 *  sub container*/
public class RequestAddSubContainer extends Request 
{
	
	/** the name of the sub container to be added */
	private String newContainerName;
	
	/** the type of the sub container to be added */
	private String newContainerType;
	
	/** the initial properties of the sub container */
	private HashMap<String,Object> initialProperties;
	
	/** which of the initial properties of the sub container are persistent */
	private HashSet<String> persistentProperties;	
	
	public RequestAddSubContainer(String name, String type)
	{
		this(name,type,null,null);
	}

	public RequestAddSubContainer(String name, String type, HashMap<String,Object> initialProperties)
	{
		this(name,type,initialProperties,null);
	}
	
	public RequestAddSubContainer(String name, String type,
			HashMap<String, Object> initialProperties,
			HashSet<String> persistentProperties) 
	{
		this.newContainerName = name;
		this.newContainerType = type;
		this.initialProperties = initialProperties;
		this.persistentProperties = persistentProperties;
	}

	/** returns the name of the sub container to be added */
	public String getNewContainerName()
	{
		return newContainerName;
	}
	
	/** returns the initial properties of the sub container to be added */
	public HashMap<String,Object> getInitialProperties()
	{
		return initialProperties;
	}
	
	/** returns the set of persistent initial properties of the sub container to be added */
	public HashSet<String> getPersistentProperties()
	{
		return persistentProperties;
	}
	
	/** returns the name of the sub container to be added */
	public String getNewContainerType()
	{
		return newContainerType;
	}

}
