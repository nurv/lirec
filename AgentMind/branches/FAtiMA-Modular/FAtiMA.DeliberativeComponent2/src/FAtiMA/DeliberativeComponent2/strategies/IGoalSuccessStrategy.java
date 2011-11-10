package FAtiMA.DeliberativeComponent2.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;

public interface IGoalSuccessStrategy {
	
	public void perceiveGoalSuccess(AgentModel am, ActivePursuitGoal g); 

}
