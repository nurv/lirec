package FAtiMA.DeliberativeComponent.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.plans.Step;

public interface IActionFailureStrategy {
	
	public void perceiveActionFailure(AgentModel am, Step a);

}
