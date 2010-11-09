package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public interface IGoalFailureStrategy {
	
	public void perceiveGoalFailure(AgentModel am, ActivePursuitGoal g);

}
