package FAtiMA.emotionalIntelligence;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;

public class EmotionalConditionsLoaderHandler extends ReflectXMLHandler {
	
	private AgentModel _aM;
	private String _currentGoalKey;
	private String _conditionType;
	private Substitution _self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF));
	
	public EmotionalConditionsLoaderHandler(AgentModel aM){
		_aM = aM;
	}
	
	public void ActivePursuitGoal(Attributes attributes) {
    	_currentGoalKey = attributes.getValue("name");
    }
	 
	public void EmotionalEpisodeCondition(Attributes attributes)
	{
		EmotionalEpisodeCondition ee;
		Goal g;
		 
		try
		{
			ee = EmotionalEpisodeCondition.ParseEmotionalEpisodeCondition(attributes);
			ee.MakeGround(_self);
			g = _aM.getGoalLibrary().GetGoal(Name.ParseName(_currentGoalKey));
			g.AddCondition(_conditionType, ee);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	 
	public void FailureConditions(Attributes attributes) {
	   _conditionType = "FailureConditions";
	}
	 
	public void PreConditions(Attributes attributes) {
		_conditionType = "PreConditions";
	}
	 
	public void SuccessConditions(Attributes attributes) {
	   _conditionType = "SuccessConditions";
	}

}
