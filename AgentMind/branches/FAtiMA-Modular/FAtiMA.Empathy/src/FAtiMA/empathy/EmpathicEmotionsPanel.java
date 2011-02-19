package FAtiMA.empathy;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.EmotionalStatePanel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;

public class EmpathicEmotionsPanel extends EmotionalStatePanel{
	private static final long serialVersionUID = 1L;

	@Override
	protected synchronized EmotionalState getEmotionalState(AgentModel am){
	  	EmotionalState es = am.getEmotionalState();
	  	
	  	ArrayList<ActiveEmotion> nonEmpathicEmotions = new ArrayList<ActiveEmotion>(); 
	  
	  	EmotionalState empathicEmotionalState = es.clone();
	  	
	  	for(ActiveEmotion aE : es.getEmotionPoolValues()){
	  		if(!(aE.getBaseEmotionClass() == EmpathicEmotion.class)){
	  			nonEmpathicEmotions.add(aE);
	  		}
	  	}
	  	
	  	for(ActiveEmotion aE : nonEmpathicEmotions){
	  		empathicEmotionalState.RemoveEmotion(aE);	
	  	}
	  	
		return empathicEmotionalState;
	}
	
	
}
