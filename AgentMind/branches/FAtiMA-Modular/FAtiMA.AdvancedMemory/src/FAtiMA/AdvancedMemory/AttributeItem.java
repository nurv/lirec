/** 
 * AttributeItem.java - Stores the name and the value for an attribute.
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
import java.util.HashSet;

import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.Time;

public class AttributeItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private Object value;
	private HashSet<String> hypernymSet;

	// If an ontology is used and applicable to this attribute item, then
	// hypernymSet contains hypernyms which were successfully applied for
	// generalising the value of this attribute item with another value. 
	// If no hypernyms could be applied successfully (even if hypernyms exist),
	// then hypernymSet is null. 
	// If no ontology was used or an ontology is not applicable for this
	// attribute item, then hypernymSet is null.

	// If an ontology is used for location and location is the target attribute,
	// then hypernymSet stores ancestors (instead of hypernyms) in an analog way.

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		setValue(value, null);
	}

	public void setValue(Object value, TimeOntology timeOntology) {
		this.value = value;
		if (timeOntology != null && value instanceof Time && name.equals("time")) {
			this.value = timeOntology.getAbstractedStr(((Time) value).getRealTime());
		}
	}

	public HashSet<String> getHypernymSet() {
		return hypernymSet;
	}

	public void setHypernymSet(HashSet<String> hypernymSet) {
		this.hypernymSet = hypernymSet;
	}

	public String toString() {
		return name + ": " + value;
	}

}
