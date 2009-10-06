package lirec.level2;

import ion.Meta.Request;


/** this type of event is raised by the competency manager when it selects a new competency execution plan 
*  the competency execution system listens for those events to carry out their execution */
public class RequestNewCompetencyExecutionPlan extends Request 
{

	/** creates a new event */
	public RequestNewCompetencyExecutionPlan(CompetencyExecutionPlan executionPlan)
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
}

