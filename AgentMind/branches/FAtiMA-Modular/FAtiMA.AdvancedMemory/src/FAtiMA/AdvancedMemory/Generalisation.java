/** 
 * Generalisation.java - Display panel for the Compound Cue mechanism
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
 * Created: 18/11/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 18/11/11 - File created
 * 
 * **/

package FAtiMA.AdvancedMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.Time;

public class Generalisation implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "Generalisation";

	private Time time;
	private ArrayList<String> filterAttributes;
	private TimeOntology timeOntology;
	private ArrayList<String> attributeNames;
	private int minimumCoverage;
	private ArrayList<GER> gers;

	public Time getTime() {
		return time;
	}

	public void setTime(Time time) {
		this.time = time;
	}

	public ArrayList<String> getFilterAttributes() {
		return filterAttributes;
	}

	public TimeOntology getTimeOntology() {
		return timeOntology;
	}

	public void setTimeOntology(TimeOntology timeOntology) {
		this.timeOntology = timeOntology;
	}

	public void setFilterAttributes(ArrayList<String> filterAttributes) {
		this.filterAttributes = filterAttributes;
	}

	public ArrayList<String> getAttributeNames() {
		return attributeNames;
	}

	public void setAttributeNames(ArrayList<String> attributeNames) {
		this.attributeNames = attributeNames;
	}

	public int getMinimumCoverage() {
		return minimumCoverage;
	}

	public void setMinimumCoverage(int minimumCoverage) {
		this.minimumCoverage = minimumCoverage;
	}

	public ArrayList<GER> getGers() {
		return gers;
	}

	public void setGers(ArrayList<GER> gers) {
		this.gers = gers;
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

	private AttributeItemSet combineItemSets(AttributeItemSet itemSetA, AttributeItemSet itemSetB) {
		AttributeItemSet itemSetCombined = new AttributeItemSet();

		// check if item sets have the same number of attributes
		if (itemSetA.size() != itemSetB.size()) {
			return null;
		}

		// merge item sets
		for (AttributeItem itemA : itemSetA.getAttributeItems()) {
			itemSetCombined.addToSet(itemA);
		}
		for (AttributeItem itemB : itemSetB.getAttributeItems()) {
			itemSetCombined.addToSet(itemB);
		}

		// check for size
		if (itemSetCombined.size() != itemSetA.size() + 1) {
			return null;
		}

		return itemSetCombined;
	}

	public GER generalise(EpisodicMemory episodicMemory, String attributeNamesStr, int minimumCoverage) {
		return generalise(episodicMemory, null, attributeNamesStr, minimumCoverage, null);
	}

	public GER generalise(ArrayList<ActionDetail> actionDetails, String attributeNamesStr, int minimumCoverage) {
		return generalise(actionDetails, null, attributeNamesStr, minimumCoverage, null);
	}

	public GER generalise(EpisodicMemory episodicMemory, String filterAttributesStr, String attributeNamesStr, int minimumCoverage, TimeOntology timeOntology) {

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

		return generalise(actionDetails, filterAttributesStr, attributeNamesStr, minimumCoverage, timeOntology);
	}

	public GER generalise(ArrayList<ActionDetail> actionDetails, String filterAttributesStr, String attributeNamesStr, int minimumCoverage, TimeOntology timeOntology) {

		// initialise
		GER gerMax = null;
		int coverageMax = 0;
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

		// extract attribute names
		ArrayList<String> attributeNames = new ArrayList<String>();
		if (attributeNamesStr != null) {
			StringTokenizer stringTokenizer = new StringTokenizer(attributeNamesStr, "*");
			while (stringTokenizer.hasMoreTokens()) {
				String attributeName = stringTokenizer.nextToken();
				attributeNames.add(attributeName);
			}
		}

		// create initial attribute item sets
		ArrayList<AttributeItemSet> attributeItemSets = new ArrayList<AttributeItemSet>();
		for (String attributeName : attributeNames) {
			for (ActionDetail actionDetail : actionDetailsFiltered) {

				// create attribute item
				AttributeItem attributeItem = new AttributeItem();
				attributeItem.setName(attributeName);
				attributeItem.setValue(actionDetail.getValueByName(attributeName), timeOntology);

				// check if an attribute item set already contains this item
				boolean containedInSets = false;
				for (AttributeItemSet attributeItemSet : attributeItemSets) {
					if (attributeItemSet.contains(attributeItem)) {
						containedInSets = true;
						break;
					}
				}

				// create and add attribute item set
				if (!containedInSets) {
					AttributeItemSet attributeItemSet = new AttributeItemSet();
					attributeItemSet.addToSet(attributeItem);
					attributeItemSets.add(attributeItemSet);
				}

			}
		}

		// filter by coverage
		for (int i = 0; i < attributeItemSets.size(); i++) {
			AttributeItemSet attributeItemSet = attributeItemSets.get(i);
			int coverage = attributeItemSet.getCoverage(actionDetails, timeOntology);
			if (coverage < minimumCoverage) {
				attributeItemSets.remove(i);
				i--;
			}
		}

		// combine k item sets to k+1 item sets
		while (true) {

			// stop if list is empty		
			if (attributeItemSets.size() == 0) {
				break;
			}

			// stop if k = number of attribute
			if (attributeItemSets.get(0).size() == attributeNames.size()) {
				break;
			}

			ArrayList<AttributeItemSet> attributeItemSetsCombined = new ArrayList<AttributeItemSet>();
			for (int i = 0; i < attributeItemSets.size(); i++) {
				for (int j = i; j < attributeItemSets.size(); j++) {

					AttributeItemSet itemSetI = attributeItemSets.get(i);
					AttributeItemSet itemSetJ = attributeItemSets.get(j);
					AttributeItemSet itemSetCombined = combineItemSets(itemSetI, itemSetJ);

					// check if combination was valid
					if (itemSetCombined != null) {

						// check if combined item set is not in list yet
						boolean contained = false;
						for (AttributeItemSet attributeItemSet : attributeItemSetsCombined) {
							if (attributeItemSet.equals(itemSetCombined)) {
								contained = true;
								break;
							}
						}
						if (!contained) {
							attributeItemSetsCombined.add(itemSetCombined);
						}

					}

				}
			}
			attributeItemSets = attributeItemSetsCombined;

			// filter by coverage
			for (int i = 0; i < attributeItemSets.size(); i++) {
				AttributeItemSet attributeItemSet = attributeItemSets.get(i);
				int coverage = attributeItemSet.getCoverage(actionDetails, timeOntology);
				if (coverage < minimumCoverage) {
					attributeItemSets.remove(i);
					i--;
				}
			}

		}

		// store GERs

		ArrayList<GER> gers = new ArrayList<GER>();

		for (AttributeItemSet attributeItemSet : attributeItemSets) {

			GER ger = new GER();
			ger.setAttributeItemSet(attributeItemSet);
			int coverage = attributeItemSet.getCoverage(actionDetails, timeOntology);
			ger.setCoverage(coverage);
			gers.add(ger);

			// update maximum
			if (coverage > coverageMax) {
				coverageMax = coverage;
				gerMax = ger;
			}

		}

		// update attributes
		this.time = time;
		this.filterAttributes = filterAttributes;
		this.timeOntology = timeOntology;
		this.attributeNames = attributeNames;
		this.minimumCoverage = minimumCoverage;
		this.gers = gers;

		return gerMax;
	}

}
