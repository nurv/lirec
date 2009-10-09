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

package lirec.level2;

import ion.Meta.Request;

import java.util.HashMap;

/** this request can be scheduled with competencies to request it to run*/
public class RequestStartCompetency extends Request {

	/** the parameters for running the competence*/
	private HashMap<String, String> parameters;
	
	/** create a new request to start a competency
	 * 
	 * @param parameters the parameters for starting the competency
	 */
	public RequestStartCompetency(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	/** returns the parameters */
	public HashMap<String,String> getParameters()
	{
		return parameters;
	}
	
}
