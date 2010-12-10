package FAtiMA.emotionalIntelligence;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;

public class EmotionalIntelligence implements IComponent {
	
	public static final String NAME = "EmotionalIntelligence";

	@Override
	public String name() {
		return EmotionalIntelligence.NAME; 
	}

	@Override
	public void initialize(AgentModel am) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reset() {
	}

	@Override
	public void updateCycle(AgentModel am, long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return null;
	}
}
