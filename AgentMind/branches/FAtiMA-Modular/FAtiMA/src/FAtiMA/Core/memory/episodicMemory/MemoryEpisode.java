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
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 18/Jul/2006 - File created
 * Jo�o Dias: 14/01/2007 - Added setLocation method
 * Jo�o Dias: 16/01/2007 - Removed the abstract field and corresponding Getter
 * 			 			 - Restructured the generation of the summary of an event. Defined new
 * 						   methods that construct the summary and return an xml description
 *						   ready to be used by the LanguageEngine
 * Matthias Keysermann: 08/03/2011 - added retrieval storage to VerifiesKey/s, GetDetailsByKey/s
 * Matthias Keysermann: 04/05/2011 - verifiesKey/s loops always over all details and then returns true/false
 *                                   retrievals for all matching details are created (instead of only the first)
 */

package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.EmotionValence;
import FAtiMA.Core.wellFormedNames.Substitution;

public class MemoryEpisode implements Serializable {

	private static final long serialVersionUID = 1L;

	// HashMaps for faster access to events
	private HashMap<String, ArrayList<ActionDetail>> subjectIndex;
	private HashMap<String, ArrayList<ActionDetail>> actionIndex;
	private HashMap<String, ArrayList<ActionDetail>> targetIndex;

	//private String _abstract;
	private Time _time;
	private ArrayList<String> _people;
	private ArrayList<String> _location; // changed from String to arraylist - Meiyii 13/09/10
	private ArrayList<String> _objects;
	private ArrayList<ActionDetail> _details;
	private int _numberOfDominantActions;

	public MemoryEpisode(String location, Time time) {
		this._location = new ArrayList<String>();
		if (!location.equals(""))
			this._location.add(location);
		this._time = time;
		this._people = new ArrayList<String>();
		this._objects = new ArrayList<String>();
		this._details = new ArrayList<ActionDetail>();
		this._numberOfDominantActions = 3;
		subjectIndex = new HashMap<String, ArrayList<ActionDetail>>();
		actionIndex = new HashMap<String, ArrayList<ActionDetail>>();
		targetIndex = new HashMap<String, ArrayList<ActionDetail>>();
	}

	/*
	 * Meiyii 22/12/10
	 * Called during loading
	 */
	public MemoryEpisode(ArrayList<String> location, ArrayList<String> people, ArrayList<String> objects) {
		this._location = location;
		this._people = people;
		this._objects = objects;
		this._time = new Time();
		this._details = new ArrayList<ActionDetail>();
		subjectIndex = new HashMap<String, ArrayList<ActionDetail>>();
		actionIndex = new HashMap<String, ArrayList<ActionDetail>>();
		targetIndex = new HashMap<String, ArrayList<ActionDetail>>();
	}

	public void applySubstitution(Substitution s) {
		for (ActionDetail detail : _details) {
			detail.applySubstitution(s);
		}
	}

	public void setTime(Time time) {
		this._time = time;
	}

	public Time getTime() {
		return this._time;
	}

	public ArrayList<String> getPeople() {
		return this._people;
	}

	public ArrayList<String> getLocation() {
		return this._location;
	}

	public ArrayList<String> getObjects() {
		return this._objects;
	}

	public ArrayList<ActionDetail> getDetails() {
		return this._details;
	}

	// not called 
	public ActionDetail getActionDetail(int actionID) {
		if (actionID < 0 || actionID >= this._details.size()) {
			return null;
		} else
			return (ActionDetail) this._details.get(actionID);
	}

	/*
	 * Meiyii
	 * Called only during loading - Add event to the details list without further processing 
	 */
	public void putActionDetail(ActionDetail ad) {
		_details.add(ad);

		// update HashMaps
		addToSubjectIndex(ad);
		addToActionIndex(ad);
		addToTargetIndex(ad);
	}

	public void AddActionDetail(ActionDetail ad) {
		putActionDetail(ad);
		UpdateMemoryFields(ad);
	}

	private void addToSubjectIndex(ActionDetail actionDetail) {
		synchronized (this) {
			String key = actionDetail.getSubject();
			ArrayList<ActionDetail> actionDetails = subjectIndex.get(key);
			if (actionDetails == null) {
				actionDetails = new ArrayList<ActionDetail>();
				subjectIndex.put(key, actionDetails);
			}
			actionDetails.add(actionDetail);
		}
	}

	private void addToActionIndex(ActionDetail actionDetail) {
		synchronized (this) {
			String key = actionDetail.getAction();
			ArrayList<ActionDetail> actionDetails = actionIndex.get(key);
			if (actionDetails == null) {
				actionDetails = new ArrayList<ActionDetail>();
				actionIndex.put(key, actionDetails);
			}
			actionDetails.add(actionDetail);
		}
	}

	private void addToTargetIndex(ActionDetail actionDetail) {
		synchronized (this) {
			String key = actionDetail.getTarget();
			ArrayList<ActionDetail> actionDetails = targetIndex.get(key);
			if (actionDetails == null) {
				actionDetails = new ArrayList<ActionDetail>();
				targetIndex.put(key, actionDetails);
			}
			actionDetails.add(actionDetail);
		}
	}

	public boolean RemoveAction(ActionDetail actionDetail) {
		synchronized (this) {

			// try to remove action detail from episode
			for (int i = 0; i < _details.size(); i++) {
				ActionDetail detail = _details.get(i);

				// compare pointers here
				// no ArrayList.remove(Object) as equals() is overwritten in ActionDetail
				if (detail == actionDetail) {

					// remove detail from episode
					_details.remove(i);

					// update HashMaps
					removeFromSubjectIndex(actionDetail);
					removeFromActionIndex(actionDetail);
					removeFromTargetIndex(actionDetail);

					// update episode summary (if desired)
					// currently not desired, all values stay in summary

					// delete empty episodes (if desired)
					//if (memoryEpisode.getDetails().size() == 0) {
					//	_memoryEpisodes.remove(memoryEpisode);
					//}

					return true;
				}

			}

			return false;
		}
	}

	private void removeFromSubjectIndex(ActionDetail actionDetail) {
		synchronized (this) {
			String key = actionDetail.getSubject();
			ArrayList<ActionDetail> actionDetails = subjectIndex.get(key);
			if (actionDetails != null) {
				for (int i = 0; i < actionDetails.size(); i++) {
					// compare pointers here
					// no ArrayList.remove(Object) as equals() is overwritten in ActionDetail
					if (actionDetails.get(i) == actionDetail) {
						actionDetails.remove(i);
						break;
					}
				}
				if (actionDetails.size() == 0) {
					subjectIndex.remove(key);
				}
			}
		}
	}

	private void removeFromActionIndex(ActionDetail actionDetail) {
		synchronized (this) {
			String key = actionDetail.getAction();
			ArrayList<ActionDetail> actionDetails = actionIndex.get(key);
			if (actionDetails != null) {
				for (int i = 0; i < actionDetails.size(); i++) {
					// compare pointers here
					// no ArrayList.remove(Object) as equals() is overwritten in ActionDetail
					if (actionDetails.get(i) == actionDetail) {
						actionDetails.remove(i);
						break;
					}
				}
				if (actionDetails.size() == 0) {
					actionIndex.remove(key);
				}
			}
		}
	}

	private void removeFromTargetIndex(ActionDetail actionDetail) {
		synchronized (this) {
			String key = actionDetail.getTarget();
			ArrayList<ActionDetail> actionDetails = targetIndex.get(key);
			if (actionDetails != null) {
				for (int i = 0; i < actionDetails.size(); i++) {
					// compare pointers here
					// no ArrayList.remove(Object) as equals() is overwritten in ActionDetail
					if (actionDetails.get(i) == actionDetail) {
						actionDetails.remove(i);
						break;
					}
				}
				if (actionDetails.size() == 0) {
					targetIndex.remove(key);
				}
			}
		}
	}

	public void UpdateMemoryFields(ActionDetail ad) {
		AddPeople(ad.getSubject());

		//check target type
		Object aux = ad.getTargetDetails("type");
		if (new String("object").equals(aux)) {
			AddObject(ad.getTarget());
		} else if (new String("character").equals(aux)) {
			AddPeople(ad.getTarget());
		}

		// check object type
		aux = ad.getObjectDetails("type");
		if (new String("object").equals(aux)) {
			AddObject(ad.getObject());
		} else if (new String("character").equals(aux)) {
			AddPeople(ad.getObject());
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

	/*private ArrayList<ActionDetail> FilterInternalEvents(ArrayList<ActionDetail> events)
	{
		ActionDetail action;
		ArrayList<ActionDetail> newList = new ArrayList<ActionDetail>();
		for(ListIterator<ActionDetail> li = events.listIterator();li.hasNext();)
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
	
	private ArrayList<ActionDetail> FilterExternalEvents(ArrayList<ActionDetail> events)
	{
		ActionDetail action;
		ArrayList<ActionDetail> newList = new ArrayList<ActionDetail>();
		for(ListIterator<ActionDetail> li = events.listIterator();li.hasNext();)
		{
			action = li.next();
			if(!action.getAction().equals("activate") && 
					!action.getAction().equals("succeed") &&
					!action.getAction().equals("fail"))
			{
				newList.add(action);
			}
		}
		return newList;
	}
	*/

	public String GenerateSummary(Memory m) {
		ActionDetail action;
		BaseEmotion strongestEmotion = null;
		BaseEmotion secondStrongestEmotion = null;
		int numberOfDetails = _numberOfDominantActions;

		// version with both internal and external events
		List<ActionDetail> auxList = new ArrayList<ActionDetail>(_details);
		// version with only internal events
		//List auxList = (List) FilterInternalEvents(_details);
		// version with only external events
		//List auxList = (List) FilterExternalEvents(_details);
		// version with empty summary
		//List auxList = (List) new ArrayList();

		Collections.sort(auxList, new ActionDetailComparator(ActionDetailComparator.CompareByEmotionIntensity));
		if (auxList.size() > numberOfDetails) {
			auxList = auxList.subList(auxList.size() - numberOfDetails, auxList.size());
		}

		if (auxList.size() > 0) {
			//determine the strongest feeling
			action = (ActionDetail) auxList.get(auxList.size() - 1);
			strongestEmotion = action.getEmotion();

			if (auxList.size() > 1) {
				for (int i = auxList.size() - 2; i >= 0; i--) {
					action = (ActionDetail) auxList.get(i);
					secondStrongestEmotion = action.getEmotion();
					if (secondStrongestEmotion.getType() != strongestEmotion.getType()) {
						break;
					} else {
						secondStrongestEmotion = null;
					}
				}
			}
		}

		Collections.sort(auxList, new ActionDetailComparator(ActionDetailComparator.CompareByOrder));

		String AMSummary = "";
		boolean firstEvent = true;

		ListIterator<ActionDetail> li = auxList.listIterator();
		while (li.hasNext()) {
			action = li.next();
			if (action.getEmotion().GetPotential() > 0) {
				AMSummary += "<Event>";
				if (firstEvent) {
					AMSummary += "<Location>" + this._location + "</Location>";
					AMSummary += SummaryGenerator.generateTimeDescription(this._time.getElapsedNarrativeTime());
					firstEvent = false;
				}

				AMSummary += SummaryGenerator.GenerateActionSummary(m, action);

				if (strongestEmotion != null && action.getEmotion().getType() == strongestEmotion.getType() && action.getEmotion().GetPotential() == strongestEmotion.GetPotential()) {
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

	public BaseEmotion getStrongestEmotion() {
		BaseEmotion em;
		BaseEmotion strongestEmotion = null;

		for (ActionDetail action : this._details) {
			em = action.getEmotion();
			if (em != null) {
				if (strongestEmotion == null || em.GetPotential() > strongestEmotion.GetPotential()) {
					strongestEmotion = em;
				}
			}
		}

		return strongestEmotion;
	}

	public float determineEmotionAverage() {

		ListIterator<ActionDetail> li;
		ActionDetail action;
		BaseEmotion em;
		float value = 0;
		int numberOfEmotionalEvents = 0;

		//determine the average intensity of emotions in the episode
		for (li = this._details.listIterator(); li.hasNext();) {
			action = li.next();
			em = action.getEmotion();
			if (em.GetPotential() > 0) {
				if (em.getValence() == EmotionValence.POSITIVE) {
					value += em.GetPotential();
				} else {
					value -= em.GetPotential();
				}

				numberOfEmotionalEvents++;
			}
		}

		if (numberOfEmotionalEvents == 0) {
			return 0;
		} else {
			return value / numberOfEmotionalEvents;
		}
	}

	public float determineEmotionStdDeviation() {
		ListIterator<ActionDetail> li;
		ActionDetail action;
		BaseEmotion em;
		float quadraticError = 0;
		float error;
		int numberOfEmotionalEvents = 0;
		float avg = this.determineEmotionAverage();

		//determine the standard deviation of emotion intensity in the episode 
		for (li = this._details.listIterator(); li.hasNext();) {
			action = li.next();
			em = action.getEmotion();
			if (em.GetPotential() > 0) {
				if (em.getValence() == EmotionValence.POSITIVE) {
					error = em.GetPotential() - avg;
				} else {
					error = -em.GetPotential() - avg;
				}

				quadraticError += Math.pow(error, 2);

				numberOfEmotionalEvents++;
			}
		}

		if (numberOfEmotionalEvents <= 1) {
			return 0;
		} else {
			return (float) Math.sqrt(quadraticError / (numberOfEmotionalEvents - 1));
		}
	}

	// Meiyii 13/09/10
	public void AddLocation(String location) {
		if (!location.equals("")) {
			if (!this._location.contains(location)) {
				this._location.add(location);
			}
		}
	}

	public void AddPeople(String subject) {
		if (subject != null) {
			if (!this._people.contains(subject)) {
				this._people.add(subject);
			}
		}
	}

	public void AddObject(String object) {
		if (object != null) {
			if (!this._objects.contains(object)) {
				this._objects.add(object);
			}
		}
	}

	public float AssessGoalFamiliarity(Goal g) {
		float familiarity = 0;
		ActionDetail action;
		ListIterator<ActionDetail> li = _details.listIterator();
		Event e = g.GetActivationEvent();

		while (li.hasNext()) {
			action = li.next();
			if (action.getAction().equals(e.GetAction())) {
				//ok, we've found a goal activation

				if (action.getTarget().equals(e.GetTarget())) {
					// and the goal is of the same class of the one we're searching for
					// it seems familiar
					familiarity += 0.5;

					if (action.getParameters().toString().equals(e.GetParameters().toString())) {
						//and the parameters of the goal are also equal which makes the 
						//goal even more familiar
						familiarity += 0.5;
					}
				}
			}
		}

		return familiarity;
	}

	public float AssessFamiliarity(Event event) {
		ActionDetail action;
		ListIterator<ActionDetail> li = _details.listIterator();
		float familiarity = 0;

		while (li.hasNext()) {
			action = li.next();
			if (action.getAction().equals(event.GetAction())) {
				//if I've seen the action before, I'm sligthly familiar 

				familiarity += 0.2;

				if (action.getSubject().equals(Constants.SELF)) {
					//if the event was performed by me, I'm more familiar with it
					familiarity += 0.4;

				}

				if (action.getTarget() != null) {
					if (action.getTarget().equals(event.GetTarget())) {
						familiarity += 0.2;
					}
				} else if (event.GetTarget() == null) {
					familiarity += 0.2;
				}

				if (action.getParameters().toString().equals(event.GetParameters().toString())) {
					familiarity += 0.2;
				}
			}
		}

		return familiarity;
	}

	public int CountEvent(ArrayList<SearchKey> searchKeys) {
		ListIterator<ActionDetail> li;
		ActionDetail action;
		int count = 0;

		li = this._details.listIterator();
		while (li.hasNext()) {
			action = li.next();
			if (action.verifiesKeys(searchKeys)) {
				count++;
			}
		}

		return count;
	}

	public boolean VerifiesKeys(ArrayList<SearchKey> searchKeys) {
		ActionDetail action;
		ListIterator<ActionDetail> li = _details.listIterator();

		boolean status = false;

		while (li.hasNext()) {
			action = (ActionDetail) li.next();
			if (action.verifiesKeys(searchKeys)) {
				// 08/03/11 - Matthias
				action.getRetrievalQueue().addRetrievalTime(new Time());

				status = true;
			}
		}

		return status;
	}

	public boolean VerifiesKeysIndexed(ArrayList<SearchKey> searchKeys) {
		return (GetDetailsByKeysIndexed(searchKeys).size() > 0);
	}

	public boolean VerifiesKey(SearchKey searchKey) {
		ListIterator<ActionDetail> li;
		ActionDetail action;

		synchronized (this) {

			// always loop over events in order to create a retrieval
			/*
			short field = searchKey.getField();
			if (field == SearchKey.PEOPLE) {
				return this._people.contains(searchKey.getKey());
			} else if (field == SearchKey.LOCATION) {
				return this._location.equals(searchKey.getKey());
			} else if (field == SearchKey.OBJECTS) {
				return this._objects.contains(searchKey.getKey());
			} else {
			*/

			li = this._details.listIterator();

			boolean status = false;

			while (li.hasNext()) {
				action = li.next();
				if (action.verifiesKey(searchKey)) {
					// 08/03/11 - Matthias
					action.getRetrievalQueue().addRetrievalTime(new Time());

					status = true;
				}
			}
			//}

			return status;

		}
	}

	public boolean VerifiesKeyIndexed(SearchKey searchKey) {
		synchronized (this) {
			// always loop over events in order to create a retrieval
			/*
			short field = searchKey.getField();
			if (field == SearchKey.PEOPLE) {
				return this._people.contains(searchKey.getKey());
			} else if (field == SearchKey.LOCATION) {
				return this._location.equals(searchKey.getKey());
			} else if (field == SearchKey.OBJECTS) {
				return this._objects.contains(searchKey.getKey());
			} else {
			*/
			return (GetDetailsByKeyIndexed(searchKey).size() > 0);
			//}
		}
	}

	public ArrayList<ActionDetail> GetDetailsByKey(SearchKey key) {
		ArrayList<ActionDetail> details = new ArrayList<ActionDetail>();

		for (ActionDetail action : _details) {
			if (action.verifiesKey(key)) {
				// 08/03/11 - Matthias
				action.getRetrievalQueue().addRetrievalTime(new Time());

				details.add(action);
			}
		}

		return details;
	}

	public ArrayList<ActionDetail> GetDetailsByKeyIndexed(SearchKey key) {
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		keys.add(key);
		return GetDetailsByKeysIndexed(keys);
	}

	public ArrayList<ActionDetail> GetDetailsByKeys(ArrayList<SearchKey> keys) {
		ArrayList<ActionDetail> details = new ArrayList<ActionDetail>();

		for (ActionDetail action : _details) {
			if (action.verifiesKeys(keys)) {
				// 08/03/11 - Matthias
				action.getRetrievalQueue().addRetrievalTime(new Time());

				details.add(action);
			}
		}
		return details;

	}

	public ArrayList<ActionDetail> GetDetailsByKeysIndexed(ArrayList<SearchKey> keys) {
		synchronized (this) {

			// keep track of smallest list
			ArrayList<ActionDetail> actionDetailsMinCount = null;
			int minCount = -1;

			// loop over search keys
			for (SearchKey searchKey : keys) {

				// note: subject is a required search criterion
				if (searchKey.getField() == SearchKey.SUBJECT) {
					// subject is search criterion, obtain details
					ArrayList<ActionDetail> actionDetailsBySubject = subjectIndex.get(searchKey.getKey());
					// if null or empty return an empty list
					if (actionDetailsBySubject == null || actionDetailsBySubject.size() == 0) {
						return new ArrayList<ActionDetail>();
					}
					// check if list is smaller
					if (minCount == -1 || actionDetailsBySubject.size() < minCount) {
						actionDetailsMinCount = actionDetailsBySubject;
						minCount = actionDetailsMinCount.size();
					}

					// note: action is a required search criterion
				} else if (searchKey.getField() == SearchKey.ACTION) {
					// action is search criterion, obtain details
					ArrayList<ActionDetail> actionDetailsByAction = actionIndex.get(searchKey.getKey());
					// if null or empty return an empty list
					if (actionDetailsByAction == null || actionDetailsByAction.size() == 0) {
						return new ArrayList<ActionDetail>();
					}
					// check if list is smaller
					if (minCount == -1 || actionDetailsByAction.size() < minCount) {
						actionDetailsMinCount = actionDetailsByAction;
						minCount = actionDetailsMinCount.size();
					}

					// note: object is a optional search criterion
				} else if (searchKey.getField() == SearchKey.TARGET) {
					// target is search criterion, obtain details
					ArrayList<ActionDetail> actionDetailsByTarget = targetIndex.get(searchKey.getKey());
					if (actionDetailsByTarget == null || actionDetailsByTarget.size() == 0) {
						return new ArrayList<ActionDetail>();
					}
					// check if list is smaller
					if (minCount == -1 || actionDetailsByTarget.size() < minCount) {
						actionDetailsMinCount = actionDetailsByTarget;
						minCount = actionDetailsMinCount.size();
					}
				}
			}

			// check if a minimum list has been set
			if (actionDetailsMinCount == null) {
				// none of the above search criteria were used
				// whole episode needs to be searched
				actionDetailsMinCount = _details;
			}

			// search in the smallest list
			ArrayList<ActionDetail> foundPastEvents = new ArrayList<ActionDetail>();
			for (ActionDetail actionDetail : actionDetailsMinCount) {
				if (actionDetail.verifiesKeys(keys)) {
					actionDetail.getRetrievalQueue().addRetrievalTime(new Time());
					foundPastEvents.add(actionDetail);
				}
			}

			return foundPastEvents;
		}
	}

	public String toXML() {
		ActionDetail detail;
		String episode = "<Episode>";
		episode += "<Location>" + this._location + "</Location>";
		episode += "<Time>" + this._time + "</Time>";
		episode += "<People>" + this._people + "</People>";
		episode += "<Objects>" + this._objects + "</Objects>";
		for (ListIterator<ActionDetail> li = _details.listIterator(); li.hasNext();) {
			detail = (ActionDetail) li.next();
			episode += detail.toXML();
		}
		episode += "</Episode>\n";

		return episode;
	}

}
