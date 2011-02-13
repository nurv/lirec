package FAtiMA.ToM;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.plans.DefaultDetectThreatStrategy;
import FAtiMA.Core.util.Constants;

public class DetectThreatStrategy extends DefaultDetectThreatStrategy {
	
	public DetectThreatStrategy()
	{
	}
	
	@Override
	public boolean isThreat(Condition c1, Condition c2) {
		if(c1.getToM().equals(Constants.UNIVERSAL) || c1.getToM().equals(Constants.UNIVERSAL) || c1.getToM().equals(c2.getToM()))
		{
			return super.isThreat(c1, c2);
		}
		else return false;
	}
}
