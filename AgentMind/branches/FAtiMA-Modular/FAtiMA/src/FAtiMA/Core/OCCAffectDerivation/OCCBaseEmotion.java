package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Name;

public class OCCBaseEmotion extends BaseEmotion {

	protected OCCBaseEmotion(OCCEmotionType type, float potential,Event cause,Name direction){
		super(type.name(), type.getValence(), type.getAppraisalVariables(),potential, cause,direction);
	}
	
	protected OCCBaseEmotion(OCCEmotionType type, float potential,Event cause) {
		super(type.name(), type.getValence(), type.getAppraisalVariables(),potential, cause);

	}
	
	private static final long serialVersionUID = 1L;

}
