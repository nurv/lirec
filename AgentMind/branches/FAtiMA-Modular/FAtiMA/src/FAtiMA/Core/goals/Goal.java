/** 
 * Goal.java - abstract goal class used to represent common aspects of Interest
 * goals and ActivePursuit goals.
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
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 * João Dias: 12/07/2006 - Replaced the deprecated Ground methods for the new ones.
 * João Dias: 12/07/2006 - the class is now Clonable
 * João Dias: 17/07/2006 - the Methods decreaseImportanceOfFailure and DecreaseImportanceOfSuccess
 * 						   receive one additional parameter (the amount to decrease)
 * João Dias: 17/07/2006 - Important change in the calculation of ImportanceOfSuccess and
 * 						   ImportanceOfFailure. These values are not stored inside the class 
 * 					       as before. Now they are stored in the KnowledgeBase according to 
 * 						   the goal's name. This allows a better actualization of the goal's 
 * 						   importance even when there are several different instantiations of a 
 * 						   same goal (there were some problems before in this situation).
 * 						   Additionally, this change make classes that reference goals to be
 * 						   serialized more easily. 
 * João Dias: 28/09/2006 - Refactoring: Renamed the method GetImportanceOfSucess to
 * 						   GetImportanceOfSuccess (you can see why)
 * João Dias: 22/12/2006 - After some reconsideration, we do need a fixed part and a dinamic
 * 						   part for a goal's importance. The fixed part can be changed externally
 * 						   by a request, while the dinamic part is changed by emotion-focused
 * 						   coping and by the selection of coping strategies
 * João Dias: 18/02/2007 - Added the methods GetActivationEvent, GetFailureEvent, GetSuccessEvent
 */

package FAtiMA.Core.goals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IntegrityValidator;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.exceptions.UnreachableGoalException;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.util.enumerables.GoalEvent;
import FAtiMA.Core.wellFormedNames.IGroundable;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * abstract goal class used to represent common aspects of Interest
 * goals and ActivePursuit goals.
 * 
 * @author João Dias
 */
public abstract class Goal implements IGroundable, Cloneable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public static final String GOALSUCCESS = "GS";
	public static final String GOALFAILURE = "GF";
	public static final String GOALDROPED = "GD";
	public static final String IMPORTANCEOFSUCCESS = "IOS";
	public static final String IMPORTANCEOFFAILURE = "IOF";
	public static final String ACTIVATIONEVENT = "activate";
	public static final String SUCCESSEVENT = "succeed";
	public static final String FAILUREEVENT = "fail";
	
	protected static int goalCounter = 1;

	protected Name _name;
	protected String _key;
	protected ArrayList<Substitution> _appliedSubstitutions;
	protected int _goalID;
	protected int _baseIOF;
	protected int _baseIOS;
	protected Name _dynamicIOF;
	protected Name _dynamicIOS;

	/**
	 * Creates a new Empty Goal. Not used directly since its an abstract class
	 */
	public Goal() {
	}

	/**
	 * Creates a new Goal. Not used directly since its an abstract class
	 * @param description - the goal's name
	 */
	public Goal(Name description) {
		_key = description.toString();
		_appliedSubstitutions = new ArrayList<Substitution>();
		_goalID = goalCounter++;
		_name = description;
		_baseIOF = 0;
		_baseIOF = 0;
		_dynamicIOF = Name.ParseName(IMPORTANCEOFFAILURE + "(" + _goalID + ")");
		_dynamicIOS = Name.ParseName(IMPORTANCEOFSUCCESS + "(" + _goalID + ")");
	}
	
	public ArrayList<Substitution> getAppliedSubstitutions()
	{
		return _appliedSubstitutions;
	}
	
	
	/**
	 * Generates a status description of goal according to
	 * the received status. Ex: if the goal is Fight(Luke)
	 * and the status is GOALSUCCESS the method returns
	 * GOALSUCCESS(Fight,Luke)
	 * 
	 * @param status - the goal status, use one of the static status values:
	 * 				   Goal.GOALSUCCESS
	 * 				   Goal.GOALFAILURE
	 * 				   Goal.GOALDROPED
	 * 				   ... etc
	 * @return
	 */
	public String GenerateGoalStatus(String status) {
	    String aux;
	    aux = status + "(";
		ListIterator<Symbol> li = _name.GetLiteralList().listIterator();
		
		aux = aux + li.next();
		while(li.hasNext()) {
		    aux = aux + "," + li.next();
		}
		aux = aux + ")";
		return aux;
	}

	/**
	 * Adds a condition to the goal
	 * @param conditionType - the type of the condition: 
	 * 						  success condition, precondition, etc 
	 * @param cond - the condition to add
	 */
	public abstract void AddCondition(String conditionType, Condition cond);
	
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
	public abstract void CheckIntegrity(IntegrityValidator val) throws UnreachableGoalException;
	
	/**
	 * Decreases the ImportanceOfFailure of a goal by a given ammount
	 * Used for emotion-focused coping strategies like disengagement
	 */
	public void DecreaseImportanceOfFailure(AgentModel am, float decr) {
		if(decr <= 0) return;
		
		Float iof = (Float) am.getMemory().getSemanticMemory().AskProperty(_dynamicIOF);
		if(iof == null)
		{
			iof = new Float(0);
		}
		float imp = _baseIOF + iof.floatValue() - decr;
		if(imp > 0) {
			iof = new Float(imp - _baseIOF);
		}
		else iof = new Float(0 - _baseIOF);
		
		am.getMemory().getSemanticMemory().Tell(_dynamicIOF,iof);
	}
	
	public String getKey(){
		return this._key;
	}
	
	/**
	 * Increases the ImportanceOfFailure of a goal by a given ammount
	 * Used for emotion-focused coping strategies
	 */
	public void IncreaseImportanceOfFailure(AgentModel am, float incr) {
		if(incr <= 0) return;
		
		Float iof = (Float) am.getMemory().getSemanticMemory().AskProperty(_dynamicIOF);
		if(iof == null)
		{
			iof = new Float(incr);
		}
		else
		{
			iof = new Float(iof.floatValue() + incr);
		}
		
		am.getMemory().getSemanticMemory().Tell(_dynamicIOF,iof);
	}
	
	/**
	 * Decreases the ImportanceOfSuccess of a goal by a given amount
	 * Used for emotion-focused coping strategies like disengagement
	 */
	public void DecreaseImportanceOfSuccess(AgentModel am, float decr) {
		if(decr <= 0) return;
		
		Float ios = (Float) am.getMemory().getSemanticMemory().AskProperty(_dynamicIOS);
		if(ios == null)
		{
			ios = new Float(0);
		}
		float aux = _baseIOS + ios.floatValue() - decr;
		
		if(aux > 0) {
			ios = new Float(aux - _baseIOS);
		}
		else ios = new Float(0 - _baseIOS);
		
		am.getMemory().getSemanticMemory().Tell(_dynamicIOS,ios);
	}
	
	/**
	 * Increases the ImportanceOfSuccess of a goal by a fixed ammount
	 * Used for emotion-focused coping strategies
	 */
	public void IncreaseImportanceOfSuccess(AgentModel am, float incr) {
		if(incr <= 0) return;
		
		Float ios = (Float) am.getMemory().getSemanticMemory().AskProperty(_dynamicIOS);
		if(ios == null)
		{
			ios = new Float(incr);
		}
		else 
		{
			ios = new Float(ios.floatValue() + incr);
		}
		
		am.getMemory().getSemanticMemory().Tell(_dynamicIOS,ios);
	}
	
	/**
	 * Gets the goal's importance of failure
	 * @return the importance of failure ranged [0;10]
	 */
	public float GetImportanceOfFailure(AgentModel am) {
		Float aux = (Float) am.getMemory().getSemanticMemory().AskProperty(this._dynamicIOF);
		if(aux != null) return aux.floatValue() + _baseIOF;
		else return _baseIOF;
	}
	
	/**
	 * Gets the goal's importance of success
	 * @return the importance of success ranged [0;10]
	 */
	public float GetImportanceOfSuccess(AgentModel am) {
		Float aux = (Float) am.getMemory().getSemanticMemory().AskProperty(this._dynamicIOS);
		if(aux != null) return aux.floatValue() + _baseIOS;
		else return _baseIOS;
	}
	
	/*
	public float GetIntrinsicImportanceOfSucess() {
		return _importanceOfSucess;
	}*/

	/**
	 * Gets the goal's name
	 * @return the name of the goal
	 */
	public Name getName() {
		return _name;
	}
	
	/**
	 * Gets an Event that represents the goal's activation
	 * @return an Event that contains a description of the goal's activation
	 */
	public Event GetActivationEvent()
	{
		// Meiyii - using GoalEvent enum
		return generateEventDescription(GoalEvent.ACTIVATION);
	}
	
	/**
	 * Gets an Event that represents the goal's success
	 * @return an Event that contains a description of the goal's success
	 */
	public Event GetSuccessEvent()
	{
		// Meiyii - using GoalEvent enum
		return generateEventDescription(GoalEvent.SUCCESS);
	}
	
	/**
	 * Gets an Event that represents the goal's failure
	 * @return an Event that contains a description of the goal's failure
	 */
	public Event GetFailureEvent()
	{
		// Meiyii - using GoalEvent enum
		return generateEventDescription(GoalEvent.FAILURE);
		
	}
	
	public Event GetCancelEvent()
	{
		// Meiyii - using GoalEvent enum
		return generateEventDescription(GoalEvent.CANCEL);	
	}
	
	/* 06/01/10 - Meiyii
	 * Modify the description so that the goal action (activation, success, failure)
	 * is stored in the field status instead of action, added a GOAL eventType 
	*/
	protected Event generateEventDescription(short goalEventType)
	{
		String target = "";
		String name;
		ListIterator<Symbol> li = this._name.GetLiteralList().listIterator();
		
		name = li.next().toString();
		if(li.hasNext())
		{
			target = li.next().toString();
		}
		
		Event e = new Event("SELF",name,target,EventType.GOAL,goalEventType);
		
	    while(li.hasNext())
	    {
	    	e.AddParameter(new Parameter("param",li.next().toString()));
	    }
	    
	    return e;
	}

	
	
	/**
	 * Replaces all unbound variables in the object by applying a numeric 
     * identifier to each one. For example, the variable [x] becomes [x4]
     * if the received ID is 4. 
     * Attention, this method modifies the original object.
     * @param variableID - the identifier to be applied
	 */
    public abstract void ReplaceUnboundVariables(int variableID);
    


	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @see Substitution
	 */
    public abstract void MakeGround(ArrayList<Substitution> bindings);
    

	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @see Substitution
	 */
    public abstract void MakeGround(Substitution subst);


	/**
	 * Indicates if the Goal is grounded (no unbound variables in its name and conditions)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if all the goal's conditions and name are grounded, false otherwise
	 */
	public boolean isGrounded() {
		return _name.isGrounded();
	}
	
	/**
	 * Sets the goal's importance of failure
	 * @param imp - the new importance of failure (ranged [0;10])
	 */
	public void SetImportanceOfFailure(AgentModel am, float imp) {
		
		if(imp < 0) {
			imp = 0;
		}
		else if (imp > 10)
		{
			imp = 10;
		}
		
		_baseIOF = Math.round(imp);
		
		Float iof = (Float) am.getMemory().getSemanticMemory().AskProperty(_dynamicIOF);
		if(iof != null)
		{
			float aux = _baseIOF + iof.floatValue();
			if(aux < 0) {
				iof = new Float(0 - _baseIOF);
				am.getMemory().getSemanticMemory().Tell(this._dynamicIOF,iof);
			}
		}
	}
	
	/**
	 * Sets the goal's importance of success
	 * @param imp - the new importance of success (ranged [0;10])
	 */
	public void SetImportanceOfSuccess(AgentModel am, float imp) {
		
		if(imp < 0) {
			imp = 0;
		}
		else if (imp > 10)
		{
			imp = 10;
		}
		
		_baseIOS = Math.round(imp);
		
		Float ios = (Float) am.getMemory().getSemanticMemory().AskProperty(_dynamicIOS);
		if(ios != null)
		{
			float aux = _baseIOS + ios.floatValue();
			if(aux < 0) {
				ios = new Float(0 - _baseIOS);
				am.getMemory().getSemanticMemory().Tell(this._dynamicIOS,ios);
			}
		}
	}
	
	/**
	 * Converts the Goal to a String
	 * @return the converted String
	 */
	public String toString() {
		return _name.toString();
	}

	/**
	 * Gets a list of conditions, a list of bindings and returns a new list of conditions
	 * where the conditions were grounded with the list of received bindings. Defined for
	 * internal use of the InterestGoal and ActivePursuitGoal classes 
	 * @param originalList - the list of conditions to ground
	 * @param bindings - a list of substitutions to apply to the condition list
	 * @return a list with grounded conditions 
	 * @see Substitution
	 */
	protected ArrayList<Condition> GroundConditionList(ArrayList<Condition> originalList, ArrayList<Substitution> bindings) {
		ArrayList<Condition> newList;
		Condition cond;
		Condition newCondition;
		ListIterator<Condition> li;

		newList = new ArrayList<Condition>(originalList.size());
		li = originalList.listIterator();

		while (li.hasNext()) {
			cond = (Condition) li.next();
			newCondition = (Condition) cond.clone();
			newCondition.MakeGround(bindings);
			if (!newCondition.isGrounded())
				return null;
			newList.add(newCondition);
		}

		return newList;
	}
}