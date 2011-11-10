package FAtiMA.motivationalSystem;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;

public class NeedsLoaderHandler  extends ReflectXMLHandler {
	
	AgentModel _agent;
	String _currentGoalKey;
	String _currentStepKey;
		

	public NeedsLoaderHandler(AgentModel agent, MotivationalComponent motivationalState){
		this._agent = agent;
	}
	
	public void MotivationalParameter(Attributes attributes) {
		String motivatorName;
		String motivatorType;
		String updateType;
		boolean internalUpdate;
		Motivator motivator;

		motivatorName = attributes.getValue("motivator");
		motivatorType = attributes.getValue("type");
		updateType = attributes.getValue("update");
		
		if(updateType != null && updateType.equalsIgnoreCase("external"))
		{	
			internalUpdate = false;
		}
		else
		{
			internalUpdate = true;
		}
		
		if(motivatorType != null && motivatorType.equals("Linear"))
		{
			motivator = new LinearMotivator(motivatorName,
					new Float(attributes.getValue("decayFactor")).floatValue(),
					new Float(attributes.getValue("weight")).floatValue(),
					new Float(attributes.getValue("intensity")).floatValue(),
					internalUpdate);
		}
		else
		{
			motivator = new Motivator(motivatorName,
					new Float(attributes.getValue("decayFactor")).floatValue(),
					new Float(attributes.getValue("weight")).floatValue(),
					new Float(attributes.getValue("intensity")).floatValue(),
					internalUpdate);
		}
		
		MotivationalComponent ms = (MotivationalComponent)_agent.getComponent(MotivationalComponent.NAME);
		
		ms.AddMotivator(motivator);
		
		AgentLogger.GetInstance().logAndPrint("Motivator found: " + motivatorName);
	}
	
	public void Goal(Attributes attributes) {
    	_currentGoalKey = attributes.getValue("name");
    }
	
	public void ActivePursuitGoal(Attributes attributes) {
    	_currentGoalKey = attributes.getValue("name");
    }
	public void InterestGoal(Attributes attributes)
	{
		_currentGoalKey = attributes.getValue("name");
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

		if(driveName != null && _currentStepKey != null){
			motivComp.addActionEffectsOnDrive(_currentStepKey, driveName, new Symbol(target), new Symbol(value));
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
		Symbol t = new Symbol(target);
		Substitution self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF));
		t.MakeGround(self);

		if(driveName != null && _currentGoalKey != null){
			ms.addExpectedGoalEffectOnDrive(_currentGoalKey, effectType, driveName, t, new Symbol(value));
		}
	}
}
