/** 
 * ActivationValue.java - class for storing the activation value
 * and the number of retrievals included in the calculcation
 * for a certain ActionDetail
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
 * Created: 08/03/2011 
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 08/03/2011 - File created
 */

package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;

public class ActivationValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private int detailID;
	private double value;
	private int numRetrievals;

	public ActivationValue(int detailID) {
		this.detailID = detailID;
		value = 0;
		numRetrievals = 0;
	}

	public ActivationValue(int detailID, double activationValue,
			int numRetrievals) {
		this.detailID = detailID;
		this.value = activationValue;
		this.numRetrievals = numRetrievals;
	}

	public int getDetailID() {
		return detailID;
	}

	public void setDetailID(int detailID) {
		this.detailID = detailID;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getNumRetrievals() {
		return numRetrievals;
	}

	public void setNumRetrievals(int numRetrievals) {
		this.numRetrievals = numRetrievals;
	}

	public String toString() {
		String str = "Activation value for id " + detailID + ":"
				+ this.getValue() + "(" + this.getNumRetrievals()
				+ " retrievals)\n";
		return str;
	}

}
