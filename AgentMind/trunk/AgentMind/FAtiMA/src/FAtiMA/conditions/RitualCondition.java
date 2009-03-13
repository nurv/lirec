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

package FAtiMA.conditions;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

import FAtiMA.memory.SearchKey;
import FAtiMA.memory.Memory;
import FAtiMA.memory.shortTermMemory.ShortTermMemory;
import FAtiMA.sensorEffector.Event;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;


/**
 * @author João Dias
 *
 */

public class RitualCondition extends PredicateCondition {
	
	boolean _repeat; 
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final RitualCondition ParseRitualCondition(Attributes attributes)
	{
		ArrayList roles = new ArrayList();
    	String aux;
    	Symbol ritualName = null;
    	
    	aux = attributes.getValue("name");
    	if(aux != null)
    	{
    		ritualName = new Symbol(aux);
    	}
    	
    	aux = attributes.getValue("roles");
		
		if(aux != null) {
			StringTokenizer st = new StringTokenizer(aux, ",");
			while(st.hasMoreTokens()) {
				roles.add(new Symbol(st.nextToken()));
			}
		}
		
		return new RitualCondition(ritualName,roles);
	}
	
	protected ArrayList _roles;
	protected Symbol _ritualName;

	protected RitualCondition()
	{
	}
	
	public RitualCondition(Symbol ritualName, ArrayList roles)
	{
		this._ritualName = ritualName;
		this._positive = true;
		this._roles = (ArrayList) roles.clone();
		
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
		
		this._name = Name.ParseName(name);
	}

	public Object clone() {
		
		RitualCondition rc = new RitualCondition();
		
		rc._repeat = this._repeat;
		rc._positive = this._positive;
		rc._ritualName = (Symbol) this._ritualName.clone();
		rc._name = (Name) this._name.clone();
		
		rc._roles = new ArrayList(this._roles.size());
		ListIterator li = this._roles.listIterator();
		
		while(li.hasNext())
		{
			rc._roles.add(((Symbol)li.next()).clone());
		}
		
		return rc;
	}

	public Object GenerateName(int id) {
		RitualCondition rc = (RitualCondition) this.clone();
		rc.ReplaceUnboundVariables(id);
		return rc;
	}

	public void ReplaceUnboundVariables(int variableID) {
		this._name.ReplaceUnboundVariables(variableID);
		this._ritualName.ReplaceUnboundVariables(variableID);
		
		ListIterator li = this._roles.listIterator();
		while(li.hasNext())
		{
			((Symbol) li.next()).ReplaceUnboundVariables(variableID);
		}
	}

	public Object Ground(ArrayList bindingConstraints) {
		
		RitualCondition rc = (RitualCondition) this.clone();
		rc.MakeGround(bindingConstraints);
		return rc;
	}

	public void MakeGround(ArrayList bindings) {
		this._name.MakeGround(bindings);
		this._ritualName.MakeGround(bindings);
				
		ListIterator li = this._roles.listIterator();
		while(li.hasNext())
		{
			((Symbol) li.next()).MakeGround(bindings);
		}
	}

	public Object Ground(Substitution subst) {
		RitualCondition rc = (RitualCondition) this.clone();
		rc.MakeGround(subst);
		return rc;
	}

	public void MakeGround(Substitution subst) {
		this._name.MakeGround(subst);
		this._ritualName.MakeGround(subst);
		
		ListIterator li = this._roles.listIterator();
		while(li.hasNext())
		{
			((Symbol) li.next()).MakeGround(subst);
		}
	}
	
	/**
	 * Checks if the RitualCondition is verified in the agent's AutobiographicalMemory
	 * @return true if the RitualCondition is verified, false otherwise
	 * @see AutobiographicalMemory
	 */
	public boolean CheckCondition() {
		
		if(!_name.isGrounded()) return false;
		
		ArrayList keys = new ArrayList();
		
		keys.add(new SearchKey(SearchKey.SUBJECT,Memory.GetInstance().getSelf()));
		
		keys.add(new SearchKey(SearchKey.ACTION,"succeed"));
		
		keys.add(new SearchKey(SearchKey.TARGET,this._ritualName.toString()));
		
		for(int i = 0; i < _roles.size(); i++)
		{
			keys.add(new SearchKey(SearchKey.CONTAINSPARAMETER,_roles.get(i).toString()));
			
		}
		
		if(this._repeat){
			keys.add(new SearchKey(SearchKey.MAXELAPSEDTIME, new Long(1000)));
		}
		
		
		return ShortTermMemory.GetInstance().ContainsRecentEvent(keys);
		
	}
	
	protected ArrayList GetPossibleBindings()
	{
		return null;
	}

	public void setRepeat(boolean repeat) {
		_repeat = repeat;
		
	}
}