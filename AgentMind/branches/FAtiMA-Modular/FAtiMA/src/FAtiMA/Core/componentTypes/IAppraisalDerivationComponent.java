package FAtiMA.Core.componentTypes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.sensorEffector.Event;

public interface IAppraisalDerivationComponent extends IComponent {
	
	public void appraisal(AgentModel am, Event e, AppraisalFrame af);
	
	public void inverseAppraisal(AgentModel am, AppraisalFrame af);
	
	public AppraisalFrame reappraisal(AgentModel am);

}
