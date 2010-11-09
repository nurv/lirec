package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.plan.Step;

public interface IActionSuccessStrategy {
	
	public void perceiveActionSuccess(AgentModel am, Step a);

}
