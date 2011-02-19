package FAtiMA.empathy;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.ActionTendencies;

public class EmpathicActions extends ActionTendencies {

	private static final long serialVersionUID = 1L;
	
	@Override
	public ValuedAction SelectAction(AgentModel am) {
		ValuedAction va;
		ValuedAction bestAction = null;
		EmotionalState emState = am.getEmotionalState();
		ArrayList<ActiveEmotion> empathicEmotions = new ArrayList<ActiveEmotion>();
		
		for(ActiveEmotion activeEmotion : emState.getEmotionPoolValues()){
			if(activeEmotion.getBaseEmotionClass() instanceof EmpathicEmotion){
				empathicEmotions.add(activeEmotion);
			}
		}
		
		if(empathicEmotions.size() == 0){
			return null;
		}
				
		for(Action a : _actions){
			va = a.TriggerAction(am, empathicEmotions.listIterator());
			if (va != null && !isIgnored(va))
				if(bestAction == null || va.getValue(emState) > bestAction.getValue(emState)) 
				{
				    bestAction = va;
				}
		}
		
		return bestAction;
	}
}
