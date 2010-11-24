package FAtiMA.Core;

import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.sensorEffector.Event;

public interface IComponent {
	
	public String name();
	
	public void initialize(AgentModel am);
	
	public void reset();
	
	public void updateCycle(AgentModel am, long time);
	
	public void perceiveEvent(AgentModel am, Event e);
	
	public void appraisal(AgentModel am, Event e, AppraisalStructure as);
	
	public AgentDisplayPanel createDisplayPanel(AgentModel am);
	
}
