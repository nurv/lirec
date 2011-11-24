/** 
 * GER.java - Stores an attribute item set together with its coverage.
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

public class GER implements Serializable {

	private static final long serialVersionUID = 1L;

	private AttributeItemSet attributeItemSet;

	private int coverage;

	public AttributeItemSet getAttributeItemSet() {
		return attributeItemSet;
	}

	public void setAttributeItemSet(AttributeItemSet attributeItemSet) {
		this.attributeItemSet = attributeItemSet;
	}

	public int getCoverage() {
		return coverage;
	}

	public void setCoverage(int coverage) {
		this.coverage = coverage;
	}

	public AttributeItem getAttributeItem(int i) {
		return attributeItemSet.get(i);
	}

	public AttributeItem getAttributeItem(String name) {
		for (AttributeItem attributeItem : attributeItemSet.getAttributeItems()) {
			if (attributeItem.getName().equals(name)) {
				return attributeItem;
			}
		}
		return null;
	}

	public String toString() {
		return attributeItemSet.toString() + " Coverage: " + coverage;
	}
}
