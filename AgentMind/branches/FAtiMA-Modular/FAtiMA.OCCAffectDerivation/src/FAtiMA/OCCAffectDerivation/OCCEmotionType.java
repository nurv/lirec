package FAtiMA.OCCAffectDerivation;

import FAtiMA.Core.util.enumerables.EmotionValence;

public enum OCCEmotionType {
	ADMIRATION (EmotionValence.POSITIVE){public String[] getAppraisalVariables(){return attribution;}},
	ANGER (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return composed;}},
	DISAPPOINTMENT (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return positiveProspect;}},
	DISTRESS (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return wellBeing;}},
	FEAR (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return negativeProspect;}},
	FEARS_CONFIRMED (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return negativeProspect;}},
	GRATIFICATION (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return composed;}},
	GLOATING (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return fortuneOfOthers;}},
	HAPPY_FOR(EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return fortuneOfOthers;}},
	HATE (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return attraction;}},
	HOPE (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return positiveProspect;}},
	JOY (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return wellBeing;}},
	LOVE (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return attraction;}},
	PITTY (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return fortuneOfOthers;}},
	PRIDE (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return attribution;}},
	RELIEF (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return negativeProspect;}},
	REMORSE (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return composed;}},
	REPROACH (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return attribution;}},
	RESENTMENT (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return fortuneOfOthers;}},
	SATISFACTION (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return positiveProspect;}},
	SHAME (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return attribution;}};
	
	
	public abstract String[] getAppraisalVariables();

	private static String[] attribution = {OCCAppraisalVariables.PRAISEWORTHINESS.name()};
	private static String[] wellBeing = {OCCAppraisalVariables.DESIRABILITY.name()};
	private static String[] fortuneOfOthers = {OCCAppraisalVariables.DESIRABILITY.name(),OCCAppraisalVariables.DESFOROTHER.name()};
	private static String[] attraction = {OCCAppraisalVariables.DESIRABILITY.name()};
	private static String[] composed = {OCCAppraisalVariables.DESIRABILITY.name(),OCCAppraisalVariables.PRAISEWORTHINESS.name()};
	
	private static String[] positiveProspect = {OCCAppraisalVariables.GOALCONDUCIVENESS.name(),OCCAppraisalVariables.GOALSTATUS.name(), OCCAppraisalVariables.FAILUREPROBABILITY.name()};
	private static String[] negativeProspect = {OCCAppraisalVariables.GOALCONDUCIVENESS.name(),OCCAppraisalVariables.GOALSTATUS.name(), OCCAppraisalVariables.SUCCESSPROBABILITY.name()};	

	
	private final EmotionValence valence; 
	    
	OCCEmotionType(EmotionValence v){
		this.valence = v;
	}
		
	public EmotionValence getValence(){return valence;}
}
