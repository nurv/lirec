package FAtiMA.Core.deliberativeLayer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.plan.Step;

public interface IActionFailureStrategy {
	
	public void perceiveActionFailure(AgentModel am, Step a);

}
