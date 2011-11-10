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

package FAtiMA.Core.conditions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.memory.semanticMemory.KnowledgeBase;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;

public class EmotionCondition extends PredicateCondition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String _emotionType;
	
	protected Symbol _intensity;
	protected Symbol _direction;
	
	
	public EmotionCondition(boolean active, String emotion)
	{
		super(active,null,Constants.UNIVERSAL);
		this._emotionType = emotion;
		
		this._direction = null;
		this._intensity = new Symbol("0");
		UpdateName();
		
	}
	
	public EmotionCondition(boolean active, Symbol ToM, String emotion)
	{
		super(active,null,ToM);
		this._emotionType = emotion;	
		this._direction = null;
		this._intensity = new Symbol("0");	
		UpdateName();
	}
	
	protected EmotionCondition(EmotionCondition eC){
    	super(eC);
    	_emotionType = eC._emotionType;
		_intensity = (Symbol) eC._intensity.clone();
		
		if(eC._direction != null)
		{
			_direction = (Symbol) eC._direction.clone();
		}
	}
    
	/**
	 * Clones this EmotionCondition, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The EmotionCondition's copy.
	 */
	public Object clone(){
		return new EmotionCondition(this);
	}
	
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

		emotionType = attributes.getValue("emotion").toUpperCase(Locale.ENGLISH);
		
		aux = attributes.getValue("agent");
		if(aux != null)
		{
			ec = new EmotionCondition(active,new Symbol(aux),emotionType);
		}
		else
		{
			ec = new EmotionCondition(active,emotionType);
		}
		
		aux = attributes.getValue("cause");
		
		
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
	
	protected static Symbol FloatToSymbol(float f)
	{
		return new Symbol(Float.toString(f));
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
		String aux;
		
		aux = this._emotionType + "("; 
		
		if(this._direction != null)
		{
			aux += this._direction;
		}
		aux+=")";
		
		setName(Name.ParseName(aux));
	}
	

	/**
	 * Gets the condition's value - the object compared against the condition's name
	 * @return the condition's value
	 */
	 public Name getValue()
	 {
		return this._intensity;
	 }
	
	/**
	 * Checks if the Predicate is verified in the agent's KnowledgeBase
	 * @return true if the Predicate is verified, false otherwise
	 * @see KnowledgeBase
	 */
	public float CheckCondition(AgentModel am) {
		float fIntensity;
		float satisfactionLevel;
		float maxSatisfactionLevel = 0;
		
		if(!getToM().isGrounded()) return 0;
		
		if(!this.isGrounded()) return 0;
		
	
		AgentModel perspective = am.getModelToTest(getToM());
		
		
		
		EmotionalState es = perspective.getEmotionalState();
		
		for(ActiveEmotion aem : es.GetEmotionsIterator())
		{
			if(aem.getType().equalsIgnoreCase(this._emotionType))
			{
				//TODO I should check direction here also
				if(this._intensity.isGrounded())
				{
					fIntensity = Float.parseFloat(this._intensity.toString());
					if(fIntensity != 0)
					{
						satisfactionLevel = aem.GetIntensity()/fIntensity;
						if(satisfactionLevel > maxSatisfactionLevel)
						{
							maxSatisfactionLevel = satisfactionLevel;
						}
					}
				}				
			}
		}
		
		if(getPositive())
		{
			return Math.min(maxSatisfactionLevel,1f);
		}
		else
		{
			if(maxSatisfactionLevel == 1)
			{
				return 0;
			}
			return 1;
		}
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
		
		if (this.isGrounded()) {
			if(CheckCondition(am)>0.3)
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		//we cannot determine bindings for negative emotion conditions,
		//assume false
		if(!this.getPositive()) return null;
		subSets = SearchEmotion(am);
		if(subSets.size() == 0) return null;
		return subSets;
	}
	
	private ArrayList<SubstitutionSet> SearchEmotion(AgentModel am)
	{
		ArrayList<Substitution> bindings;
		ArrayList <SubstitutionSet>substitutionSets = new ArrayList<SubstitutionSet>();
		AgentModel perspective = am.getModelToTest(getToM());
		SubstitutionSet sSet;
		boolean intensityOk;
		boolean directionOk;
		
		EmotionalState es = perspective.getEmotionalState();
		
		for(ActiveEmotion aem : es.GetEmotionsIterator())
		{
			sSet = new SubstitutionSet();
			intensityOk = false;
			directionOk = false;
			
			if(aem.getType().equalsIgnoreCase(this._emotionType))
			{
				if(this._intensity.isGrounded())
				{
					if(aem.GetIntensity() >= Float.parseFloat(this._intensity.toString()))
					{
						intensityOk = true;
					}
				}
				else
				{
					intensityOk = true;
					Symbol intensityValue = FloatToSymbol(aem.GetIntensity());
					Substitution s = new Substitution(this._intensity,intensityValue);
					sSet.AddSubstitution(s);
				}
				
				if(this._direction != null)
				{
					bindings = Unifier.Unify(this._direction,aem.GetDirection());
					if(bindings != null)
					{
						directionOk = true;
						sSet.AddSubstitutions(bindings);
					}
				}
				else
				{
					directionOk = true;
				}
				
				if(intensityOk && directionOk)
				{
					substitutionSets.add(sSet);
				}
			}
		}
		
		return substitutionSets;
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
    	super.ReplaceUnboundVariables(variableID);
    	this._intensity.ReplaceUnboundVariables(variableID);
    	if(this._direction != null)
    	{
    		this._direction.ReplaceUnboundVariables(variableID);
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
    	super.MakeGround(bindings);
    	this._intensity.MakeGround(bindings);
    	if(this._direction != null)
    	{
    		this._direction.MakeGround(bindings);
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
    	super.MakeGround(subst);
    	this._intensity.MakeGround(subst);
    	if(this._direction != null)
    	{
    		this._direction.MakeGround(subst);
    	}
    }
    
    /**
	 * Indicates if the condition is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the condition is grounded, false otherwise
	 */
	public boolean isGrounded() {
		if(this._direction != null)
		{
			if(!this._direction.isGrounded())
			{
				return false;
			}
		}
		
		return (super.isGrounded() && _intensity.isGrounded());
	}
}
