/** 
 * EpisodicMemory.java - 
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
 * Jo�o Dias: 06/09/2006 - changed the way that InterpersonalRelations are determined
 * 						   and stored in memory. Now the like relation can be retrieved
 * 						   directly from semantic memory. So I removed the methods related
 * 						   to InterpersonalRelations from this class
 * Jo�o Dias: 09/01/2007 - the field location in the AM is now being created properly when 
 * 						   new information is stored
 * Jo�o Dias: 16/02/2007 - the method SummarizeLastEvent now returns an XML description of the summary
 * 						   ready to be used by the LanguageEngine
 * Jo�o Dias: 22/02/2007 - the Autobiographical memory now registers if new data was added to it and 
 * 						   provides a method that verifies if new data was added since last time it
 * 						   was verified
 * Meiyii Lim: 13/03/2009 - search for recent events is now also performed in STM,
 * 							AM now stores only external events that have an emotional impact on the agent
 * 							and internal events (ie. goal activation, success and failure)
 * Matthias Keysermann: 04/05/2011 - ContainsPast/RecentEvent, SearchForPast/RecentEvents loops always over all episodes and then returns true/false
 * Matthias Keysermann: 10/11/2011 - Removed ContainsPast/RecentEvent (covered by SearchForPast/RecentEvents)
 * **/

package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.wellFormedNames.Substitution;

public class AutobiographicalMemory implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<MemoryEpisode> _memoryEpisodes;

	public AutobiographicalMemory() {
		this._memoryEpisodes = new ArrayList<MemoryEpisode>();
	}

	public void applySubstitution(Substitution s) {
		for (MemoryEpisode mem : _memoryEpisodes) {
			mem.applySubstitution(s);
		}
	}

	public void StoreAction(ActionDetail actionDetail) {
		String location;
		synchronized (this) {

			MemoryEpisode memoryEpisode;

			// Meiyii 13/09/10
			int i = _memoryEpisodes.size() - 1;
			if (i >= 0) {
				memoryEpisode = _memoryEpisodes.get(i);
				if(memoryEpisode.getLocation().size() == 0)
				{
					memoryEpisode.AddLocation(actionDetail.getLocation());
				}
				else
				{
					location = memoryEpisode.getLocation().get(0);
					if(!actionDetail.getLocation().equals(location))
					{
						memoryEpisode = new MemoryEpisode(actionDetail.getLocation(), new Time());
						_memoryEpisodes.add(memoryEpisode);
						memoryEpisode.AddActionDetail(actionDetail);
					}
				}
				
				memoryEpisode.AddActionDetail(actionDetail);
			}

		}
	}

	public boolean RemoveAction(ActionDetail actionDetail) {
		synchronized (this) {

			// delete action detail
			for (MemoryEpisode memoryEpisode : _memoryEpisodes) {
				if (RemoveAction(actionDetail, memoryEpisode)) {
					return true;
				}
			}

			return false;
		}
	}

	public boolean RemoveAction(ActionDetail actionDetail, MemoryEpisode memoryEpisode) {
		synchronized (this) {

			// remove detail from episode
			return memoryEpisode.RemoveAction(actionDetail);

		}
	}

	public void NewEpisode(String location) {
		synchronized (this) {
			MemoryEpisode episode = new MemoryEpisode(location, new Time());
			this._memoryEpisodes.add(episode);
		}
	}

	public Object GetSyncRoot() {
		return this;
	}

	public ArrayList<MemoryEpisode> GetAllEpisodes() {
		return this._memoryEpisodes;
	}

	public int countMemoryDetails() {
		int aux = 0;
		MemoryEpisode episode;

		synchronized (this) {
			ListIterator<MemoryEpisode> li = this._memoryEpisodes.listIterator();
			while (li.hasNext()) {
				episode = li.next();
				aux += episode.getDetails().size();
			}
			return aux;
		}
	}

	public float AssessGoalFamiliarity(Goal g) {
		MemoryEpisode episode;
		float similarEvents = 0;

		synchronized (this) {
			for (int i = 0; i < this._memoryEpisodes.size(); i++) {
				episode = (MemoryEpisode) this._memoryEpisodes.get(i);
				similarEvents += episode.AssessGoalFamiliarity(g);
			}
		}
		return similarEvents;
	}

	// currently not used
	public float AssessFamiliarity(Event e) {
		MemoryEpisode episode;
		float similarEvents = 0;
		float familiarity = 0;

		synchronized (this) {
			for (int i = 0; i < this._memoryEpisodes.size(); i++) {
				episode = (MemoryEpisode) this._memoryEpisodes.get(i);
				similarEvents += episode.AssessFamiliarity(e);
			}
		}

		// familiarity function f(x) = 1 - 1/(x/2 +1)
		// where x represents the number of similar events founds
		// familiarity = 1 - (1 / (similarEvents/2 + 1));

		return familiarity;
	}

	public int CountEvent(ArrayList<SearchKey> searchKeys) {
		MemoryEpisode episode;
		int count = 0;

		synchronized (this) {
			if (this._memoryEpisodes.size() > 0) {
				for (int i = 0; i < this._memoryEpisodes.size(); i++) {
					episode = this._memoryEpisodes.get(i);
					count += episode.CountEvent(searchKeys);
				}
			}
			return count;
		}
	}

	public ArrayList<ActionDetail> SearchForRecentEvents(ArrayList<SearchKey> searchKeys) {
		MemoryEpisode currentEpisode;
		synchronized (this) {

			if (this._memoryEpisodes.size() > 0) {
				currentEpisode = this._memoryEpisodes.get(this._memoryEpisodes.size() - 1);
				return currentEpisode.GetDetailsByKeys(searchKeys);
			}

			return new ArrayList<ActionDetail>();
		}
	}

	public ArrayList<ActionDetail> SearchForRecentEventsIndexed(ArrayList<SearchKey> searchKeys) {
		MemoryEpisode currentEpisode;
		synchronized (this) {

			if (_memoryEpisodes.size() > 0) {
				currentEpisode = _memoryEpisodes.get(_memoryEpisodes.size() - 1);
				return currentEpisode.GetDetailsByKeysIndexed(searchKeys);
			}

			return new ArrayList<ActionDetail>();
		}
	}

	public ArrayList<ActionDetail> SearchForPastEvents(ArrayList<SearchKey> searchKeys) {
		MemoryEpisode episode;
		ArrayList<ActionDetail> details;
		ActionDetail action;
		ListIterator<ActionDetail> li;

		synchronized (this) {
			ArrayList<ActionDetail> foundPastEvents = new ArrayList<ActionDetail>();

			if (this._memoryEpisodes.size() > 1) {
				for (int i = 0; i < this._memoryEpisodes.size() - 1; i++) {
					episode = (MemoryEpisode) this._memoryEpisodes.get(i);
					details = episode.GetDetailsByKeys(searchKeys);
					li = details.listIterator();
					while (li.hasNext()) {
						action = li.next();
						if (!foundPastEvents.contains(action)) {
							foundPastEvents.add(action);
						}
					}
				}
			}

			return foundPastEvents;
		}
	}

	public ArrayList<ActionDetail> SearchForPastEventsIndexed(ArrayList<SearchKey> searchKeys) {
		synchronized (this) {

			// store found events in a list
			ArrayList<ActionDetail> foundPastEvents = new ArrayList<ActionDetail>();

			// check if past episodes exist
			if (_memoryEpisodes.size() > 1) {

				// loop over episodes excluding current episode
				for (int i = 0; i < _memoryEpisodes.size() - 1; i++) {
					MemoryEpisode episode = _memoryEpisodes.get(i);
					foundPastEvents.addAll(episode.GetDetailsByKeysIndexed(searchKeys));
				}
			}

			return foundPastEvents;
		}
	}

	public String SummarizeEpisode(Memory m, int episodeID) {

		String AMSummary = "";

		//System.out.println("Number of Events in AM: " + this._memoryEvents.size());

		if (this._memoryEpisodes.size() > episodeID) {
			MemoryEpisode episode = (MemoryEpisode) this._memoryEpisodes.get(episodeID);
			long elapsedTime = episode.getTime().getElapsedRealTime();
			//float avgEmotion = episode.determineEmotionAverage();
			//float stdDev = episode.determineEmotionStdDeviation();
			AgentLogger.GetInstance().logAndPrint("Preparing Summary Generation...");
			AgentLogger.GetInstance().logAndPrint("Time elapsed since episode happened: " + elapsedTime);
			//AgentLogger.GetInstance().logAndPrint("Average of the episode's emotions: " + avgEmotion);
			//AgentLogger.GetInstance().logAndPrint("Std deviation of the episode's emotions: " + stdDev);
			//if an hour passed since the user seen the last episode, we should report a summary
			//we should also report  if the episode is ambiguous, i.e if it is not very far from a neutral average
			//if(elapsedTime >= 36000000 || stdDev >= 2.5)
			//{
			AMSummary += episode.GenerateSummary(m);
			//}
		}

		return AMSummary;
	}

	/*
	 * Put an episode to the AM - used when reloading in the memory
	 * Meiyii - 17/12/10
	 */
	public void putEpisode(MemoryEpisode me) {
		_memoryEpisodes.add(me);
	}

	public String toXML() {
		String am = "<AutobiographicMemory>";
		for (ListIterator<MemoryEpisode> li = this._memoryEpisodes.listIterator(); li.hasNext();) {
			MemoryEpisode episode = li.next();
			am += episode.toXML();
		}
		am += "</AutobiographicMemory>\n";
		return am;
	}

	/*public ArrayList Reconstruct(ArrayList searchKeys)
	{
		ArrayList reconstruction = new ArrayList();
		MemoryEpisode memoryEvent;
		
		synchronized(this)
		{
			ListIterator li = this._memoryEvents.listIterator();
			
			while(li.hasNext())
			{
				memoryEvent = (MemoryEpisode) li.next();
				if(memoryEvent.VerifiesKeys(searchKeys))
				{
					reconstruction.add(memoryEvent);
				}
			}
			
			return reconstruction;
		}
	}*/

	/*public ArrayList Reconstruct(SearchKey k)
	{
		ArrayList reconstruction = new ArrayList();
		MemoryEpisode memoryEvent;
		
		synchronized(this)
		{
			ListIterator li = this._memoryEvents.listIterator();
			
			while(li.hasNext())
			{
				memoryEvent = (MemoryEpisode) li.next();
				if(memoryEvent.VerifiesKey(k))
				{
					reconstruction.add(memoryEvent);
				}
			}
			
			return reconstruction;
		}
	}*/

}
