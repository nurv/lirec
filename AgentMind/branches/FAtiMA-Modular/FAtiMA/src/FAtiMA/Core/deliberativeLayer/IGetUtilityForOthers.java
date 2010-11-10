package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public interface IGetUtilityForOthers {
	
	public float getUtilityForOthers(AgentModel am, ActivePursuitGoal g);

}
