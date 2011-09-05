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

import cmion.architecture.CmionEvent;

/** this type of event is raised by the competency execution system when a competency execution plan has been carried out successfully 
*  the competency manager listens for those events */
public class EventCompetencyExecutionPlanSucceeded extends CompetencyExecutionPlanEvent 
{

	/** creates a new event */
	public EventCompetencyExecutionPlanSucceeded(CompetencyExecutionPlan executionPlan)
	{
		super();
		this.executionPlan = executionPlan;
	}
	/** the competency execution plan that this event refers to */
	private CompetencyExecutionPlan executionPlan;

	/** returns the competency execution plan that this event refers to */
	public CompetencyExecutionPlan getCompetencyExecutionPlan()
	{
		return executionPlan;
	}
	
	/** displays information about this event */
	@Override
	public String toString()
	{
		// not sure how to display more concise short information about this
		String evtString =  "Competency Execution plan succeeded";
		return evtString;
	}

}
