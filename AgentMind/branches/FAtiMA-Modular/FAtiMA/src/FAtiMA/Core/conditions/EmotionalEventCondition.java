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


import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.memory.semanticMemory.KnowledgeBase;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;

public class EmotionalEventCondition extends EmotionCondition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Symbol _subject;
	protected Symbol _action;
	protected Symbol _target;
	protected ArrayList<Symbol> _parameters;
	
	
	public EmotionalEventCondition(boolean active, String emotion, Symbol intensity, Symbol subject, Symbol action, Symbol target, ArrayList<Symbol> parameters)
	{
		super(active,emotion);
		
		this._subject = subject;
		this._action = action;
		this._target = target;
		this._parameters = parameters;
		
		this.SetIntensity(intensity);
		
		this.UpdateName();
		
	}
	
	public EmotionalEventCondition(boolean active, Symbol ToM, String emotion, Symbol intensity, Symbol subject, Symbol action, Symbol target, ArrayList<Symbol> parameters)
	{
		super(active,ToM,emotion);
		
		this._subject = subject;
		this._action = action;
		this._target = target;
		this._parameters = parameters;
		
		this.SetIntensity(intensity);
		
		this.UpdateName();
		
	}
	
	protected EmotionalEventCondition(EmotionalEventCondition eC)
	{
    	super(eC);
    	
    	if(eC._subject != null)
    	{
    		this._subject = (Symbol) eC._subject.clone();
    	}
    	
    	if(eC._target != null)
    	{
    		this._target = (Symbol) eC._target.clone();
    	}
    	
    	if(eC._action != null)
    	{
    		this._action = (Symbol) eC._action.clone();
    	}
    	
    	
    	this._parameters = new ArrayList<Symbol>(eC._parameters.size());
		
		for(Symbol s : eC._parameters){
			_parameters.add((Symbol) s.clone());
		}
    	
    	this.UpdateName();
	}
	
	protected void UpdateName()
	{
		String aux = this._subject + "," + this._action;
		if(this._target != null)
		{
			aux = aux + "," + this._target;
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			aux = aux + "," + li.next();
		}
		
		aux = aux + "," + this._emotionType + "," + this._intensity;
		
		this.setName(Name.ParseName("EMOTIONALEVENT(" + aux + ")"));
	}
    
	/**
	 * Clones this EmotionCondition, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The EmotionCondition's copy.
	 */
	public Object clone(){
		return new EmotionalEventCondition(this);
	}
	
	/**
	 * Parses a EmotionCondition given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the EmotionCondition Parsed
	 */
	public static EmotionalEventCondition ParseEmotionalEventCondition(Attributes attributes) throws InvalidEmotionTypeException {
		
		EmotionalEventCondition eec;
		String aux;
		String emotionType;
		boolean active;
		Symbol intensity= new Symbol("0");
		
		
		aux = attributes.getValue("active");
		if(aux != null)
		{
			active = Boolean.parseBoolean(aux);
		}
		else active = true;

		emotionType = attributes.getValue("emotion").toUpperCase(Locale.ENGLISH);
		
		aux = attributes.getValue("min-intensity");
		if(aux != null)
		{
			intensity = new Symbol(aux);
		}
		
		
		Symbol subject = null;
		Symbol action = null;
		Symbol target = null;
		ArrayList<Symbol> parameters = new ArrayList<Symbol>();
		
		aux = attributes.getValue("subject");
		if(aux!= null)
		{
			subject = new Symbol(aux);
		}
		
		aux = attributes.getValue("action");
		if(aux!= null)
		{
			action = new Symbol(aux);
		}
		
		aux = attributes.getValue("target");
		if(aux != null)
		{
			target = new Symbol(aux);
		}
		
		aux = attributes.getValue("parameters");
		
		if(aux != null) {
			StringTokenizer st = new StringTokenizer(aux, ",");
			while(st.hasMoreTokens()) {
				parameters.add(new Symbol(st.nextToken()));
			}
		}
		
		aux = attributes.getValue("agent");
		if(aux != null)
		{
			eec = new EmotionalEventCondition(active,new Symbol(aux),emotionType,intensity,subject,action,target,parameters);
		}
		else 
		{
			eec = new EmotionalEventCondition(active,emotionType,intensity,subject,action,target,parameters);
		}
			
		return eec;
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
		boolean result = false;
		boolean perspectiveChange;
		Event storedEvent;
		
		if(!getToM().isGrounded()) return 0;
		
		//if(!this.isGrounded()) return 0;
		
		AgentModel perspective = am.getModelToTest(getToM());
		perspectiveChange = !getToM().equals(Constants.SELF);
		EmotionalState es = perspective.getEmotionalState();
		
		for(ActiveEmotion aem : es.GetEmotionsIterator())
		{
			if(aem.getType().equalsIgnoreCase(this._emotionType))
			{
				fIntensity = Float.parseFloat(this._intensity.toString());
				if(aem.GetIntensity() > fIntensity)
				{
					if(perspectiveChange)
					{
						storedEvent = aem.GetCause().RemovePerspective(getToM().toString());
						storedEvent = storedEvent.ApplyPerspective(am.getName());
					}
					else
					{
						storedEvent = aem.GetCause();
					}
					
					
					if(this._subject == null || this._subject.toString().equals(storedEvent.GetSubject()))
					{
						if(this._action == null || this._action.toString().equals(storedEvent.GetAction()))
						{
							if(this._target == null || this._target.toString().equals(storedEvent.GetTarget()))
							{
								result = true;
								break;
							}
						}
					}
				}				
			}
		}
		
		if(getPositive() == result)
		{
			return 1;
		}
		else
		{
			return 0;
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
			if(CheckCondition(am)==1)
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
		ArrayList <SubstitutionSet>substitutionSets = new ArrayList<SubstitutionSet>();
		ArrayList<Parameter> parameters;
		Symbol symbol;
		Parameter param;
		
		SubstitutionSet sSet;
		boolean perspectiveChange;
		boolean subjectOk;
		boolean actionOk;
		boolean targetOk;
		boolean intensityOk;
		Event storedEvent;
		
		AgentModel perspective = am.getModelToTest(getToM());
		perspectiveChange = !getToM().toString().equals(Constants.SELF);
		
		EmotionalState es = perspective.getEmotionalState();
		
		for(ActiveEmotion aem : es.GetEmotionsIterator())
		{
			sSet = new SubstitutionSet();
			intensityOk = false;
			actionOk = false;
			subjectOk = false;
			targetOk = false;
			
			
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
				
				if(perspectiveChange)
				{
					storedEvent = aem.GetCause().RemovePerspective(getToM().toString());
					storedEvent = storedEvent.ApplyPerspective(am.getName());
				}
				else
				{
					storedEvent = aem.GetCause();
				}
				if(this._subject != null)
				{
					if(this._subject.isGrounded())
					{
						if(this._subject.toString().equals(storedEvent.GetSubject()))
						{
							subjectOk = true;
						}
					}
					else
					{
						subjectOk = true;
						Substitution s = new Substitution(this._subject,new Symbol(storedEvent.GetSubject()));
						sSet.AddSubstitution(s);
					}
				}
				else
				{
					subjectOk = true;
				}
				
				if(this._action != null)
				{
					if(this._action.isGrounded())
					{
						if(this._action.toString().equals(storedEvent.GetAction()))
						{
							actionOk = true;
						}
					}
					else
					{
						actionOk = true;
						Substitution s = new Substitution(this._action,new Symbol(storedEvent.GetAction()));
						sSet.AddSubstitution(s);
					}
				}
				else
				{
					actionOk = true;
				}
				
				if(this._target != null)
				{
					if(this._target.isGrounded())
					{
						if(aem.GetCause().GetTarget() != null && storedEvent.GetTarget().equals(this._target.toString()))
						{
							targetOk = true;
						}
					}
					else
					{
						targetOk = true;
						if(storedEvent.GetTarget() != null)
						{
							Substitution s = new Substitution(this._target,new Symbol(storedEvent.GetTarget()));
							sSet.AddSubstitution(s);
						}
					}
				}
				else
				{
					targetOk = true;
				}
				
				parameters = storedEvent.GetParameters();
				
				for(int i=0; i < this._parameters.size(); i++)
				{
					symbol = this._parameters.get(i);
					if(symbol.isGrounded())
					{
						if(parameters.size() > i)
						{
							param = parameters.get(i);
							if(!symbol.toString().equals(param.GetValue().toString()))
							{
								targetOk = false;
							}
						}
					}
					else 
					{
						if(parameters.size() > i)
						{
							param = parameters.get(i);
							Substitution s = new Substitution(symbol,new Symbol(param.GetValue().toString()));
							sSet.AddSubstitution(s);
						}					
					}
				}
				
				if(intensityOk && subjectOk && actionOk && targetOk)
				{
					AgentLogger.GetInstance().logAndPrint("EmotionalEvent with substitutions " + sSet + " for condition: " + this.getName());
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
    	if(this._subject != null)
    	{
    		this._subject.ReplaceUnboundVariables(variableID);
    	}
    	if(this._action != null)
    	{
    		this._action.ReplaceUnboundVariables(variableID);
    	}
    	if(this._target != null)
    	{
    		this._target.ReplaceUnboundVariables(variableID);
    	}
		
		for(Symbol s : this._parameters){
			s.ReplaceUnboundVariables(variableID);
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
    	if(this._subject != null)
    	{
    		this._subject.MakeGround(bindings);
    	}
    	if(this._action != null)
    	{
    		this._action.MakeGround(bindings);
    	}
    	if(this._target != null)
    	{
    		this._target.MakeGround(bindings);
    	}
		
		for(Symbol s : this._parameters){
			s.MakeGround(bindings);
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
    	if(this._subject != null)
    	{
    		this._subject.MakeGround(subst);
    	}
    	if(this._action != null)
    	{
    		this._action.MakeGround(subst);
    	}
    	if(this._target != null)
    	{
    		this._target.MakeGround(subst);
    	}
		
		for(Symbol s : this._parameters){
			s.MakeGround(subst);
		}
    }
    
    /**
	 * Indicates if the condition is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the condition is grounded, false otherwise
	 */
	public boolean isGrounded() {
		
		if(!super.isGrounded()) return false;
		
		if(this._subject != null && !this._subject.isGrounded())
		{
			return false;
		}
		
		if(this._action != null && !this._action.isGrounded())
		{
			return false;
		}
		
		if(this._target != null && !this._target.isGrounded())
		{
			return false;
		}
		
		for(Symbol s : this._parameters)
		{
			if(!s.isGrounded()) return false;
		}
		
		return true;
	}
}
