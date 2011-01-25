package FAtiMA.Core.emotionalState;

import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.enumerables.EmotionValence;

public final class NeutralEmotion extends BaseEmotion{
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "Neutral";	
	public static final EmotionValence VALENCE = EmotionValence.POSITIVE;
	private static final String[] APPRAISAL_VARIABLES = {};
	public static final float POTENTIAL = 0;	
	

	public NeutralEmotion(Event e){
		super(NAME, VALENCE,APPRAISAL_VARIABLES,POTENTIAL,e);
	}
}
