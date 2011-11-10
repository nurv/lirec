package FAtiMA.DeliberativeComponent2.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.DeliberativeComponent2.Intention;

public class DefaultExpectedUtility implements IExpectedUtilityStrategy 
{
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g)
	{
		return g.GetImportanceOfSuccess(am);// * g.getProbability(am);
	}
	
	public float getExpectedUtility(AgentModel am, Intention i)
	{
		return i.getGoal().GetImportanceOfSuccess(am);// * i.GetProbability(am);
	}

}
