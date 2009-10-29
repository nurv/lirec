/*
 * MotivatorState.java - Represents the character's motivational state
 */

package FAtiMA.motivationalSystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;

import FAtiMA.AgentModel;
import FAtiMA.AgentSimulationTime;
import FAtiMA.conditions.MotivatorCondition;
import FAtiMA.culture.CulturalDimensions;
import FAtiMA.deliberativeLayer.plan.EffectOnDrive;
import FAtiMA.deliberativeLayer.plan.IPlanningOperator;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.emotionalState.Appraisal;
import FAtiMA.emotionalState.AppraisalVector;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.exceptions.InvalidMotivatorTypeException;
import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.sensorEffector.Event;
import FAtiMA.sensorEffector.Parameter;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.enumerables.MotivatorType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;
import FAtiMA.wellFormedNames.Unifier;

/**
 * Implements the character's motivational state. You cannot create an MotivatorState, 
 * since there is one and only one instance of the MotivatorState for the agent. 
 * If you want to access it use MotivatorState.GetInstance() method.
 * 
 * @author Meiyii Lim, Samuel Mascarenhas 
 */

public class MotivationalState implements Serializable {
	
	
	private static final long serialVersionUID = 1L;
	protected String _selfName;
	protected Motivator[]  _selfMotivators;
	protected Hashtable _otherAgentsMotivators;
	protected long _lastTime;
	protected int _goalTried;
	protected int _goalSucceeded;

	/**
 	 * Singleton pattern 
	 */
	private static MotivationalState _motStateInstance = null;


	/**
	 * Creates an empty MotivationalState
	 */
	public MotivationalState() {
		_selfMotivators = new Motivator[MotivatorType.numberOfTypes()];
		_otherAgentsMotivators = new Hashtable();
		_goalTried = 0;
		_goalSucceeded = 0;
		_lastTime = AgentSimulationTime.GetInstance().Time();
	}

	public Hashtable getOtherAgentsMotivators(){
		return _otherAgentsMotivators;
	}
	
	public Motivator[] getSelfMotivators(){
		return _selfMotivators;
	}
	
	
	public void InitializeOtherAgentMotivators(String agentName){
		Motivator[]  agentMotivators = new Motivator[5];
				
		for(int i=0; i < agentMotivators.length; i++){
			agentMotivators[i] = new Motivator(_selfMotivators[i]);
			agentMotivators[i].SetIntensity(5);
		}
		
		_otherAgentsMotivators.put(agentName, agentMotivators);
	}
	
	
	/** 
	 * Adds a motivator to the MotivationalState
	 */
	public void AddSelfMotivator(String characterName, Motivator motivator)
	{
		_selfName = new String(characterName);
		_selfMotivators[motivator.GetType()] = new Motivator(motivator);
	}
	
	
	public Motivator GetSelfMotivator(short motivatorType){
		return _selfMotivators[motivatorType];
	}
	
	
	
	/** 
	 * Updates the intensity of the motivators based on the event received
	 * @throws InvalidMotivatorTypeException 
	 */
	public void UpdateMotivators(AgentModel am, Event e, ArrayList operators)
	{
		ArrayList substitutions;
		IPlanningOperator operator;
		Step action;
		EffectOnDrive effectOnDrive;
		MotivatorCondition motCondition;
		String eventSubject = e.GetSubject();
		String eventTarget = e.GetTarget();
		float contributionToNeed = 0;
		float contributionToSubjectNeeds=0f;
		float contributionToTargetNeeds=0f;
	    float contributionToSelfNeeds = 0f;  //used for events performed by the agent
		
		
		
		//LSignal an ReplyLSignal Events Update The Motivatores Using The Cultural Dimensions
		if(e.GetAction().equalsIgnoreCase("LSignal")
				|| e.GetAction().equalsIgnoreCase("ReplyLSignal")){
			
			
			String lSignalName = ((Parameter)e.GetParameters().get(1)).toString();
			Name lSignalValueProperty = Name.ParseName(lSignalName + "(value)");
			float lSignalValue = ((Float)am.getMemory().AskProperty(lSignalValueProperty)).floatValue();
			
			
			float affiliationEffect = CulturalDimensions.GetInstance().determineAffiliationEffectFromLSignal(eventSubject,eventTarget,lSignalName,lSignalValue);
			
			
			if(eventTarget.equalsIgnoreCase(am.getName())){		
				contributionToSelfNeeds += _selfMotivators[MotivatorType.AFFILIATION].UpdateIntensity(affiliationEffect);
				
			}else{
				Motivator[] otherAgentsMotivators = (Motivator[]) _otherAgentsMotivators.get(eventTarget.toString());
				if(otherAgentsMotivators != null){
					otherAgentsMotivators[MotivatorType.AFFILIATION].UpdateIntensity(affiliationEffect);
				}
			}	
		}
		
		
		//Other Events Update The Motivators According to The Effects Specified By the Author in The Actions.xml 
		for(ListIterator li = operators.listIterator(); li.hasNext();)
		{
			
			operator = (IPlanningOperator) li.next();
			if(operator instanceof Step)
			{
				action = (Step) operator;
				substitutions = Unifier.Unify(e.toStepName(),action.getName());
				if(substitutions != null)
				{

					substitutions.add(new Substitution(new Symbol("[AGENT]"),new Symbol(e.GetSubject())));
					action = (Step) action.clone();
					action.MakeGround(substitutions);

					for(ListIterator li2 = action.getEffectsOnDrives().listIterator(); li2.hasNext();)
					{
						effectOnDrive = (EffectOnDrive) li2.next();
						motCondition = (MotivatorCondition) effectOnDrive.GetEffectOnDrive();
						Name target = motCondition.GetTarget();

						if (target.toString().equalsIgnoreCase("SELF"))
						{
							AgentLogger.GetInstance().log("Updating self motivator " + motCondition.GetDrive());
							try {
								short driveType = MotivatorType.ParseType(motCondition.GetDrive());
								contributionToNeed = _selfMotivators[driveType].UpdateIntensity(motCondition.GetEffect());
								contributionToSelfNeeds += contributionToNeed;
							} catch (InvalidMotivatorTypeException e1) {
								e1.printStackTrace();
							}
						}else{
							try {
								short driveType = MotivatorType.ParseType(motCondition.GetDrive());
								Motivator[] otherAgentsMotivators = (Motivator[]) _otherAgentsMotivators.get(target.toString());		
								contributionToNeed = otherAgentsMotivators[driveType].UpdateIntensity(motCondition.GetEffect());
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
		
	
	
		
		this.updateEmotionalState(am, e, contributionToSelfNeeds, contributionToSubjectNeeds, contributionToTargetNeeds);
		
	}

	
	
	private void updateEmotionalState(AgentModel am, Event e, float contributionToSelfNeeds, float contributionToSubjectNeeds, float contributionToTargetNeeds) {
		ArrayList emotions;
		BaseEmotion em;
		
		float praiseWorthiness = CulturalDimensions.GetInstance().determinePraiseWorthiness( contributionToSubjectNeeds,contributionToTargetNeeds);
		
		AppraisalVector vec = new AppraisalVector();
		vec.setAppraisalVariable(AppraisalVector.DESIRABILITY, contributionToSelfNeeds);
		vec.setAppraisalVariable(AppraisalVector.PRAISEWORTHINESS, praiseWorthiness);
	
		emotions = Appraisal.GenerateEmotions(am, e, vec, null);
		
		
		ListIterator li = emotions.listIterator();
		while(li.hasNext())
		{
			em = (BaseEmotion) li.next();
			am.getEmotionalState().AddEmotion(em, am);
		}
		

	}

	/**
	 * Gets the current motivator with the highest need (i.e. the one with the lowest intensity)
	 * in the character's motivational state
	 * @return the motivator with the highest need or null if motivational state is empty
	 */
	// CURRENTLY NOT BEING USED
	public Motivator GetSelfHighestNeedMotivator() {
		float maxNeed = 0;
		Motivator currentMotivator;
		Motivator maxMotivator=null;
		
		for(int i = 0; i < _selfMotivators.length; i++){
			if(_selfMotivators[i].GetNeed() > maxNeed) {
				maxMotivator = _selfMotivators[i];
				maxNeed = _selfMotivators[i].GetNeed();
			}
		}
		
		return maxMotivator;
	}
	
	/**
	 * Gets the received motivator's intensity, i.e. the current level of the motivator
	 * @return a float value corresponding to the motivator's intensity
	 */
	public float GetIntensity(String agentName, short type)
	{
		if(agentName.equalsIgnoreCase(_selfName)){
			return _selfMotivators[type].GetIntensity();
		}else{

			Motivator[] otherAgentMotivator = (Motivator[])_otherAgentsMotivators.get(agentName);
			
			if(otherAgentMotivator != null){
				return otherAgentMotivator[type].GetIntensity();
			}else{
				return 0;
			}
		}
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
		if(agentName.equalsIgnoreCase(_selfName)){
			return _selfMotivators[type].GetNeedUrgency();
		}else{
		
			
			Motivator[] otherAgentMotivator = (Motivator[])_otherAgentsMotivators.get(agentName);
		
			
			if(otherAgentMotivator != null){
				return otherAgentMotivator[type].GetNeedUrgency();
			}else{
				return 0;
			}
			
		}
	}
	
	
	
	/**
	 * Gets the received motivator's weight, i.e. how important is the motivator to the agent
	 * @return a float value corresponding to the motivator's weight
	 */
	public float GetWeight(String agentName, short type)
	{
		if(agentName.equalsIgnoreCase(_selfName)){
			return _selfMotivators[type].GetWeight();
		}else{
			return ((Motivator[])_otherAgentsMotivators.get(agentName))[type].GetWeight();
		}
	}
	
	
	/** 
	 * Calculates the agent's competence about a goal
	 * @param succeed - whether a goal has succeeded, true is success, and false is failure
	 */
	public void UpdateCompetence(boolean succeed)
	{
		Motivator competenceM = _selfMotivators[MotivatorType.COMPETENCE];
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
		
		Motivator competenceM = _selfMotivators[MotivatorType.COMPETENCE];
		
		newCompetence = alpha * value + (1 - alpha) * competenceM.GetIntensity();
		
		if(newCompetence < 1)
		{
			newCompetence = 1;
		}
		
		return newCompetence;
	}
	
	public float PredictCompetenceChange(boolean succeed)
	{
		Motivator competenceM = _selfMotivators[MotivatorType.COMPETENCE];
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
		_selfMotivators[MotivatorType.CERTAINTY].UpdateIntensity(expectation*3);
		//System.out.println("Certainty after update" + _selfMotivators[MotivatorType.CERTAINTY].GetIntensity());
	}
	
	
	/**TODO find a decay formula
	 * Decays all needs according to the System Time
	 */
	public void Decay() {

		long currentTime = AgentSimulationTime.GetInstance().Time();;
		if (currentTime >= _lastTime + 1000) {
			_lastTime = currentTime;
			
			
			//decay self motivators
			for(int i = 0; i < _selfMotivators.length; i++){
				_selfMotivators[i].DecayMotivator();
			}
			
			
			//decay other motivators
			
			Collection listOfOtherAgentsMotivators = _otherAgentsMotivators.values();

			Iterator it;
		
			it = listOfOtherAgentsMotivators.iterator();
			
			while (it.hasNext()){
				Motivator[] otherAgentsMotivators = (Motivator[]) it.next();
				for(int i = 0; i < _selfMotivators.length; i++){
					otherAgentsMotivators[i].DecayMotivator();
				}	
			}
		}
	}
	

	/**
	 * Converts the motivational state to XML
	 * @return a XML String that contains all information in the motivational state
	 */
	public String toXml() {
		String result;
		Iterator it;

		result = "<MotivationalState>";
		for(int i = 0; i < _selfMotivators.length; i++){
			result = result + _selfMotivators[i].toXml();
		}
		
		result = result + "</MotivationalState>";
		return result;
	}
}
