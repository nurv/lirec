/** 
 * InterestGoal.java - Implements OCC's Interest goals
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
 * João Dias: 24/05/2006 - Removed getProtectedConditions() method that was
 * 						   similar to getProtectionConstraints() and was not
 * 						   being used
 * João Dias: 10/07/2006 - the class is now serializable
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 * João Dias: 12/07/2006 - Replaced the deprecated Ground methods for the new ones.
 * João Dias: 12/07/2006 - the class is now Clonable
 * João Dias: 20/07/2006 - the field protectionConstraints was saving a list of
 * 						   of ProtectedConditions, now it stores a list of conditions
 */

package FAtiMA.Core.goals;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.IntegrityValidator;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.exceptions.UnreachableGoalException;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;


/**
 * Implements OCC's Interest goals
 * @author João Dias
 */
public class InterestGoal extends Goal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Condition> _protectionConstraints;
	
	/**
	 * Creates a new InterestGoal
	 * @param description - the InterestGoal's name or description
	 */
	public InterestGoal(Name description) {
		super(description);
		_protectionConstraints = new ArrayList<Condition>(1);
	}

	private InterestGoal() {
		_protectionConstraints = new ArrayList<Condition>(1);
	}

	/**
	 * Adds a condition to the goal's protected conditions
	 * @param conditionType - in this method the conditionType is irrelevant
	 * 						  because an InterestGoal only has protection conditions 
	 * @param cond - the condition to add
	 */
	public void AddCondition(String conditionType, Condition cond) {
		_protectionConstraints.add(cond);
	}
	
	/**
	 * Checks the integrity of the goal. For instance it checks if the goal's 
	 * success conditions are reachable by at leas one action in the domain operators.
	 * If not it means that the goal will never be achieve and probably is a typo in
	 * the goal's definition (or in the actions file) 
	 * @param val - the validator used to check the goal
	 * @throws UnreachableGoalException - thrown if a goal's success conditions can never
	 * 									  be achieved because there is no operator with such
	 * 									  effects
	 */
	public void CheckIntegrity(IntegrityValidator val) throws UnreachableGoalException {
	    
	    if(val.FindUnreachableConditions(_name.toString(),_protectionConstraints)) {
	        //throw new UnreachableGoalException(_name.toString());
	    }
	}

	/**
	 * Gets the InterestGoal's protected conditions
	 * @return an ArrayList with the conditions that the goal wants to protect
	 */
	public ArrayList<Condition> getProtectionConstraints() {
		return _protectionConstraints;
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
    	
    	li = this._protectionConstraints.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).ReplaceUnboundVariables(variableID);
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
    	
    	this._appliedSubstitutions.addAll(bindings);
    	
    	ListIterator<Condition> li;
    	
    	this._name.MakeGround(bindings);
    	
    	li = this._protectionConstraints.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).MakeGround(bindings);
    	}
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
    	
    	this._appliedSubstitutions.add(subst);
    	
    	this._name.MakeGround(subst);
    	
    	li = this._protectionConstraints.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).MakeGround(subst);
    	}
    }
	
	/**
	 * Clones this InterestGoal, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The Goal's copy.
	 */
	public Object clone()
	{
		ListIterator<Condition> li;
		InterestGoal g = new InterestGoal();
		
		g._goalID = this._goalID;
		g._key = this._key;
		g._name = (Name) this._name.clone();
		g._baseIOF = this._baseIOF;
		g._baseIOS = this._baseIOS;
		g._dynamicIOF = (Name) this._dynamicIOF.clone();
		g._dynamicIOS = (Name) this._dynamicIOS.clone();
		
		g._appliedSubstitutions = new ArrayList<Substitution>(this._appliedSubstitutions.size());
		for(Substitution s : this._appliedSubstitutions)
		{
			g._appliedSubstitutions.add((Substitution) s.clone());
		}
		
		
		if(this._protectionConstraints != null)
		{
			g._protectionConstraints = new ArrayList<Condition>(this._protectionConstraints.size());
			li = this._protectionConstraints.listIterator();
			while(li.hasNext())
			{
				g._protectionConstraints.add((Condition) li.next().clone());
			}
		}
		
		return g;
	}
	
	/**
	 * Converts the InterestGoal to a String
	 * @return the converted String
	 */
	public String toString() {
		return "InterestGoal: " + super.toString(); 
	}
}