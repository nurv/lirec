package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.util.Constants;

public class DefaultStrategy implements IUtilityStrategy, IProbabilityStrategy 
{	
	public float getUtility(AgentModel am, ActivePursuitGoal g)
	{
		return g.GetImportanceOfSuccess(am);
	}
	
	public float getProbability(AgentModel am, ActivePursuitGoal g)
	{
		return g.getProbability(am);
	}

}
