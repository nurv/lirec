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

import ion.Meta.Request;

import java.util.HashMap;

/** a request that can be made to any Lirec Storage Container for adding a new
 *  sub container*/
public class RequestAddSubContainer extends Request 
{
	
	/** the name of the sub container to be added */
	private String newContainerName;
	
	/** the type of the sub container to be added */
	private String newContainerType;
	
	/** the initial properties of the sub container */
	private HashMap<String,Object> initialProperties;
	
	public RequestAddSubContainer(String name, String type)
	{
		this.newContainerName = name;
		this.newContainerType = type;
	}

	public RequestAddSubContainer(String name, String type, HashMap<String,Object> properties)
	{
		this.newContainerName = name;
		this.newContainerType = type;
		this.initialProperties = properties;
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
	
	/** returns the name of the sub container to be added */
	public String getNewContainerType()
	{
		return newContainerType;
	}

}
