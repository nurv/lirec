/** 
 * STMemoryRecord.java - Record structure for recent events  
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
 * Company: HWU
 * Project: LIREC
 * Created: 11/03/2009 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 11/03/09 - File created
 * 
 * **/

package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.deliberativeLayer.goals.Goal;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Substitution;


/**
 * Record structure for recent events
 * 
 * @author Meiyii Lim
 */

public class ShortTermEpisodicMemory implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int eventID = 0;
	
	public static final short MAXRECORDS = 10;
	private ArrayList<ActionDetail> _details;
	
	public ShortTermEpisodicMemory()
	{
		this._details = new ArrayList<ActionDetail>(MAXRECORDS);
	}
	
	public ArrayList<ActionDetail> getDetails()
	{
		return this._details;
	}
	
	public void AddActionDetail(Memory m, Event e, String location)
	{
		ActionDetail action;
			
		action = new ActionDetail(m, ShortTermEpisodicMemory.eventID++, e, location);
		_details.add(action);
	}
	
	public void AddActionDetail(ActionDetail ad)
	{
		_details.add(ad);
	}
	
	public void applySubstitution(Substitution s)
	{
		for(ActionDetail detail : _details)
		{
			detail.applySubstitution(s);
		}
	}
	
	public int GetCount()
	{
		return this._details.size();
	}
	
	public void SetEventID(int eventID)
	{
		ShortTermEpisodicMemory.eventID = eventID;
	}
	
	public void ResetEventID()
	{
		ShortTermEpisodicMemory.eventID = 0;
	}
	
	public ActionDetail GetNewestRecord()
	{
		return (ActionDetail) this._details.get(_details.size()-1);
	}
	
	public ActionDetail GetOldestRecord()
	{
		return (ActionDetail) this._details.get(0);
	}
	
	public void DeleteOldestRecord()
	{
		this._details.remove(0);
	}
	
	public void AssociateEmotionToDetail(Memory m, ActiveEmotion em, Event cause, String location)
	{
		ActionDetail action;
		if(cause != null)
		{
			
			for(int i = _details.size() -1; i >= 0; i--){
				action = _details.get(i);
				if(action.ReferencesEvent(cause))
				{
					action.UpdateEmotionValues(m, em);
					return;
				}
			}
	
			//action = new ActionDetail(m,eventID++,cause,location);
			//_details.add(action);
			//action.UpdateEmotionValues(m, em);
		}
	}	
	
	public boolean VerifiesKeys(ArrayList<SearchKey> searchKeys)
	{
		ActionDetail action;
		ListIterator<ActionDetail> li = _details.listIterator();
		
		while(li.hasNext())
		{
			action = li.next();
			if(action.verifiesKeys(searchKeys))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean VerifiesKey(SearchKey k)
	{
		ListIterator<ActionDetail> li;
		ActionDetail action;
		
		li = this._details.listIterator();
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
			if(action.verifiesKey(k)) return true;
		}
		return false;
	}	
	
	public ArrayList<ActionDetail> GetDetailsByKey(SearchKey key)
	{
		ListIterator<ActionDetail> li;
		ActionDetail action;
		ArrayList<ActionDetail> details = new ArrayList<ActionDetail>();
		
		li = this._details.listIterator();
		while(li.hasNext())
		{
			action =  li.next();
			if(action.verifiesKey(key)) 
			{
				details.add(action);
			}
		}
		
		return details;
	}
	
	public ArrayList<ActionDetail> GetDetailsByKeys(ArrayList<SearchKey> keys)
	{
		ListIterator<ActionDetail> li;
		ActionDetail action;
		ArrayList<ActionDetail> details = new ArrayList<ActionDetail>();
		
		li = this._details.listIterator();
		while(li.hasNext())
		{
			action = li.next();
			if(action.verifiesKeys(keys) && !details.contains(action)) 
			{
				details.add(action);
			}
		}
		
		return details;
	}
	
	public float AssessGoalFamiliarity(Goal g)
	{
		float familiarity = 0;
		ActionDetail action;
		ListIterator<ActionDetail> li = _details.listIterator();
		Event e = g.GetActivationEvent();
		
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
			if(action.getAction().equals(e.GetAction()))
			{
				//ok, we've found a goal activation				
				if(action.getTarget().equals(e.GetTarget()))
				{
					// and the goal is of the same class of the one we're searching for
					// it seems familiar
					familiarity += 0.5; 
					
					if(action.getParameters().toString().equals(e.GetParameters().toString()))
					{
						//and the parameters of the goal are also equal which makes the 
						//goal even more familiar
						familiarity +=0.5;
					}
				}				
			}
		}		
		return familiarity;
	}
	
	public int CountEvent(ArrayList<SearchKey> searchKeys)
	{
		ListIterator<ActionDetail> li;
		ActionDetail action;
		int count = 0;
		
		li = this._details.listIterator();
		while(li.hasNext())
		{
			action = li.next();
			if(action.verifiesKeys(searchKeys)) 
			{
				count++;
			}
		}
		
		return count;
	}
	
	public String toXML()
	{
		ActionDetail detail;
		String record = "<STEpisodicMemory>";
		for(ListIterator<ActionDetail> li = _details.listIterator();li.hasNext();)
		{
			detail = li.next();
			record += detail.toXML();
		}
		record += "</STEpisodicMemory>\n";
		
		return record;
	}
}
