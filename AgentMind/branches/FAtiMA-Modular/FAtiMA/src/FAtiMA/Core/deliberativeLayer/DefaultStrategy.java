package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.Core.util.Constants;

public class DefaultStrategy implements IUtilityForTargetStrategy, IProbabilityStrategy 
{	
	public float getUtilityForTarget(String target, AgentModel am, ActivePursuitGoal g)
	{
		if(target.equals(Constants.SELF))
			return g.GetImportanceOfSuccess(am);
		
		else return 0;
	}
	
	public float getProbability(AgentModel am, ActivePursuitGoal g)
	{
		return g.getProbability(am);
	}

}
