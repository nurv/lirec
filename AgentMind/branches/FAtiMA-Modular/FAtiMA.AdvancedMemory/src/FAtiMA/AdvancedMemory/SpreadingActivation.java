/** 
 * SpreadingActivation.java - A class to perform spreading activation, i.e. filter events
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

package FAtiMA.AdvancedMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.StringTokenizer;

import FAtiMA.AdvancedMemory.ontology.TreeOntology;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.Time;

public class SpreadingActivation implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "Spreading Activation";

	// time when mechanism was executed
	private Time time;
	// number of days provided (before filtering)
	private int numDaysProvided;
	// number of working days provided (before filtering)
	private int numWorkingDaysProvided;
	// attributes used for action detail filtering	
	private ArrayList<String> filterAttributes;
	// time ontology used for attribute time (null if not used)
	private TimeOntology timeOntology;
	// noun ontology used for attribute target (null if not used)
	private NounOntology targetOntology;
	// noun ontology used for attribute object (null if not used)
	private NounOntology objectOntology;
	// location ontology used for attribute location (null if not used)
	private TreeOntology locationOntology;
	// name of attribute for whose values frequencies are calculated
	private String targetAttributeName;
	// attribute value and corresponding frequencies
	private HashMap<String, Integer> frequencies;
	// hypernyms successfully applied for generalising while calculating frequencies
	private HashMap<String, HashSet<String>> targetAttributeHypernyms;
	// the attribute value with maximum frequency

	// If an ontology is used for target/object and target/object is the target attribute,
	// then targetAttributeHypernyms maps corresponding values to a set of hypernyms which
	// were successfully applied for generalising the corresponding value with another
	// value. 
	// If no hypernyms could be applied successfully (even if hypernyms exist), then no
	// key for the corresponding value exists in targetAttributeHypernyms.
	// If no ontology was used for target/object, then targetAttributeHypernyms is null.

	// If an ontology is used for location and location is the target attribute, then
	// targetAttributeHypernyms stores ancestors (instead of hypernyms) in an analog way.

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public int getNumDaysProvided() {
		return numDaysProvided;
	}

	public void setNumDaysProvided(int numDaysProvided) {
		this.numDaysProvided = numDaysProvided;
	}

	public int getNumWorkingDaysProvided() {
		return numWorkingDaysProvided;
	}

	public void setNumWorkingDaysProvided(int numWorkingDaysProvided) {
		this.numWorkingDaysProvided = numWorkingDaysProvided;
	}

	public ArrayList<String> getFilterAttributes() {
		return filterAttributes;
	}

	public void setFilterAttributes(ArrayList<String> filterAttributes) {
		this.filterAttributes = filterAttributes;
	}

	public TimeOntology getTimeOntology() {
		return timeOntology;
	}

	public void setTimeOntology(TimeOntology timeOntology) {
		this.timeOntology = timeOntology;
	}

	public NounOntology getTargetOntology() {
		return targetOntology;
	}

	public void setTargetOntology(NounOntology targetOntology) {
		this.targetOntology = targetOntology;
	}

	public NounOntology getObjectOntology() {
		return objectOntology;
	}

	public void setObjectOntology(NounOntology objectOntology) {
		this.objectOntology = objectOntology;
	}

	public TreeOntology getLocationOntology() {
		return locationOntology;
	}

	public void setLocationOntology(TreeOntology locationOntology) {
		this.locationOntology = locationOntology;
	}

	public String getTargetAttributeName() {
		return targetAttributeName;
	}

	public void setTargetAttributeName(String targetAttributeName) {
		this.targetAttributeName = targetAttributeName;
	}

	public HashMap<String, Integer> getFrequencies() {
		return frequencies;
	}

	public void setFrequencies(HashMap<String, Integer> frequencies) {
		this.frequencies = frequencies;
	}

	public HashMap<String, HashSet<String>> getTargetAttributeHypernyms() {
		return targetAttributeHypernyms;
	}

	public void setTargetAttributeHypernyms(HashMap<String, HashSet<String>> targetAttributeHypernyms) {
		this.targetAttributeHypernyms = targetAttributeHypernyms;
	}

	public Object spreadActivation(EpisodicMemory episodicMemory, String targetAttributeName) {
		return spreadActivation(episodicMemory, null, targetAttributeName, null, null, null, null);
	}

	public Object spreadActivation(ArrayList<ActionDetail> actionDetails, String targetAttributeName) {
		return spreadActivation(actionDetails, null, targetAttributeName, null, null, null, null);
	}

	public Object spreadActivation(EpisodicMemory episodicMemory, String filterAttributesStr, String targetAttributeName, TimeOntology timeOntology, NounOntology targetOntology,
			NounOntology objectOntology, TreeOntology locationOntology) {

		ArrayList<ActionDetail> actionDetails = new ArrayList<ActionDetail>();
		for (MemoryEpisode memoryEpisode : episodicMemory.getAM().GetAllEpisodes()) {
			actionDetails.addAll(memoryEpisode.getDetails());
		}
		for (ActionDetail actionDetail : episodicMemory.getSTEM().getDetails()) {
			actionDetails.add(actionDetail);
		}

		// alternative:
		// create search keys from filter attributes string
		// use memory search to get list of action details (both past and recent)
		// call generalise with these action details and an empty filter attributes string (or null)
		// but: no ontology usage then

		return spreadActivation(actionDetails, filterAttributesStr, targetAttributeName, timeOntology, targetOntology, objectOntology, locationOntology);
	}

	public Object spreadActivation(ArrayList<ActionDetail> actionDetails, String filterAttributesStr, String targetAttributeName, TimeOntology timeOntology, NounOntology targetOntology,
			NounOntology objectOntology, TreeOntology locationOntology) {

		// count number of days and number of working days provided
		HashSet<Calendar> daysProvided = new HashSet<Calendar>();
		HashSet<Calendar> workingDaysProvided = new HashSet<Calendar>();
		for (ActionDetail actionDetail : actionDetails) {
			// set date and time
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(actionDetail.getTime().getRealTime());
			// reset time
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			// add to sets
			daysProvided.add(calendar);
			if (TimeOntology.isWorkingDay(calendar.getTimeInMillis())) {
				workingDaysProvided.add(calendar);
			}
		}
		int numDaysProvided = daysProvided.size();
		int numWorkingDaysProvided = workingDaysProvided.size();

		// initialise
		Object valueMax = null;
		int frequencyMax = 0;
		Time time = new Time();

		// extract filter attributes
		ArrayList<String> filterAttributes = new ArrayList<String>();
		if (filterAttributesStr != null) {
			StringTokenizer stringTokenizer = new StringTokenizer(filterAttributesStr, "*");
			while (stringTokenizer.hasMoreTokens()) {
				String filterAttribute = stringTokenizer.nextToken();
				filterAttributes.add(filterAttribute);
			}
		}

		// filter action details
		ArrayList<ActionDetail> actionDetailsFiltered = new ArrayList<ActionDetail>();
		actionDetailsFiltered.addAll(actionDetails);
		for (String filterAttribute : filterAttributes) {
			String[] attributeSplitted = filterAttribute.split(" ");
			String name = attributeSplitted[0];
			String value = "";
			if (attributeSplitted.length == 2) {
				value = attributeSplitted[1];
			}
			actionDetailsFiltered = ActionDetailFilter.filterActionDetails(actionDetailsFiltered, name, value, timeOntology, targetOntology, objectOntology, locationOntology);
		}

		// calculate frequencies

		HashMap<String, Integer> frequencies = new HashMap<String, Integer>();

		for (ActionDetail actionDetail : actionDetailsFiltered) {

			AttributeItem attributeItem = new AttributeItem();
			attributeItem.setName(targetAttributeName);
			attributeItem.setValue(actionDetail.getValueByName(targetAttributeName), timeOntology);

			Object value = attributeItem.getValue();

			String valueStr = String.valueOf(value);
			Integer frequency = frequencies.get(valueStr);
			if (frequency == null) {
				frequency = 1;
			} else {
				frequency = frequency + 1;
			}
			frequencies.put(valueStr, frequency);

			// update maximum
			if (frequency > frequencyMax) {
				frequencyMax = frequency;
				valueMax = value;
			}

		}

		// ontology usage: recalculate frequencies including hypernyms

		// target ontology
		if (targetOntology != null && targetAttributeName.equals("target")) {

			// initialise
			targetAttributeHypernyms = new HashMap<String, HashSet<String>>();
			HashMap<String, Integer> frequenciesOntology = new HashMap<String, Integer>();
			Object valueMaxOntology = null;
			int frequencyMaxOntology = 0;

			// loop over target attribute values
			for (String targetAttributeValue : frequencies.keySet()) {

				// initialise
				frequenciesOntology.put(targetAttributeValue, 0);
				targetAttributeHypernyms.put(targetAttributeValue, new HashSet<String>());

				// loop over action details
				for (ActionDetail actionDetail : actionDetailsFiltered) {
					AttributeItem attributeItem = new AttributeItem();
					attributeItem.setName(targetAttributeName);
					attributeItem.setValue(actionDetail.getValueByName(targetAttributeName), timeOntology);

					Object value = attributeItem.getValue();

					// find common hypernyms for current target attribute value and value of current action detail

					String[] nouns = new String[2];
					nouns[0] = targetAttributeValue;
					nouns[1] = String.valueOf(value);
					LinkedList<String> nounsGeneralised = targetOntology.generaliseNouns(nouns);

					// check if common hypernyms exist 
					if (nounsGeneralised.size() > 0) {

						// update hypernym set
						HashSet<String> hypernymSet = targetAttributeHypernyms.get(targetAttributeValue);
						// add only the first common hypernym to set
						hypernymSet.add(nounsGeneralised.getFirst());
						targetAttributeHypernyms.put(targetAttributeValue, hypernymSet);

						// update freqency
						Integer frequencyOntology = frequenciesOntology.get(targetAttributeValue);
						frequencyOntology = frequencyOntology + 1;
						frequenciesOntology.put(targetAttributeValue, frequencyOntology);

						// update maximum
						if (frequencyOntology > frequencyMaxOntology) {
							frequencyMaxOntology = frequencyOntology;
							valueMaxOntology = value;
						}

					}

				}

			}

			// assign result to original variables
			frequencies = frequenciesOntology;
			frequencyMax = frequencyMaxOntology;
			valueMax = valueMaxOntology;

		}

		// object ontology
		if (objectOntology != null && targetAttributeName.equals("object")) {

			// initialise
			targetAttributeHypernyms = new HashMap<String, HashSet<String>>();
			HashMap<String, Integer> frequenciesOntology = new HashMap<String, Integer>();
			Object valueMaxOntology = null;
			int frequencyMaxOntology = 0;

			// loop over target attribute values
			for (String targetAttributeValue : frequencies.keySet()) {

				// initialise
				frequenciesOntology.put(targetAttributeValue, 0);
				targetAttributeHypernyms.put(targetAttributeValue, new HashSet<String>());

				// loop over action details
				for (ActionDetail actionDetail : actionDetailsFiltered) {
					AttributeItem attributeItem = new AttributeItem();
					attributeItem.setName(targetAttributeName);
					attributeItem.setValue(actionDetail.getValueByName(targetAttributeName), timeOntology);

					Object value = attributeItem.getValue();

					// find common hypernyms for current target attribute value and value of current action detail

					String[] nouns = new String[2];
					nouns[0] = targetAttributeValue;
					nouns[1] = String.valueOf(value);
					LinkedList<String> nounsGeneralised = objectOntology.generaliseNouns(nouns);

					// check if common hypernyms exist 
					if (nounsGeneralised.size() > 0) {

						// update hypernym set
						HashSet<String> hypernymSet = targetAttributeHypernyms.get(targetAttributeValue);
						// add only the first common hypernym to set
						hypernymSet.add(nounsGeneralised.getFirst());
						targetAttributeHypernyms.put(targetAttributeValue, hypernymSet);

						// update freqency
						Integer frequencyOntology = frequenciesOntology.get(targetAttributeValue);
						frequencyOntology = frequencyOntology + 1;
						frequenciesOntology.put(targetAttributeValue, frequencyOntology);

						// update maximum
						if (frequencyOntology > frequencyMaxOntology) {
							frequencyMaxOntology = frequencyOntology;
							valueMaxOntology = value;
						}

					}

				}

			}

			// assign result to original variables
			frequencies = frequenciesOntology;
			frequencyMax = frequencyMaxOntology;
			valueMax = valueMaxOntology;

		}

		// location ontology
		if (locationOntology != null && targetAttributeName.equals("location")) {

			// initialise
			targetAttributeHypernyms = new HashMap<String, HashSet<String>>();
			HashMap<String, Integer> frequenciesOntology = new HashMap<String, Integer>();
			Object valueMaxOntology = null;
			int frequencyMaxOntology = 0;

			// loop over target attribute values
			for (String targetAttributeValue : frequencies.keySet()) {

				// initialise
				frequenciesOntology.put(targetAttributeValue, 0);
				targetAttributeHypernyms.put(targetAttributeValue, new HashSet<String>());

				// loop over action details
				for (ActionDetail actionDetail : actionDetailsFiltered) {
					AttributeItem attributeItem = new AttributeItem();
					attributeItem.setName(targetAttributeName);
					attributeItem.setValue(actionDetail.getValueByName(targetAttributeName), timeOntology);

					Object value = attributeItem.getValue();

					// find common ancestors for current target attribute value and value of current action detail

					String locationA = targetAttributeValue;
					String locationB = String.valueOf(value);
					LinkedList<String> locationsGeneralised = locationOntology.getClosestCommonAncestors(locationA, locationB);

					// check if common ancestors exist 
					if (locationsGeneralised.size() > 0) {

						// update hypernym set
						HashSet<String> hypernymSet = targetAttributeHypernyms.get(targetAttributeValue);
						// add only the first common hypernym to set
						hypernymSet.add(locationsGeneralised.getFirst());
						targetAttributeHypernyms.put(targetAttributeValue, hypernymSet);

						// update freqency
						Integer frequencyOntology = frequenciesOntology.get(targetAttributeValue);
						frequencyOntology = frequencyOntology + 1;
						frequenciesOntology.put(targetAttributeValue, frequencyOntology);

						// update maximum
						if (frequencyOntology > frequencyMaxOntology) {
							frequencyMaxOntology = frequencyOntology;
							valueMaxOntology = value;
						}

					}

				}

			}

			// assign result to original variables
			frequencies = frequenciesOntology;
			frequencyMax = frequencyMaxOntology;
			valueMax = valueMaxOntology;

		}

		// update attributes
		this.time = time;
		this.numDaysProvided = numDaysProvided;
		this.numWorkingDaysProvided = numWorkingDaysProvided;
		this.filterAttributes = filterAttributes;
		this.timeOntology = timeOntology;
		this.targetOntology = targetOntology;
		this.objectOntology = objectOntology;
		this.locationOntology = locationOntology;
		this.targetAttributeName = targetAttributeName;
		this.frequencies = frequencies;

		//System.out.println("ValueMax " + valueMax);
		return valueMax;
	}
}
