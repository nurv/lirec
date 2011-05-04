/** 
 * Step.java - Implements a Step/Action/Operator in the Plan. Represented similarly to a STRIPS operator
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
 * Created: 11/01/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 11/01/2004 - File created
 * João Dias: 16/05/2006 - Added getPrecondition(Integer) method
 * João Dias: 16/05/2006 - ID changed from int to Integer
 * João Dias: 17/05/2006 - Added the clone() Method
 * João Dias: 22/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated 
 * João Dias: 21/09/2006 - Added the attribute _agent which represents the name
 * 						   the agent that is going to execute the action. This allows
 * 						   the planner to consider actions of other agents
 * 						 - Added probability to the execution of actions. Only used when
 * 						   the step corresponds to an action of another character.
 * 						 - Added method UpdateEffectsProbability. This functionality was
 * 						   wrongly placed inside the class EmotionalPlanner.
 * João Dias: 02/10/2006 - Added the method getEffect(Integer id) which is analog to method
 * 						   getPrecondition(Integer id)
 * João Dias: 03/10/2006 - Removed the restriction that you can only use variables in step's
 * 						   definition that are also used in the step's name. However, if you 
 * 					       use a variable not defined in the name, you will still get a warning.
 * João Dias: 04/10/2006 - Added the attribute selfExecutable that indicates if the action can
 * 						   be executable by the Agent or not.
 * 						 - Added corresponding SetSelfExecutable(boolean) method
 * João Dias: 22/12/2006 - The UpdateEffectsProbability method now only updates the probabilities
 * 						   of grounded effects. Effects with unbound variables are not updated 
 * 						   at the moment. I'm considering revising this latter.
 */

package FAtiMA.Core.plans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IntegrityValidator;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.exceptions.UnknownSpeechActException;
import FAtiMA.Core.exceptions.UnspecifiedVariableException;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;


/**
 * Represents a plan action/step/operator. Based in Strips, but the effects 
 * have associated probabilities.
 * 
 * @author João Dias
 */

public class Step implements IPlanningOperator, Cloneable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer _id;
	
	private Name _name;
	private String _key;
	private Symbol _agent;
	private ArrayList<Condition> _preconditions;
	private ArrayList<Effect> _effects;
	
	private float _baseprob;
	
	private boolean _selfExecutable;

	/**
	 * Creates a new Step
	 * @param agent - the name of the agent that executes the action
	 * @param action - the name of the action or step
	 * @param probability - the likelihood of the action's execution
	 * 						 	by another agent 
	 */
	public Step(Symbol agent, Name action, float probability) {
		_agent = agent;
		_name = action;
		_key = action.toString();
		_effects = new ArrayList<Effect>(3);
		_preconditions = new ArrayList<Condition>(3);
		
		_selfExecutable = (!_agent.isGrounded()) || 
				_agent.toString().equals(Constants.SELF);
		
		_baseprob = probability;
	}

	/**
	 * Creates a new Step
	 * @param agent - the name of the agent that is going to execute the action
	 * @param action - the name of the action or step
	 * @param preconditions - the step's preconditions (a list of conditions)
	 * @param effects - the step's effects (a list of effects)
	 * @param probability - the likelihood of the action's execution
	 * 						by another agent 
	 */
	public Step(Symbol agent, Name action, float probability, ArrayList<Condition> preconditions, ArrayList<Effect> effects) {
		_agent = agent;
		_name = action;
		_key = action.toString();
		_effects = effects;
		_preconditions = preconditions;
		_baseprob = probability;
		
		_selfExecutable = !_agent.isGrounded() || 
		_agent.toString().equals(Constants.SELF);
	}

	private Step() {
	}
	
	public String getKey(){
		return this._key;
	}

	/**
	 * Adds an effect to the Step
	 * @param effect - the Effect to Add
	 */
	public void AddEffect(Effect effect) {
		_effects.add(effect);
	}
	

	/**
	 * Adds a precondition to the Step
	 * @param cond - the precondition to add
	 */
	public void AddPrecondition(Condition cond) {
		_preconditions.add(cond);
	}
	
	private Name GetBiasName()
	{
		String name = "ProbBias(" + _agent;
		for(Symbol s : _name.GetLiteralList())
		{
			if(s.isGrounded())
			{
				name = name + "," + s;
			}
			
		}
		name = name + ")";
		return Name.ParseName(name);
	}
	
	/**
	 * Decreases a Step's probability of execution by a fixed 
	 * ammount. 
	 */
	public void DecreaseProbability(AgentModel am) {
		Float bias;
		float prob;
		float newprob;
		float newbias;
		
		bias = (Float) am.getMemory().getSemanticMemory().AskProperty(GetBiasName());
		if(bias != null)
		{
			prob = bias.floatValue() + _baseprob;
		}
		else
		{
			prob = _baseprob;
		}
		
		newprob = 0.6f * prob;
		newbias = newprob - _baseprob;
		
		am.getMemory().getSemanticMemory().Tell(GetBiasName(),new Float(newbias));   
	}
	
	/**
	 * Increases a Step's probability of execution by a fixed ammount.
	 */
	public void IncreaseProbability(AgentModel am) {
		Float bias;
		float prob;
		float newprob;
		float newbias;
		
		bias = (Float) am.getMemory().getSemanticMemory().AskProperty(GetBiasName());
		if(bias != null)
		{
			prob = bias.floatValue() + _baseprob;
		}
		else
		{
			prob = _baseprob;
		}
		
		newprob = 0.6f * prob + 0.4f;
		newbias = newprob - _baseprob;
		am.getMemory().getSemanticMemory().Tell(GetBiasName(),new Float(newbias));   
	}
	
	/**
	 * Gets the Step's probability of execution
	 * @return the steps's probability
	 */
	public float getProbability(AgentModel am) {
		
		if(!_selfExecutable)
		{
			Float aux = (Float) am.getMemory().getSemanticMemory().AskProperty(GetBiasName());
			if(aux != null)
			{
				return _baseprob + aux.floatValue();
			}
			else 
			{
				return _baseprob;
			}
		}
		else
		{
			return 1;
		}
	}
	
	/**
	 * Sets the Step's base probability of execution
	 */
	public void setProbability(float baseProb) {
		this._baseprob = baseProb;
	}
	
	/**
	 * Updates the probabilities of the step's effects by checking
	 * if the effects did happen or not after the execution of the step
	 */
	public void updateEffectsProbability(AgentModel am)
	{
		Effect e;
		ListIterator<Effect> li =  _effects.listIterator();
		
		while(li.hasNext()) {
			e = (Effect) li.next();
			//we only update the probability if the condition is grounded 
			if(e.GetEffect().isGrounded())
			{
				if (e.GetEffect().CheckCondition(am)) {
					e.IncreaseProbability(am);
				}
				else {
					//System.out.println("In updateEffectsProbability" + e.toString());
					//MotivationalState.GetInstance().UpdateCertainty((-e.GetProbability())*0.2f);
					e.DecreaseProbability(am);
				}
			}
		}
	}
	
	/**
	 * Checks if the specification of a step is correct. The method checks
	 * if any unbound variables used in the step's effects and preconditions are also used
	 * in the step's name (they must be). Additionally, it determines if a existing precondition
	 * is unachievable, i.e. there is no effect from any other step that achieves the 
	 * precondition. Additionaly, it also verifies if Speech-Act related
	 * effects correspond to predifined SpeechActs. 
	 * @param val - an IntegrityValidator used to validate the SpeechActs effects
	 * @throws UnspecifiedVariableException - if the Step uses unbound variables not specified
	 * 										  in the Step's name
	 * @throws UnknownSpeechActException - if the Step uses a Speech-Act effect not defined
	 * @see IntegrityValidator
	 */
	public void CheckIntegrity(IntegrityValidator val) throws UnspecifiedVariableException, UnknownSpeechActException {
	    Step s2;
	    Step s3;
	    s2 = (Step) this.clone();
	    s2.ReplaceUnboundVariables(0);
	    @SuppressWarnings("unused")
		Condition c1;
	    Condition c2;
	    Condition c3;
	    ArrayList<Substitution> substs = new ArrayList<Substitution>();
	    ListIterator<Condition> lc1;
	    ListIterator<Condition> lc2;
	    ListIterator<Condition> lc3;
	    ListIterator<Effect> le1;
	    ListIterator<Effect> le2;
	    ListIterator<Effect> le3;
	    
	    //search for unreachable preconditions
	    val.FindUnreachableConditions(this._name.toString(),_preconditions);
	    
	    //Checking variable names
	    if(Unifier.Unify(this._agent, s2._agent, substs) &&
	    		Unifier.Unify(this._name, s2._name,substs)) {
	    	s3 = (Step) this.clone();
	    	s3.MakeGround(substs);
	        lc1 = this._preconditions.listIterator();
	        lc2 = s2._preconditions.listIterator();
	        lc3 = s3._preconditions.listIterator();
	        
	        while(lc1.hasNext()) {
	            c1 = (Condition) lc1.next();
	            c2 = (Condition) lc2.next();
	            c3 = (Condition) lc3.next();
	            if(!c2.toString().equals(c3.toString())) {
	            	//System.out.println("WARNING: variable used in condition/effect " + c1 + " was not declared in step: " + _name);
	                //throw new UnspecifiedVariableException(this._name.toString(),c1.toString());
	            }
	        }
	        
	        le1 = this._effects.listIterator();
	        le2 = s2._effects.listIterator();
	        le3 = s3._effects.listIterator();
	        
	        while(le1.hasNext()) {
	            c1 = ((Effect) le1.next()).GetEffect();
	            c2 = ((Effect) le2.next()).GetEffect();
	            c3 = ((Effect) le3.next()).GetEffect();
	            if(!c2.toString().equals(c3.toString())) {
	            	//System.out.println("WARNING: variable used in condition/effect " + c1 + " was not declared in step: " + _name);
	                //throw new UnspecifiedVariableException(this._name.toString(),c1.toString());
	            }
	        }
	        
	        //TODO might need to check the integrity of effects on drives as well
	    }
	    
	    //if the action corresponds to a SpeechAct/AskQuestion, verify if the type exists
	    val.CheckSpeechAction(this._name);
	}
	
	/**
	 * Checks if the Step's preconditions are verified in the current State, i.e. the 
	 * KnowledgeBase
	 * @return true if all preconditions are true according to the world state stored
	 * 		   in the KnowledgeBase, false otherwise
	 */
	public boolean checkPreconditions(AgentModel am) {
		
		for(Condition c : _preconditions)
		{
			if(!c.CheckCondition(am)) return false;
		}
		
		return true;
	}

	/**
	 * Compares this step with another received operator to see if they are equal
	 * @param op - the planning operator to compare to
	 * @return true if the operators have the same ID in a plan
	 */
	public boolean equals(IPlanningOperator op ) {
		return _id == op.getID(); 
	}

	/**
	 * Gets the step's effects
	 * @return an ArrayList with all the step's effects
	 */
	public ArrayList<Effect> getEffects() {
		return _effects;
	}
	

	/**
	 * Gets the ID of the Step in the plan
	 * @return - the Step's ID
	 */
	public Integer getID()
	{
	    return this._id;
	}

	/**
	 * Gets the Step's name
	 * @return the Step's name
	 */
	public Name getName() {
		return _name;
	}
	
	/**
	 * Gets the name of the agent that executes the action
	 * @return the name of the executing action
	 */
	public Symbol getAgent()
	{
		return _agent;
	}

	/**
	 * Gets the preconditions of the Step
	 * @return an ArrayList with all the Step's preconditions
	 */
	public ArrayList<Condition> getPreconditions() {
		return _preconditions;
	}
	
	/**
	 * Gets the step's precondition with the given ID
	 * @param preconditionID - the id of the step's precondition
	 * @return the precondition
	 */
	public Condition getPrecondition(Integer preconditionID)
	{
	    return (Condition) this._preconditions.get(preconditionID.intValue());
	}
	
	/**
	 * Gets the step's effect with the given ID
	 * @param effectID - the id of the step's effect
	 * @return the effect
	 */
	public Effect getEffect(Integer effectID)
	{
		return (Effect) this._effects.get(effectID.intValue());
	}
	
	/**
	 * Sets if the Action can be executed by the self Agent
	 * @param selfExecutable
	 */
	public void SetSelfExecutable(boolean selfExecutable)
	{
		this._selfExecutable = selfExecutable;
	}
	
	/**
	 * Clones this Step, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The Step's copy.
	 */
	public Object clone() {
		Step op = new Step();
		op._agent = (Symbol) this._agent.clone();
		op._name = (Name) this._name.clone();
		op._key = new String(this._key);
		op._id = this._id;
		op._baseprob = this._baseprob;
		op._selfExecutable = this._selfExecutable;
		
		if(_preconditions != null) {
			op._preconditions = new ArrayList<Condition>(_preconditions.size());
			for(Condition cond : _preconditions)
			{
				op._preconditions.add((Condition)cond.clone());
			}	
		}
		
		if(_effects != null) {
			op._effects = new ArrayList<Effect>(_effects.size());
			for(Effect effect : _effects)
			{
				op._effects.add((Effect)effect.clone());
			}	
		}
		
		return op;
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
    	
    	this._agent.ReplaceUnboundVariables(variableID); 
    	this._name.ReplaceUnboundVariables(variableID);
    	 
    	 if(this._preconditions != null)
    	 {
    		for(Condition c : this._preconditions)
     	 	{
     			c.ReplaceUnboundVariables(variableID);
 	       	 
     	 	}
    	 }
    	 
    	 if(this._effects != null)
    	 {
    		for(Effect e : this._effects)
    	 	{
    			e.ReplaceUnboundVariables(variableID);
	       	 
    	 	}
    	 }
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
         
         this._agent.MakeGround(bindings);
    	 this._name.MakeGround(bindings);
    	 
    	 if(this._preconditions != null)
    	 {
    		for(Condition c : _preconditions)
    		{
    			c.MakeGround(bindings);
    		}
    	 }
    	 
    	 if(this._effects != null)
    	 {
    		for(Effect e : _effects)
    		{
    			e.MakeGround(bindings);
    		}
    	 }   	 	    	 
    	 UpdateSelfExecutable();
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
    	
    	this._agent.MakeGround(subst);
    	this._name.MakeGround(subst);
   	 
    	if(this._preconditions != null)
   	 	{
    		for(Condition c : _preconditions)
    		{
    			c.MakeGround(subst);
    		}
   	 	}
   	 
   	 	if(this._effects != null)
   	 	{
   	 		for(Effect e : _effects)
   	 		{
   	 			e.MakeGround(subst);
   	 		}
   	 	}
      	
    	UpdateSelfExecutable();
    }

    /**
	 * Indicates if the name is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the name is grounded, false otherwise
	 */
    public boolean isGrounded()
    {	
    	if (!this._agent.isGrounded()) return false;
    	if (!this._name.isGrounded()) return false;
    	
    	
    	
    	
    	if(this._preconditions != null)
    	{
    		for(Condition c : _preconditions)
        	{
        		if(!c.isGrounded()) return false;
        	}
    	}
    	
    	if(this._effects != null)
    	{
    		for(Effect e : _effects)
        	{
        		if(!e.isGrounded()) return false;
        	}    	
    	}
   	 	  
    	
        return true;
    }
	
    private void UpdateSelfExecutable()
    {
    	//once selfExecutable is false it can no longer become
    	//true
    	this._selfExecutable = this._selfExecutable &&
    		(!_agent.isGrounded() || 
   	 		 _agent.toString().equals("SELF"));
    }

	/**
	 * Sets the Step's name
	 * @param name - the new Name of the Step
	 */
	public void setAction(Name name) {
		_name = name;
	}
	
	/**
	 * Sets the Step's ID in the plan
	 * @param id - the new Step's ID
	 */
	public void setID(Integer id) {
		_id = id;
	}
	
	/**
	 * Converts the step into a String
	 * @return the converted String
	 */
	public String toString() {
		return _agent.toString() + ":" + _name.toString();
	}
}