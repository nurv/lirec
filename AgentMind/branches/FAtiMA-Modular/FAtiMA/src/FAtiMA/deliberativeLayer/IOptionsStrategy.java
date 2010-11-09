package FAtiMA.deliberativeLayer;

import java.util.Collection;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;

public interface IOptionsStrategy {
	
	public Collection<? extends ActivePursuitGoal> options(AgentModel am);

}
