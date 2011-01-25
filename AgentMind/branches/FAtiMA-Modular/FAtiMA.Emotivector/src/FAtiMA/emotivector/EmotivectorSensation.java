package FAtiMA.emotivector;

import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Name;

public class EmotivectorSensation extends BaseEmotion{

	private static final short FIXED_POTENTIAL = 5;
	
	protected EmotivectorSensation(EmotivectorSensationType type, Event cause) {
		
		super(type.name(), type.getValence(), type.getAppraisalVariables(), FIXED_POTENTIAL, cause, null);
		// TODO Auto-generated constructor stub
	}

}
