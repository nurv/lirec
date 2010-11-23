package FAtiMA.empathy;

import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.util.enumerables.FacialExpressionType;
import FAtiMA.wellFormedNames.Name;

public class FacialExpressionSimulator {
	
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
	
	
	public String UpdateFacialExpression(Name facialExpressionProperty){
		    
		//search for the previous facial expression in the KB
		String previousFacialExpression = (String)KnowledgeBase.GetInstance().AskProperty(facialExpressionProperty);

        //checks the current strongest active emotion that is based on the reactive appraisal
		ActiveEmotion currentSrongestEmotion = EmotionalState.GetInstance().GetStrongestExpressiveEmotion();
	
		short newFacialExpression = -1;
		
	    //changes the facial expression accordingly
		if(currentSrongestEmotion == null){
			newFacialExpression = FacialExpressionType.NEUTRAL;
		}else{
			switch(currentSrongestEmotion.GetType()){
				
			    case EmotionType.ANGER : 
				case EmotionType.HATE : newFacialExpression = FacialExpressionType.ANGRY; 
										break;
				
				case EmotionType.JOY :
				case EmotionType.ADMIRATION :
				case EmotionType.GLOATING:				
				case EmotionType.LOVE : newFacialExpression = FacialExpressionType.HAPPY; 
				                        break;
				 
				case EmotionType.DISTRESS : newFacialExpression = FacialExpressionType.SAD;
											break;
				                    
				default : newFacialExpression = FacialExpressionType.NEUTRAL; break;
			}
		}
		
		String newFacialExpressionName = FacialExpressionType.GetName(newFacialExpression);
		
		if(!newFacialExpressionName.equalsIgnoreCase(previousFacialExpression)){	
			
			KnowledgeBase.GetInstance().Tell(facialExpressionProperty, newFacialExpressionName);			
			return newFacialExpressionName;
		}else{
			return null;
		}	
	}
	
	public String CalculateFacialExpression(ActiveEmotion e){	    
   
		if(e == null){
			return FacialExpressionType.GetName(FacialExpressionType.NEUTRAL);
		}else{
			switch(e.GetType()){				
			    case EmotionType.ANGER : 
				case EmotionType.HATE : return FacialExpressionType.GetName(FacialExpressionType.ANGRY); 		
				case EmotionType.JOY :
				case EmotionType.ADMIRATION :
				case EmotionType.GLOATING:				
				case EmotionType.LOVE :  return FacialExpressionType.GetName(FacialExpressionType.HAPPY);
				case EmotionType.DISTRESS : return FacialExpressionType.GetName(FacialExpressionType.SAD);
				default : return FacialExpressionType.GetName(FacialExpressionType.NEUTRAL);
			}
		}
	}

	public short CalculateEmotionType(short facialExpressionType) {
		switch(facialExpressionType){
			case FacialExpressionType.ANGRY: return EmotionType.ANGER;
			case FacialExpressionType.HAPPY: return EmotionType.JOY;
			case FacialExpressionType.SAD: return EmotionType.DISTRESS;
			case FacialExpressionType.NEUTRAL : return EmotionType.NEUTRAL;
			default : return EmotionType.NEUTRAL;
		}
	}
}
