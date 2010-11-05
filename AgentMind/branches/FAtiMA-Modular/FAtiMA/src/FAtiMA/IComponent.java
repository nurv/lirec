package FAtiMA;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.Display.AgentDisplayPanel;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.AppraisalStructure;
import FAtiMA.sensorEffector.Event;
import FAtiMA.wellFormedNames.Name;

public interface IComponent {
	
	public String name();
	
	public void initialize(AgentCore ag);
	
	public void reset();
	
	public void decay(long time);
	
	public void update(AgentModel am);
	
	public IComponent createModelOfOther();
	
	public AppraisalStructure appraisal(Event e, AgentModel am);
	
	public AppraisalStructure composedAppraisal(Event e, AppraisalStructure v, AgentModel am);
	
	public void emotionActivation(Event e, ActiveEmotion em, AgentModel am);
	
	public void coping(AgentModel am);
	
	public void propertyChangedPerception(String ToM, Name propertyName, String value);
	
	public void lookAtPerception(AgentCore ag, String subject, String target);
	
	public void entityRemovedPerception(String entity);
	
	public AgentDisplayPanel createComponentDisplayPanel(AgentModel am);
	
}
