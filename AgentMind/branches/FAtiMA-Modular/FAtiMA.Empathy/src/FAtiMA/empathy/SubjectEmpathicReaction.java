package FAtiMA.empathy;

import java.util.ArrayList;

import FAtiMA.Core.memory.episodicMemory.AutobiographicalMemory;
import FAtiMA.Core.memory.semanticMemory.KnowledgeBase;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Name;

public class SubjectEmpathicReaction extends EmpatheticReaction{

	public SubjectEmpathicReaction(Event e, ArrayList potentialEmphaticEmotions){
		super(e,potentialEmphaticEmotions);	
	}
	
	public String getEmpathicTargetFacialExpression(){
		Name facialExpProperty = Name.ParseName(_event.GetSubject() + "(facial-exp)");
		String facialExpression = (String)KnowledgeBase.GetInstance().AskProperty(facialExpProperty);
		//System.out.println("Event-> " + _event + " Target-> " + _event.GetTarget() + " Facial Exp-> " + expression);		
		return facialExpression;
	}

	
	private float obtainLikeRelationshipFromKB(String targetAgent){
		String agentName = AutobiographicalMemory.GetInstance().getSelf();
		Name likeProperty = Name.ParseName("Like("+ agentName + "," + targetAgent +")");
		Float likeValue = (Float) KnowledgeBase.GetInstance().AskProperty(likeProperty);

		if(likeValue == null){	
			return 0f;
		}else{
			return likeValue.floatValue();				
		}	
	}
	
	protected float getFamiliarityWithEmpathicTarget() {
		String selfName = AutobiographicalMemory.GetInstance().getSelf();
		String targetName = _event.GetSubject();
		
		Name likeProperty = Name.ParseName("Like("+ selfName + "," + targetName +")");
		Float likeValue = (Float) KnowledgeBase.GetInstance().AskProperty(likeProperty);

		if(likeValue == null){	
			return 0f;
		}else{
			return likeValue.floatValue();				
		}	
	}
}
