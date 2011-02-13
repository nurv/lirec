package FAtiMA.DeliberativeComponent;

import java.io.Serializable;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;

public class DefaultStrategy implements Serializable, IUtilityStrategy, IProbabilityStrategy, IGetUtilityForOthers
{	
	/**
	 * s
	 */
	private static final long serialVersionUID = 1L;

	public float getUtility(AgentModel am, ActivePursuitGoal g)
	{
		return g.GetImportanceOfSuccess(am);
	}
	
	public float getProbability(AgentModel am, ActivePursuitGoal g)
	{
		return g.getProbability(am);
	}
	
	public float getProbability(AgentModel am, Intention i)
	{
		return i.GetProbability(am);
	}

	@Override
	public float getUtilityForOthers(AgentModel am, ActivePursuitGoal g) {
		return 0;
	}

}
