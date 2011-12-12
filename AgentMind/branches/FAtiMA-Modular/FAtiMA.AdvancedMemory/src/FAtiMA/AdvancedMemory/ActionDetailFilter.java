/** 
 * ActionDetailFilter.java - A class for filtering action details.
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
 * Created: 05/12/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 05/12/11 - File created
 * 
 * **/

package FAtiMA.AdvancedMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

import FAtiMA.AdvancedMemory.ontology.TreeOntology;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;

public class ActionDetailFilter implements Serializable {

	private static final long serialVersionUID = 1L;

	public static ArrayList<ActionDetail> filterActionDetails(ArrayList<ActionDetail> actionDetails, String attributeName, Object attributeValue, TimeOntology timeOntology,
			NounOntology targetOntology, NounOntology objectOntology, TreeOntology locationOntology) {

		ArrayList<ActionDetail> actionDetailsFiltered = new ArrayList<ActionDetail>();

		for (ActionDetail actionDetail : actionDetails) {

			AttributeItem attributeItem = new AttributeItem();
			attributeItem.setName(attributeName);
			attributeItem.setValue(actionDetail.getValueByName(attributeName), timeOntology);

			Object attributeValueCurrent = attributeItem.getValue();

			// check if value matches exactly
			if (attributeValueCurrent != null && attributeValueCurrent.equals(attributeValue)) {
				actionDetailsFiltered.add(actionDetail);

			} else if (targetOntology != null && attributeName.equals("target")) {

				// check if common hypernyms exists

				String[] nouns = new String[2];
				nouns[0] = String.valueOf(attributeValue);
				nouns[1] = String.valueOf(attributeValueCurrent);
				LinkedList<String> nounsGeneralised = targetOntology.generaliseNouns(nouns);

				if (nounsGeneralised.size() > 0) {
					actionDetailsFiltered.add(actionDetail);
				}

			} else if (objectOntology != null && attributeName.equals("object")) {

				// check if common hypernyms exists

				String[] nouns = new String[2];
				nouns[0] = String.valueOf(attributeValue);
				nouns[1] = String.valueOf(attributeValueCurrent);
				LinkedList<String> nounsGeneralised = objectOntology.generaliseNouns(nouns);

				if (nounsGeneralised.size() > 0) {
					actionDetailsFiltered.add(actionDetail);
				}

			} else if (locationOntology != null && attributeName.equals("location")) {

				// check if common ancestors exist
				String location1 = String.valueOf(attributeValue);
				String location2 = String.valueOf(attributeValueCurrent);
				LinkedList<String> locationsGeneralised = locationOntology.getClosestCommonAncestors(location1, location2);

				if (locationsGeneralised.size() > 0) {
					actionDetailsFiltered.add(actionDetail);
				}

			}

		}

		return actionDetailsFiltered;
	}

}
