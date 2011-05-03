package FAtiMA.culture;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.DeliberativeComponent.IOptionsStrategy;
import FAtiMA.DeliberativeComponent.IUtilityStrategy;
import FAtiMA.DeliberativeComponent.Intention;
import FAtiMA.DeliberativeComponent.strategies.IExpectedUtilityStrategy;
import FAtiMA.DeliberativeComponent.strategies.IGetUtilityForOthers;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.ReactiveComponent.ReactiveComponent;
import FAtiMA.ToM.ToMComponent;
import FAtiMA.motivationalSystem.MotivationalComponent;


public class CulturalDimensionsComponent implements IAppraisalDerivationComponent, IOptionsStrategy, IExpectedUtilityStrategy {
	static public final String NAME = "CulturalDimensionsComponent";
	
	final float ALPHA = 0.3f;
	final float POWER_DISTANCE_K = 1.2f;
	final float MAX_DIMENSIONAL_SCORE = 100;
	
	private String cultureFile;
	private int[] _dimensionalValues;	
	private ArrayList<Ritual> _rituals;
	private HashMap<String,Ritual> _ritualOptions;
	

	public CulturalDimensionsComponent(String cultureFile){
		this.cultureFile = cultureFile;
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
		DeliberativeComponent dc = (DeliberativeComponent) aM.getComponent(DeliberativeComponent.NAME);
		this.loadCulture(aM);
		dc.addOptionsStrategy(this);
		dc.setExpectedUtilityStrategy(this);
		aM.getRemoteAgent().setProcessActionStrategy(new CultureProcessActionStrategy());
	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame af)
	{
		float desirabilityForOtherAgents = 0;
		float desirabilityForAgentResponsible = 0;
		
		if(e.GetSubject().equalsIgnoreCase(Constants.SELF)){
			/* if the agent was responsible for the event,
			 * desirabilityForAgentResponsible is equal to the
			 * desirability the event had for the agent itself
			 */
			desirabilityForAgentResponsible = af.getAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name());
			for(String variable : af.getAppraisalVariables())
			{
				if(variable.startsWith(OCCAppraisalVariables.DESFOROTHER.name()))
				{		
					desirabilityForOtherAgents += af.getAppraisalVariable(variable);
				}
			}
		}else{
			/* if the agent was not responsible for the event,
			 * then the desirability the event had for the agent itself is added to
			 * the desirabirabilityForOtherAgents
			 */
			desirabilityForOtherAgents += af.getAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name());
			for(String variable : af.getAppraisalVariables())
			{
				if(variable.startsWith(OCCAppraisalVariables.DESFOROTHER.name()))
				{
					String agentName = variable.substring(OCCAppraisalVariables.DESFOROTHER.name().length());
					if (agentName.equalsIgnoreCase(e.GetSubject())){
						desirabilityForAgentResponsible = af.getAppraisalVariable(variable);
					}
					desirabilityForOtherAgents  += af.getAppraisalVariable(variable);
				}
			}
		}
				
		float praiseWorthiness = this.determinePraiseWorthiness(desirabilityForAgentResponsible, desirabilityForOtherAgents);
		
		af.SetAppraisalVariable(NAME, (short)4, OCCAppraisalVariables.PRAISEWORTHINESS.name(), praiseWorthiness);	
		
	}
	
	private void addRitualOptions(Event e, AgentModel am){
		ArrayList<SubstitutionSet> substitutions, substitutions2;
		Ritual r2, r3;
		String ritualName;
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		
		//this section detects if a ritual has started with another agent's action
		if(!e.GetSubject().equals(Constants.SELF))
		{
			for(Ritual r : this._rituals)
			{		
				substitutions = r.findMatchWithStep(new Symbol(e.GetSubject()),e.toStepName());
				for(SubstitutionSet sSet : substitutions)
				{
					r2 = (Ritual) r.clone();
					r2.MakeGround(sSet.GetSubstitutions());
					
					//we must check the ritual preconditions
					substitutions2 = Condition.CheckActivation(am,r2.GetPreconditions());
					if(substitutions2 != null)
					{
						for(SubstitutionSet sSet2 : substitutions2)
						{
							r3 = (Ritual) r2.clone();
							r3.MakeGround(sSet2.GetSubstitutions());
							//the last thing we need to check is if the agent is included in the ritual's
							//roles and if the ritual has not succeeded, because if not there is no sense in including the ritual as a goal
							if(r3.GetRoles().contains(Constants.SELF)
									&& !r3.CheckSuccess(am))
							{
								ritualName = r3.getNameWithCharactersOrdered();
								r3.setUrgency(2);
								if(!_ritualOptions.containsKey(ritualName) && !dc.containsIntention(r3))
								{
									AgentLogger.GetInstance().logAndPrint("Reactive Activation of a Ritual:" + r3.getName());
									_ritualOptions.put(ritualName,r3);
								}
							}
						}
					}
				}
			}	
		}
	}
	
	public void AddRitual(Ritual r)
	{
		_rituals.add(r);
		//_planner.AddOperator(r);
	}
	
	
	private void loadCulture(AgentModel aM){
		
		DeliberativeComponent dp = (DeliberativeComponent) aM.getComponent(DeliberativeComponent.NAME);

		AgentLogger.GetInstance().log("LOADING Culture: " + this.cultureFile);
		CultureLoaderHandler cultureLoader = new CultureLoaderHandler(aM,this);
		
		try{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
				
			parser.parse(new File(this.cultureFile), cultureLoader);

			for(Ritual r : cultureLoader.GetRituals(aM)){
				this._rituals.add(r);
				dp.addGoal(r);
			}
			cultureLoader = new CultureLoaderHandler(aM,this);
			parser.parse(new File(ConfigurationManager.getGoalsFile()), cultureLoader);

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

	public float determinePraiseWorthiness(float contributionToResponsibleAgentNeeds, float contributionToOthersNeeds) {
		float collectivismCoefficient = _dimensionalValues[CulturalDimensionType.COLLECTIVISM] / MAX_DIMENSIONAL_SCORE;

		
		if((contributionToOthersNeeds >= 0)  && (contributionToResponsibleAgentNeeds - contributionToOthersNeeds > 0)){
			//if agent doesn't lower other agents needs and he does something better for himself than to others
			return 0;
		}
		else{
			float praiseworthiness = (contributionToOthersNeeds - contributionToResponsibleAgentNeeds) * collectivismCoefficient;
			return praiseworthiness;
		}
	}

	

	//Unused methods from the interface:
	@Override
	public void reset(){}
	

	@Override
	public Collection<? extends ActivePursuitGoal> options(AgentModel am) {
		
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		
		Iterator<Ritual> it = _ritualOptions.values().iterator();
		Ritual r;
		
		while(it.hasNext())
		{
			r = it.next();
			if(dc.containsIntention(r))
			{
				it.remove();
			}
		}
		
		return _ritualOptions.values();
		
	}
	
	private float culturalEU(AgentModel am, ActivePursuitGoal g, float probability)
	{
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		
		IUtilityStrategy str =  dc.getUtilityStrategy();
		
		float contributionToSelf = str.getUtility(am, g);
		
		IGetUtilityForOthers ostrat = dc.getUtilityForOthersStrategy();
		
		float contributionOthers = ostrat.getUtilityForOthers(am, g);
				
		float culturalGoalUtility = determineCulturalUtility(am, g,contributionToSelf,contributionOthers);		
		
		float EU = (culturalGoalUtility + g.GetGoalUrgency()) * probability;
		
		
		AgentLogger.GetInstance().intermittentLog("Goal: " + g.getName() + " CulturalUtilitity: " + culturalGoalUtility + " Competence: " + probability +
				" Urgency: "+ g.GetGoalUrgency() + " Total: " + EU);
		return EU;
	}

	@Override
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g) {
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		
		float probability = dc.getProbabilityStrategy().getProbability(am, g);
		return culturalEU(am,g,probability);
	}
	
	@Override
	public float getExpectedUtility(AgentModel am, Intention i) {
		
		DeliberativeComponent dp = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME); 
		
		float probability = dp.getProbabilityStrategy().getProbability(am, i);
		
		return culturalEU(am,i.getGoal(),probability);
	}

	@Override
	public void update(AgentModel am,long time) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void update(AgentModel am,Event e) {
		this.addRitualOptions(e,am);
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return null;
	}

	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;	
	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
	}

	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {ReactiveComponent.NAME,DeliberativeComponent.NAME,MotivationalComponent.NAME,ToMComponent.NAME};
		return dependencies;
	}	
}
