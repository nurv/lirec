/**
 * EventCondition.java - 
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
 * Created: 31/Ago/2006
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 31/Ago/2006 - File created
 * João Dias: 27/09/2006 - Changed the attribute named ocurred (typo) to occurred
 * João Dias: 02/10/2006 - changes in the Search keys for parameters used to retrieve
 * 						   or search for an event in Autobiographical Memory
 * João Dias: 24/03/2008 - Restructure, changed the way EventConditions Hierarchy. Now, PastEventConditions
 * 						   is the super class, and RecentEventConditions is the child class
 * Meiyii Lim: 12/03/2009 - Search for recent events in Short Term Memory instead of Autobiographical Memory
 */

package FAtiMA.conditions;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.memory.ActionDetail;
import FAtiMA.memory.SearchKey;
import FAtiMA.memory.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.memory.shortTermMemory.ShortTermMemory;
import FAtiMA.sensorEffector.Event;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;


/**
 * @author João Dias
 *
 */

public class RecentEventCondition extends PastEventCondition {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected RecentEventCondition()
	{
	}
	
	public RecentEventCondition(PastEventCondition cond)
	{
		this._subject = cond._subject;
		this._action = cond._action;
		this._target = cond._target;
		this._name = cond._name;
		this._positive = cond._positive;
		this._parameters = cond._parameters;
	}
	
	public RecentEventCondition(boolean occurred, Name event)
	{
		super(occurred, event);	
	}
	
	public RecentEventCondition(boolean occurred, Symbol subject, Symbol action, Symbol target, ArrayList parameters)
	{
		super(occurred, subject, action, target, parameters);
	}
	
	public RecentEventCondition(boolean occurred, Event e)
	{
		super(occurred, e);
	}

	public Object clone() {
		RecentEventCondition newEvent = new RecentEventCondition();
		
		newEvent._positive = this._positive;
		
		newEvent._name = (Name) this._name.clone();
		newEvent._subject = (Symbol) this._subject.clone();
		newEvent._action = (Symbol) this._action.clone();
		if(this._target != null)
		{
			newEvent._target = (Symbol) this._target.clone();
		}
		
		newEvent._parameters = new ArrayList(this._parameters.size());
		
		ListIterator li = this._parameters.listIterator();
		
		while(li.hasNext())
		{
			newEvent._parameters.add(((Symbol)li.next()).clone());
		}
		
		return newEvent;
	}

	public Object GenerateName(int id) {
		RecentEventCondition event = (RecentEventCondition) this.clone();
		event.ReplaceUnboundVariables(id);
		return event;
	}

	public void ReplaceUnboundVariables(int variableID) {
		this._name.ReplaceUnboundVariables(variableID);
		this._subject.ReplaceUnboundVariables(variableID);
		this._action.ReplaceUnboundVariables(variableID);
		if(this._target != null)
		{
			this._target.ReplaceUnboundVariables(variableID);
		}
		
		ListIterator li = this._parameters.listIterator();
		while(li.hasNext())
		{
			((Symbol) li.next()).ReplaceUnboundVariables(variableID);
		}
	}

	public Object Ground(ArrayList bindingConstraints) {
		
		RecentEventCondition event = (RecentEventCondition) this.clone();
		event.MakeGround(bindingConstraints);
		return event;
	}

	public void MakeGround(ArrayList bindings) {
		this._name.MakeGround(bindings);
		this._subject.MakeGround(bindings);
		this._action.MakeGround(bindings);
		if(this._target != null)
		{
			this._target.MakeGround(bindings);
		}
		
		ListIterator li = this._parameters.listIterator();
		while(li.hasNext())
		{
			((Symbol) li.next()).MakeGround(bindings);
		}
	}

	public Object Ground(Substitution subst) {
		RecentEventCondition event = (RecentEventCondition) this.clone();
		event.MakeGround(subst);
		return event;
	}

	public void MakeGround(Substitution subst) {
		this._name.MakeGround(subst);
		this._subject.MakeGround(subst);
		this._action.MakeGround(subst);
		if(this._target != null)
		{
			this._target.MakeGround(subst);
		}
		
		ListIterator li = this._parameters.listIterator();
		while(li.hasNext())
		{
			((Symbol) li.next()).MakeGround(subst);
		}
	}
	
	/**
	 * Checks if the EventCondition is verified in the agent's AutobiographicalMemory
	 * @return true if the EventPredicate is verified, false otherwise
	 * @see AutobiographicalMemory
	 */
	public boolean CheckCondition() {
		
		if(!_name.isGrounded()) return false;
		
		//Meiyii 12/03/09 Search for recent events in both STM and AM
		// If recentEventCondition is tested only against STM, goal will be reset after 
		// the event has been transferred to AM (this is useful if we want goal to decay over time)
		// If recentEventCondition is tested only against AM, goal will be reset 
		// continuously before the event is being transferred from STM to AM 
		return _positive == ShortTermMemory.GetInstance().ContainsRecentEvent(GetSearchKeys())
			|| AutobiographicalMemory.GetInstance().ContainsRecentEvent(GetSearchKeys()); 
	}
	
	protected ArrayList GetPossibleBindings()
	{
		//Meiyii 12/03/09 Search for recent events in both STM and AM
		ArrayList events = ShortTermMemory.GetInstance().SearchForRecentEvents(GetSearchKeys());
		events.addAll(AutobiographicalMemory.GetInstance().SearchForRecentEvents(GetSearchKeys()));	
		return events;
	}
	
	
	protected ArrayList GetSearchKeys()
	{
		ArrayList keys = super.GetSearchKeys();
		
		//we only want to search for events that happened at most 1 second before
		//keys.add(new SearchKey(SearchKey.MAXELAPSEDTIME, new Long(1000)));
		
		return keys;
	}
	
}
