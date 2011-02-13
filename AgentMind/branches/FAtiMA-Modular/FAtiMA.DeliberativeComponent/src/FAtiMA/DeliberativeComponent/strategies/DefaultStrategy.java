package FAtiMA.DeliberativeComponent.strategies;

import java.io.Serializable;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.DeliberativeComponent.IProbabilityStrategy;
import FAtiMA.DeliberativeComponent.IUtilityStrategy;
import FAtiMA.DeliberativeComponent.Intention;

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
