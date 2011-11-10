package FAtiMA.Core.componentTypes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.parsers.ReflectXMLHandler2;

public interface IComponent {
	
	public String name();
	
	public void initialize(AgentModel am);
	
	public void reset();
	
	public void update(AgentModel am, long time);
	
	public void update(AgentModel am, Event e);
	
	public AgentDisplayPanel createDisplayPanel(AgentModel am);
	
	public ReflectXMLHandler2 getActionsParser(AgentModel am);
	
	public ReflectXMLHandler2 getGoalsParser(AgentModel am);
	
	public ReflectXMLHandler2 getPersonalityParser(AgentModel am);
	
	public void parseAdditionalFiles(AgentModel am);
	
	public String[] getComponentDependencies();
	
}
