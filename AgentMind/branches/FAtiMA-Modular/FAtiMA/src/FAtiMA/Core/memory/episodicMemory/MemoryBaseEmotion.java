package FAtiMA.Core.memory.episodicMemory;

import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.enumerables.EmotionValence;
import FAtiMA.Core.wellFormedNames.Name;

public class MemoryBaseEmotion extends BaseEmotion {
	
	private final static long serialVersionUID = 1L;
	
	public MemoryBaseEmotion(String type, EmotionValence valence,
			String[] appraisalVariables, float potential, Event cause,
			Name direction) {
		super(type, valence, appraisalVariables, potential, cause, direction);
	}
	
}
