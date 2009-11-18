/** 
 * ActivePursuitGoal.java - Implements OCC's ActivePursuit goals that have activation
 * conditions
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 17/01/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 17/01/2004 - File created
 * João Dias: 24/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable
 * João Dias: 12/07/2006 - The check methods now receive a reference to the KnowledgeBase
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 * João Dias: 12/07/2006 - Replaced the deprecated Ground methods for the new ones.
 * João Dias: 12/07/2006 - the class is now Clonable 
 * João Dias: 31/08/2006 - Important conceptual change: Since we have now two types of memory,
 * 						   the KnowledgeBase (Semantic memory) and Autobiographical memory (episodic memory),
 * 						   and we have RecentEvent and PastEvent conditions that are searched in episodic
 * 						   memory (and the old conditions that are searched in the KB), it does not make 
 * 						   sense anymore to receive a reference to the KB in searching methods 
 * 						   (checkActivation, CheckSuccess, etc) for Goals. Since both the KB 
 * 						   and AutobiographicalMemory are singletons that can be accessed from any part of 
 * 						   the code, these methods do not need to receive any argument. It's up to each type
 * 						   of condition to decide which memory to use when searching for information.
 * João Dias: 07/09/2006 - Changed the test being performed in the methods CheckSuccessConditions and
 * 						   CheckFailureConditions. The previous test would allways return false if the condition
 * 						   to be grounded was not grounded. Now I'm trying to see if there any possible bindings
 * 						   that can make the condition true. This means that now is possible to have success and
 * 						   failure conditions that have unbound variables.
 * 						 - Fixed a small bug in the CheckActivation method that occurred when you had more than
 * 						   one possible SubstitutionSet for verifying a goal's precondition ex: {[X]/John} and 
 * 						   {[X]/Luke}. If the first substitution failed to achieve the condition, the method would
 * 						   return failure and would not test the next Substitution.
 * João Dias: 12/09/2006 - Fixed a bug in method CheckActivation introduced by the previous change
 * João Dias: 03/10/2006 - Important refactorization: the method CheckActivation() was moved from this
 * 						   class to the Condition, since it is indeed a very general method that can be 
 * 						   reused to test a list of preconditions. The only thing that the old method was
 * 						   using from an ActivePursuitGoal was the precondition list being tested. Therefore,
 * 						   this method has become a static method that receives as a parameter the list 
 * 						   of preconditions to test.
 * 						 - The active pursuit goal now uses the static CheckActivation(ArrayList) method
 * 						   from the class Condition to test its preconditions;
 * João Dias: 26/02/2008 - Added the expected contribution of the goal to PSI drives
 * João Dias: 11/06/2008 - Added the notion of uncertainty to goals, which represents the uncertainty the agent has
 * 						   about its knowledge of the goal. In other words, it corresponds to the estimated error on
 * 						   the estimation of the goal's likelihood of success. 
 */

package FAtiMA.deliberativeLayer.goals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import FAtiMA.AgentModel;
import FAtiMA.IntegrityValidator;
import FAtiMA.ModelOfOther;
import FAtiMA.conditions.Condition;
import FAtiMA.conditions.NewEventCondition;
import FAtiMA.conditions.RecentEventCondition;
import FAtiMA.culture.CulturalDimensions;
import FAtiMA.deliberativeLayer.plan.Effect;
import FAtiMA.deliberativeLayer.plan.EffectOnDrive;
import FAtiMA.deliberativeLayer.plan.IPlanningOperator;
import FAtiMA.deliberativeLayer.plan.Plan;
import FAtiMA.exceptions.InvalidMotivatorTypeException;
import FAtiMA.exceptions.UnreachableGoalException;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.MotivatorType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;


/**
 * Implements OCC's ActivePursuit goals that have activation
 * conditions
 * 
 * @author João Dias
 */
public class ActivePursuitGoal extends Goal implements IPlanningOperator {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final float alfa = 0.3f;
	
	protected boolean _active;
	protected ArrayList<Condition> _failureConditions;
	protected ArrayList<Condition> _preConditions;
	protected ArrayList<Condition> _successConditions;
	protected int _numberOfTries;
	protected Hashtable<String, Float> _expectedEffects;
		
	protected Float _probability = null;
	protected boolean _probabilityDetermined = false;
	protected Float _familiarity = null;
	protected Float _urgency = null;
	
	//attributes related to IPlanningOperator
	protected Integer _id;
	protected Symbol _agent;
	protected ArrayList<Effect> _effects;

	
	
	
	
	/**
	 * Creates a new ActivePursuitGoal
	 * @param description - the goal's name or description
	 */
	public ActivePursuitGoal(Name description) {
		super(description);
		
		_expectedEffects = new Hashtable<String,Float>();
		_preConditions = new ArrayList<Condition>(5);
		_successConditions = new ArrayList<Condition>(2);
		_failureConditions = new ArrayList<Condition>(2);
		_active = false;
		_numberOfTries = 0;
		
		//IPlanningOperator
		_agent = new Symbol(Constants.SELF);
		_effects = new ArrayList<Effect>();
		
	}

	protected ActivePursuitGoal() {
	}

	/**
	 * Adds a condition to the goal
	 * @param conditionType - the type of the condition: 
	 * 						  PreConditions
	 * 			  			  SuccessConditions
	 * 						  FailureConditions 
	 * @param cond - the condition to add
	 */
	public void AddCondition(String conditionType, Condition cond) {
		if (conditionType.equals("PreConditions"))
			_preConditions.add(cond);
		else if (conditionType.equals("SuccessConditions"))
			_successConditions.add(cond);
		else if (conditionType.equals("FailureConditions")) 
		    _failureConditions.add(cond);
	}
	
	
	
	/**
	 * Checks an ActivePursuitGoal's failure conditions
	 * if at least one of them is verified the goal fails
	 * @return true if the goal failed, false otherwise
	 */
	public boolean CheckFailure(AgentModel am) {
	    ListIterator<Condition> li;
		Condition cond;
		li = _failureConditions.listIterator();
		
		while (li.hasNext()) {
			cond = (Condition) li.next();
			if (cond.GetValidBindings(am) != null)
				return true;
		}
		return false;
	}
	
	/**
	 * Checks the integrity of the goal. For instance it checks if the goal's 
	 * success conditions are reachable by at least one action in the domain operators.
	 * If not it means that the goal will never be achieve and probably is a typo in
	 * the goal's definition (or in the actions file) 
	 * @param val - the validator used to check the goal
	 * @throws UnreachableGoalException - thrown if a goal's success conditions can never
	 * 									  be achieved because there is no operator with such
	 * 									  effects
	 */
	public void CheckIntegrity(IntegrityValidator val) throws UnreachableGoalException {
	    if(val.FindUnreachableConditions(_name.toString(),_successConditions)) {
	        throw new UnreachableGoalException(_name.toString());
	    }
	}

	/**
	 * Checks an ActivePursuitGoal's success conditions
	 * if all of them are verified the goal succeeds
	 * @return true if the goal succeeded, false otherwise
	 */
	public boolean CheckSucess(AgentModel am) {
	    ListIterator<Condition> li;
		Condition cond;
		li = _successConditions.listIterator();
		
		while (li.hasNext()) {
			cond = (Condition) li.next();
			if(cond.GetValidBindings(am) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean mayContainSelf()
	{
		ListIterator<Condition> li = this._successConditions.listIterator();
		Condition cond;
		while(li.hasNext())
		{
			cond = (Condition) li.next();
			if(cond.isGrounded())
			{
				if(cond.getName().toString().contains(Constants.SELF))
				{
					return true;
				}
			}
			else
			{
				return true;
			}
		}
		
		return false;
	}
	
	
	//TODO: Throw an exception instead of System.exit
	public void SetExpectedEffectOnDrive(String type, String driveName, String target, float value){

		if(!type.equalsIgnoreCase("OnSelect") && !type.equalsIgnoreCase("OnIgnore")){
			AgentLogger.GetInstance().logAndPrint("ERROR: Goal '"+ this +"' has an expected effect different from OnSelect or OnIgnore");
			AgentLogger.GetInstance().close();
			System.exit(1);
		}
		
		if(!driveName.equalsIgnoreCase("Affiliation") && !driveName.equalsIgnoreCase("Integrity") && !driveName.equalsIgnoreCase("Energy")){
			AgentLogger.GetInstance().logAndPrint("ERROR: Goal '"+ this +"' has an expected effect different from OnSelect or OnIgnore");
			AgentLogger.GetInstance().close();
			System.exit(1);			
		}
		
		if(!target.equalsIgnoreCase("[SELF]") && !target.equalsIgnoreCase("[target]")){
			AgentLogger.GetInstance().logAndPrint("ERROR: Goal '"+ this +"' has an expected effect different from OnSelect or OnIgnore");
			AgentLogger.GetInstance().close();
			System.exit(1);			
		}
		
		this._expectedEffects.put(new String(type+driveName+target), new Float(value));
	}
	
	//TODO: Throw an exception instead of System.exit
	public Float GetExpectedEffectOnDrive(String type, String driveName, String target){
		
		
		if(!type.equalsIgnoreCase("OnSelect") && !type.equalsIgnoreCase("OnIgnore")){
			AgentLogger.GetInstance().logAndPrint("ERROR: Goal '"+ this +"' has an expected effect different from OnSelect or OnIgnore");
			AgentLogger.GetInstance().close();
			System.exit(1);			
		}
		
		if(!driveName.equalsIgnoreCase("Affiliation") && !driveName.equalsIgnoreCase("Integrity")  &&  !driveName.equalsIgnoreCase("Energy")){
			AgentLogger.GetInstance().logAndPrint("ERROR: Goal '"+ this +"' has an expected effect different from OnSelect or OnIgnore");
			AgentLogger.GetInstance().close();
			System.exit(1);			
		}
		
		if(!target.equalsIgnoreCase("[SELF]") && !target.equalsIgnoreCase("[target]")){
			AgentLogger.GetInstance().logAndPrint("ERROR: Goal '"+ this +"' has an expected effect different from OnSelect or OnIgnore");
			AgentLogger.GetInstance().close();
			System.exit(1);			
		}
		
		Float res =(Float)this._expectedEffects.get(type+driveName+target);
		
		if(res == null){
			return new Float(0);	
		}else{
			return res;
		}
	}
	
	
	
	public int GetNumberOfTries()
	{
		return this._numberOfTries;
	}
	
	
	
	/**
	 * @author Samuel Mascarenhas
	 * @return the goal's urgency ranging from 0 (not urgent) to 1 (very urgent)
	 */
	private float GetGoalUrgency(){
		
		ListIterator<Condition> li;
		Condition cond;
		
		if(_urgency == null)
		{
			_urgency = new Float(0);
			li = this._preConditions.listIterator();
	    	while(li.hasNext())
	    	{
	    		cond = ((Condition) li.next());
	    		if(cond instanceof RecentEventCondition){
	    			_urgency = new Float(1);
	    		}
	    		if(cond instanceof NewEventCondition){
	    			_urgency = new Float(2);
	    			break;
	    		}
	    	}
		}
		
		return _urgency.floatValue();	
	}
	
	/*private float GetGoalFamiliarity(AgentModel am)
	{
		if (_familiarity == null)
		{
			_familiarity = new Float(am.getMemory().AssessGoalFamiliarity(this));
		}
		
		return _familiarity.floatValue();
	}*/
	
	public Float GetProbability(AgentModel am)
	{
		if(!_probabilityDetermined)
		{
			_probabilityDetermined = true;
			_probability = am.getMemory().getEpisodicMemory().AssessGoalProbability(this);
		}
		
		return _probability;
	}
	
	public void SetProbability(Float p)
	{
		_probability = p; 
	}
	
	public float getUncertainty(AgentModel am)
	{
		Float aux = (Float) am.getMemory().getSemanticMemory().AskProperty(this.getName());
		if(aux != null) return aux.floatValue();
		else return 0.0f;
	}
	
	public void setUncertainty(AgentModel am, float uncertainty)
	{	
		am.getMemory().getSemanticMemory().Tell(this.getName(), new Float(uncertainty));
	}
	
	
	public float getContributionToTargetNeeds(AgentModel am)
	{
		float result = 0;		
		
		List<Symbol> goalTargets = _name.GetLiteralList();
		Iterator<Symbol> it = goalTargets.iterator();
		it.next();// first literal is the name of the goal;
		
		while(it.hasNext()){
			String target = ((Symbol)it.next()).toString();
			result += this.getContributionToNeeds(am, target);			
		}
		return result;
	}

	
	public float getContributionToPersonalNeeds(AgentModel am)
	{
		return this.getContributionToNeeds(am, "SELF");
	}
	
	private double determineQuadraticNeedVariation(float currentIntensity, float expectedContribution){
		final float MAX_INTENSITY = 10;
		final float MIN_INTENSITY = 0;
		double result = 0;
		float finalIntensity;
		double currentIntensityNecessity;
		double finalIntensityNecessity;
		
		finalIntensity = currentIntensity + expectedContribution;
		finalIntensity = Math.min(finalIntensity, MAX_INTENSITY);
		finalIntensity = Math.max(finalIntensity, MIN_INTENSITY);
		
		currentIntensityNecessity = Math.pow(MAX_INTENSITY - currentIntensity,2); 
		finalIntensityNecessity = Math.pow(MAX_INTENSITY - finalIntensity,2);

		
		result = - (finalIntensityNecessity - currentIntensityNecessity); 
		return result;
	}
	
	
	private float getContributionToNeeds(AgentModel am, String target){
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
						expectedContribution = this.GetExpectedEffectOnDrive(effectTypes[c], nonCognitiveDrives[i], "[SELF]").floatValue();
						currentIntensity =  am.getMotivationalState().GetIntensity(MotivatorType.ParseType(nonCognitiveDrives[i]));
						result +=  auxMultiplier * this.determineQuadraticNeedVariation(currentIntensity, expectedContribution); 
					}
					auxMultiplier = -1;

				}
				
				float currentCompetenceIntensity = am.getMotivationalState().GetIntensity(MotivatorType.COMPETENCE);
				float expectedCompetenceContribution = am.getMotivationalState().PredictCompetenceChange(true);
				result += this.determineQuadraticNeedVariation(currentCompetenceIntensity, expectedCompetenceContribution);
				
				float currentUncertaintyIntensity = am.getMotivationalState().GetIntensity(MotivatorType.CERTAINTY);
				//expected error assuming that the goal is successful
				float expectedError = 1 - getProbability(am);
				float currentError = getUncertainty(am);
				float expectedUncertaintyContribution = 10*(currentError - expectedError); 
				result += this.determineQuadraticNeedVariation(currentUncertaintyIntensity,expectedUncertaintyContribution);	
								
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
							expectedContribution = this.GetExpectedEffectOnDrive(effectTypes[c], nonCognitiveDrives[i], "[target]").floatValue();
							currentIntensity =  m.getMotivationalState().GetIntensity(MotivatorType.ParseType(nonCognitiveDrives[i]));
							result += auxMultiplier * this.determineQuadraticNeedVariation(currentIntensity, expectedContribution); 		
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
	
	/*public float getContributionToPersonalNeeds(){
		float result = 0;
		float expectedAffiliationOnSelect = this.GetExpectedEffectOnDrive("OnSelect", "Affiliation", "[SELF]").floatValue();
		float expectedEnergyOnSelect = this.GetExpectedEffectOnDrive("OnSelect", "Energy", "[SELF]").floatValue();
		float expectedIntegrityOnSelect = this.GetExpectedEffectOnDrive("OnSelect", "Integrity", "[SELF]").floatValue();
		
		
		
		
		result += this.GetExpectedEffectOnDrive("OnSelect", "Affiliation", "[SELF]").floatValue() * MotivationalState.GetInstance().GetNeedUrgency(_agent.getName(),MotivatorType.AFFILIATION); 
		result += this.GetExpectedEffectOnDrive("OnSelect", "Energy", "[SELF]").floatValue() *  MotivationalState.GetInstance().GetNeedUrgency(_agent.getName(),MotivatorType.ENERGY);
		result += this.GetExpectedEffectOnDrive("OnSelect", "Integrity", "[SELF]").floatValue() * MotivationalState.GetInstance().GetNeedUrgency(_agent.getName(),MotivatorType.INTEGRITY);
		
		result += (-1) * this.GetExpectedEffectOnDrive("OnIgnore", "Affiliation", "[SELF]").floatValue(); 
		result += (-1) * this.GetExpectedEffectOnDrive("OnIgnore", "Energy", "[SELF]").floatValue();
		result += (-1) * this.GetExpectedEffectOnDrive("OnIgnore", "Integrity", "[SELF]").floatValue();
		
		
	    if(GetProbability() == null){
	    	// if the probability is null it means that the agent never tried to achieve this goal
	    	// so we cannot determine the contribution to competence (hence it's zero). However, in this case
	    	// the contribution to certainty will be the highest possible
	    	result += 10 * MotivationalState.GetInstance().GetNeedUrgency(_agent.getName(),MotivatorType.CERTAINTY);
	    }else{
		    result += 10 * getCompetence() * MotivationalState.GetInstance().GetNeedUrgency(_agent.getName(),MotivatorType.COMPETENCE);
		    result += 10 * getUncertainty() * MotivationalState.GetInstance().GetNeedUrgency(_agent.getName(),MotivatorType.CERTAINTY);
	    }
		
		return result;		
	}*/
	
	private float getCompetence(AgentModel am){
		float generalCompetence = am.getMotivationalState().GetIntensity(MotivatorType.COMPETENCE)/10;
		Float probability = GetProbability(am);
		
		if(probability != null){
			return (generalCompetence + probability.floatValue())/2;
		}else{
			//if there is no knowledge about the goal probability, the goal was never executed before
			//however, the agent assumes that he will be successful in achieving it 
			return (generalCompetence + 1)/2;
		}
	}
	
	
	public float GetExpectedUtility(AgentModel am)
	{		
		float contributionToSelf = getContributionToPersonalNeeds(am);
		float contributionOthers = getContributionToTargetNeeds(am);
		
		float culturalGoalUtility = CulturalDimensions.GetInstance().determineCulturalUtility(am, this,contributionToSelf,contributionOthers);		
		
		float EU = culturalGoalUtility * getCompetence(am) + (1 + this.GetGoalUrgency());
		
		
		AgentLogger.GetInstance().intermittentLog("Goal: " + this.getName() + " CulturalUtilitity: " + culturalGoalUtility + " Competence: " + this.getCompetence(am) +
				" Urgency: "+ this.GetGoalUrgency() + " Total: " + EU);
		return EU;
		
		
	}
	
	
	public void IncrementNumberOfTries()
	{
		this._numberOfTries++;
	}
	
	
	/**
	 * Gets the goal's failure conditions
	 * @return a list with the goal's failure conditions
	 */
	public ArrayList<Condition> GetFailureConditions() {
	    return _failureConditions;
	}
	
	/**
	 * Gets the goal's success conditions
	 * @return a list with the goal's success conditions 
	 */
	public ArrayList<Condition> GetSuccessConditions() {
		return _successConditions;
	}
	
	/**
	 * Gets the goal's preconditions
	 * @return a list with the goal's preconditions
	 */
	public ArrayList<Condition> GetPreconditions() {
		return _preConditions;
	}
	
	/**
     * @deprecated use ReplaceUnboundVariables(int) instead.
	 * Replaces all unbound variables in the object by applying a numeric
	 * identifier to each one.
	 * Example: the variable [X] becomes [X4] if the received ID is 4.
	 * @param variableID - the identifier to be applied
	 * @return a new Goal with the variables changed 
	 */
	public Object GenerateName(int id)
	{
		ActivePursuitGoal aux = (ActivePursuitGoal) this.clone();
		aux.ReplaceUnboundVariables(id);
		return aux;
	}
	
	
	/**
	 * Replaces all unbound variables in the object by applying a numeric 
     * identifier to each one. For example, the variable [x] becomes [x4]
     * if the received ID is 4. 
     * Attention, this method modifies the original object.
     * @param variableID - the identifier to be applied
	 */
    public void ReplaceUnboundVariables(int variableID)
    {
    	ListIterator<Condition> li;
    	
    	this._name.ReplaceUnboundVariables(variableID);
    	
    	li = this._preConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).ReplaceUnboundVariables(variableID);
    	}
    	
    	li = this._failureConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).ReplaceUnboundVariables(variableID);
    	}
    	
    	li = this._successConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).ReplaceUnboundVariables(variableID);
    	}
    	
    	for(Effect e : this._effects)
    	{
    		e.ReplaceUnboundVariables(variableID);
    	}
    }
    
    /**
     * @deprecated use the method MakeGround(ArrayList) instead
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)".
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @return a new Goal with the substitutions applied
	 * @see Substitution
	 */
	public Object Ground(ArrayList<Substitution> bindingConstraints) 
	{
		ActivePursuitGoal aux = (ActivePursuitGoal) this.clone();
		aux.MakeGround(bindingConstraints);
		return aux;
	}

	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void MakeGround(ArrayList<Substitution> bindings)
    {
    	ListIterator<Condition> li;
    	
    	this._name.MakeGround(bindings);
    	
    	li = this._preConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).MakeGround(bindings);
    	}
    	
    	li = this._failureConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).MakeGround(bindings);
    	}
    	
    	li = this._successConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).MakeGround(bindings);
    	}
    	
    	for(Effect e : this._effects)
    	{
    		e.MakeGround(bindings);
    	}
    }
    
    
    public String getNameWithCharactersOrdered(){
    	ArrayList<Symbol> nameLiterals = this.getName().GetLiteralList();
		ArrayList<String> characterNames = new ArrayList<String>();
		
		String ritualName = nameLiterals.get(0).toString();
		
		// i starts at 1 because the literal 0 is the name of the ritual
		for(int i = 1; i < nameLiterals.size(); i++ ){ 
			characterNames.add(nameLiterals.get(i).toString());
		}
		
		Collections.sort(characterNames);
		
		String orderedName = new String(ritualName);
		
		orderedName = orderedName + "(";

		for(int i=0; i < characterNames.size(); i++)
		{
			orderedName += characterNames.get(i).toString();
			orderedName +=",";
		}
		if(characterNames.size() > 0)
		{
			orderedName = orderedName.substring(0,orderedName.length()-1);
		}
		orderedName += ")";
		
		
		return orderedName;
    }
   
    /**
     * @deprecated use the method MakeGround(Substitution) instead
	 * Applies a substitution to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)".
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @return a new Goal with the substitution applied
	 * @see Substitution
	 */
	public Object Ground(Substitution subst)
	{
		ActivePursuitGoal aux = (ActivePursuitGoal) this.clone();
		aux.MakeGround(subst);
		return aux;
	}

	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void MakeGround(Substitution subst)
    {
    	ListIterator<Condition> li;
    	
    	this._name.MakeGround(subst);
    	
    	li = this._preConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).MakeGround(subst);
    	}
    	
    	li = this._failureConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).MakeGround(subst);
    	}
    	
    	li = this._successConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).MakeGround(subst);
    	}
    	
    	for(Effect e : this._effects)
    	{
    		e.MakeGround(subst);
    	}
    }
	
	/**
	 * Clones this ActivePursuitGoal, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The Goal's copy.
	 */
	public Object clone()
	{
		ListIterator<Condition> li;
		ActivePursuitGoal g = new ActivePursuitGoal();
		g._goalID = this._goalID;
		g._active = this._active;
		g._name = (Name) this._name.clone();
		g._baseIOF = this._baseIOF;
		g._baseIOS = this._baseIOS;
		g._dynamicIOF = (Name) this._dynamicIOF.clone();
		g._dynamicIOS = (Name) this._dynamicIOS.clone();
		
		g._numberOfTries = this._numberOfTries;
	
		g._expectedEffects = new Hashtable<String,Float>(this._expectedEffects);
		
		if(this._preConditions != null)
		{
			g._preConditions = new ArrayList<Condition>(this._preConditions.size());
			li = this._preConditions.listIterator();
			while(li.hasNext())
			{
				g._preConditions.add((Condition) li.next().clone());
			}
		}
		
		if(this._failureConditions != null)
		{
			g._failureConditions = new ArrayList<Condition>(this._failureConditions.size());
			li = this._failureConditions.listIterator();
			while(li.hasNext())
			{
				g._failureConditions.add((Condition) li.next().clone());
			}
		}
		
		if(this._successConditions != null)
		{
			
			g._successConditions = new ArrayList<Condition>(this._successConditions.size());
			li = this._successConditions.listIterator();
			while(li.hasNext())
			{
				g._successConditions.add((Condition) li.next().clone());
			}
		}
		
	
		g._probability = this._probability;
		g._probabilityDetermined = this._probabilityDetermined;
		g._familiarity = this._familiarity;
		g._urgency = this._urgency;
		
		
		//IPlanningOperators attributes
		g._agent = this._agent;
		g._id = this._id;
		
		if(this._effects != null)
		{
			
			g._effects = new ArrayList<Effect>(this._effects.size());
			for(Effect e : this._effects)
			{
				g._effects.add((Effect)e.clone());
			}
		}
		
		return g;
		
	}
	
	public ArrayList<Plan> getPlans(AgentModel am)
	{
		return null;
	}
	
	/**
	 * Converts the ActivePursuitGoal to a String
	 * @return the converted String
	 */
	public String toString() {
		return "ActivePursuitGoal: " + super.toString(); 
	}
	
	//IPlanningOperator methods
	
	public void addEffect(Effect e)
	{
		_effects.add(e);
	}

	public boolean checkPreconditions(AgentModel am) {
		ListIterator<Condition> li;
		li = this._preConditions.listIterator();
		
		while(li.hasNext()) {
			if (!((Condition) li.next()).CheckCondition(am)) return false;
		}
		return true;
	}
	
	public float getProbability(AgentModel am)
	{
		Float f = this.GetProbability(am);
		if(f != null)
		{
			return f.floatValue();
		}
		else return 1;
	}

	public boolean equals(IPlanningOperator op) {
		return this._id.equals(op.getID());
	}

	public Symbol getAgent() {
		return this._agent;
	}

	public Effect getEffect(Integer effectID) {
		return (Effect) this._effects.get(effectID.intValue());
	}

	public ArrayList<Effect> getEffects() {
		return this._effects;
	}

	public Integer getID() {
		return _id;
	}

	public Condition getPrecondition(Integer preconditionID) {
		return (Condition) this._preConditions.get(preconditionID.intValue());
	}

	public ArrayList<Condition> getPreconditions() {
		return this._preConditions;
	}

	public void setID(Integer id) {
		this._id = id;		
	}
	
	public void setUrgency(float urgency)
	{
		this._urgency = new Float(urgency);
	}

	public void updateEffectsProbability(AgentModel am) {
		//this method is not applied to Goals!
	}

	public ArrayList<EffectOnDrive> getEffectsOnDrives() {
		//TODO return the appropriate effects on drives
		return new ArrayList<EffectOnDrive>();
	}
}
