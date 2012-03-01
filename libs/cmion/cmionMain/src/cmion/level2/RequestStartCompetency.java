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

import ion.Meta.Request;

import java.util.HashMap;

/** this request can be scheduled with competencies to request it to run*/
public class RequestStartCompetency extends Request {

	/** the parameters for running the competence*/
	private HashMap<String, String> parameters;

	/** the competency execution plan, as part of which this competency was started*/
	private CompetencyExecutionPlan cep;
	
	/** the execution id for the competency */
	private long executionID;
	
	/** create a new request to start a competency
	 * 
	 * @param parameters the parameters for starting the competency
	 * @param cep the competency execution plan, as part of which this competency was started
	 * @param executionID 
	 */
	public RequestStartCompetency(HashMap<String, String> parameters, CompetencyExecutionPlan cep, long executionID) {
		this.parameters = parameters;
		this.cep = cep;
		this.executionID = executionID;
	}

	/** returns the parameters */
	public HashMap<String,String> getParameters()
	{
		return parameters;
	}
	
	/** returns the competency execution plan, as part of which this competency was started*/
	public CompetencyExecutionPlan getPlan()
	{
		return cep;
	}
	
	/** returns the execution id for this competency*/
	public long getExecutionID()
	{
		return executionID;
	}
	
}
