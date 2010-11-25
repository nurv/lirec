package FAtiMA.Core;

import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.sensorEffector.Event;

public interface IProcessEmotionComponent {
	
	public void emotionActivation(AgentModel am, Event e, ActiveEmotion em);

}
