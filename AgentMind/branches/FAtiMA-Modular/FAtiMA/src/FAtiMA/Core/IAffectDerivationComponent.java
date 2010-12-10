package FAtiMA.Core;

import java.util.ArrayList;

import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;

public interface IAffectDerivationComponent extends IComponent{
	
	public ArrayList<BaseEmotion> deriveEmotions(AgentModel am, String appraisalVariable, AppraisalFrame as);

}
