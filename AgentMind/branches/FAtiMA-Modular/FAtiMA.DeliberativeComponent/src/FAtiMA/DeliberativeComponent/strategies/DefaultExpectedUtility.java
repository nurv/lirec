package FAtiMA.DeliberativeComponent.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.DeliberativeComponent.Intention;

public class DefaultExpectedUtility implements IExpectedUtilityStrategy 
{
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g)
	{
		return g.GetImportanceOfSuccess(am) * g.getProbability(am);
	}
	
	public float getExpectedUtility(AgentModel am, Intention i)
	{
		return i.getGoal().GetImportanceOfSuccess(am) * i.GetProbability(am);
	}

}
