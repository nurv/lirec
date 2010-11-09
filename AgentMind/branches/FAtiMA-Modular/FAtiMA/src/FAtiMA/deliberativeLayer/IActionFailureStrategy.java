package FAtiMA.deliberativeLayer;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.plan.Step;

public interface IActionFailureStrategy {
	
	public void perceiveActionFailure(AgentModel am, Step a);

}
