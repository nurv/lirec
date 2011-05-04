/* MoodCondition.java - 
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
 * Created: 09/02/2007
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 09/02/2007 - File created
 */

package FAtiMA.Core.conditions;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.exceptions.InvalidMoodOperatorException;
import FAtiMA.Core.exceptions.NoMoodOperatorDefinedException;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;


public class MoodCondition extends Condition {
	
	private static final long serialVersionUID = 1L;
	
	protected static final short operatorGreater = 0;
	protected static final short operatorGreaterEqual = 1;
	protected static final short operatorLesser = 2;
	protected static final short operatorLesserEqual = 3;
	protected static final short operatorEqual = 4;
	protected static final short operatorNotEqual = 5;
	protected static final short invalidOperator = 6;
	
	protected float _value;
	protected short _operator;
	
	public MoodCondition(short operator, float value)
	{
		super(Name.ParseName("mood(" + operator + ")"),Constants.UNIVERSAL);
		
		this._operator = operator;
		
		if(value > 10) {
			this._value = 10;
		}
		else if(value < -10)
		{
			this._value = -10;
		}
		else
		{
			this._value = value;
		}		
	}
	
	protected MoodCondition(MoodCondition mC)
	{
		super(mC);
		_operator = mC._operator;
		_value = mC._value;
	}
	
	
	/**
	 * Clones this EmotionCondition, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The EmotionCondition's copy.
	 */
	public Object clone(){
		return new MoodCondition(this);
	}
	
	/**
	 * Parses a EmotionCondition given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the EmotionCondition Parsed
	 */
	public static MoodCondition ParseMoodCondition(Attributes attributes) throws InvalidMoodOperatorException, NoMoodOperatorDefinedException
	{
		float value = 0;
		short operator = invalidOperator;
		
		String aux;
		
		aux = attributes.getValue("value");
		if(aux != null)
		{
			value = Float.parseFloat(aux);
		}

		aux = attributes.getValue("operator");
		if(aux == null)
		{
			throw new NoMoodOperatorDefinedException();
		}
		else
		{
			if(aux.equals("GreaterThan"))
			{
				operator = operatorGreater;
			}
			else if(aux.equals("LesserThan"))
			{
				operator = operatorLesser;
			}
			else if(aux.equals("GreaterEqual"))
			{
				operator = operatorGreaterEqual;
			}
			else if(aux.equals("LesserEqual"))
			{
				operator = operatorLesserEqual;
			}
			else if(aux.equals("="))
			{
				operator = operatorEqual;
			}
			else if(aux.equals("!="))
			{
				operator = operatorNotEqual;
			}
			else
			{
				throw new InvalidMoodOperatorException(aux);
			}
		}
	
		MoodCondition mc = new MoodCondition(operator,value);
			
		return mc;
	}
	
	

	
	
	/**
	 * Gets the condition's value - the object compared against the condition's name
	 * @return the condition's value
	 */
	 public Name GetValue()
	 {
		return new Symbol(Float.toString(this._value));
	 }
	
	/**
	 * Checks if the Predicate is verified in the agent's KnowledgeBase
	 * @return true if the Predicate is verified, false otherwise
	 * @see KnowledgeBase
	 */
	public boolean CheckCondition(AgentModel am) {
		
		float currentMood = am.getEmotionalState().GetMood();
		
		switch(this._operator)
		{
			case operatorEqual:
			{
				return currentMood == this._value;
			}
			case operatorNotEqual:
			{
				return currentMood != this._value;
			}
			case operatorGreater:
			{
				return currentMood > this._value;
			}
			case operatorGreaterEqual:
			{
				return currentMood >= this._value;
			}
			case operatorLesser:
			{
				return currentMood < this._value;
			}
			case operatorLesserEqual:
			{
				return currentMood <= this._value;
			}
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
		if(CheckCondition(am))
		{
			ArrayList<SubstitutionSet> bindings = new ArrayList<SubstitutionSet>();
			bindings.add(new SubstitutionSet());
			return bindings;
		}
		else return null;
	}
	
	public ArrayList<Substitution> GetValueBindings(AgentModel am)
	{
		if(CheckCondition(am)) {
			return new ArrayList<Substitution>();
		}
		else return null;
	}
	
	public boolean isGrounded()
	{
		return true;
	}
}