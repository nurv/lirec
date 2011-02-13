package FAtiMA.DeliberativeComponent.strategies;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.plans.Step;

public interface IActionSuccessStrategy {
	
	public void perceiveActionSuccess(AgentModel am, Step a);

}
