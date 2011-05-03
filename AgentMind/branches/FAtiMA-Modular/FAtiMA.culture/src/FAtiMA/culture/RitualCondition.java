/**
 * RitualCondition.java - 
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
 * Created: 15/04/2008
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: 15/04/2008 - File created
  */

package FAtiMA.culture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.PredicateCondition;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.AutobiographicalMemory;
import FAtiMA.Core.memory.episodicMemory.SearchKey;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.PermutationGenerator;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * @author João Dias
 *
 */

public class RitualCondition extends PredicateCondition {
	

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final RitualCondition ParseRitualCondition(Attributes attributes)
	{
		ArrayList<Symbol> roles = new ArrayList<Symbol>();
    	String aux;
    	Symbol ritualName = null;
    	boolean occurred = true; //default
    	boolean repeat = false; // default    	
    	
    	aux = attributes.getValue("name");
    	if(aux != null)
    	{
    		ritualName = new Symbol(aux);
    	}
    	
    	if(attributes.getValue("repeat") != null){
			Boolean.parseBoolean(attributes.getValue("repeat"));
    	}

    	if(attributes.getValue("occurred") != null){
    		occurred = Boolean.parseBoolean(attributes.getValue("occurred"));
    	}
    	
 		aux = attributes.getValue("roles");
    	
    	
    	if(aux != null) {
			StringTokenizer st = new StringTokenizer(aux, ",");
			while(st.hasMoreTokens()) {
				Symbol role = new Symbol(st.nextToken());
				roles.add(role);
			}
		}
		
		return new RitualCondition(ritualName,roles,new Symbol("*"),occurred,repeat);
	}
	
	protected ArrayList<Symbol> _roles;
	protected Symbol _ritualName;
	boolean _repeat; 
	

	@SuppressWarnings("unchecked")
	public RitualCondition(Symbol ritualName, ArrayList<Symbol> roles, Symbol ToM, boolean occurred, boolean repeat)
	{
		super(occurred,null,ToM);
		this._ritualName = ritualName;
		this._roles = (ArrayList<Symbol>) roles.clone();
		
		String name = ritualName + "(";
		
		for(int i=0; i < _roles.size(); i++)
		{
			name+= _roles.get(i).toString();
			name+=",";
		}
		
		if(_roles.size() > 0)
		{
			name = name.substring(0,name.length()-1);
		}
		
		name+= ")";
		
		this.setName(Name.ParseName(name));
	}

	
	public RitualCondition(RitualCondition rC){
		super(rC);
		
		_ritualName = (Symbol) rC._ritualName.clone();
		_roles = new ArrayList<Symbol>();	
		for(Symbol role : rC._roles){
			_roles.add((Symbol)role.clone());
		}
		_repeat = rC._repeat;	
	}
	
	public Object clone() {
		return new RitualCondition(this);
	}

	public Object GenerateName(int id) {
		RitualCondition rc = (RitualCondition) this.clone();
		rc.ReplaceUnboundVariables(id);
		return rc;
	}

	public void ReplaceUnboundVariables(int variableID) {
		this.getName().ReplaceUnboundVariables(variableID);
		this._ritualName.ReplaceUnboundVariables(variableID);
		
		ListIterator<Symbol> li = this._roles.listIterator();
		while(li.hasNext())
		{
			li.next().ReplaceUnboundVariables(variableID);
		}
	}

	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		
		RitualCondition rc = (RitualCondition) this.clone();
		rc.MakeGround(bindingConstraints);
		return rc;
	}

	public void MakeGround(ArrayList<Substitution> bindings) {
		this.getName().MakeGround(bindings);
		this._ritualName.MakeGround(bindings);
				
		ListIterator<Symbol> li = this._roles.listIterator();
		while(li.hasNext())
		{
			li.next().MakeGround(bindings);
		}
	}

	public Object Ground(Substitution subst) {
		RitualCondition rc = (RitualCondition) this.clone();
		rc.MakeGround(subst);
		return rc;
	}

	public void MakeGround(Substitution subst) {
		this.getName().MakeGround(subst);
		this._ritualName.MakeGround(subst);
		
		ListIterator<Symbol> li = this._roles.listIterator();
		while(li.hasNext())
		{
			li.next().MakeGround(subst);
		}
	}
	
	/**
	 * Checks if the RitualCondition is verified in the agent's AutobiographicalMemory
	 * @return true if the RitualCondition is verified, false otherwise
	 * @see AutobiographicalMemory
	 */
	public boolean CheckCondition(AgentModel am) {
		boolean result = false;
		
		if(!getName().isGrounded()) return false;
		
		
		PermutationGenerator pGenerator = new PermutationGenerator(_roles.size());
//		ArrayList<SearchKey> searchKeys = getSearchKeys();
//		for (int i = 0; i < _roles.size(); i++) {
//			searchKeys.add(new SearchKey(SearchKey.CONTAINSPARAMETER,_roles.get(i).toString()));				
//		}
		
		while(pGenerator.hasMore()){
			int [] indices = pGenerator.getNext();
		
			ArrayList<SearchKey> searchKeys = getSearchKeys();
			for (int i = 0; i < indices.length; i++) {
				searchKeys.add(new SearchKey(SearchKey.CONTAINSPARAMETER,_roles.get(indices[i]).toString()));				
			}
			
			result = am.getMemory().getEpisodicMemory().ContainsRecentEvent(searchKeys);
				
			if(result ==  getPositive()){
				return result == getPositive();
			}
		}	
		
		
		return result == getPositive();
	}
	
	private ArrayList<SearchKey> getSearchKeys()
	{
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		
		keys.add(new SearchKey(SearchKey.STATUS,Goal.SUCCESSEVENT));
		
		keys.add(new SearchKey(SearchKey.INTENTION,this._ritualName.toString()));
		
		
		if(this._repeat){
			keys.add(new SearchKey(SearchKey.MAXELAPSEDTIME, new Long(10000)));
		}
		
		return keys;
	}
	
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
		
		details = am.getMemory().getEpisodicMemory().SearchForRecentEvents(getSearchKeys());
		
		if(details.size() == 0) return null;
		
		Iterator<ActionDetail> it = details.iterator();
		while(it.hasNext())
		{
			detail = (ActionDetail) it.next();
			subSet = new SubstitutionSet();
	
			PermutationGenerator pGenerator = new PermutationGenerator(_roles.size());
			
			//we are assuming that all roles are not grounded
			while(pGenerator.hasMore()){
				int [] indices = pGenerator.getNext();
				subSet = new SubstitutionSet();
				for (int i = 0; i < indices.length; i++) {
					sub = new Substitution(_roles.get(i),new Symbol(detail.getParameters().get(indices[i]).GetValue().toString()));
					subSet.AddSubstitution(sub);
				}
				bindingSets.add(subSet);
			}
		}
	
		return bindingSets;
		
	}
}