package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public interface IProbabilityStrategy {
	
	public float getProbability(AgentModel am, ActivePursuitGoal g);
	
	public float getProbability(AgentModel am, Intention i);

}
