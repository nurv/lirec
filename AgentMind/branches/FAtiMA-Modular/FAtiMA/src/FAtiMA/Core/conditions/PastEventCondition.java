/**
 * PastEventCondition.java - 
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
 * João Dias: 24/03/2008 - Restructure, changed the way EventConditions Hierarchy. Now, PastEventConditions
 * 						   is the super class, and RecentEventConditions is the child class
 */

package FAtiMA.Core.conditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.AutobiographicalMemory;
import FAtiMA.Core.memory.episodicMemory.SearchKey;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.util.enumerables.GoalEvent;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * @author User
 *
 */
public class PastEventCondition extends PredicateCondition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Symbol _subject;
	protected Symbol _action;
	protected Symbol _target;
	protected ArrayList<Symbol> _parameters;
	
	//Meiyii - 12/01/10
	protected short _type = -1;
	protected short _status = -1;
	
	
	/**
	 * Parses a RecentEventCondition given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the EventCondition Parsed
	 */
	public static PastEventCondition ParseEvent(Attributes attributes) {
		boolean occurred;
		Symbol subject;
		Symbol action;
		Symbol target = null;
		ArrayList<Symbol> parameters = new ArrayList<Symbol>();
		
		String aux;
		aux = attributes.getValue("occurred");
		if(aux != null)
		{
			occurred = Boolean.parseBoolean(aux);
		}
		else occurred = true;

		subject = new Symbol(attributes.getValue("subject"));
		action = new Symbol(attributes.getValue("action"));
		
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
			
		return new PastEventCondition(occurred,EventType.ACTION,ActionEvent.SUCCESS,subject,action,target,parameters);
	}

	protected PastEventCondition()
	{
	}
	
	public PastEventCondition(boolean occurred, short type, short status, Symbol subject, Symbol action, Symbol target, ArrayList<Symbol> parameters)
	{
		super(occurred,null,Constants.UNIVERSAL);
		this._type = type;
		this._status = status;
		this._subject = subject;
		this._action = action;
		this._target = target;
		this._parameters = parameters;
		
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
		
		this.setName(Name.ParseName("EVENT(" + aux + ")"));
	}
	
	public PastEventCondition(boolean occurred, Event e)
	{
		super(occurred,null,new Symbol(Constants.SELF));
		
		this._subject = new Symbol(e.GetSubject());
		this._action = new Symbol(e.GetAction());
		this._target = new Symbol(e.GetTarget());
		this._parameters = new ArrayList<Symbol>(e.GetParameters().size());
		
		//Meiyii - 12/01/10
		this._type = e.GetType();
		this._status = e.GetStatus();
		
		String aux = this._subject + "," + this._action;
		if(this._target != null)
		{
			aux = aux + "," + this._target;
		}
		
		Parameter p;
		ListIterator<Parameter> li = e.GetParameters().listIterator();
		while(li.hasNext())
		{
			p = li.next();
			_parameters.add(new Symbol(p.GetValue().toString()));
			aux = aux + "," + p.GetValue();
		}
		
		this.setName(Name.ParseName("EVENT(" + aux + ")"));
	}
	
	// Meiyii - 12/01/10 added type and status
	public PastEventCondition(boolean occurred, short type, short status, Name event)
	{
		super(occurred, event,new Symbol(Constants.SELF));
		
		ListIterator<Symbol> li = event.GetLiteralList().listIterator();
		li.next();
		this._subject = (Symbol) li.next();
		this._action = (Symbol) li.next();
		if(li.hasNext())
		{
			this._target = (Symbol) li.next();
		}
		this._parameters = new ArrayList<Symbol>();
		while(li.hasNext())
		{
			this._parameters.add(li.next());
		}		
		
		this._type = type;
		this._status = status;
	}
	
	public PastEventCondition(PastEventCondition pEC){
		super(pEC);
		
		_type = pEC._type;
		_status = pEC._status;
		_subject = (Symbol) pEC._subject.clone();
		_action = (Symbol) pEC._action.clone();
		if(pEC._target != null)
		{
			_target = (Symbol) pEC._target.clone();
		}
		_parameters = new ArrayList<Symbol>(pEC._parameters.size());
		
		for(Symbol p : pEC._parameters){
			_parameters.add((Symbol) p.clone());
		}
		
	}
	
	public Object clone() {
		return new PastEventCondition(this);
	}
	
	public boolean isGrounded()
	{
		if(!(super.isGrounded() && this._subject.isGrounded() && this._action.isGrounded()))
		{
			return false;
		}
		
		if(this._target != null)
		{
			if(!this._target.isGrounded()) return false; 
		}
		
		for(Symbol s : this._parameters)
		{
			if(!s.isGrounded())
			{
				return false;
			}
		}
		
		return true; 
	}
	
	public void ReplaceUnboundVariables(int variableID) {
		super.ReplaceUnboundVariables(variableID);
		
		this._subject.ReplaceUnboundVariables(variableID);
		this._action.ReplaceUnboundVariables(variableID);
		if(this._target != null)
		{
			this._target.ReplaceUnboundVariables(variableID);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().ReplaceUnboundVariables(variableID);
		}
	}

	
	public void MakeGround(ArrayList<Substitution> bindings) {
		super.MakeGround(bindings);
	
		this._subject.MakeGround(bindings);
		this._action.MakeGround(bindings);
		if(this._target != null)
		{
			this._target.MakeGround(bindings);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().MakeGround(bindings);
		}
	}

	public void MakeGround(Substitution subst) {
		super.MakeGround(subst);
		
		this._subject.MakeGround(subst);
		this._action.MakeGround(subst);
		if(this._target != null)
		{
			this._target.MakeGround(subst);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().MakeGround(subst);
		}
	}
	
	protected ArrayList<ActionDetail> GetPossibleBindings(AgentModel am)
	{
		return am.getMemory().getEpisodicMemory().SearchForPastEvents(GetSearchKeys());
	}
	
	/**
	 * This method finds all the possible sets of Substitutions that applied to the condition
     * will make it valid (true) according to the agent's AutobiographicalMemory 
     * @return A list with all SubstitutionsSets that make the condition valid
	 * @see AutobiographicalMemory
	 */
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		ActionDetail detail;
		Substitution sub;
		SubstitutionSet subSet;
		Symbol param;
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		ArrayList<ActionDetail> details;
		
		if (getName().isGrounded()) {
			if(CheckCondition(am))
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		details = GetPossibleBindings(am);
		
		//we cannot determine bindings for negative event conditions,
		//assume false
		if(!getPositive())
		{
			if(details.size() == 0) 
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		if(details.size() == 0) return null;
		
		Iterator<ActionDetail> it = details.iterator();
		while(it.hasNext())
		{
			detail = (ActionDetail) it.next();
			subSet = new SubstitutionSet();
			
			if(!this._subject.isGrounded())
			{
				sub = new Substitution(this._subject,new Symbol(detail.getSubject()));
				subSet.AddSubstitution(sub);
			}
			// Meiyii 19/01/10
			if(this._type == EventType.GOAL)
			{
				if(!this._action.isGrounded())
				{
					sub = new Substitution(this._action,new Symbol(detail.getIntention()));
					subSet.AddSubstitution(sub);
				}
			}
			else 
			{
				if(!this._action.isGrounded())
				{
					sub = new Substitution(this._action,new Symbol(detail.getAction()));
					subSet.AddSubstitution(sub);
				}
			}
			if(this._target != null && !this._target.isGrounded())
			{
				sub = new Substitution(this._target,new Symbol(detail	.getTarget()));
				subSet.AddSubstitution(sub);
			}
			
			for(int i=0; i < this._parameters.size(); i++)
			{
				param = (Symbol) this._parameters.get(i);
				if(!param.isGrounded())
				{
					sub = new Substitution(param, new Symbol(detail.getParameters().get(i).toString()));
					subSet.AddSubstitution(sub);
				}
			}
			bindingSets.add(subSet);
		}
		return bindingSets;
	}
	
	/**
	 * Checks if the EventCondition is verified in the agent's AutobiographicalMemory
	 * @return true if the PastPredicate is verified, false otherwise
	 * @see AutobiographicalMemory
	 */
	public boolean CheckCondition(AgentModel am) {
		
		if(!(getName().isGrounded())) return false;
		
		return getPositive() == am.getMemory().getEpisodicMemory().ContainsPastEvent(GetSearchKeys()); 
	}
	
	protected ArrayList<SearchKey> GetSearchKeys()
	{
		Symbol param;
		
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		if(this._subject.isGrounded())
		{
			keys.add(new SearchKey(SearchKey.SUBJECT,this._subject.toString()));
		}
		
		//Meiyii 19/01/10
		if(this._action.isGrounded())
		{
			if(this._type == EventType.GOAL)
			{
				keys.add(new SearchKey(SearchKey.INTENTION,this._action.toString()));
			}
			else
			{
				keys.add(new SearchKey(SearchKey.ACTION,this._action.toString()));
			}
		}
		if(this._status >= 0)
		{
			if(this._type == EventType.GOAL)
			{
				keys.add(new SearchKey(SearchKey.STATUS, GoalEvent.GetName(this._status)));
			}
			else
			{
				keys.add(new SearchKey(SearchKey.STATUS, ActionEvent.GetName(this._status)));
			}
		}
	
		if(this._target != null && this._target.isGrounded())
		{
			keys.add(new SearchKey(SearchKey.TARGET, this._target.toString()));
		}
		if(this._parameters.size() > 0)
		{
			ArrayList<String> params = new ArrayList<String>();
			for(ListIterator<Symbol> li = this._parameters.listIterator();li.hasNext();)
			{
				param = (Symbol) li.next();
				if(param.isGrounded())
				{
					params.add(param.toString());
				}
				else
				{
					params.add("*");
				}
			}
			keys.add(new SearchKey(SearchKey.PARAMETERS, params));
		}		
		
		return keys;
	}

}
