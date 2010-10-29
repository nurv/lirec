package FAtiMA.culture;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import FAtiMA.Agent;
import FAtiMA.AgentModel;
import FAtiMA.IComponent;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.VersionChecker;
import FAtiMA.util.enumerables.CulturalDimensionType;
import FAtiMA.wellFormedNames.Name;


public class CulturalDimensionsComponent implements IComponent{
	final String NAME = "CulturalDimensionsComponent";
	
	final float ALPHA = 0.3f;
	final float POWER_DISTANCE_K = 1.2f;
	
	private String cultureName;
	private int[] _dimensionalValues;	
	private ArrayList<Ritual> _rituals;
	private HashMap<String,Ritual> _ritualOptions;
	

	public CulturalDimensionsComponent(String cultureName){
		this.cultureName = cultureName;
		_rituals = new ArrayList<Ritual>();
		_ritualOptions = new HashMap<String,Ritual>();
		_dimensionalValues = new int[CulturalDimensionType.numberOfTypes()];
	}

	public String name(){
		return this.NAME;
	}
	
	//unused interface methods:
	@Override
	public void initialize(AgentModel aM){
		this.loadCulture(aM);
	}
	
	@Override
	public void appraisal(Event e, AgentModel am){}
	@Override
	public void coping(){}
	
	public void AddRitual(Ritual r)
	{
		_rituals.add(r);
		//_planner.AddOperator(r);
	}
	
	
	private void loadCulture(AgentModel aM){

		AgentLogger.GetInstance().log("LOADING Culture: " + this.cultureName);
		CultureLoaderHandler cultureLoader = new CultureLoaderHandler(aM,this);
		
		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			if (VersionChecker.runningOnAndroid())
				parser.parse(new File(Agent.MIND_PATH_ANDROID + cultureName + ".xml"), cultureLoader);		
			else	
				parser.parse(new File(Agent.MIND_PATH + cultureName + ".xml"), cultureLoader);

			for(Ritual r : cultureLoader.GetRituals(aM)){
				this._rituals.add(r);
				aM.getDeliberativeLayer().AddGoal(r);
			}

		}catch(Exception e){
			throw new RuntimeException("Error on Loading the Culture XML File:" + e);
		}
		//this.changeNeedsWeightsAndDecays(aM);
	}

	public int getDimensionValue(short dimensionType){
		return _dimensionalValues[dimensionType];
	}

	public void setDimensionValue(short dimensionType, int value){
		_dimensionalValues[dimensionType] = value;
	}

	
	// Currently only affects affiliation
	/*public void changeNeedsWeightsAndDecays(AgentModel am) {
		float collectivismCoefficient = _dimensionalValues[CulturalDimensionType.COLLECTIVISM] * 0.01f;
		Motivator affiliationMotivator = am.getMotivationalState().GetMotivator(MotivatorType.AFFILIATION);		
		float personalityAffiliationWeight = affiliationMotivator.GetWeight();
		float personalityAffiliationDecayFactor = affiliationMotivator.GetDecayFactor();
		float affiliationAvgWeight = 0.5f;
		float personalityDifference = personalityAffiliationWeight - affiliationAvgWeight;
		float newAffiliationWeight = Math.max(this.ALPHA,collectivismCoefficient + personalityDifference);

		//A collectivism score of 100 doubles the agent's affiliation decay factor
		float newAffiliationDecayFactor = personalityAffiliationDecayFactor * (1 + collectivismCoefficient);
		affiliationMotivator.SetWeight(newAffiliationWeight);
		affiliationMotivator.SetDecayFactor(newAffiliationDecayFactor);
	}*/

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

	public float determinePraiseWorthiness(float contributionToSelfNeeds, float contributionToOthersNeeds) {
		float collectivismCoefficient = _dimensionalValues[CulturalDimensionType.COLLECTIVISM] * 0.01f;

		
		if((contributionToOthersNeeds >= 0)  && (contributionToSelfNeeds - contributionToOthersNeeds > 0)){
			//if agent doesn't lower other agents needs and he does something better for himself than to others
			return 0;
		}else{
			return (contributionToOthersNeeds - contributionToSelfNeeds) * collectivismCoefficient;
		}
	}

	

	//Unused methods from the interface:
	@Override
	public void reset(){}
	@Override
	public void shutdown(){}
	@Override
	public void decay(long time){}	
	@Override
	public void lookAtPerception(String subject, String target) {}	
	@Override
	public void propertyChangedPerception(String ToM, Name propertyName,String value) {}	
}
