package FAtiMA.Core.plans;

import java.io.Serializable;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.conditions.PropertyNotEqual;

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
		
		boolean different;
		
		if(c1.getName().equals(c2.getName())) 
		{
			different = !c1.GetValue().equals(c2.GetValue());
			if(c2 instanceof PropertyNotEqual)
			{
				return !different;
			}
			else return different;
	    }
		return false;
	}
}
