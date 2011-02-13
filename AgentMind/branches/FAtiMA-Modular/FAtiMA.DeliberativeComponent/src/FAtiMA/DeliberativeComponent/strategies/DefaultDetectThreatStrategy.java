package FAtiMA.DeliberativeComponent.strategies;

import java.io.Serializable;

import FAtiMA.Core.conditions.Condition;

public class DefaultDetectThreatStrategy implements Serializable, IDetectThreatStrategy {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultDetectThreatStrategy()
	{
	}

	@Override
	public boolean isThreat(Condition c1, Condition c2) {
		if(c1.getName().equals(c2.getName())) 
		{
			return !c1.GetValue().equals(c2.GetValue());
	    }
		return false;
	}
}
