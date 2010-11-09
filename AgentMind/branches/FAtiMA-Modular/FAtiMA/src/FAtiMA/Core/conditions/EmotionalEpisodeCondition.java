/**
 * EmotionalEpisodeCondition.java - 
 *  
 * Copyright (C) 2010 GAIPS/INESC-ID 
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
 * Created: 23/07/2010
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: 23/07/2010 - File created
 */

package FAtiMA.Core.conditions;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.EmotionType;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;

public class EmotionalEpisodeCondition extends Condition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Symbol _emotion;
	protected float _value;
	
	/**
	 * Parses a EmotionCondition given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the EmotionCondition Parsed
	 */
	public static EmotionalEpisodeCondition ParseEmotionalEpisodeCondition(Attributes attributes) throws InvalidEmotionTypeException {
		
		Symbol emotion = null;
		Symbol episode;
		float value = 0;
		
		String aux;
		aux = attributes.getValue("episode");
		if(aux != null)
		{
			episode = new Symbol(aux);
		}
		else
		{
			episode = new Symbol("1");
		}
		
		aux = attributes.getValue("emotion");
		if(aux != null)
		{
			emotion = new Symbol(aux);
		}
		
		aux = attributes.getValue("value");
		if(aux != null)
		{
			value = Float.parseFloat(aux);
		}
		
		return new EmotionalEpisodeCondition(episode,emotion,value);
	}
	
	private EmotionalEpisodeCondition()
	{
	}
	
	public EmotionalEpisodeCondition(Symbol episode, Symbol emotion, float value)
	{
		this._name = episode;
		this._emotion = emotion;
		this._value = value;
		this._ToM = Constants.UNIVERSAL;
	}
	
	/**
	 * Checks if the 
	 * @return 
	 * @see 
	 */
	public boolean CheckCondition(AgentModel am) {
		
		BaseEmotion emotion;
		MemoryEpisode episode;
		ArrayList<MemoryEpisode> episodes;
		
		if(!this.isGrounded()) return false;
		
		episodes = am.getMemory().getEpisodicMemory().GetAllEpisodes();
		
		int id = Integer.parseInt(this._name.toString());
		
		episode = episodes.get(id);
		
		emotion = episode.getStrongestEmotion();
		
		try {
			if(emotion.GetType() == EmotionType.ParseType(this._emotion.toString()))
			{
				if(emotion.GetPotential() >= this._value)
				{
					return true;
				}
			}
		} catch (InvalidEmotionTypeException e) {
				return false;
		}
		
		
		return false;
		
	}
	
	/**
	 * This method finds all the possible sets of Substitutions that applied to the 
	 * condition will make it valid (true) according to the agent's EmotionalState 
     * @return A list with all SubstitutionsSets that make the condition valid
	 * @see EmotionalState
	 */
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		MemoryEpisode episode;
		SubstitutionSet ss;
		Substitution s;
		BaseEmotion emotion;
		int i = 0;
		
		if (_name.isGrounded() && _emotion.isGrounded()) {
			if(CheckCondition(am))
			{ 
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		try
		{
		
			if(_name.isGrounded())
			{
				int id = Integer.parseInt(this._name.toString());
				episode = am.getMemory().getEpisodicMemory().GetAllEpisodes().get(id);
				emotion = episode.getStrongestEmotion();
				if(emotion.GetPotential() > this._value)
				{
					ss = new SubstitutionSet();
					if(!_emotion.isGrounded())
					{
						s = new Substitution(this._emotion,new Symbol(EmotionType.GetName(emotion.GetType())));
						ss.AddSubstitution(s);
						bindingSets.add(ss);
					}
					else if(emotion.GetType() == EmotionType.ParseType(this._emotion.toString()))
					{
						ss = new SubstitutionSet();
						bindingSets.add(ss);
					}
				}
				
				return bindingSets;
			}
			
			i = 0;
			for(MemoryEpisode ep : am.getMemory().getEpisodicMemory().GetAllEpisodes())
			{
				
				emotion = ep.getStrongestEmotion();
				if(emotion.GetPotential() > this._value)
				{
					ss = new SubstitutionSet();
					s = new Substitution((Symbol) this._name,new Symbol(String.valueOf(i)));
					ss.AddSubstitution(s);
					if(!_emotion.isGrounded())
					{
						
						s = new Substitution(this._emotion,new Symbol(EmotionType.GetName(emotion.GetType())));
						ss.AddSubstitution(s);
						bindingSets.add(ss);
						
					}
					else if(emotion.GetType() == EmotionType.ParseType(this._emotion.toString()))
					{
						bindingSets.add(ss);
					}	
				}
				
				i++;
			}
			
			return bindingSets;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	
	
	public boolean isGrounded()
	{
		return this._emotion.isGrounded() && this._name.isGrounded();
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
    	this._emotion.ReplaceUnboundVariables(variableID);
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
    	this._emotion.MakeGround(bindings);
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
    	this._emotion.MakeGround(subst);
    }
	
	/**
	 * Clones this EmotionCondition, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The EmotionCondition's copy.
	 */
	public Object clone()
	{
		EmotionalEpisodeCondition eec = new EmotionalEpisodeCondition();
		
		eec._emotion = (Symbol) this._emotion.clone();
		eec._value =  this._value;
		eec._name = (Symbol) this._name.clone();
		eec._ToM = (Symbol) this._ToM.clone();
	    
		return eec;
	}

	@Override
	public Name GetValue() {
		return Name.ParseName(String.valueOf(this._value));
	}

	@Override
	protected ArrayList<Substitution> GetValueBindings(AgentModel am) {
		return null;
	}
}
