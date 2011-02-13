package FAtiMA.DeliberativeComponent.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;

public interface IGoalFailureStrategy {
	
	public void perceiveGoalFailure(AgentModel am, ActivePursuitGoal g);

}
