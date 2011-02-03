/** 
 * BaseEmotion.java - Represents an emotion, which is an
 * instance of a particular Emotion Type
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
 */

package FAtiMA.Core.emotionalState;

import java.io.Serializable;

import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.enumerables.EmotionValence;
import FAtiMA.Core.wellFormedNames.Name;

/**
 * Represents an emotion, which is an
 * instance of a particular Emotion Type
 * @author João Dias
 */
public class BaseEmotion implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Event _cause;
	protected Name _direction;
	protected float _potential;
	protected final String _type; 
	protected final EmotionValence _valence;
	protected String[] _appraisalVariables;

	
	
	/**
	 * Creates a new BasicEmotion
	 * @param type - the type of the Emotion (ex: Hope, Distress). use the enumerable
	 * 				 EmotionType for the possible set of emotion types
	 * @param potential - the potential value for the intensity of the emotion
	 * @param cause - the event that caused the emotion
	 * @param direction - if the emotion is targeted to someone (ex: angry with Luke),
	 * 				      this parameter specifies the target
	 */
	protected BaseEmotion(String type, EmotionValence valence, String[] appraisalVariables, float potential, Event cause, Name direction) {
		_type = type;
		_valence = valence;
		_appraisalVariables = appraisalVariables;
		_potential = potential;
		_cause = cause;
		_direction = direction;
	}
	
	protected BaseEmotion(String type, EmotionValence valence, String[] appraisalVariables, float potential, Event cause) {
		this(type,valence,appraisalVariables, potential,cause,null);
	}
	
	/**
	 * Creates a new BaseEmotion that consists in a copy of a given emotion
	 * @param em - the emotion that will be copied into our new emotion
	 */
	public BaseEmotion(BaseEmotion em) {
		_type = em._type;
		_valence = em._valence;
		_appraisalVariables = em._appraisalVariables.clone();
		_potential = em._potential;
		_cause = em._cause;
		_direction = em._direction;
	}
	
	/**
	 * Gets the cause of the emotion
	 * @return - the event that caused the emotion
	 */
	public Event GetCause() {
		return _cause;
	}

	/**
	 * Gets the direction of the emotion (if the emotion directed to someone)
	 * @return - the name of the character to whom this emotion is directed
	 */
	public Name GetDirection() {
		return _direction;
	}
	
	
	public String[] GetAppraisalVariables()
	{
		return _appraisalVariables;
	}
	
	/**
	 * Gets an hask key used to index this BaseEmotion
	 * @return - a String used to index the BaseEMotion
	 */
	public String GetHashKey() {
		String aux = _cause.toString();
		for(String s : _appraisalVariables)
		{
			aux += "-" + s;
		}
		return aux;
	}
	
	/**
	 * Gets the emotion's potential
	 * @return - the potential for the intensity of the emotion
	 */
	public float GetPotential() {
		return _potential;
	}
	
	/**
	 * Gets the emotion's type 
	 * @return a short value corresponding to the EmotionType enumerable
	 * @see EmotionType enumerable for the set of possible emotion types
	 */
	public String getType() {
		return _type;
	}
	
	/**
	 * Gets the emotion's valence (either positive or negative)
	 * @return a short value corresponding to the EmotionValence enumerable
	 * @see EmotionValence enumerable
	 */
	public EmotionValence getValence() {
		return _valence;
	}
	
	/**
	 * Gets an HashCode
	 * @return an int that can be used as hashcode
	 */
	public int hashCode() {
		return GetHashKey().hashCode();
	}
	
	/**
	 * Sets the emotion's cause
	 * @param cause - the event that caused the emotion
	 */
	public void SetCause(Event cause) {
		_cause = cause;
	}
	
	public void increasePotential(float delta){
		this._potential += delta;
		if(this._potential < 0){
			this._potential = 0;
		}
	}
	
	public void setPotential(float potential){
		this._potential = potential;
	}
	
	/**
	 * Converts the BaseEmotion to a String
	 * @return the converted String
	 */
	public String toString() {
		return _type + ": " + _cause + " " + _direction; 
	}
}