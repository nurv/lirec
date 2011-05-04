/** 
 * Effect.java - Represents an operator's effect. Includes probability.
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
 * Created: 04/05/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 04/05/2004 - File created
 * João Dias: 17/05/2006 - Added the clone() Method
 * João Dias: 22/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 * João Dias: 15/07/2006 - Removed the KnowledgeBase from the Class fields since the KB is now
 * 						   a singleton that can be used anywhere without previous references.
 * João Dias: 12/09/2006 - Changed the methods for increasing and decreasing an effect's probability
 */

package FAtiMA.Core.plans;

import java.io.Serializable;
import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.wellFormedNames.IGroundable;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;


/**
 * Represents a step's effect. Includes information about the probability of
 * the effect to succeed in the world.
 *  
 * @author João Dias
 */

public class Effect implements IGroundable, Cloneable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static int idCounter=1;
	private float _baseprob;
	private Name _biasName; 
	private Condition _effect;
	
	/**
	 * Creates a new Effect
	 * @param stepName - the name of the step that this effect bellongs to
	 * @param prob - the effect's probability
	 * @param effect - the condition that represents the effect
	 */
	public Effect(AgentModel am, String stepName, float prob, Condition effect) {
		this._baseprob = prob;
		this._effect = effect;
		if(!stepName.equals("Start")) 
		{
			this._biasName = Name.ParseName("ProbBias(" + stepName + idCounter++ + ")");
			if(am != null)
			{
				am.getMemory().getSemanticMemory().Tell(this._biasName, new Float(0));
			}
		}
	}
	
	private Effect() {
	}
	
	/**
	 * Decreases an effect's probability by a fixed ammount.
	 * Used to perform wishfull thinking or denial withing
	 * emotion-focused coping strategies
	 */
	public void DecreaseProbability(AgentModel am) {
		float bias;
		float prob;
		float newprob;
		float newbias;
		
		bias = ((Float) am.getMemory().getSemanticMemory().AskProperty(_biasName)).floatValue();
		prob = bias + _baseprob;
		newprob = 0.6f * prob;
		newbias = newprob - _baseprob;
		am.getMemory().getSemanticMemory().Tell(_biasName,new Float(newbias));   
	}
	
	/**
	 * Gets the condition that represents the Effect
	 * @return the effect (represented as a condition)
	 */
	public Condition GetEffect() {
		return _effect;
	}
	
	/**
	 * Gets the effect's probability
	 * @return the effect's probability
	 */
	public float GetProbability(AgentModel am) {
		if(am == null) return _baseprob;
		return _baseprob + ((Float) am.getMemory().getSemanticMemory().AskProperty(_biasName)).floatValue();
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
    	this._effect.ReplaceUnboundVariables(variableID);
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
    	this._effect.MakeGround(bindings);
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
    	this._effect.MakeGround(subst);
    }
    
    /**
	 * Indicates if the name is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the name is grounded, false otherwise
	 */
    public boolean isGrounded()
    {
    	return this._effect.isGrounded();
    }
	
	/**
	 * Increases an effect's probability by a fixed ammount.
	 * Used to perform wishfull thinking or denial withing
	 * emotion-focused coping strategies
	 */
	public void IncreaseProbability(AgentModel am) {
		float bias;
		float prob;
		float newprob;
		float newbias;
		
		bias = ((Float) am.getMemory().getSemanticMemory().AskProperty(_biasName)).floatValue(); 
		prob = bias + _baseprob;
		newprob = 0.6f * prob + 0.4f;
		newbias = newprob - _baseprob;
		am.getMemory().getSemanticMemory().Tell(_biasName,new Float(newbias));   
	}
	
	/**
	 * Clones this effect, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The effects's copy.
	 */
	public Object clone()
	{
	    Effect e = new Effect();
	    e._baseprob = this._baseprob;
	    e._biasName = (Name) this._biasName.clone();
	    e._effect = (Condition) this._effect.clone();
	    
	    return e;
	}
	
	/**
	 * Converts the Effect to a String
	 * @return the converted String
	 */
	public String toString() {
		return "Effect: " + _effect + " prob: " + _baseprob;
	}
}
