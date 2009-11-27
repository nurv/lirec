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

/** a request that can be made to any Lirec Storage Container for removing an
 *  existing sub container (does not remove sub sub containers)*/
public class RequestRemoveSubContainer extends Request {

	/** the name of the sub container to be removed */
	private String name;
	
	public RequestRemoveSubContainer(String name)
	{
		this.name = name;
	}

	/** returns the name of the sub container to be removed */
	public String getName()
	{
		return name;
	}	
	
}
