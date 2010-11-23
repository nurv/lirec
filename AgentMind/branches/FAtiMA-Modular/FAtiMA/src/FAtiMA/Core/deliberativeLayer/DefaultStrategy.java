package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public class DefaultStrategy implements IUtilityStrategy, IProbabilityStrategy, IGetUtilityForOthers
{	
	public float getUtility(AgentModel am, ActivePursuitGoal g)
	{
		return g.GetImportanceOfSuccess(am);
	}
	
	public float getProbability(AgentModel am, ActivePursuitGoal g)
	{
		return g.getProbability(am);
	}

	@Override
	public float getUtilityForOthers(AgentModel am, ActivePursuitGoal g) {
		return 0;
	}

}
