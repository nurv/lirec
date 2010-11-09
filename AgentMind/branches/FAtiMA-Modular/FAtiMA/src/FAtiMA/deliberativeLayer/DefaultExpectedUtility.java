package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;

public class DefaultExpectedUtility implements IExpectedUtilityStrategy 
{
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g)
	{
		return g.GetImportanceOfSuccess(am) * g.getProbability(am);
	}

}
