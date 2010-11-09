package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public interface IUtilityForTargetStrategy {
	
	public float getUtilityForTarget(String target, AgentModel am, ActivePursuitGoal g);

}
