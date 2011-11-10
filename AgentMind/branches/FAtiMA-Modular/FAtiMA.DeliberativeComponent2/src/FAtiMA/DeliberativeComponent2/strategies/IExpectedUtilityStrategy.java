package FAtiMA.DeliberativeComponent2.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.DeliberativeComponent2.Intention;

public interface IExpectedUtilityStrategy {
	
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g);
	
	public float getExpectedUtility(AgentModel am, Intention i);

}
