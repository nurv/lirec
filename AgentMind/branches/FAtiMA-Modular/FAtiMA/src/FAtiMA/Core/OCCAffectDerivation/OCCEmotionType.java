package FAtiMA.Core.OCCAffectDerivation;

import FAtiMA.Core.util.enumerables.EmotionValence;

public enum OCCEmotionType {
	ADMIRATION (EmotionValence.POSITIVE){public String[] getAppraisalVariables(){return admirationVariables;}},
	ANGER (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return angerVariables;}},
	DISAPPOINTMENT (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return disappointmentVariables;}},
	DISTRESS (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return distressVariables;}},
	FEAR (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return fearVariables;}},
	FEARS_CONFIRMED (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return fearsConfirmedVariables;}},
	GRATIFICATION (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return gratificationVariables;}},
	GLOATING (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return gloatingVariables;}},
	HAPPY_FOR(EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return happyForVariables;}},
	HATE (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return hateVariables;}},
	HOPE (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return hopeVariables;}},
	JOY (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return joyVariables;}},
	LOVE (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return loveVariables;}},
	PITTY (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return pittyVariables;}},
	PRIDE (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return prideVariables;}},
	RELIEF (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return reliefVariables;}},
	REMORSE (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return remorseVariables;}},
	REPROACH (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return reproachVariables;}},
	RESENTMENT (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return resentmentVariables;}},
	SATISFACTION (EmotionValence.POSITIVE) {public String[] getAppraisalVariables(){return satisfactionVariables;}},
	SHAME (EmotionValence.NEGATIVE) {public String[] getAppraisalVariables(){return shameVariables;}};
	
	
	public abstract String[] getAppraisalVariables();

	private static String[] admirationVariables = {OCCAppraisalVariables.PRAISEWORTHINESS.name()};
	private static String[] angerVariables = {null};
	private static String[] disappointmentVariables = {OCCAppraisalVariables.GOALCONDUCIVENESS.name(),OCCAppraisalVariables.GOALSTATUS.name()};
	private static String[] distressVariables = {OCCAppraisalVariables.DESIRABILITY.name()};
	private static String[] fearVariables = {OCCAppraisalVariables.GOALCONDUCIVENESS.name(),OCCAppraisalVariables.FAILUREPROBABILITY.name()};
	private static String[] fearsConfirmedVariables = {OCCAppraisalVariables.GOALCONDUCIVENESS.name(),OCCAppraisalVariables.GOALSTATUS.name()};
	private static String[] gloatingVariables = {OCCAppraisalVariables.DESIRABILITY.name(),OCCAppraisalVariables.DESFOROTHER.name()};
	private static String[] gratificationVariables = {null};
	private static String[] happyForVariables = {OCCAppraisalVariables.DESIRABILITY.name(),OCCAppraisalVariables.DESFOROTHER.name()};
	private static String[] hateVariables = {OCCAppraisalVariables.LIKE.name()};
	private static String[] hopeVariables = {OCCAppraisalVariables.GOALCONDUCIVENESS.name(),OCCAppraisalVariables.SUCCESSPROBABILITY.name()};
	private static String[] joyVariables = {OCCAppraisalVariables.DESIRABILITY.name()};
	private static String[] loveVariables = {OCCAppraisalVariables.LIKE.name()};
	private static String[] pittyVariables = {OCCAppraisalVariables.DESIRABILITY.name(),OCCAppraisalVariables.DESFOROTHER.name()};
	private static String[] prideVariables = {OCCAppraisalVariables.PRAISEWORTHINESS.name()};
	private static String[] reliefVariables = {OCCAppraisalVariables.GOALCONDUCIVENESS.name(),OCCAppraisalVariables.GOALSTATUS.name()};
	private static String[] remorseVariables = {null};
	private static String[] reproachVariables = {OCCAppraisalVariables.PRAISEWORTHINESS.name()};
	private static String[] resentmentVariables = {OCCAppraisalVariables.DESIRABILITY.name(),OCCAppraisalVariables.DESFOROTHER.name()};
	private static String[] satisfactionVariables = {null};
	private static String[] shameVariables = {OCCAppraisalVariables.PRAISEWORTHINESS.name()};
	
	private final EmotionValence valence; 
	    
	OCCEmotionType(EmotionValence v){
		this.valence = v;
	}
		
	public EmotionValence getValence(){return valence;}
}
