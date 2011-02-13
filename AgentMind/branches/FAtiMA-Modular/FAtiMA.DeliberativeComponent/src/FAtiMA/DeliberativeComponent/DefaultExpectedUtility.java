package FAtiMA.DeliberativeComponent;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;

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
