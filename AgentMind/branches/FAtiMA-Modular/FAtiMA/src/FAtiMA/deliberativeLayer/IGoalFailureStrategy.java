package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;

public interface IGoalFailureStrategy {
	
	public void perceiveGoalFailure(AgentModel am, ActivePursuitGoal g);

}
