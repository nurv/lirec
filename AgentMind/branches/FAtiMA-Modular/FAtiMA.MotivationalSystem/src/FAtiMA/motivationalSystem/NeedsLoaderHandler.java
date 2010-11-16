package FAtiMA.motivationalSystem;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Symbol;

public class NeedsLoaderHandler  extends ReflectXMLHandler {
	
	AgentCore _agent;
	String _currentGoalKey;
	String _currentStepKey;
		

	public NeedsLoaderHandler(AgentCore agent, MotivationalComponent motivationalState){
		this._agent = agent;
	}
	
	public void MotivationalParameter(Attributes attributes) throws InvalidMotivatorTypeException {
		String motivatorName;
		short type;

		motivatorName = attributes.getValue("motivator");
		type = MotivatorType.ParseType(motivatorName);
		MotivationalComponent ms = (MotivationalComponent)_agent.getComponent(MotivationalComponent.NAME);
		
		ms.AddMotivator(new Motivator(type,
				new Float(attributes.getValue("decayFactor")).floatValue(),
				new Float(attributes.getValue("weight")).floatValue(),
				new Float(attributes.getValue("intensity")).floatValue()));
		
		
		AgentLogger.GetInstance().logAndPrint("Motivator found: " + type);
	}
	
	public void Goal(Attributes attributes) {
    	_currentGoalKey = attributes.getValue("key");
    }
	
	public void Action(Attributes attributes){
	 	_currentStepKey = attributes.getValue("name");	
	}
	
	public void Motivator(Attributes attributes)
	{
		MotivationalComponent motivComp = (MotivationalComponent)_agent.getComponent(MotivationalComponent.NAME);
		
		String driveName = attributes.getValue("drive");
		String value = attributes.getValue("value");
		String target = attributes.getValue("target");

		if(driveName != null && _currentGoalKey != null){
			motivComp.addActionEffectsOnDrive(_currentStepKey, driveName, new Symbol(target), Float.parseFloat(value));
		}
	}
	
	public void OnSelect(Attributes attributes)
	{
		this.setGoalExpectedEffectOnDrive(attributes, EffectType.ON_SELECT);
	}

	public void OnIgnore(Attributes attributes)
	{
		this.setGoalExpectedEffectOnDrive(attributes, EffectType.ON_IGNORE);
	}
	
	private void setGoalExpectedEffectOnDrive(Attributes attributes, short effectType){
		MotivationalComponent ms = (MotivationalComponent)_agent.getComponent(MotivationalComponent.NAME);
		
		String driveName = attributes.getValue("drive");
		String value = attributes.getValue("value");
		String target = attributes.getValue("target");

		if(driveName != null && _currentGoalKey != null){
			ms.addExpectedGoalEffectOnDrive(_currentGoalKey, effectType, driveName, new Symbol(target), Float.parseFloat(value));
		}
	}
}
