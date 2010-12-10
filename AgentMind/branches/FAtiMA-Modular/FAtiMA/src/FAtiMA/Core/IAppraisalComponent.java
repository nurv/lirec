package FAtiMA.Core;

import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.sensorEffector.Event;

public interface IAppraisalComponent extends IComponent {
	
public void perceiveEvent(AgentModel am, Event e);
	
	public void startAppraisal(AgentModel am, Event e, AppraisalFrame as);
	
	public void continueAppraisal(AgentModel am);

}
