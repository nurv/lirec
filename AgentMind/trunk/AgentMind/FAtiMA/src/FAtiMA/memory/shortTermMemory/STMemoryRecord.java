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

package FAtiMA.memory.shortTermMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.deliberativeLayer.goals.Goal;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.memory.ActionDetail;
import FAtiMA.memory.SearchKey;
import FAtiMA.memory.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.enumerables.EmotionType;


/**
 * Record structure for recent events
 * 
 * @author Meiyii Lim
 */

public class STMemoryRecord implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int eventID = 0;
	
	public static final short MAXRECORDS = 10;
	private ArrayList _details;
	
	public STMemoryRecord()
	{
		this._details = new ArrayList(MAXRECORDS);
	}
	
	public ArrayList getDetails()
	{
		return this._details;
	}
	
	public void AddActionDetail(Event e, String location)
	{
		ActionDetail action;
		//System.out.println("EventID: " + eventID);
		
		action = new ActionDetail(eventID++,e,location);
				
		System.out.println("Action added: " + action.toXML());
		
		_details.add(action);
			
		//UpdateMemoryFields(action);
	}
	
	public int GetCount()
	{
		return this._details.size();
	}
	
	public void SetEventID(int eventID)
	{
		this.eventID = eventID;
	}
	
	public void ResetEventID()
	{
		this.eventID = 0;
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
	
	public void AssociateEmotionToDetail(ActiveEmotion em, Event cause, String location)
	{
		ActionDetail action;
		if(cause != null)
		{
			String key = cause.toString();
			
			for(int i = _details.size() -1; i >= 0; i--){
				action = (ActionDetail) _details.get(i);
				if(action.ReferencesEvent(cause))
				{
					action.UpdateEmotionValues(em);
					return;
				}
			}
			
			// if we get here it means that the event doesn't exist in the records
			// and we need to add it but first check if the number of records has 
			// already reached its maximum
			if(this.GetCount() == this.MAXRECORDS)
			{
				ActionDetail detail = this.GetOldestRecord();
				
				if((detail.getAction().equals("activate") || 
						detail.getAction().equals("succeed") ||
						detail.getAction().equals("fail")) ||
						((!detail.getAction().equals("activate") && 
						!detail.getAction().equals("succeed") &&
						!detail.getAction().equals("fail")) &&
						(detail.getEmotion().GetType()) != EmotionType.NEUTRAL))
				{
					AutobiographicalMemory.GetInstance().StoreAction(detail);
					System.out.println("Record transferred to AM: " + detail.toXML());
				}
				this.DeleteOldestRecord();
			}
			action = new ActionDetail(eventID++,cause,location);
			_details.add(action);
			action.UpdateEmotionValues(em);
		}
	}	
	
	public boolean VerifiesKeys(ArrayList searchKeys)
	{
		ActionDetail action;
		ListIterator li = _details.listIterator();
		
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
			if(action.verifiesKeys(searchKeys))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public boolean VerifiesKey(SearchKey k)
	{
		ListIterator li;
		ActionDetail action;
		
		li = this._details.listIterator();
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
			if(action.verifiesKey(k)) return true;
		}
		return false;
	}	
	
	public ArrayList GetDetailsByKey(SearchKey key)
	{
		ListIterator li;
		ActionDetail action;
		ArrayList details = new ArrayList();
		
		li = this._details.listIterator();
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
			if(action.verifiesKey(key)) 
			{
				details.add(action);
			}
		}
		
		return details;
	}
	
	public ArrayList GetDetailsByKeys(ArrayList keys)
	{
		ListIterator li;
		ActionDetail action;
		ArrayList details = new ArrayList();
		
		li = this._details.listIterator();
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
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
		ListIterator li = _details.listIterator();
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
	
	public int CountEvent(ArrayList searchKeys)
	{
		ListIterator li;
		ActionDetail action;
		int count = 0;
		
		li = this._details.listIterator();
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
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
		String record = "<Record>";
		for(ListIterator li = _details.listIterator();li.hasNext();)
		{
			detail = (ActionDetail) li.next();
			record += detail.toXML();
		}
		record += "</Record>\n";
		
		return record;
	}
}
