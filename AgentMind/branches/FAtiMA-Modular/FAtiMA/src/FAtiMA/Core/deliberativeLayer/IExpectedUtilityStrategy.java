package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public interface IExpectedUtilityStrategy {
	
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g);
	
	public float getExpectedUtility(AgentModel am, Intention i);

}
