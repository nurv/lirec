package FAtiMA;

import FAtiMA.sensorEffector.Event;

public interface IComponent {
	
	public String name();
	
	public void initialize();
	
	public void reset();
	
	public void shutdown();
	
	public void decay(long time);
	
	public void update(Event e);
	
	public void appraisal(Event e, AgentModel am);
	
	public void coping();
	
}
