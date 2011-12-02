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
		return getCoverage(actionDetails, null);
	}

	public int getCoverage(ArrayList<ActionDetail> actionDetails, TimeOntology timeOntology) {
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
						matching = false;
						break;
					}
				} else {
					if (!attributeValue.equals(value)) {
						matching = false;
						break;
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
