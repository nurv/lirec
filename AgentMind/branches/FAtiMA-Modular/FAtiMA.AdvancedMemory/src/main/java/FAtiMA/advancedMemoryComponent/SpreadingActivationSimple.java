/** 
 * SpreadingActivationSimple.java - A class to perform spreading activation, i.e. filter events
 * by given attribute values and determine the frequencies of the target attribute values 
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
 * Created: 14/07/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History:
 * Matthias Keysermann: 14/07/11 - File created
 * 
 * **/

package FAtiMA.advancedMemoryComponent;

import java.util.ArrayList;
import java.util.HashMap;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;

public class SpreadingActivationSimple {

	public void spreadActivation(EpisodicMemory episodicMemory, ArrayList<String> knownInfo, String question) {

		ArrayList<ActionDetail> events = new ArrayList<ActionDetail>();
		// add events from AM
		for (MemoryEpisode episode : episodicMemory.getAM().GetAllEpisodes()) {
			events.addAll(episode.getDetails());
		}
		// add events from STEM
		events.addAll(episodicMemory.getSTEM().getDetails());

		// filter events
		for (String info : knownInfo) {
			String[] infoSplitted = info.split(" ");
			String attrName = infoSplitted[0];
			String attrValue = infoSplitted[1];
			events = filterEvents(events, attrName, attrValue);
		}

		// calculate frequencies
		HashMap<Object, Integer> frequencies = calculateFrequencies(events, question);

		// DEBUG
		for (Object key : frequencies.keySet()) {
			System.out.println(key + " " + frequencies.get(key));
		}

	}

	private ArrayList<ActionDetail> filterEvents(ArrayList<ActionDetail> events, String attrName, Object attrValue) {

		// DEBUG
		System.out.println("Filtering " + attrName + "=" + attrValue);

		ArrayList<ActionDetail> eventsFiltered = new ArrayList<ActionDetail>();

		for (ActionDetail event : events) {

			Object attrValueCurrent = null;

			if (attrName.equals("subject")) {
				attrValueCurrent = event.getSubject();
			} else if (attrName.equals("action")) {
				attrValueCurrent = event.getAction();
			} else if (attrName.equals("target")) {
				attrValueCurrent = event.getTarget();
			} else if (attrName.equals("object")) {
				attrValueCurrent = event.getObject();
			} else if (attrName.equals("location")) {
				attrValueCurrent = event.getLocation();
			} else if (attrName.equals("intention")) {
				attrValueCurrent = event.getIntention();
			} else if (attrName.equals("status")) {
				attrValueCurrent = event.getStatus();
			} else if (attrName.equals("emotion")) {
				attrValueCurrent = event.getEmotion();
			} else if (attrName.equals("speechActMeaning")) {
				attrValueCurrent = event.getSpeechActMeaning();
			} else if (attrName.equals("multimediaPath")) {
				attrValueCurrent = event.getMultimediaPath();
			} else if (attrName.equals("praiseworthiness")) {
				attrValueCurrent = event.getPraiseworthiness();
			} else if (attrName.equals("desirability")) {
				attrValueCurrent = event.getDesirability();
			} else if (attrName.equals("time")) {
				attrValueCurrent = event.getTime();
			}

			if (attrValueCurrent != null && attrValueCurrent.equals(attrValue)) {
				eventsFiltered.add(event);
			}
		}

		return eventsFiltered;
	}

	private HashMap<Object, Integer> calculateFrequencies(ArrayList<ActionDetail> events, String attrName) {

		// DEBUG
		System.out.println("Calculating frequency for " + attrName);

		HashMap<Object, Integer> frequencies = new HashMap<Object, Integer>();

		// gather values
		for (ActionDetail event : events) {

			Object attrValueCurrent = null;

			if (attrName.equals("subject")) {
				attrValueCurrent = event.getSubject();
			} else if (attrName.equals("action")) {
				attrValueCurrent = event.getAction();
			} else if (attrName.equals("target")) {
				attrValueCurrent = event.getTarget();
			} else if (attrName.equals("object")) {
				attrValueCurrent = event.getObject();
			} else if (attrName.equals("location")) {
				attrValueCurrent = event.getLocation();
			} else if (attrName.equals("intention")) {
				attrValueCurrent = event.getIntention();
			} else if (attrName.equals("status")) {
				attrValueCurrent = event.getStatus();
			} else if (attrName.equals("emotion")) {
				attrValueCurrent = event.getEmotion();
			} else if (attrName.equals("speechActMeaning")) {
				attrValueCurrent = event.getSpeechActMeaning();
			} else if (attrName.equals("multimediaPath")) {
				attrValueCurrent = event.getMultimediaPath();
			} else if (attrName.equals("praiseworthiness")) {
				attrValueCurrent = event.getPraiseworthiness();
			} else if (attrName.equals("desirability")) {
				attrValueCurrent = event.getDesirability();
			} else if (attrName.equals("time")) {
				attrValueCurrent = event.getTime();
			}

			if (attrValueCurrent != null) {
				Integer frequency = frequencies.get(attrValueCurrent);
				if (frequency == null) {
					frequencies.put(attrValueCurrent, 1);
				} else {
					frequencies.put(attrValueCurrent, frequency.intValue() + 1);
				}
			}

		}

		return frequencies;
	}

}
