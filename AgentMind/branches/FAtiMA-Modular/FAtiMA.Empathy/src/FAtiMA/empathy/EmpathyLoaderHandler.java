package FAtiMA.empathy;

import org.xml.sax.Attributes;

import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.parsers.ReactiveLoaderHandler;

public class EmpathyLoaderHandler extends ReactiveLoaderHandler{

	private EmpathyComponent _empathyComponent;


	public EmpathyLoaderHandler(EmpathyComponent eC) {
		this._empathyComponent = eC;
	}

	public void EmpathicAction(Attributes attributes){
		_action = new Action(Name.ParseName(attributes.getValue("action")));  	
	}

	@Override
	public void ElicitingEmotion(Attributes attributes) throws InvalidEmotionTypeException {
	   	parseElicitingEmotion(attributes.getValue("type"),new Integer(attributes.getValue("minIntensity")));
    	_empathyComponent.getEmpathicActions().AddAction(_action);
	}
	
	@Override
	public void EmotionalReaction(Attributes attributes){
		//This is here to prevent from parsing twice the emotional reaction rules 
	}
	
	@Override
	public void Event(Attributes attributes){
		
	}
	
}
