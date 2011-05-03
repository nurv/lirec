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
 * Matthias Keysermann: 09/03/2011 - added retrieval storage to VerifiesKey/s, GetDetailsByKey/s
 * Matthias Keysermann: 07/04/2011 - added GetEventID 
 */

package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
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
	
	public static final short MAXRECORDS = 10000;
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
	
	/*
	 * Meiyii
	 * Called only during loading - Add event to the details list without further processing 
	 */
	public void putActionDetail(ActionDetail ad)
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

	// 07/04/11 Matthias
	public int GetEventID() {
		return ShortTermEpisodicMemory.eventID;
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
	
	public BaseEmotion getStrongestEmotion()
	{
		BaseEmotion em;
		BaseEmotion strongestEmotion = null;
		
		for(ActionDetail action : this._details)
		{
			em = action.getEmotion();
			if(em != null)
			{
				if(strongestEmotion == null || em.GetPotential() > strongestEmotion.GetPotential())
				{
					strongestEmotion = em;
				}
			}
		}
		
		return strongestEmotion;
	}
	
	private ArrayList<ActionDetail> FilterExternalEvents(ArrayList<ActionDetail> events)
	{
		ActionDetail action;
		ArrayList<ActionDetail> newList = new ArrayList<ActionDetail>();
		for(ListIterator<ActionDetail> li = events.listIterator();li.hasNext();)
		{
			action = li.next();
			if(action.getAction()!=null)
			{
				newList.add(action);
			}
		}
		return newList;
	}
	
	
	public String GenerateSummary(Memory m)
	{
		ActionDetail action;
		BaseEmotion strongestEmotion = null;
		BaseEmotion secondStrongestEmotion = null;
		int numberOfDetails = 3;
		
		Name locationKey = Name.ParseName(Constants.SELF + "(location)");
		String location = (String) m.getSemanticMemory().AskProperty(locationKey);
		
		if(location == null)
		{
			location = Constants.EMPTY_LOCATION;
		}
		
		// version with both internal and external events
		List<ActionDetail> auxList = new ArrayList<ActionDetail>(_details);
		// version with only internal events
		//List auxList = (List) FilterInternalEvents(_details);
		//// version with only external events
		//List auxList = (List) FilterExternalEvents(_details);
		// version with empty summary
		//List auxList = (List) new ArrayList();
		
		
		Collections.sort(auxList, new ActionDetailComparator(ActionDetailComparator.CompareByEmotionIntensity));
		if(auxList.size() > numberOfDetails)
		{
			auxList = auxList.subList(auxList.size()-numberOfDetails,auxList.size());
		}
		
		if(auxList.size() > 0) 
		{
			//determine the strongest feeling
			action = (ActionDetail) auxList.get(auxList.size()-1);
			strongestEmotion = action.getEmotion();
			
			if(auxList.size() > 1)
			{
				for(int i = auxList.size() - 2;i >= 0; i--)
				{
					action = (ActionDetail) auxList.get(i);
					secondStrongestEmotion = action.getEmotion();
					if(secondStrongestEmotion.getType() != strongestEmotion.getType())
					{
						break;
					}
					else 
					{
						secondStrongestEmotion = null;
					}
				}
			}
		}
		
		Collections.sort(auxList, new ActionDetailComparator(ActionDetailComparator.CompareByOrder));
		
		String AMSummary = "";
		boolean firstEvent = true;
		
		ListIterator<ActionDetail> li = auxList.listIterator();
		while(li.hasNext())
		{
			action = li.next();
			if(action.getEmotion().GetPotential() > 0)
			{
				AMSummary += "<Event>";
				if(firstEvent)
				{
					AMSummary +="<Location>" + location + "</Location>";
					AMSummary += SummaryGenerator.generateTimeDescription(1000);
					firstEvent = false;
				}
				
				AMSummary += SummaryGenerator.GenerateActionSummary(m, action);
				
				if(strongestEmotion != null &&
						action.getEmotion().getType() == strongestEmotion.getType() &&
						action.getEmotion().GetPotential() == strongestEmotion.GetPotential())
				{
					AMSummary += SummaryGenerator.GenerateEmotionSummary(m, strongestEmotion);
				}
				
				/*if(secondStrongestEmotion != null &&
						action.getEmotion().GetType() == secondStrongestEmotion.GetType() &&
						action.getEmotion().GetPotential() == secondStrongestEmotion.GetPotential())
				{
					AMSummary += SummaryGenerator.GenerateEmotionSummary(secondStrongestEmotion);
				}*/
				
				AMSummary += "</Event>";
			}
		}
		
		return AMSummary;
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
				// 09/03/11 - Matthias
				action.getRetrievalQueue().addRetrievalTime(new Time());
				// DEBUG
				//System.out.println("ShortTermEpisodicMemory.java: retrieval of detail " + action.getID());
				
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
			if(action.verifiesKey(k))
			{
				// 09/03/11 - Matthias
				action.getRetrievalQueue().addRetrievalTime(new Time());
				// DEBUG
				//System.out.println("ShortTermEpisodicMemory.java: retrieval of detail " + action.getID());
				
				return true;
			}
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
				// 09/03/11 - Matthias
				action.getRetrievalQueue().addRetrievalTime(new Time());
				// DEBUG
				//System.out.println("ShortTermEpisodicMemory.java: retrieval of detail " + action.getID());
				
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
				// 09/03/11 - Matthias
				action.getRetrievalQueue().addRetrievalTime(new Time());
				// DEBUG
				//System.out.println("ShortTermEpisodicMemory.java: retrieval of detail " + action.getID());
				
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
