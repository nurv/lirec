package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public interface IGoalSuccessStrategy {
	
	public void perceiveGoalSuccess(AgentModel am, ActivePursuitGoal g); 

}
