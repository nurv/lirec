package FAtiMA.Core.plans;

import FAtiMA.Core.conditions.Condition;

public interface IDetectThreatStrategy {
	
	/**
	 * Checks if a given condition c1 threatens another condition c2 
	 * @param c1 - the condition that we want to check for threat
	 * @param c2 - a possible threatened condition
	 * @return true if condition c1 threatens condition c2 
	 */
	public boolean isThreat(Condition c1, Condition c2);
}
