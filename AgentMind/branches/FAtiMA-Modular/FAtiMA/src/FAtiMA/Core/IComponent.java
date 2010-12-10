package FAtiMA.Core;

import FAtiMA.Core.Display.AgentDisplayPanel;

public interface IComponent {
	
	public String name();
	
	public void initialize(AgentModel am);
	
	public void reset();
	
	public void updateCycle(AgentModel am, long time);
	
	public AgentDisplayPanel createDisplayPanel(AgentModel am);
	
}
