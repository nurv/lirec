package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;

public interface IProbabilityStrategy {
	
	public float getProbability(AgentModel am, ActivePursuitGoal g);

}
