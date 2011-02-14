package FAtiMA.Core.componentTypes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.sensorEffector.Event;

public interface IComponent {
	
	public String name();
	
	public void initialize(AgentModel am);
	
	public void reset();
	
	public void update(AgentModel am, long time);
	
	public void update(AgentModel am, Event e);
	
	public AgentDisplayPanel createDisplayPanel(AgentModel am);
	
	public String[] getComponentDependencies();
	
}
