package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.util.Constants;

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
