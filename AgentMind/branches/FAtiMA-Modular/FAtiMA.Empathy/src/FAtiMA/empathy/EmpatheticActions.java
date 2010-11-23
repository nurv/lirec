package FAtiMA.empathy;

import java.util.Iterator;

import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.reactiveLayer.Action;
import FAtiMA.Core.reactiveLayer.ActionTendencies;

public class EmpatheticActions extends ActionTendencies{
	
	
	//just to prevent mistakes
	public ValuedAction SelectAction(EmotionalState emState){
		return SelectAction();
	}
	
	
	public ValuedAction SelectAction() {
		Iterator it;
		Action a;
		ValuedAction va;
		ValuedAction bestAction = null;
		
		it = _actions.iterator();
		while(it.hasNext()) {
		
			a = (Action) it.next();
			va = a.TriggerAction(EmotionalState.GetInstance().GetEmpatheticEmotionsIterator());
			
			
			if (va != null && !isIgnored(va)) {
				if(bestAction == null || va.GetValue() > bestAction.GetValue()) 
				{
				    bestAction = va;
				}
			}	
		}
		
		return bestAction;
	}
}
