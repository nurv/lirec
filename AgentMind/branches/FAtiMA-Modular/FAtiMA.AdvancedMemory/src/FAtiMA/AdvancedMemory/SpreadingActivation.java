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
import java.util.HashMap;
import java.util.StringTokenizer;

import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.Time;

public class SpreadingActivation implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "Spreading Activation";

	private Time time;
	private ArrayList<String> filterAttributes;
	private TimeOntology timeOntology;
	private String targetAttributeName;
	private HashMap<String, Integer> frequencies;

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
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

	private ArrayList<ActionDetail> filterActionDetails(ArrayList<ActionDetail> actionDetails, String attributeName, Object attributeValue, TimeOntology timeOntology) {

		ArrayList<ActionDetail> actionDetailsFiltered = new ArrayList<ActionDetail>();

		for (ActionDetail actionDetail : actionDetails) {

			AttributeItem attributeItem = new AttributeItem();
			attributeItem.setName(attributeName);
			attributeItem.setValue(actionDetail.getValueByName(attributeName), timeOntology);

			Object attributeValueCurrent = attributeItem.getValue();

			if (attributeValueCurrent != null && attributeValueCurrent.equals(attributeValue)) {
				actionDetailsFiltered.add(actionDetail);
			}
		}

		return actionDetailsFiltered;
	}

	public Object spreadActivation(EpisodicMemory episodicMemory, String targetAttributeName) {
		return spreadActivation(episodicMemory, null, targetAttributeName, null);
	}

	public Object spreadActivation(ArrayList<ActionDetail> actionDetails, String targetAttributeName) {
		return spreadActivation(actionDetails, null, targetAttributeName, null);
	}

	public Object spreadActivation(EpisodicMemory episodicMemory, String filterAttributesStr, String targetAttributeName, TimeOntology timeOntology) {

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

		return spreadActivation(actionDetails, filterAttributesStr, targetAttributeName, timeOntology);
	}

	public Object spreadActivation(ArrayList<ActionDetail> actionDetails, String filterAttributesStr, String targetAttributeName, TimeOntology timeOntology) {

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
			actionDetailsFiltered = filterActionDetails(actionDetailsFiltered, name, value, timeOntology);
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

		// update attributes
		this.time = time;
		this.filterAttributes = filterAttributes;
		this.timeOntology = timeOntology;
		this.targetAttributeName = targetAttributeName;
		this.frequencies = frequencies;

		return valueMax;
	}

}
