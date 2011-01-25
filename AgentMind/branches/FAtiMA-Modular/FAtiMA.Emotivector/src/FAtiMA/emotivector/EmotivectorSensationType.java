package FAtiMA.emotivector;

import FAtiMA.Core.util.enumerables.EmotionValence;

public enum EmotivectorSensationType {
	WEAKER_PUNISHMENT (EmotionValence.NEGATIVE),
	STRONGER_PUNISHMENT (EmotionValence.NEGATIVE),
	EXPECTED_PUNISHMENT (EmotionValence.NEGATIVE),
	UNEXPECTED_PUNISHMENT (EmotionValence.NEGATIVE),
	WEAKER_REWARD (EmotionValence.POSITIVE),
	EXPECTED_REWARD (EmotionValence.POSITIVE),
	STRONGER_REWARD (EmotionValence.POSITIVE),
	UNEXPECTED_REWARD (EmotionValence.POSITIVE),
	NEUTRAL (EmotionValence.POSITIVE);
	
	private String[] appraisalVariables = 
	  {EmotivectorComponent.AppraisalVariables.EXPECTED_VALUE.name(),
	   EmotivectorComponent.AppraisalVariables.SENSED_VALUE.name()};


	public final String [] getAppraisalVariables(){
		return this.appraisalVariables;
	}
	
    private final EmotionValence valence; 
    
	EmotivectorSensationType(EmotionValence v){
		this.valence = v;
	}
	
	public EmotionValence getValence(){return valence;}
}
