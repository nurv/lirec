package FAtiMA.Core.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;

public interface IUtilityStrategy {
	
	public float getUtility(AgentModel am, ActivePursuitGoal g);

}
