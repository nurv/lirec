package FAtiMA.empathy;

import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.sensorEffector.Event;

public class EmpathicEmotion extends BaseEmotion {

	private static final long serialVersionUID = 1L;

	public EmpathicEmotion(BaseEmotion em,Event originalEvent) {
		super(em);
		this.SetCause(originalEvent);
	}
}
