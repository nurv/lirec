/** 
 * AttributeItemSet.java - Stores a list of attribute items and forms a set regarding
 * the names of the attribute items as long as the method addToSet() is used for adding.
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
 * Created: 21/11/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 21/11/11 - File created
 * 
 * **/

package FAtiMA.AdvancedMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import FAtiMA.AdvancedMemory.ontology.TreeOntology;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;

public class AttributeItemSet implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<AttributeItem> attributeItems;

	public AttributeItemSet() {
		attributeItems = new ArrayList<AttributeItem>();
	}

	public ArrayList<AttributeItem> getAttributeItems() {
		return attributeItems;
	}

	public void setAttributeItems(ArrayList<AttributeItem> attributeItems) {
		this.attributeItems = attributeItems;
	}

	public boolean addToSet(AttributeItem attributeItem) {
		if (!containsName(attributeItem.getName())) {
			return attributeItems.add(attributeItem);
		}
		return false;
	}

	public boolean removeFromSet(AttributeItem attributeItem) {
		return attributeItems.remove(attributeItem);
	}

	public void clear() {
		attributeItems.clear();
	}

	public int size() {
		return attributeItems.size();
	}

	public AttributeItem get(int i) {
		return attributeItems.get(i);
	}

	public int getCoverage(ArrayList<ActionDetail> actionDetails) {
		return getCoverage(actionDetails, null, null, null, null);
	}

	public int getCoverage(ArrayList<ActionDetail> actionDetails, TimeOntology timeOntology, NounOntology targetOntology, NounOntology objectOntology, TreeOntology locationOntology) {
		int coverage = 0;

		for (ActionDetail actionDetail : actionDetails) {
			boolean matching = true;

			for (AttributeItem attributeItem : attributeItems) {
				String attributeName = attributeItem.getName();
				Object attributeValue = attributeItem.getValue();

				AttributeItem item = new AttributeItem();
				item.setName(attributeName);
				item.setValue(actionDetail.getValueByName(attributeName), timeOntology);
				Object value = item.getValue();

				if (attributeValue == null) {
					if (value != null) {
						// no two null values
						matching = false;
						break;

					}

				} else {

					if (!attributeValue.equals(value)) {
						// values are different

						// ontology usage: check for common hypernyms/ancestors

						if (attributeName.equals("target")) {

							if (targetOntology != null) {
								// use target ontology

								String[] nouns = new String[2];
								nouns[0] = String.valueOf(attributeValue);
								nouns[1] = String.valueOf(value);
								LinkedList<String> nounsGeneralised = targetOntology.generaliseNouns(nouns);

								if (nounsGeneralised.size() == 0) {
									// no common hypernyms found
									matching = false;
									break;

								} else {
									// common hypernyms found
									HashSet<String> hypernymSet = attributeItem.getHypernymSet();
									if (hypernymSet == null) {
										hypernymSet = new HashSet<String>();
										attributeItem.setHypernymSet(hypernymSet);
									}
									// add only the first common hypernym to set
									hypernymSet.add(nounsGeneralised.getFirst());

								}

							} else {
								// no target ontology given
								matching = false;
								break;

							}

						} else if (attributeName.equals("object")) {

							if (objectOntology != null) {
								// use object ontology

								String[] nouns = new String[2];
								nouns[0] = String.valueOf(attributeValue);
								nouns[1] = String.valueOf(value);
								LinkedList<String> nounsGeneralised = objectOntology.generaliseNouns(nouns);

								if (nounsGeneralised.size() == 0) {
									// no common hypernyms found
									matching = false;
									break;

								} else {
									// common hypernyms found
									HashSet<String> hypernymSet = attributeItem.getHypernymSet();
									if (hypernymSet == null) {
										hypernymSet = new HashSet<String>();
										attributeItem.setHypernymSet(hypernymSet);
									}
									hypernymSet.add(nounsGeneralised.getFirst());

								}

							} else {
								// no object ontology given
								matching = false;
								break;

							}

						} else if (attributeName.equals("location")) {

							if (locationOntology != null) {
								// use location ontology

								String locationA = String.valueOf(attributeValue);
								String locationB = String.valueOf(value);
								LinkedList<String> locationsGeneralised = locationOntology.getClosestCommonAncestors(locationA, locationB);

								if (locationsGeneralised.size() == 0) {
									// no common ancestors found
									matching = false;
									break;

								} else {
									// common ancestors found
									HashSet<String> hypernymSet = attributeItem.getHypernymSet();
									if (hypernymSet == null) {
										hypernymSet = new HashSet<String>();
										attributeItem.setHypernymSet(hypernymSet);
									}
									// add only the first common ancestor to set
									hypernymSet.add(locationsGeneralised.getFirst());

								}

							} else {
								// no location ontology given
								matching = false;
								break;

							}

						} else {
							// no ontology for current attribute
							matching = false;
							break;

						}

					}
				}
			}

			if (matching) {
				coverage += 1;
			}

		}

		return coverage;
	}

	public boolean containsName(String name) {
		for (AttributeItem attributeItemCurrent : attributeItems) {
			String nameCurrent = attributeItemCurrent.getName();
			if (nameCurrent == null) {
				if (name == null) {
					return true;
				}
			} else {
				if (nameCurrent.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean contains(AttributeItem attributeItem) {
		String name = attributeItem.getName();
		Object value = attributeItem.getValue();
		for (AttributeItem attributeItemCurrent : attributeItems) {
			String nameCurrent = attributeItemCurrent.getName();
			Object valueCurrent = attributeItemCurrent.getValue();
			boolean nameEquals = false;
			if (nameCurrent == null) {
				nameEquals = name == null;
			} else {
				nameEquals = nameCurrent.equals(name);
			}
			if (nameEquals) {
				if (valueCurrent == null) {
					return value == null;
				} else {
					return valueCurrent.equals(value);
				}
			}
		}
		return false;
	}

	public boolean equals(Object o) {
		if (!(o instanceof AttributeItemSet)) {
			return false;
		}
		AttributeItemSet attributeItemSetO = (AttributeItemSet) o;
		// check for size
		if (size() != attributeItemSetO.size()) {
			return false;
		}
		// check for items
		for (AttributeItem attributeItem : getAttributeItems()) {
			if (!attributeItemSetO.contains(attributeItem)) {
				return false;
			}
		}
		return true;
	}

	public String toString() {
		String str = "";
		for (AttributeItem attributeItem : attributeItems) {
			str += "# " + attributeItem + " #";
		}
		return str;
	}

}
