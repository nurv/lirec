/** 
 * MemoryEpisode.java - 
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
 * Created: 18/Jul/2006 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 18/Jul/2006 - File created
 * João Dias: 14/01/2007 - Added setLocation method
 * João Dias: 16/01/2007 - Removed the abstract field and corresponding Getter
 * 			 			 - Restructured the generation of the summary of an event. Defined new
 * 						   methods that construct the summary and return an xml description
 *						   ready to be used by the LanguageEngine 
 * **/

package FAtiMA.memory.autobiographicalMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import FAtiMA.deliberativeLayer.goals.Goal;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.memory.ActionDetail;
import FAtiMA.memory.ActionDetailComparator;
import FAtiMA.memory.SearchKey;
import FAtiMA.memory.Time;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.EmotionValence;
import FAtiMA.memory.Memory;


public class MemoryEpisode implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//private String _abstract;
	private Time _time;
	private ArrayList _people;
	private String _location;
	private ArrayList _objects;
	//private HashMap _detailsByKey;
	private ArrayList _details;
	
	private int _numberOfDominantActions;
	//private ArrayList _dominantActions;
	
	public MemoryEpisode(String location, Time time)
	{
		this._location = location;
		this._time = time;
		this._people = new ArrayList();
		this._objects = new ArrayList();
		//this._detailsByKey = new HashMap();
		this._details = new ArrayList();
		this._numberOfDominantActions = 3;
		//this._dominantActions = new ArrayList(this._numberOfDominantActions);	
	}
	
	/*public MemoryEpisode(String location)
	{
		this._location = location;
		this._time = null;
		this._people = new ArrayList();
		this._objects = new ArrayList();
		//this._detailsByKey = new HashMap();
		this._details = new ArrayList();
		this._numberOfDominantActions = 3;
		//this._dominantActions = new ArrayList(this._numberOfDominantActions);	
	}*/
	
	
	/*public String getAbstract() 
	{
		return this._abstract;
	}*/
	
	public void setTime(Time time)
	{
		this._time = time;
	}
	
	public Time getTime()
	{
		return this._time;
	}
	
	public ArrayList getPeople()
	{
		return this._people;
	}
	
	public String getLocation()
	{
		return this._location;
	}
	
	public void setLocation(String location)
	{
		this._location = location;
	}
	
	public ArrayList getObjects()
	{
		return this._objects;
	}
	
	public ArrayList getDetails()
	{
		return this._details;
	}
	
	// not called 
	public ActionDetail getActionDetail(int actionID)
	{
		if(actionID < 0 || actionID >= this._details.size())
		{
			return null;
		}
		else return (ActionDetail) this._details.get(actionID);
	}
	
	public void AddActionDetail(ActionDetail action)
	{		
		_details.add(action);
			
		UpdateMemoryFields(action);
	}
	
	private void UpdateMemoryFields(ActionDetail action)
	{
		AddPeople(action.getSubject());
		
		Object aux = action.getTargetDetails("type");
		if(new String("object").equals(aux))
		{
			AddObject(action.getTarget());
		}
		else if(new String("character").equals(aux))
		{
			AddPeople(action.getTarget());
		}
	}
	
	/*
	private void UpdateAbstract()
	{
		Random random = new Random();
		ActionDetail action;
		BaseEmotion strongestEmotion = null;
		BaseEmotion secondStrongestEmotion = null;
		int numberOfDetails = _numberOfDominantActions + random.nextInt(3);
		
		List auxList = (List) _details.clone();
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
					if(secondStrongestEmotion.GetType() != strongestEmotion.GetType())
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
		
		ListIterator li = auxList.listIterator();
		this._abstract = "";
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
			if(action.getEmotion().GetPotential() > 0)
			{
				this._abstract = this._abstract + BuildDescription(action);
				
				if(strongestEmotion != null &&
						action.getEmotion().GetType() == strongestEmotion.GetType() &&
						action.getEmotion().GetPotential() == strongestEmotion.GetPotential())
				{
					this._abstract += " and I felt " + BuildFeelingDescription(strongestEmotion);
				}
				
				if(secondStrongestEmotion != null &&
						action.getEmotion().GetType() == secondStrongestEmotion.GetType() &&
						action.getEmotion().GetPotential() == secondStrongestEmotion.GetPotential())
				{
					this._abstract += " which made me feel " + BuildFeelingDescription(secondStrongestEmotion);
				}
				
				
				this._abstract += ", ";
			}
		}
		
		if(!this._abstract.equals(""))
		{
			this._abstract = this._abstract.substring(0,this._abstract.length()-2);
		}	
	}
	*/
	
	private ArrayList FilterInternalEvents(ArrayList events)
	{
		ActionDetail action;
		ArrayList newList = new ArrayList();
		for(ListIterator li = events.listIterator();li.hasNext();)
		{
			action = (ActionDetail) li.next();
			if(action.getAction().equals("activate") || 
					action.getAction().equals("succeed") ||
					action.getAction().equals("fail"))
			{
				newList.add(action);
			}
		}
		return newList;
	}
	
	private ArrayList FilterExternalEvents(ArrayList events)
	{
		ActionDetail action;
		ArrayList newList = new ArrayList();
		for(ListIterator li = events.listIterator();li.hasNext();)
		{
			action = (ActionDetail) li.next();
			if(!action.getAction().equals("activate") && 
					!action.getAction().equals("succeed") &&
					!action.getAction().equals("fail"))
			{
				newList.add(action);
			}
		}
		return newList;
	}
	
	public String GenerateSummary(Memory m)
	{
		Random random = new Random();
		ActionDetail action;
		BaseEmotion strongestEmotion = null;
		BaseEmotion secondStrongestEmotion = null;
		int numberOfDetails = _numberOfDominantActions;
		
		// version with both internal and external events
		List auxList = (List) _details.clone();
		// version with only internal events
		//List auxList = (List) FilterInternalEvents(_details);
		// version with only external events
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
					if(secondStrongestEmotion.GetType() != strongestEmotion.GetType())
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
		
		ListIterator li = auxList.listIterator();
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
			if(action.getEmotion().GetPotential() > 0)
			{
				AMSummary += "<Event>";
				if(firstEvent)
				{
					AMSummary +="<Location>" + this._location + "</Location>";
					AMSummary += SummaryGenerator.generateTimeDescription(this._time.getElapsedNarrativeTime());
					firstEvent = false;
				}
				
				AMSummary += SummaryGenerator.GenerateActionSummary(m, action);
				
				if(strongestEmotion != null &&
						action.getEmotion().GetType() == strongestEmotion.GetType() &&
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
	
	public float determineEmotionAverage()
	{
	
		ListIterator li;
		ActionDetail action;
		BaseEmotion em;
		float value = 0;
		int numberOfEmotionalEvents=0;
		
		//determine the average intensity of emotions in the episode
		for(li = this._details.listIterator();li.hasNext();)
		{
			action = (ActionDetail) li.next();
			em = action.getEmotion();
			if(em.GetPotential() > 0)
			{
				if(em.GetValence() == EmotionValence.POSITIVE)
				{
					value += em.GetPotential();
				}
				else
				{
					value -= em.GetPotential();
				}
				
				numberOfEmotionalEvents++;
			}
		}
		
		if(numberOfEmotionalEvents == 0)
		{
			return 0;
		}
		else
		{
			return value/numberOfEmotionalEvents;
		}
	}
	
	public float determineEmotionStdDeviation()
	{
		ListIterator li;
		ActionDetail action;
		BaseEmotion em;
		float quadraticError = 0;
		float error;
		int numberOfEmotionalEvents=0;
		float avg = this.determineEmotionAverage();
		
		//determine the standard deviation of emotion intensity in the episode 
		for(li = this._details.listIterator();li.hasNext();)
		{
			action = (ActionDetail) li.next();
			em = action.getEmotion();
			if(em.GetPotential() > 0)
			{
				if(em.GetValence() == EmotionValence.POSITIVE)
				{
					error = em.GetPotential() - avg;
				}
				else
				{
					error = - em.GetPotential() - avg;
				}
				
				quadraticError += Math.pow(error, 2);
				
				numberOfEmotionalEvents++;
			}
		}
		
		if(numberOfEmotionalEvents <= 1)
		{
			return 0;
		}
		else
		{
			return (float) Math.sqrt(quadraticError / (numberOfEmotionalEvents - 1));
		}	
	}
	
	public void AddPeople(String subject)
	{
		if(subject != null)
		{
			if(!this._people.contains(subject))
			{
				this._people.add(subject);
			}
		}
	}
	
	public void AddObject(String objectName)
	{
		if(objectName != null)
		{
			if(!this._objects.contains(objectName))
			{
				this._objects.add(objectName);
			}
		}
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
	
	public float AssessFamiliarity(Event event)
	{
		ActionDetail action;
		ListIterator li = _details.listIterator();
		float familiarity = 0;
		
		while(li.hasNext())
		{
			action = (ActionDetail) li.next();
			if(action.getAction().equals(event.GetAction()))
			{
				//if I've seen the action before, I'm sligthly familiar 
				
				familiarity += 0.2;
				
				if(action.getSubject().equals(Constants.SELF))
				{
					//if the event was performed by me, I'm more familiar with it
					familiarity += 0.4; 
					
				}
				
				if(action.getTarget() != null)
				{
					if(action.getTarget().equals(event.GetTarget()))
					{
						familiarity += 0.2;
					}
				}
				else if(event.GetTarget() == null)
				{
					familiarity += 0.2;
				}
				
				if(action.getParameters().toString().equals(event.GetParameters().toString()))
				{
					familiarity += 0.2;
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
		short field = k.getField();
		
		if(field == SearchKey.PEOPLE)
		{
			return this._people.contains(k.getKey());
		}
		else if(field == SearchKey.LOCATION)
		{
			return this._location.equals(k.getKey());
		}
		else if(field == SearchKey.OBJECTS)
		{
			return this._objects.contains(k.getKey());
		}
		else
		{
			li = this._details.listIterator();
			while(li.hasNext())
			{
				action = (ActionDetail) li.next();
				if(action.verifiesKey(k)) return true;
			}
			return false;
		}
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
	
	public String toXML()
	{
		ActionDetail detail;
		String episode = "<Episode>";
		episode += "<Location>" + this._location + "</Location>";
		episode += "<Time>" + this._time + "</Time>";
		episode += "<People>" + this._people + "</People>";
		episode += "<Objects>" + this._objects + "</Objects>";
		for(ListIterator li = _details.listIterator();li.hasNext();)
		{
			detail = (ActionDetail) li.next();
			episode += detail.toXML();
		}
		episode += "</Episode>\n";
		
		return episode;
	}
}
