package FAtiMA.empathy;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.OCCAffectDerivation.OCCBaseEmotion;
import FAtiMA.OCCAffectDerivation.OCCEmotionType;

public class FacialExpressionSimulator {
	
	final private String FACIAL_EXP_PROPERTY = "(facial-exp)";
	final private float DEFAULT_EMOTION_POTENTIAL = 5;
	
	
	/**
	 * Singleton pattern 
	 */
	private static FacialExpressionSimulator _facialSim;
	
	public static FacialExpressionSimulator gI()
	{
		if(_facialSim == null)
		{
			_facialSim = new FacialExpressionSimulator();
		
		
			
		}
		
		return _facialSim;
	} 
	
	
	public FacialExpressionType updateFacialExpression(AgentModel am){
		    
		//search for the previous facial expression in the KB
		String previousFacialExpression = (String) am.getMemory().getSemanticMemory().AskProperty(Name.ParseName(Constants.SELF + FACIAL_EXP_PROPERTY));
		
		if(previousFacialExpression == null){
			//Set the initial FacialExpression
			am.getMemory().getSemanticMemory().Tell(Name.ParseName(Constants.SELF + FACIAL_EXP_PROPERTY), FacialExpressionType.NEUTRAL.name());
			previousFacialExpression = FacialExpressionType.NEUTRAL.name();
		}
		
        //checks the current strongest active emotion that is based on the reactive appraisal
		ActiveEmotion currentSrongestEmotion = am.getEmotionalState().GetStrongestEmotion();
		
		FacialExpressionType newFacialExpression;
		
	    //changes the facial expression accordingly
		if(currentSrongestEmotion == null){
			newFacialExpression = FacialExpressionType.NEUTRAL;
		}else{
			switch(OCCEmotionType.valueOf(currentSrongestEmotion.getType())){
				
			    case ANGER : 
				case HATE : newFacialExpression = FacialExpressionType.ANGRY; 
										break;
				case JOY :
				case ADMIRATION :
				case GLOATING:				
				case LOVE : newFacialExpression = FacialExpressionType.HAPPY; 
				        break;
				case DISTRESS : newFacialExpression = FacialExpressionType.SAD;
					break;
				                    
				default : newFacialExpression = FacialExpressionType.NEUTRAL; break;
			}
		}
		
		if(newFacialExpression != FacialExpressionType.valueOf(previousFacialExpression)){	
			am.getMemory().getSemanticMemory().Tell(Name.ParseName(Constants.SELF + FACIAL_EXP_PROPERTY), newFacialExpression.name());			
			return newFacialExpression;
		}else{
			return null;
		}	
	}
	
	public FacialExpressionType CalculateFacialExpression(BaseEmotion e){	    
   
		if(e == null){
			return FacialExpressionType.NEUTRAL;
		}else{
			switch(OCCEmotionType.valueOf(e.getType())){				
			    case ANGER : 
				case HATE :return FacialExpressionType.ANGRY; 		
				case JOY :
				case ADMIRATION :
				case GLOATING:				
				case LOVE : return FacialExpressionType.HAPPY;
				case DISTRESS : return FacialExpressionType.SAD;
				default : return FacialExpressionType.NEUTRAL;
			}
		}
	}

	public OCCBaseEmotion recognizeEmotion(FacialExpressionType facialExpressionType, Event e) {
		switch(facialExpressionType){
			case ANGRY: return new OCCBaseEmotion(OCCEmotionType.ANGER, DEFAULT_EMOTION_POTENTIAL, e);
			case HAPPY: return new OCCBaseEmotion(OCCEmotionType.JOY, DEFAULT_EMOTION_POTENTIAL, e);
			case SAD: return new OCCBaseEmotion(OCCEmotionType.DISTRESS,DEFAULT_EMOTION_POTENTIAL, e);
			case NEUTRAL : return null;
			default : return null;
		}
	}
	
	public FacialExpressionType determineTargetFacialExpression(String target, AgentModel am){
		//search for the previous facial expression in the KB
		String targetFacialExpression = (String)am.getMemory().getSemanticMemory().AskProperty(Name.ParseName(target + FACIAL_EXP_PROPERTY));
		
		if(targetFacialExpression == null){
			return null;
		}else{
			return FacialExpressionType.valueOf(targetFacialExpression);
		}
		
	}
}
