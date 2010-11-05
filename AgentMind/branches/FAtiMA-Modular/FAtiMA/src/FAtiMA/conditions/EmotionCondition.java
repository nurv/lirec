/**
 * EmotionCondition.java - 
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
 * Created: 28/09/2006
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 28/09/2006 - File created
 */

package FAtiMA.conditions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;

import org.xml.sax.Attributes;

import FAtiMA.AgentModel;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.exceptions.InvalidEmotionTypeException;
import FAtiMA.memory.semanticMemory.KnowledgeBase;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Symbol;
import FAtiMA.wellFormedNames.Unifier;

public class EmotionCondition extends PredicateCondition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected short _emotionType;
	protected Symbol _intensity;
	protected Symbol _direction;
	
	/**
	 * Parses a EmotionCondition given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the EmotionCondition Parsed
	 */
	public static EmotionCondition ParseEmotionCondition(Attributes attributes) throws InvalidEmotionTypeException {
		EmotionCondition ec;
		boolean active;
		String emotionType;
		Symbol intensity= new Symbol("0");
		
		String aux;
		aux = attributes.getValue("active");
		if(aux != null)
		{
			active = Boolean.parseBoolean(aux);
		}
		else active = true;

		emotionType = attributes.getValue("emotion");
		
		aux = attributes.getValue("agent");
		if(aux != null)
		{
			ec = new EmotionCondition(active,new Symbol(aux),EmotionType.ParseType(emotionType));
		}
		else
		{
			ec = new EmotionCondition(active,
					EmotionType.ParseType(emotionType));
		}
		
		
		aux = attributes.getValue("target");
		if(aux != null)
		{
			ec.SetDirection(new Symbol(aux));
		}
		
		aux = attributes.getValue("min-intensity");
		if(aux != null)
		{
			intensity = new Symbol(aux);
		}
		ec.SetIntensity(intensity);
			
		return ec;
	}
	
	private static Symbol FloatToSymbol(float f)
	{
		DecimalFormat oneDForm = new DecimalFormat("#.#");
		return new Symbol(Double.valueOf(oneDForm.format(f)).toString()); 
	}
	
	private EmotionCondition()
	{
	}
	
	public EmotionCondition(boolean active, short emotion)
	{
		this._positive = active;
		this._emotionType = emotion;	
		this._direction = null;
		this._intensity = new Symbol("0");
		this._ToM = Constants.UNIVERSAL;
		
		UpdateName();
	}
	
	public EmotionCondition(boolean active, Symbol ToM, short emotion)
	{
		this._positive = active;
		this._emotionType = emotion;	
		this._direction = null;
		this._intensity = new Symbol("0");
		this._ToM = ToM;
		
		UpdateName();
	}
	
	public void SetIntensity(Symbol intensity)
	{
		this._intensity = intensity;
	}
	
	public void SetIntensity(float intensity)
	{
		this._intensity = FloatToSymbol(intensity);
	}
	
	public void SetDirection(Symbol direction)
	{
		this._direction = direction;
		UpdateName();
	}
	
	private void UpdateName()
	{
		String aux = EmotionType.GetName(this._emotionType) + "("; 
		
		if(this._direction != null)
		{
			aux += this._direction;
		}
		aux+=")";
		this._name = Name.ParseName(aux);
	}
	
	/**
	 * Gets the condition's value - the object compared against the condition's name
	 * @return the condition's value
	 */
	 public Name GetValue()
	 {
		return this._intensity;
	 }
	
	/**
	 * Checks if the Predicate is verified in the agent's KnowledgeBase
	 * @return true if the Predicate is verified, false otherwise
	 * @see KnowledgeBase
	 */
	public boolean CheckCondition(AgentModel am) {
		boolean result;
		if(!_ToM.isGrounded()) return false;
		
		
		if(!_name.isGrounded() || !_intensity.isGrounded()) return false;
		
		result = SearchEmotion(am).size() > 0; 
		return _positive == result;
	}
	
	/**
	 * This method finds all the possible sets of Substitutions that applied to the 
	 * condition will make it valid (true) according to the agent's EmotionalState 
     * @return A list with all SubstitutionsSets that make the condition valid
	 * @see EmotionalState
	 */
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		ArrayList<SubstitutionSet> subSets;
		
		if (_name.isGrounded() && _intensity.isGrounded()) {
			if(CheckCondition(am))
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		//we cannot determine bindings for negative emotion conditions,
		//assume false
		if(!this._positive) return null;
		subSets = SearchEmotion(am);
		if(subSets.size() == 0) return null;
		return subSets;
	}
	
	private ArrayList<SubstitutionSet> SearchEmotion(AgentModel am)
	{
		ActiveEmotion aem;
		ArrayList<Substitution> bindings;
		ArrayList <SubstitutionSet>substitutionSets = new ArrayList<SubstitutionSet>();
		AgentModel perspective = am.getModelToTest(_ToM);
		
		EmotionalState es = perspective.getEmotionalState();
		
		for(Iterator<ActiveEmotion> it = es.GetEmotionsIterator();it.hasNext();)
		{
			aem = (ActiveEmotion) it.next();
			if(aem.GetType() == this._emotionType)
			{
				if(this._intensity.isGrounded())
				{
					if(aem.GetIntensity() >= Float.parseFloat(this._intensity.toString()))
					{
						if(this._direction != null)
						{
							bindings = Unifier.Unify(this._direction,aem.GetDirection());
							if(bindings != null)
							{
								substitutionSets.add(new SubstitutionSet(bindings));
							}
						}
						else
						{
							substitutionSets.add(new SubstitutionSet());
						}
					}	
				}
				else
				{
					SubstitutionSet sset1;
					Symbol intensityValue = FloatToSymbol(aem.GetIntensity());
					Substitution s = new Substitution(this._intensity,intensityValue);
					if(this._direction != null)
					{
						bindings = Unifier.Unify(this._direction,aem.GetDirection());
						if(bindings != null)
						{
							sset1 = new SubstitutionSet(bindings);
							sset1.AddSubstitution(s);
							substitutionSets.add(sset1);
						}
					}
					else
					{
						sset1 = new SubstitutionSet();
						sset1.AddSubstitution(s);
						substitutionSets.add(sset1);
					}
				}
				
			}
		}
		
		return substitutionSets;
	}
	
	 /**
     * @deprecated use ReplaceUnboundVariables(int) instead.
	 * Replaces all unbound variables in the object by applying a numeric
	 * identifier to each one.
	 * Example: the variable [X] becomes [X4] if the received ID is 4.
	 * @param variableID - the identifier to be applied
	 * @return a new Condition with the variables changed 
	 */
	public Object GenerateName(int id) {
		EmotionCondition aux = (EmotionCondition) this.clone();
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
    	this._ToM.ReplaceUnboundVariables(variableID);
    	this._name.ReplaceUnboundVariables(variableID);
    	this._intensity.ReplaceUnboundVariables(variableID);
    	if(this._direction != null)
    	{
    		this._direction.ReplaceUnboundVariables(variableID);
    	}
    }
	
    /**
     * @deprecated use the method MakeGround(ArrayList) instead
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)".
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @return a new Predicate with the substitutions applied
	 * @see Substitution
	 */
	public Object Ground(ArrayList<Substitution> bindings) {
		EmotionCondition aux = (EmotionCondition) this.clone();
		aux.MakeGround(bindings);
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
    	this._ToM.MakeGround(bindings);
    	this._name.MakeGround(bindings);
    	this._intensity.MakeGround(bindings);
    	if(this._direction != null)
    	{
    		this._direction.MakeGround(bindings);
    	}
    }
	
    /**
     * @deprecated use the method MakeGround(Substitution) instead
	 * Applies a substitution to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)".
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @return a new Predicate with the substitution applied
	 * @see Substitution
	 */
	public Object Ground(Substitution subst) {
		EmotionCondition aux = (EmotionCondition) this.clone();
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
    	this._ToM.MakeGround(subst);
    	this._name.MakeGround(subst);
    	this._intensity.MakeGround(subst);
    	if(this._direction != null)
    	{
    		this._direction.MakeGround(subst);
    	}
    }
	
	/**
	 * Clones this EmotionCondition, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The EmotionCondition's copy.
	 */
	public Object clone()
	{
		EmotionCondition ec = new EmotionCondition();
		ec._positive = this._positive;
		ec._ToM = (Symbol) this._ToM.clone();
		ec._emotionType = this._emotionType;
		ec._name = (Name) this._name.clone();
		ec._intensity = (Symbol) this._intensity.clone();
		
		if(this._direction != null)
		{
			ec._direction = (Symbol) this._direction.clone();
		}
	    
		return ec;
	}
}
