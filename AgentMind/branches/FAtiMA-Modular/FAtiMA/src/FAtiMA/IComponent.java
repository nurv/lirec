package FAtiMA;

import FAtiMA.sensorEffector.Event;
import FAtiMA.wellFormedNames.Name;

public interface IComponent {
	
	public String name();
	
	public void initialize(AgentModel am);
	
	public void reset();
	
	public void shutdown();
	
	public void decay(long time);
	
	public void appraisal(Event e, AgentModel am);
	
	public void coping();
	
	public void propertyChangedPerception(String ToM, Name propertyName, String value);
	
	public void lookAtPerception(String subject, String target);
	
}
