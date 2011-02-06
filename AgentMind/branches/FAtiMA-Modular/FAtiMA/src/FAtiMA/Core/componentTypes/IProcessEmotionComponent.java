package FAtiMA.Core.componentTypes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.ActiveEmotion;

public interface IProcessEmotionComponent extends IComponent{
	
	public void emotionActivation(AgentModel am, ActiveEmotion em);

}
