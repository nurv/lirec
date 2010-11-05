/*
 * MotivatorState.java - Represents the character's motivational state
 */

package FAtiMA.motivationalSystem;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.AgentCore;
import FAtiMA.AgentModel;
import FAtiMA.AgentSimulationTime;
import FAtiMA.IComponent;
import FAtiMA.Display.AgentDisplayPanel;
import FAtiMA.ToM.ModelOfOther;
import FAtiMA.deliberativeLayer.IExpectedUtilityStrategy;
import FAtiMA.deliberativeLayer.IProbabilityStrategy;
import FAtiMA.deliberativeLayer.IUtilityForTargetStrategy;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.deliberativeLayer.plan.EffectOnDrive;
import FAtiMA.deliberativeLayer.plan.IPlanningOperator;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.Appraisal;
import FAtiMA.emotionalState.AppraisalVector;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;
import FAtiMA.wellFormedNames.Unifier;

/**
 * Implements the character's motivational state.
 * 
 * @author Meiyii Lim, Samuel Mascarenhas 
 */

public class MotivationalState implements Serializable, Cloneable, IComponent, IExpectedUtilityStrategy, IProbabilityStrategy, IUtilityForTargetStrategy {
	
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME ="MotivationalState";
	
	protected Motivator[]  _motivators;
	//protected Hashtable<String,Motivator[]> _otherAgentsMotivators;
	protected long _lastTime;
	protected int _goalTried;
	protected int _goalSucceeded;

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
	public AppraisalVector UpdateMotivators(AgentModel am, Event e, ArrayList<? extends IPlanningOperator> operators)
	{
		ArrayList<Substitution> substitutions;
		IPlanningOperator operator;
		Step action;
		EffectOnDrive effectOnDrive;
		MotivatorCondition motCondition;
		String eventSubject = e.GetSubject();
		String eventTarget = e.GetTarget();
		float deviation = 0;
		double contributionToNeed =0f;
		float contributionToSubjectNeeds=0f;
		float contributionToTargetNeeds=0f;
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
					
					//TODO ver com o samuel isto dos target needs

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
						
						if(eventSubject.equalsIgnoreCase(target.toString())){
							contributionToSubjectNeeds += contributionToNeed;
						}else{
							contributionToTargetNeeds += contributionToNeed;
						}
					}			
				}	
			}			
		}
		
		AppraisalVector vec = new AppraisalVector();
		vec.setAppraisalVariable(AppraisalVector.DESIRABILITY, contributionToSelfNeeds);
		
		return vec;
		
		
	}
	
	public float getContributionToNeeds(AgentModel am, ActivePursuitGoal g, String target){
		float result = 0;
		String[] effectTypes = {"OnSelect","OnIgnore"};
		String[] nonCognitiveDrives = {"Affiliation","Integrity","Energy"};
		float expectedContribution;
		float currentIntensity = 0;
		float auxMultiplier; // this is used for the effects that are OnIgnore
			
		try {
			
				
			// If target is SELF
			if(target.equalsIgnoreCase(Constants.SELF)){
				auxMultiplier = 1;
				//Calculate the effect on Non-Cognitive Needs
				for (int c = 0; c < effectTypes.length; c++ ){
					
					for(int i = 0; i < nonCognitiveDrives.length; i++){
						expectedContribution = g.GetExpectedEffectOnDrive(effectTypes[c], nonCognitiveDrives[i], "[SELF]").floatValue();
						currentIntensity =  am.getMotivationalState().GetIntensity(MotivatorType.ParseType(nonCognitiveDrives[i]));
						result +=  auxMultiplier * MotivationalState.determineQuadraticNeedVariation(currentIntensity, expectedContribution); 
					}
					auxMultiplier = -1;

				}
				
				float currentCompetenceIntensity = am.getMotivationalState().GetIntensity(MotivatorType.COMPETENCE);
				float expectedCompetenceContribution = am.getMotivationalState().PredictCompetenceChange(true);
				result += MotivationalState.determineQuadraticNeedVariation(currentCompetenceIntensity, expectedCompetenceContribution);
				
				float currentUncertaintyIntensity = am.getMotivationalState().GetIntensity(MotivatorType.CERTAINTY);
				//expected error assuming that the goal is successful
				float expectedError = 1 - g.getProbability(am);
				float currentError = g.getUncertainty(am);
				float expectedUncertaintyContribution = 10*(currentError - expectedError); 
				result += MotivationalState.determineQuadraticNeedVariation(currentUncertaintyIntensity,expectedUncertaintyContribution);	
								
			}
			else{
			// If target is NOT SELF
			// Only the non-cognitive needs are taken into account for other agents. This is because his actions cannot impact those needs.
				
				if(am.getToM().containsKey(target))
				{
					auxMultiplier = 1;
					ModelOfOther m = am.getToM().get(target);
					
					//Calculate the effect on Non-Cognitive Needs
					for (int c = 0; c < effectTypes.length; c++ ){
						for(int i = 0; i < nonCognitiveDrives.length; i++){
							expectedContribution = g.GetExpectedEffectOnDrive(effectTypes[c], nonCognitiveDrives[i], "[target]").floatValue();
							currentIntensity =  GetIntensity(MotivatorType.ParseType(nonCognitiveDrives[i]));
							result += auxMultiplier * MotivationalState.determineQuadraticNeedVariation(currentIntensity, expectedContribution); 		
						}
						auxMultiplier = -1;
					}		
				}
			}
		} catch (InvalidMotivatorTypeException e) {
			AgentLogger.GetInstance().log("EXCEPTION:" + e);
			e.printStackTrace();
		}


		return result;
	}
	
	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g)
	{		
		float utility = am.getDeliberativeLayer().getUtilityForTargetStrategy().getUtilityForTarget(Constants.SELF, am, g);
		float probability = am.getDeliberativeLayer().getProbabilityStrategy().getProbability(am, g);
		
		
		float EU = utility * probability + (1 + g.GetGoalUrgency());
		
		
		AgentLogger.GetInstance().intermittentLog("Goal: " + g.getName() + " Utilitity: " + utility + " Competence: " + probability +
				" Urgency: "+ g.GetGoalUrgency() + " Total: " + EU);
		return EU;
	}
	
	public float getUtilityForTarget(String target, AgentModel am, ActivePursuitGoal g)
	{
		return getContributionToNeeds(am,g,target);
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

	@Override
	public AppraisalVector appraisal(Event e, AgentModel am) {
		Event event2 = e.ApplyPerspective(am.getName());
		return UpdateMotivators(am, event2, am.getDeliberativeLayer().getEmotionalPlanner().GetOperators());	
	}


	@Override
	public void propertyChangedPerception(String ToM, Name propertyName,
			String value) {
		
	}


	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
	}


	@Override
	public void initialize(AgentCore am) {
		
		am.getDeliberativeLayer().setExpectedUtilityStrategy(this);
		am.getDeliberativeLayer().setProbabilityStrategy(this);
		am.getDeliberativeLayer().setUtilityForTargetStrategy(this);
	}


	@Override
	public void update(AgentModel am) {
	}


	@Override
	public void coping(AgentModel am) {
	}


	@Override
	public AppraisalVector composedAppraisal(Event e, AppraisalVector v,
			AgentModel am) {
		return null;
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
}
