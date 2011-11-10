package FAtiMA.DeliberativeComponent2;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;

public interface IUtilityStrategy {
	
	public float getUtility(AgentModel am, ActivePursuitGoal g);

}
