package FAtiMA.empathy;

import java.util.ArrayList;
import java.util.Iterator;

import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.util.enumerables.FacialExpressionType;

public abstract class EmpatheticReaction extends Thread{
	
	static final int REACTION_DELAY_MS = 600; // a  small delay to wait for the other agent reactions to the event
	
	Event _event;
	ArrayList _potentialEmotions;
	
	public EmpatheticReaction(Event e, ArrayList potentialEmphaticEmotions){
		if(e == null){
			_event = null;
		}else{
			_event = (Event)e.clone();	
		}
		_potentialEmotions = new ArrayList(potentialEmphaticEmotions);
	}	
	
	public void run(){
		
		
		this.pause(REACTION_DELAY_MS);
		
		float familiarity = getFamiliarityWithEmpathicTarget();
		
		if(existsSimilarityWithEmpathicTarget()){		
			Iterator it = _potentialEmotions.iterator();
			while(it.hasNext()){
				BaseEmotion e = (BaseEmotion) it.next();
				e.increasePotential(e.GetPotential() * ((familiarity/10)*2));
				System.out.println(familiarity);
			}	
			EmotionalState.GetInstance().AddPotentialEmpathicEmotions(_potentialEmotions);
		}else if(familiarity > 0){
			short emotionType = getTargetEmotionTypeFromFacialExpression();
			BaseEmotion potentialEmotion = new BaseEmotion(emotionType,familiarity,_event,null);
			_potentialEmotions.clear();
			_potentialEmotions.add(potentialEmotion);
			EmotionalState.GetInstance().AddPotentialEmpathicEmotions(_potentialEmotions);
		}
    }

	private short getTargetEmotionTypeFromFacialExpression(){
		String targetFacialExpression = getEmpathicTargetFacialExpression(); 
		short facialExpressionType = FacialExpressionType.ParseType(targetFacialExpression);
		short result = FacialExpressionSimulator.gI().CalculateEmotionType(facialExpressionType);
		
		return result;
	}

	private boolean existsSimilarityWithEmpathicTarget(){
		String targetFacialExpression = getEmpathicTargetFacialExpression(); 
		String simulatedFacialExpression =  getSimulatedFacialExpression();
		
		if(targetFacialExpression == null || simulatedFacialExpression == null){
			return false;
		}else{
			return targetFacialExpression.equals(simulatedFacialExpression);	
		}	
	}

	abstract protected String getEmpathicTargetFacialExpression();
	
	abstract protected float getFamiliarityWithEmpathicTarget();
		
	private String getSimulatedFacialExpression() {
		ActiveEmotion strongestSimulatedEmotion = EmotionalState.GetInstance().SimulateStrongestExpressiveEmotion(_potentialEmotions);
		return FacialExpressionSimulator.gI().CalculateFacialExpression(strongestSimulatedEmotion);
	}

	private void pause(int miliseconds){	
		 try {
			Thread.sleep(REACTION_DELAY_MS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
