package FAtiMA.Core.componentTypes;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;

public interface IAffectDerivationComponent extends IComponent{
	
	public ArrayList<BaseEmotion> affectDerivation(AgentModel am, AppraisalFrame af);
	public void inverseAffectDerivation(AgentModel am, BaseEmotion em, AppraisalFrame af);
	

}
