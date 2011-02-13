package FAtiMA.DeliberativeComponent;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;

public interface IGoalSuccessStrategy {
	
	public void perceiveGoalSuccess(AgentModel am, ActivePursuitGoal g); 

}
