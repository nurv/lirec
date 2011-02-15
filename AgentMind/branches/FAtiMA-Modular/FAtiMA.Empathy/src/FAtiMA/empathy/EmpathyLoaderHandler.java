package FAtiMA.empathy;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.EmotionCondition;
import FAtiMA.Core.conditions.MoodCondition;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.conditions.PredicateCondition;
import FAtiMA.Core.conditions.PropertyCondition;
import FAtiMA.Core.emotionalState.ElicitingEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.parsers.ReactiveLoaderHandler;

public class EmpathyLoaderHandler extends ReactiveLoaderHandler{

	private EmpathyComponent _empathyComponent;


	public EmpathyLoaderHandler(EmpathyComponent eC) {
		this._empathyComponent = eC;
	}

	public void EmpatheticAction(Attributes attributes){
		_action = new Action(Name.ParseName(attributes.getValue("action")));  	
	}

	@Override
	public void ElicitingEmotion(Attributes attributes) throws InvalidEmotionTypeException {
	   	parseElicitingEmotion(attributes.getValue("type"),new Integer(attributes.getValue("minIntensity")));
    	_empathyComponent.getEmpatheticActions().AddAction(_action);
	}
	
	@Override
	public void EmotionalReaction(Attributes attributes){
		//This is here to prevent from parsing twice the emotional reaction rules 
	}
	
}
