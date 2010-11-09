package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;

public interface IGoalSuccessStrategy {
	
	public void perceiveGoalSuccess(AgentModel am, ActivePursuitGoal g); 

}
