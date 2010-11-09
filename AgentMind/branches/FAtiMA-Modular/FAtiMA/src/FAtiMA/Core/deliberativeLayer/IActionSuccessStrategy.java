package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.plan.Step;

public interface IActionSuccessStrategy {
	
	public void perceiveActionSuccess(AgentModel am, Step a);

}
