package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;

public interface IUtilityForTargetStrategy {
	
	public float getUtilityForTarget(String target, AgentModel am, ActivePursuitGoal g);

}
