package FAtiMA.Core.plans;

import java.io.Serializable;

import FAtiMA.Core.conditions.Condition;

public class DefaultDetectThreatStrategy implements Serializable, IDetectThreatStrategy {
	
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
