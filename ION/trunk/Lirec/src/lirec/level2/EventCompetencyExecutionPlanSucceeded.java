package lirec.level2;

import ion.Meta.Event;

/** this type of event is raised by the competency execution system when a competency execution plan has been carried out successfully 
*  the competency manager listens for those events */
public class EventCompetencyExecutionPlanSucceeded extends Event 
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
}
