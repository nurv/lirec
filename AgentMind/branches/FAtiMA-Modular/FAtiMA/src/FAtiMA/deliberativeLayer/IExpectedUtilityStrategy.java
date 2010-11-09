package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;

public interface IExpectedUtilityStrategy {
	
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g);

}
