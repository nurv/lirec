package FAtiMA.empathy;

import java.util.ArrayList;

import FAtiMA.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.sensorEffector.Event;
import FAtiMA.wellFormedNames.Name;

public class TargetEmpathicReaction extends EmpatheticReaction{

	public TargetEmpathicReaction(Event e, ArrayList potentialEmphaticEmotions){
		super(e,potentialEmphaticEmotions);	
	}
	
	
	public String getEmpathicTargetFacialExpression(){
		Name facialExpProperty = Name.ParseName(_event.GetTarget() + "(facial-exp)");
		String facialExpression = (String)KnowledgeBase.GetInstance().AskProperty(facialExpProperty);
		//System.out.println("Event-> " + _event + " Target-> " + _event.GetTarget() + " Facial Exp-> " + expression);		
		return facialExpression;
	}
	
	
	protected float getFamiliarityWithEmpathicTarget() {
		String selfName = AutobiographicalMemory.GetInstance().getSelf();
		String targetName = _event.GetTarget();
		
		Name likeProperty = Name.ParseName("Like("+ selfName + "," + targetName +")");
		Float likeValue = (Float) KnowledgeBase.GetInstance().AskProperty(likeProperty);

		if(likeValue == null){	
			return 0f;
		}else{
			return likeValue.floatValue();				
		}	
	}
}
