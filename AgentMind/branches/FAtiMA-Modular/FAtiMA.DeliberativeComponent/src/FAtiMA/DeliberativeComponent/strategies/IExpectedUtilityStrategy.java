package FAtiMA.DeliberativeComponent.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.DeliberativeComponent.Intention;

public interface IExpectedUtilityStrategy {
	
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g);
	
	public float getExpectedUtility(AgentModel am, Intention i);

}
