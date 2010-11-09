package FAtiMA.Core.deliberativeLayer;

import java.util.Collection;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;

public interface IOptionsStrategy {
	
	public Collection<? extends ActivePursuitGoal> options(AgentModel am);

}
