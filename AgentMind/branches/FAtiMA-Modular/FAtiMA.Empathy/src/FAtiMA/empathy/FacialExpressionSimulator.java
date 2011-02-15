package FAtiMA.empathy;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.OCCAffectDerivation.OCCEmotionType;

public class FacialExpressionSimulator {
	
	final private Name FACIAL_EXP_PROPERTY = Name.ParseName("SELF(facial-exp)");
	
	
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
	
	
	public String updateFacialExpression(AgentModel am){
		    
		//search for the previous facial expression in the KB
		String facialExpressionValue = (String) am.getMemory().getSemanticMemory().AskProperty(FACIAL_EXP_PROPERTY);
		
		FacialExpressionType previousFacialExpression = FacialExpressionType.valueOf(facialExpressionValue);
		
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
		
		String newFacialExpressionName = newFacialExpression.name();
		
		if(!newFacialExpressionName.equalsIgnoreCase(previousFacialExpression.name())){	
			am.getMemory().getSemanticMemory().Tell(FACIAL_EXP_PROPERTY, newFacialExpressionName);			
			return newFacialExpressionName;
		}else{
			return null;
		}	
	}
	
	public FacialExpressionType CalculateFacialExpression(ActiveEmotion e){	    
   
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

	public OCCEmotionType CalculateEmotionType(FacialExpressionType facialExpressionType) {
		switch(facialExpressionType){
			case ANGRY: return OCCEmotionType.ANGER;
			case HAPPY: return OCCEmotionType.JOY;
			case SAD: return OCCEmotionType.DISTRESS;
			case NEUTRAL : return null;
			default : return null;
		}
	}
}
