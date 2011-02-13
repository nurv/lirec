/** 
 * IPanningOperator.java - Interface that specifies methods needed for the planner to use
 * 						   a given object as an operator
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
 * Created: 08/04/2008 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: 08/04/2008 - File created
 **/

package FAtiMA.Core.plans;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.wellFormedNames.IGroundable;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

public interface IPlanningOperator extends Cloneable, IGroundable {

	/**
	 * Gets the operator's probability of execution
	 * @return the operators's probability
	 */
	public float getProbability(AgentModel am);
	
	
	/**
	 * Updates the probabilities of the operators's effects 
	 */
	public void updateEffectsProbability(AgentModel am);
	
	/**
	 * Checks if the operators's preconditions are verified in the current State
	 * @return true if all preconditions are true according to the current State, 
	 *         false otherwise
	 */
	public boolean checkPreconditions(AgentModel am);
	
	/**
	 * Compares this planOperator with another planOperator to see if they are equal
	 * @param step - the step to compare to
	 * @return true if the operators have the same ID in a plan
	 */
	public boolean equals(IPlanningOperator op);
	
	/**
	 * Gets the operator's effects
	 * @return an ArrayList with all the operators's effects
	 */
	public ArrayList<Effect> getEffects();
	
	
	/**
	 * Gets the ID of the Operator in the plan
	 * @return - the Operator's ID
	 */
	public Integer getID();
	

	/**
	 * Gets the Operators's name
	 * @return the Operator's name
	 */
	public Name getName(); 
	
	/**
	 * Gets the name of the agent that executes the operator
	 * @return the name of the agent that executes the operator
	 */
	public Symbol getAgent();
	
	/**
	 * Gets the preconditions of the Operator
	 * @return an ArrayList with all the Operator's preconditions
	 */
	public ArrayList<Condition> getPreconditions(); 
	
	/**
	 * Gets the operator's precondition with the given ID
	 * @param preconditionID - the id of the operators's precondition
	 * @return the precondition
	 */
	public Condition getPrecondition(Integer preconditionID);

	
	/**
	 * Gets the operators's effect with the given ID
	 * @param effectID - the id of the operators's effect
	 * @return the effect
	 */
	public Effect getEffect(Integer effectID);
	
	/**
	 * Sets the operators's ID in the plan
	 * @param id - the new operators's ID
	 */
	public void setID(Integer id);
	
	public Object clone();
}
