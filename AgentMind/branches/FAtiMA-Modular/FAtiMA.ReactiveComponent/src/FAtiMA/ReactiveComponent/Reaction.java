/** 
 * Reaction.java - Emotional Reaction based in Construal Frames that specify values
 * for some of OCC's appraisal variables: Desirability, DesirabilityForOther, Like and
 * Praiseworthiness.
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
 * João Dias: 23/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable
 * João DiaS: 18/09/2006 - Removed the Like attribute because it's no longer used
 * 						 - added the attribute other;
 * 						 - the class is now groundable and clonable
 */

package FAtiMA.ReactiveComponent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.IntegrityValidator;
import FAtiMA.Core.exceptions.UnknownSpeechActException;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.wellFormedNames.IGroundable;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * Represents an Emotional Reaction based in Construal Frames that specify values
 * for some of OCC's appraisal variables: Desirability, DesirabilityForOther, Like and
 * Praiseworthiness.
 * 
 * @author João Dias
 */
public class Reaction implements Serializable, IGroundable, IReactionNode {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Float _desirability;
	protected Float _desirabilityForOther;
	protected Float _praiseworthiness;
	protected Float _like;
	
	protected Symbol _other;
	
	protected Event _event;
	

	/**
	 * Creates a new empty Emotional Reaction
	 */
	public Reaction() {
		_desirability = null;
		_desirabilityForOther = null;
		_praiseworthiness = null;
		_like = null;
		_event = null;
		_other = null;
	}

	/**
	 * Creates a new empty Emotional Reaction
	 * @param event - the event that this reaction references 
	 */
	public Reaction(Event event){
		_event = event;
		_desirability = null;
		_praiseworthiness = null;
		_desirabilityForOther = null;
		_like = null;
		_other = null;
	}
	
	/**
	 * Creates a new Emotional Reaction
	 * @param desirability - the desirability of the event
	 * @param desirabilityForOther - the desirability of the event for other agents
	 * @param praiseworthiness - the paiseworthiness of the event
	 * @param other - which character does the desirabilityForOther variable reference
	 */
	public Reaction(Float desirability, Float desirabilityForOther, Float praiseworthiness, Symbol other) {
		_desirability = desirability;
		_desirabilityForOther = desirabilityForOther;
		_praiseworthiness = praiseworthiness;
		_other = other;
		_like = null;
		_event = null;
	}
	
	/**
	 * Checks the integrity of the Reaction by testing if the reaction references an event
	 * with an speechAct not defined. In this case it throws the exception.
	 */
	public void CheckIntegrity(IntegrityValidator val) throws UnknownSpeechActException {
	    String aux;
	    aux = _event.GetAction() + "(" +  _event.GetTarget();
	    ListIterator<Parameter> li = _event.GetParameters().listIterator();
	    while(li.hasNext()) {
	        aux = aux + "," + (li.next()).GetValue();
	    }
	    aux = aux + ")";
	    val.CheckSpeechAction(Name.ParseName(aux));
	}
	
	/**
	 * Gets the appraisal variable: Desirability of the event
	 * @return - the event's desirability
	 */
	public Float getDesirability() {
		return _desirability;
	}

	/**
	 * Gets the appraisal variable: DesirabilityForOther of the event
	 * @return - the event's desirability for other agent
	 */
	public Float getDesirabilityForOther() {
		return _desirabilityForOther;
	}
	
	public Float getLike()
	{
		return _like;
	}

	/**
	 * Gets the event referenced by the emotional reaction
	 * @return the reaction's event
	 */
	public Event getEvent() {
		return _event;
	}

	/**
	 * Gets the appraisal variable: Praiseworthiness of the event
	 * @return - the event's praiseworthiness for the agent
	 */
	public Float getPraiseworthiness() {
		return _praiseworthiness;
	}
	
	/**
	 * Gets the name of the character that the appraisal variable
	 * desirabilityForOther refers
	 * @return - the name of the desirabilityForOther's character
	 */
	public Symbol getOther()
	{
		return _other;
	}

	/**
	 * tests if a given event matches the emotional Reaction
	 * @param eventPerception - the event to test with the Reaction
	 * @return true if the event corresponds to the emotional Reaction, false otherwise
	 */
	public boolean MatchEvent(Event eventPerception) {
		return Event.MatchEvent(_event, eventPerception);
	}

	/**
	 * Sets the appraisal variable: Desirability
	 * @param integer - the new value of Desirability for the reaction
	 */
	public void setDesirability(Float f) {
		_desirability = f;
	}

	/**
	 * Sets the appraisal variable: DesirabilityForOther
	 * @param integer - the new value of DesirabilityForOther for the reaction
	 */
	public void setDesirabilityForOther(Float f) {
		_desirabilityForOther = f;
	}
	
	public void setLike(Float f)
	{
		_like = f;
	}

	/**
	 * Sets the event that the emotional reaction references
	 * @param event - the new event referenced by the reaction 
	 */
	public void setEvent(Event event) {
		_event = event;
	}

	/**
	 * Sets the appraisal variable: Praiseworhtiness
	 * @param integer - the new value of Praiseworthiness for the reaction
	 */
	public void setPraiseworthiness(Float f) {
		_praiseworthiness = f;
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
    	if(this._other != null)
    	{
    		this._other.ReplaceUnboundVariables(variableID);
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
    	if(this._other != null)
    	{
    		this._other.MakeGround(bindings);
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
    	if(this._other != null)
    	{
    		this._other.MakeGround(subst);
    	}
    }
	
	/**
	 * Indicates if the Predicate is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the Predicate is grounded, false otherwise
	 */
	public boolean isGrounded() {
		if(this._other == null) return true;
		return this._other.isGrounded();
	}
	
	public Object clone()
	{
		Reaction r = new Reaction();
		r._desirability = this._desirability;
		r._desirabilityForOther = this._desirabilityForOther;
		r._praiseworthiness = this._praiseworthiness;
		r._event = (Event) this._event.clone();
		if(this._other != null)
		{
			r._other = (Symbol) this._other.clone();
		}
		
		return r;
	}
	
	/**
	 * Converts the emotional Reaction to a String
	 * @return the converted String
	 */
	public String toString() {
		return _event + " (" + _desirability + "," + _desirabilityForOther + "," + _praiseworthiness + ")";
	}

	@Override
	public Reaction getReaction(Event e) {
		return this;
	}
}