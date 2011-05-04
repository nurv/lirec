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

package FAtiMA.emotionalIntelligence;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.ShortTermEpisodicMemory;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;

public class EmotionalEpisodeCondition extends Condition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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
	
	protected Symbol _emotion;
	protected float _value;
	
	public EmotionalEpisodeCondition(Symbol episode, Symbol emotion, float value)
	{
		super(episode,Constants.UNIVERSAL);
		this._emotion = emotion;
		this._value = value;
	}
	
	public EmotionalEpisodeCondition(EmotionalEpisodeCondition eec)
	{
		super(eec);
		this._emotion = (Symbol) eec._emotion.clone();
		this._value = eec._value;
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
		
		if(this.getName().toString().equals("STM"))
		{
			ShortTermEpisodicMemory stem = am.getMemory().getEpisodicMemory().getSTEM();
			emotion = stem.getStrongestEmotion();
		}
		else
		{
			int id = Integer.parseInt(this.getName().toString());
			episode = episodes.get(id);
			emotion = episode.getStrongestEmotion();
		}
		
		
		if(emotion != null && emotion.getType().equalsIgnoreCase(this._emotion.toString()))
		{
			if(emotion.GetPotential() >= this._value)
			{
				return true;
			}
		}		
		
		return false;
		
	}
	
	/**
	 * Clones this EmotionCondition, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The EmotionCondition's copy.
	 */
	public Object clone()
	{
		return new EmotionalEpisodeCondition(this);
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
		
		if (getName().isGrounded() && _emotion.isGrounded()) {
			if(CheckCondition(am))
			{ 
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		ShortTermEpisodicMemory stem = am.getMemory().getEpisodicMemory().getSTEM();
		
		try
		{
		
			if(getName().isGrounded())
			{
				if(getName().toString().equals("STM"))
				{
					emotion = stem.getStrongestEmotion();
				}
				else
				{
					int id = Integer.parseInt(this.getName().toString());
					episode = am.getMemory().getEpisodicMemory().GetAllEpisodes().get(id);
					emotion = episode.getStrongestEmotion();
				}
				
				
				if(emotion != null && emotion.GetPotential() > this._value)
				{
					ss = new SubstitutionSet();
					if(!_emotion.isGrounded())
					{
						s = new Substitution(this._emotion,new Symbol(emotion.getType().toString()));
						ss.AddSubstitution(s);
						bindingSets.add(ss);
					}
					else if(emotion.getType().equals(this._emotion.toString()))
					{
						ss = new SubstitutionSet();
						bindingSets.add(ss);
					}
				}
				
				return bindingSets;
			}
			
			
			emotion = stem.getStrongestEmotion();
			{
				if(emotion!= null && emotion.GetPotential() > this._value)
				{
					ss = new SubstitutionSet();
					s = new Substitution((Symbol) this.getName(),new Symbol("STM"));
					ss.AddSubstitution(s);
					if(!_emotion.isGrounded())
					{
						
						s = new Substitution(this._emotion,new Symbol(emotion.getType()));
						ss.AddSubstitution(s);
						bindingSets.add(ss);
						
					}
					else if(emotion.getType().equalsIgnoreCase(this._emotion.toString()))
					{
						bindingSets.add(ss);
					}	
				}
			}
			
			i = 0;
			for(MemoryEpisode ep : am.getMemory().getEpisodicMemory().GetAllEpisodes())
			{
				
				emotion = ep.getStrongestEmotion();
				if(emotion!= null && emotion.GetPotential() > this._value)
				{
					ss = new SubstitutionSet();
					s = new Substitution((Symbol) this.getName(),new Symbol(String.valueOf(i)));
					ss.AddSubstitution(s);
					if(!_emotion.isGrounded())
					{
						
						s = new Substitution(this._emotion,new Symbol(emotion.getType()));
						ss.AddSubstitution(s);
						bindingSets.add(ss);
						
					}
					else if(emotion.getType().equalsIgnoreCase(this._emotion.toString()))
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
	
	@Override
	public Name GetValue() {
		return Name.ParseName(String.valueOf(this._value));
	}
	
    @Override
	protected ArrayList<Substitution> GetValueBindings(AgentModel am) {
		return null;
	}
	
	public boolean isGrounded()
	{
		return this._emotion.isGrounded() && this.getName().isGrounded();
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
    	this.getToM().MakeGround(bindings);
    	this.getName().MakeGround(bindings);
    	this._emotion.MakeGround(bindings);
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
    	this.getToM().MakeGround(subst);
    	this.getName().MakeGround(subst);
    	this._emotion.MakeGround(subst);
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
    	this.getToM().ReplaceUnboundVariables(variableID);
    	this.getName().ReplaceUnboundVariables(variableID);
    	this._emotion.ReplaceUnboundVariables(variableID);
    }
}
