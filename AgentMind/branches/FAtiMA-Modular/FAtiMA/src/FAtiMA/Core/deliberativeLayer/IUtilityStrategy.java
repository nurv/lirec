package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public interface IUtilityStrategy {
	
	public float getUtility(AgentModel am, ActivePursuitGoal g);

}
