package FAtiMA.culture;

import java.util.ArrayList;


import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.motivationalSystem.Motivator;
import FAtiMA.util.enumerables.CulturalDimensionType;
import FAtiMA.util.enumerables.MotivatorType;
import FAtiMA.wellFormedNames.Name;


public class CulturalDimensions {
	final float ALPHA = 0.3f;
	final float POWER_DISTANCE_K = 1.2f;

	int[] _dimensionalValues;
	ArrayList<String> _positiveLSignals;
	ArrayList<String> _negativeLSignals;
	ArrayList<String> _positiveReplyLSignals;
	ArrayList<String> _negativeReplyLSignals;
	
	

	/**
	 * Singleton pattern 
	 */	
	private static CulturalDimensions _culturalDimensionsInstance = null;


	public static CulturalDimensions GetInstance()
	{
		if(_culturalDimensionsInstance == null)
		{
			_culturalDimensionsInstance = new CulturalDimensions();

		}
		return _culturalDimensionsInstance;
	}


	private CulturalDimensions() {
		_dimensionalValues = new int[CulturalDimensionType.numberOfTypes()];
		_positiveLSignals = new ArrayList<String>();
		_negativeLSignals = new ArrayList<String>();
		_positiveReplyLSignals = new ArrayList<String>();
		_negativeReplyLSignals = new ArrayList<String>();
	}

	

	public int getDimensionValue(short dimensionType){
		return _dimensionalValues[dimensionType];
	}

	public void setDimensionValue(short dimensionType, int value){
		_dimensionalValues[dimensionType] = value;
	}


	public void addPositiveLSignal(String lSignal){	
		_positiveLSignals.add(lSignal);
	}
	
	public void addNegativeLSignal(String lSignal){	
		_negativeLSignals.add(lSignal);
	}
	
	public void addPositiveReplyLSignal(String lSignal){	
		_positiveReplyLSignals.add(lSignal);
	}
	
	public void addNegativeReplyLSignal(String lSignal){	
		_negativeReplyLSignals.add(lSignal);
	}
	
	// Currently only affects affiliation
	public void changeNeedsWeightsAndDecays(AgentModel am) {

		float collectivismCoefficient = _dimensionalValues[CulturalDimensionType.COLLECTIVISM] * 0.01f;
		Motivator affiliationMotivator = am.getMotivationalState().GetSelfMotivator(MotivatorType.AFFILIATION);		
		float personalityAffiliationWeight = affiliationMotivator.GetWeight();
		float personalityAffiliationDecayFactor = affiliationMotivator.GetDecayFactor();
		float affiliationAvgWeight = 0.5f;
		float personalityDifference = personalityAffiliationWeight - affiliationAvgWeight;
		float newAffiliationWeight = Math.max(this.ALPHA,collectivismCoefficient + personalityDifference);

		//A collectivism score of 100 doubles the agent's affiliation decay factor
		float newAffiliationDecayFactor = personalityAffiliationDecayFactor * (1 + collectivismCoefficient);


		affiliationMotivator.SetWeight(newAffiliationWeight);
		affiliationMotivator.SetDecayFactor(newAffiliationDecayFactor);

	}

	public float determineCulturalUtility(AgentModel am, ActivePursuitGoal goal, float selfContrib, float otherContrib){

		
		//float powerDistanceCoefficient = _dimensionalValues[CulturalDimensionType.POWERDISTANCE] * 0.01f;
		float collectivismCoefficient = _dimensionalValues[CulturalDimensionType.COLLECTIVISM] * 0.01f;
		float individualismCoefficient = 1 - collectivismCoefficient; 
	
		//String goalName = goal.getName().GetFirstLiteral().toString();	
		String target = goal.getName().GetLiteralList().get(1).toString();
		
		float likeValue = this.obtainLikeRelationshipFromKB(am, target);
		//float differenceInPower = this.obtainDifferenceInPowerFromKB(am, target);
			
		float result = selfContrib + //(otherContrib * Math.pow(POWER_DISTANCE_K, differenceInPower) + 
												 (otherContrib * collectivismCoefficient)+ 
												 (otherContrib * likeValue * individualismCoefficient);
		
		return result;
	}

	private float obtainLikeRelationshipFromKB(AgentModel am, String targetAgent){
		Name likeProperty = Name.ParseName("Like("+ am.getName() + "," + targetAgent +")");
		Float likeValue = (Float) am.getMemory().getSemanticMemory().AskProperty(likeProperty);

		if(likeValue == null){	
			return 0f;
		}else{
			return likeValue.floatValue();				
		}	
	}

	/*private int obtainDifferenceInPowerFromKB(AgentModel am, String targetAgent){

		String agentName = am.getName();

		Name selfPowerPropertyName = Name.ParseName(agentName+"(power)");
		String selfPowerProperty = (String)am.getMemory().AskProperty(selfPowerPropertyName);
		Name targetPowerPropertyName = Name.ParseName(targetAgent+"(power)");
		String targetPowerProperty = (String)am.getMemory().AskProperty(targetPowerPropertyName);

		if(selfPowerProperty == null || targetPowerProperty == null){
			//AgentLogger.GetInstance().logAndPrint("WARNING! Agent power properties not present in KB");
			return 0;
		}else{
			int selfPowerValue = Integer.parseInt(selfPowerProperty);
			int targetPowerValue = Integer.parseInt(targetPowerProperty);

			return targetPowerValue - selfPowerValue;
		}
	}*/

	public float determineAffiliationEffectFromLSignal(String subject, String target, String signalName, float signalValue) {
		return signalValue;
	}


	public float determinePraiseWorthiness(float contributionToSelfNeeds, float contributionToOthersNeeds) {
		float collectivismCoefficient = _dimensionalValues[CulturalDimensionType.COLLECTIVISM] * 0.01f;

		
		if((contributionToOthersNeeds >= 0)  && (contributionToSelfNeeds - contributionToOthersNeeds > 0)){
			//if agent doesn't lower other agents needs and he does something better for himself than to others
			return 0;
		}else{
			return (contributionToOthersNeeds - contributionToSelfNeeds) * collectivismCoefficient;
		}
	}

/*
	public String determineLSignalToSend(String targetAgentName, boolean reply) {
		float powerDistanceCoefficient = _dimensionalValues[CulturalDimensionType.POWERDISTANCE] * 0.01f;
		float collectivismCoefficient = _dimensionalValues[CulturalDimensionType.COLLECTIVISM] * 0.01f;
		float individualismCoefficient = 1 - collectivismCoefficient;
		float likeValue = this.obtainLikeRelationshipFromKB(targetAgentName);
		float powerDistance = this.obtainDistanceInPowerFromKB(targetAgentName);

		float lSignalBaseValue = 0;

		lSignalBaseValue += likeValue * individualismCoefficient;		

		if(powerDistance > 0){
			lSignalBaseValue += powerDistance * powerDistanceCoefficient;	
		}

		if(lSignalBaseValue >= 0){
			return getRandomLSignal("positive", reply);
		}else{
			return getRandomLSignal("negative", reply);
		}	
	}

	private String getRandomLSignal(String signal, boolean reply){

		Random randomGenerator = new Random();
		int randomIndex;

		if(signal.equalsIgnoreCase("positive") && reply == false){
			randomIndex = randomGenerator.nextInt(_positiveLSignals.size());
			return (String)_positiveLSignals.get(randomIndex);
		}
		
		if(signal.equalsIgnoreCase("positive") && reply == true){
			randomIndex = randomGenerator.nextInt(_positiveReplyLSignals.size());
			return (String)_positiveReplyLSignals.get(randomIndex);
		}
		
		if(signal.equalsIgnoreCase("negative") && reply == false){
			randomIndex = randomGenerator.nextInt(_negativeLSignals.size());
			return (String)_negativeLSignals.get(randomIndex);
		}
		
		if(signal.equalsIgnoreCase("negative") && reply == true){
			randomIndex = randomGenerator.nextInt(_negativeReplyLSignals.size());
			return (String)_negativeReplyLSignals.get(randomIndex);
		}
		else{
			return null;
		}
	}*/
}
