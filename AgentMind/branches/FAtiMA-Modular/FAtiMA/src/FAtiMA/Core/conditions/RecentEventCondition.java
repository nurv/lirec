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
 */

package FAtiMA.Core.conditions;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.AutobiographicalMemory;
import FAtiMA.Core.memory.episodicMemory.SearchKey;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * @author João Dias
 *
 */

public class RecentEventCondition extends PastEventCondition {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public RecentEventCondition(PastEventCondition cond)
	{
		super(cond);
	}
	
	//Meiyii - 12/01/12 added type and status 
	public RecentEventCondition(boolean occurred, short type, short status, Name event)
	{
		super(occurred, type, status, event);	
	}
	
	// not currently called
	public RecentEventCondition(boolean occurred, short type, short status, Symbol subject, Symbol action, Symbol target, ArrayList<Symbol> parameters)
	{
		super(occurred, type, status, subject, action, target, parameters);
	}
	
	// not currently called
	public RecentEventCondition(boolean occurred, Event e)
	{
		super(occurred, e);
	}

	public Object clone() {		
		return new RecentEventCondition(this);
	}
	
	/**
	 * Checks if the EventCondition is verified in the agent's AutobiographicalMemory
	 * @return true if the EventPredicate is verified, false otherwise
	 * @see AutobiographicalMemory
	 */
	public boolean CheckCondition(AgentModel am) {
		
		if(!getName().isGrounded()) return false;
		
		if(getPositive() == false){
			System.out.print("");
		}
		
		boolean result = getPositive() == am.getMemory().getEpisodicMemory().ContainsRecentEvent(GetSearchKeys()); 
 
		
		return result; 	
	}
	
	protected ArrayList<ActionDetail> GetPossibleBindings(AgentModel am)
	{
		return am.getMemory().getEpisodicMemory().
				SearchForRecentEvents(GetSearchKeys());
	}
	
	
	protected ArrayList<SearchKey> GetSearchKeys()
	{
		ArrayList<SearchKey> keys = super.GetSearchKeys();
		
		//we only want to search for events that happened at most 1 second before
		//keys.add(new SearchKey(SearchKey.MAXELAPSEDTIME, new Long(1000)));
		
		return keys;
	}
	
}
