package FAtiMA.Core.componentTypes;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Name;

public interface IAdvancedPerceptionsComponent extends IComponent{
	
	public void propertyChangedPerception(AgentModel am, String ToM, Name propertyName, String value);
	
	public void lookAtPerception(AgentCore ag, String subject, String target);
	
	public void entityRemovedPerception(AgentModel am, String entity);
	
	public void actionFailedPerception(AgentModel am, Event e);

}
