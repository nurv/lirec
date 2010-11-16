package FAtiMA.Core;

import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Name;

public interface IComponent {
	
	public String name();
	
	public void initialize(AgentModel am);
	
	public void reset();
	
	public void decay(long time);
	
	public void update(AgentModel am);
	
	public void update(Event e, AgentModel am);
	
	public IComponent createModelOfOther();
	
	public void appraisal(Event e, AppraisalStructure as, AgentModel am);
	
	public void emotionActivation(Event e, ActiveEmotion em, AgentModel am);
	
	public void coping(AgentModel am);
	
	public void propertyChangedPerception(String ToM, Name propertyName, String value);
	
	public void lookAtPerception(AgentCore ag, String subject, String target);
	
	public void entityRemovedPerception(String entity);
	
	public AgentDisplayPanel createComponentDisplayPanel(AgentModel am);
	
	public void processExternalRequest(String requestMsg);
	
}
