package FAtiMA.DeliberativeComponent.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;

public interface IGetUtilityForOthers {
	
	public float getUtilityForOthers(AgentModel am, ActivePursuitGoal g);

}
