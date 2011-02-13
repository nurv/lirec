package FAtiMA.DeliberativeComponent;

import java.util.Collection;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;

public interface IOptionsStrategy {
	
	public Collection<? extends ActivePursuitGoal> options(AgentModel am);

}
