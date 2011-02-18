package FAtiMA.empathy;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.EmotionalStatePanel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;

public class EmpathicEmotionsPanel extends EmotionalStatePanel{
	private static final long serialVersionUID = 1L;

	@Override
	protected EmotionalState getEmotionalState(AgentModel am){
	  	EmotionalState es = am.getEmotionalState();
	 
	  	EmotionalState empathicEmotionalState = es.clone();
		
	  	for(ActiveEmotion aE : empathicEmotionalState.getEmotionPool()){
	  		if(!(aE.getBaseEmotionClass() == EmpathicEmotion.class)){
	  			empathicEmotionalState.RemoveEmotion(aE);
	  		}
	  	}
	  	
		return empathicEmotionalState;
	}
	
	
}
