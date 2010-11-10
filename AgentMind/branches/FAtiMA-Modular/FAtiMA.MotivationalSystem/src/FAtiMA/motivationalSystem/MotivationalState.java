/*
 * MotivatorState.java - Represents the character's motivational state
 */

package FAtiMA.motivationalSystem;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ListIterator;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.AgentSimulationTime;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.deliberativeLayer.IActionFailureStrategy;
import FAtiMA.Core.deliberativeLayer.IExpectedUtilityStrategy;
import FAtiMA.Core.deliberativeLayer.IGoalFailureStrategy;
import FAtiMA.Core.deliberativeLayer.IGoalSuccessStrategy;
import FAtiMA.Core.deliberativeLayer.IProbabilityStrategy;
import FAtiMA.Core.deliberativeLayer.IUtilityStrategy;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.Core.deliberativeLayer.plan.EffectOnDrive;
import FAtiMA.Core.deliberativeLayer.plan.IPlanningOperator;
import FAtiMA.Core.deliberativeLayer.plan.Step;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.ExpectedEffectType;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;


/**
 * Implements the character's motivational state.
 * 
 * @author Meiyii Lim, Samuel Mascarenhas 
 */

public class MotivationalState implements Serializable, Cloneable, IComponent, IExpectedUtilityStrategy, IProbabilityStrategy, IUtilityStrategy, IGoalSuccessStrategy, IGoalFailureStrategy, IActionFailureStrategy {
	
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME ="MotivationalState";
	
	
	protected Motivator[]  _motivators;
	
	//protected Hashtable<String,Motivator[]> _otherAgentsMotivators;
	protected long _lastTime;
	protected int _goalTried;
	protected int _goalSucceeded;
	
	protected HashMap<String,Float> _appraisals;
	protected HashMap<String,ExpectedEffectOnDrives> _goalEffectsOnDrives;

	public static double determineQuadraticNeedVariation(float currentLevel, float deviation){
		final float MAX_INTENSITY = 10;
		final float MIN_INTENSITY = 0;
		double result = 0;
		float finalLevel;
		double currentLevelStr;
		double finalLevelStr;
		
		finalLevel = currentLevel + deviation;
		finalLevel = Math.min(finalLevel, MAX_INTENSITY);
		finalLevel = Math.max(finalLevel, MIN_INTENSITY);
		
		currentLevelStr = Math.pow(MAX_INTENSITY - currentLevel,2); 
		finalLevelStr = Math.pow(MAX_INTENSITY - finalLevel,2);

		result = - (finalLevelStr - currentLevelStr); 
		return result;
	}


	/**
	 * Creates an empty MotivationalState
	 */
	public MotivationalState() {
		_motivators = new Motivator[MotivatorType.numberOfTypes()];
		//_otherAgentsMotivators = new Hashtable<String,Motivator[]>();
		_goalTried = 0;
		_goalSucceeded = 0;
		_lastTime = AgentSimulationTime.GetInstance().Time();
		_appraisals = new HashMap<String,Float>();
		_goalEffectsOnDrives = new HashMap<String,ExpectedEffectOnDrives>();
	}
	
	public void addEffectOnDrive(String goal, short effectType, String driveName, Symbol target, float value)
	{
		ExpectedEffectOnDrives effects;
		if(!_goalEffectsOnDrives.containsKey(goal))
		{
			effects = new ExpectedEffectOnDrives(goal);
			_goalEffectsOnDrives.put(goal, effects);
		}
		else
		{
			effects = _goalEffectsOnDrives.get(goal);
		}
		
		effects.AddEffect(new FAtiMA.motivationalSystem.EffectOnDrive(effectType, driveName, target, value));
	}
	
	public Motivator[] getMotivators(){
		return _motivators;
	}
	
	
	/** 
	 * Adds a motivator to the MotivationalState
	 */
	public void AddMotivator(Motivator motivator)
	{
		_motivators[motivator.GetType()] = new Motivator(motivator);
	}
	
	
	public Motivator GetMotivator(short motivatorType){
		return _motivators[motivatorType];
	}
	
	
	
	/** 
	 * Updates the intensity of the motivators based on the event received
	 * @throws InvalidMotivatorTypeException 
	 */
	public float UpdateMotivators(AgentModel am, Event e, ArrayList<? extends IPlanningOperator> operators)
	{
		ArrayList<Substitution> substitutions;
		IPlanningOperator operator;
		Step action;
		EffectOnDrive effectOnDrive;
		MotivatorCondition motCondition;
		float deviation = 0;
		double contributionToNeed =0f;
	    float contributionToSelfNeeds = 0f;  //used for events performed by the agent
		
		
		
		//Other Events Update The Motivators According to The Effects Specified By the Author in The Actions.xml 
		for(ListIterator<? extends IPlanningOperator> li = operators.listIterator(); li.hasNext();)
		{
			
			operator = li.next();
			if(operator instanceof Step)
			{
				action = (Step) operator;
				substitutions = Unifier.Unify(e.toStepName(),action.getName());
				if(substitutions != null)
				{

					substitutions.add(new Substitution(new Symbol("[AGENT]"),new Symbol(e.GetSubject())));
					action = (Step) action.clone();
					action.MakeGround(substitutions);

					for(ListIterator<EffectOnDrive> li2 = action.getEffectsOnDrives().listIterator(); li2.hasNext();)
					{
						effectOnDrive = (EffectOnDrive) li2.next();
						motCondition = (MotivatorCondition) effectOnDrive.GetEffectOnDrive();
						Name target = motCondition.GetTarget();

						if (target.toString().equalsIgnoreCase(Constants.SELF))
						{
							AgentLogger.GetInstance().log("Updating self motivator " + motCondition.GetDrive());
							try {
								short driveType = MotivatorType.ParseType(motCondition.GetDrive());
								float oldLevel = _motivators[driveType].GetIntensity();
								deviation = _motivators[driveType].UpdateIntensity(motCondition.GetEffect());
								contributionToNeed = determineQuadraticNeedVariation(oldLevel, deviation)*0.1f;
								contributionToSelfNeeds += contributionToNeed;
							} catch (InvalidMotivatorTypeException e1) {
								e1.printStackTrace();
							}
						}
					}			
				}	
			}			
		}
		
		return contributionToSelfNeeds;
	}
	
	public float getContributionToNeeds(AgentModel am, ActivePursuitGoal g){
		float result = 0;
		String[] effectTypes = {"OnSelect","OnIgnore"};
		String[] nonCognitiveDrives = {"Affiliation","Integrity","Energy"};
		float expectedContribution;
		float currentIntensity = 0;
		float auxMultiplier; // this is used for the effects that are OnIgnore
		
		
		
		
		try {		
			result = 0;
			auxMultiplier = 1;
			
			ExpectedEffectOnDrives effects = _goalEffectsOnDrives.get(g.getKey());
			for(FAtiMA.motivationalSystem.EffectOnDrive e : effects.getEffects())
			{
				Symbol target = (Symbol) e.getTarget().clone();
				target.MakeGround(g.getAppliedSubstitutions());
				if(target.toString().equals(am.getName()))
				{
					expectedContribution = e.getValue();
					currentIntensity = GetIntensity(MotivatorType.ParseType(e.getDriveName()));
					if(e.getType() == ExpectedEffectType.ON_SELECT)
					{
						auxMultiplier = 1;
					}
					else if(e.getType() == ExpectedEffectType.ON_IGNORE)
					{
						auxMultiplier = -1;
					}
					result +=  auxMultiplier * MotivationalState.determineQuadraticNeedVariation(currentIntensity, expectedContribution);
				}
			}	
				
			float currentCompetenceIntensity = GetIntensity(MotivatorType.COMPETENCE);
			float expectedCompetenceContribution = PredictCompetenceChange(true);
			result += MotivationalState.determineQuadraticNeedVariation(currentCompetenceIntensity, expectedCompetenceContribution);
				
			float currentUncertaintyIntensity = GetIntensity(MotivatorType.CERTAINTY);
			//expected error assuming that the goal is successful
			float expectedError = 1 - g.getProbability(am);
			float currentError = g.getUncertainty(am);
			float expectedUncertaintyContribution = 10*(currentError - expectedError); 
			result += MotivationalState.determineQuadraticNeedVariation(currentUncertaintyIntensity,expectedUncertaintyContribution);	
								
	
		} catch (InvalidMotivatorTypeException e) {
			AgentLogger.GetInstance().log("EXCEPTION:" + e);
			e.printStackTrace();
		}


		return result;
	}
	
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g)
	{		
		float utility = am.getDeliberativeLayer().getUtilityStrategy().getUtility(am, g);
		float probability = am.getDeliberativeLayer().getProbabilityStrategy().getProbability(am, g);
		
		
		float EU = utility * probability + (1 + g.GetGoalUrgency());
		
		
		AgentLogger.GetInstance().intermittentLog("Goal: " + g.getName() + " Utilitity: " + utility + " Competence: " + probability +
				" Urgency: "+ g.GetGoalUrgency() + " Total: " + EU);
		return EU;
	}
	
	public float getUtility(AgentModel am, ActivePursuitGoal g)
	{
		return getContributionToNeeds(am,g);
	}
	
	public float getProbability(AgentModel am, ActivePursuitGoal g)
	{
		return getCompetence(am,g);
	}
	
	public float getCompetence(AgentModel am, ActivePursuitGoal g){
		float generalCompetence = GetIntensity(MotivatorType.COMPETENCE)/10;
		Float probability = g.GetProbability(am);
		
		if(probability != null){
			return (generalCompetence + probability.floatValue())/2;
		}else{
			//if there is no knowledge about the goal probability, the goal was never executed before
			//however, the agent assumes that he will be successful in achieving it 
			return (generalCompetence + 1)/2;
		}
	}
	
	

	/**
	 * Gets the current motivator with the highest need (i.e. the one with the lowest intensity)
	 * in the character's motivational state
	 * @return the motivator with the highest need or null if motivational state is empty
	 */
	// CURRENTLY NOT BEING USED
	public Motivator GetHighestNeedMotivator() {
		float maxNeed = 0;
		Motivator maxMotivator=null;
		
		for(int i = 0; i < _motivators.length; i++){
			if(_motivators[i].GetNeed() > maxNeed) {
				maxMotivator = _motivators[i];
				maxNeed = _motivators[i].GetNeed();
			}
		}
		
		return maxMotivator;
	}
	
	/**
	 * Gets the received motivator's intensity, i.e. the current level of the motivator
	 * @return a float value corresponding to the motivator's intensity
	 */
	public float GetIntensity(short type)
	{
		return _motivators[type].GetIntensity();
		
		/*if(agentName.equalsIgnoreCase(Constants.SELF)){
			return _selfMotivators[type].GetIntensity();
		}else{

			Motivator[] otherAgentMotivator = (Motivator[])_otherAgentsMotivators.get(agentName);
			
			if(otherAgentMotivator != null){
				return otherAgentMotivator[type].GetIntensity();
			}else{
				return 0;
			}
		}*/
	}
	
	/**
	 * Gets the received motivator's need
	 * @return a float value corresponding to the motivator's intensity
	 */
	/*public float GetNeed(String agentName, short type)
	{
		if(agentName.equalsIgnoreCase(_selfName)){
			return _selfMotivators[type].GetNeed();
		}else{
		
			
			Motivator[] otherAgentMotivator = (Motivator[])_otherAgentsMotivators.get(agentName);
		
			
			if(otherAgentMotivator != null){
				return otherAgentMotivator[type].GetNeed();
			}else{
				return 0;
			}
			
		}
	}*/
	
	/**
	 * Gets the motivator's urgency
	 * discretizing the need intensity into diffent categories 
	 * (very urgent, urgent, not urgent, satisfied)
	 * @return a multiplier corresponding to the motivator's urgency 
	 */
	public float GetNeedUrgency(String agentName, short type)
	{
		return _motivators[type].GetNeedUrgency();
	}
	
	
	
	/**
	 * Gets the received motivator's weight, i.e. how important is the motivator to the agent
	 * @return a float value corresponding to the motivator's weight
	 */
	public float GetWeight(short type)
	{	
		return _motivators[type].GetWeight();
	}
	
	
	/** 
	 * Calculates the agent's competence about a goal
	 * @param succeed - whether a goal has succeeded, true is success, and false is failure
	 */
	public void UpdateCompetence(boolean succeed)
	{
		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];
		//System.out.println("Competence before update" + competenceM.GetIntensity());
		
		competenceM.SetIntensity(newCompetence(succeed));
		//System.out.println("Competence after update" + competenceM.GetIntensity());
	}
	
	private float newCompetence(boolean succeed)
	{
		float alpha = 0.25f;
		int value = 0;
		float newCompetence;
		if(succeed)
		{
			value = 10;
		}
		
		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];
		
		newCompetence = alpha * value + (1 - alpha) * competenceM.GetIntensity();
		
		if(newCompetence < 1)
		{
			newCompetence = 1;
		}
		
		return newCompetence;
	}
	
	public float PredictCompetenceChange(boolean succeed)
	{
		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];
		return newCompetence(succeed) - competenceM.GetIntensity();
	}
	
	/**
	 * Update the agent's certainty value
	 * @param expectation - ranges from -1 to 1, -1 means complete violation of expectation while
	 * 						1 means complete fulfilment of expectation
	 * Changed the factor from 10 to 3 (Meiyii)
	 */
	public void UpdateCertainty(float expectation)
	{
		//System.out.println("Certainty before update" + _selfMotivators[MotivatorType.CERTAINTY].GetIntensity());
		_motivators[MotivatorType.CERTAINTY].UpdateIntensity(expectation*3);
		//System.out.println("Certainty after update" + _selfMotivators[MotivatorType.CERTAINTY].GetIntensity());
	}
	
	
	/**TODO find a decay formula
	 * Decays all needs according to the System Time
	 */
	public void decay(long currentTime) {

		if (currentTime >= _lastTime + 1000) {
			_lastTime = currentTime;
			
			
			//decay self motivators
			for(int i = 0; i < _motivators.length; i++){
				_motivators[i].DecayMotivator();
			}
			
		}
	}
	

	/**
	 * Converts the motivational state to XML
	 * @return a XML String that contains all information in the motivational state
	 */
	public String toXml() {
		String result;

		result = "<MotivationalState>";
		for(int i = 0; i < _motivators.length; i++){
			result = result + _motivators[i].toXml();
		}
		
		result = result + "</MotivationalState>";
		return result;
	}


	@Override
	public String name() {
		return MotivationalState.NAME;
	}


	@Override
	public void reset() {
		// TODO Auto-generated method stub
	
	}
	
	public void update(Event e, AgentModel am)
	{
		Event event2 = e.ApplyPerspective(am.getName());
		float result =  UpdateMotivators(am, event2, am.getDeliberativeLayer().getEmotionalPlanner().GetOperators());
		_appraisals.put(e.toString(), new Float(result));
	}

	@Override
	public void appraisal(Event e, AppraisalStructure as, AgentModel am) {
		Float desirability = _appraisals.get(e.toString());
		if(desirability != null)
		{
			as.SetAppraisalVariable(NAME, (short) 8, AppraisalStructure.DESIRABILITY, desirability.floatValue());
		}
	}


	@Override
	public void propertyChangedPerception(String ToM, Name propertyName,
			String value) {
		
	}


	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
	}


	@Override
	public void initialize(AgentModel am) {
		am.getDeliberativeLayer().setExpectedUtilityStrategy(this);
		am.getDeliberativeLayer().setProbabilityStrategy(this);
		am.getDeliberativeLayer().setUtilityStrategy(this);
		am.getDeliberativeLayer().addActionFailureStrategy(this);
		am.getDeliberativeLayer().addGoalFailureStrategy(this);
		am.getDeliberativeLayer().addGoalSuccessStrategy(this);
	}


	@Override
	public void update(AgentModel am) {
		_appraisals.clear();
	}


	@Override
	public void coping(AgentModel am) {
	}


	@Override
	public void emotionActivation(Event e, ActiveEmotion em, AgentModel am) {
	}


	@Override
	public void entityRemovedPerception(String entity) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public IComponent createModelOfOther() {
		MotivationalState ms = new MotivationalState();
		Motivator m2;
		
		for(Motivator m : _motivators)
		{
			m2 = new Motivator(m);
			m2.SetIntensity(5);
			ms.AddMotivator(m2);
		}
		
		return ms;
	}


	@Override
	public AgentDisplayPanel createComponentDisplayPanel(AgentModel am) {
		return new NeedsPanel(this);
	}


	@Override
	public void perceiveGoalSuccess(AgentModel am, ActivePursuitGoal g) {
		
		UpdateCompetence(true);
	    
	    //observed error = |realsuccess - estimation of success|
	    //given that the goal succeeded, the real success is 1 and the formula resumes to
	    //observed error = 1 - estimation of success 
	    float observedError = 1 - g.getProbability(am);
	    float previousExpectedError = g.getUncertainty(am);
	    
	    float newExpectedError = ActivePursuitGoal.alfa * observedError + (1 - ActivePursuitGoal.alfa) * previousExpectedError;
	    float deltaError = newExpectedError - previousExpectedError;
	    UpdateCertainty(-deltaError);
	    g.setUncertainty(am,newExpectedError);
	}


	@Override
	public void perceiveGoalFailure(AgentModel am, ActivePursuitGoal g) {
		//_numberOfGoalsTried++;
		UpdateCompetence(false);
		
	    //observed error = |estimation of success - realsuccess|
	    //given that the goal failed, the real success is none and the formula resumes to
	    //observed error = estimation of success - 0 (=) estimation of success
	    float observedError = g.getProbability(am);
	    float previousExpectedError = g.getUncertainty(am);
	    
	    float newExpectedError = ActivePursuitGoal.alfa * observedError + (1 - ActivePursuitGoal.alfa) * previousExpectedError;
	    float deltaError = newExpectedError - previousExpectedError;
	    UpdateCertainty(-deltaError);
	    g.setUncertainty(am, newExpectedError);
	}


	@Override
	public void perceiveActionFailure(AgentModel am, Step a) {
		//System.out.println("Calling UpdateCertainty (other's action: step completed)");
		UpdateCertainty(-a.getProbability(am));
	}
}