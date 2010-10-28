package FAtiMA;

import FAtiMA.sensorEffector.Event;
import FAtiMA.wellFormedNames.Name;

public interface IComponent {
	
	public String name();
	
	public void Initialize(AgentModel am);
	
	public void reset();
	
	public void shutdown();
	
	public void decay(long time);
	
	public void appraisal(Event e, AgentModel am);
	
	public void coping();
	
	public void PropertyChangedPerception(String ToM, Name propertyName, String value);
	
	public void LookAtPerception(String subject, String target);
	
}
