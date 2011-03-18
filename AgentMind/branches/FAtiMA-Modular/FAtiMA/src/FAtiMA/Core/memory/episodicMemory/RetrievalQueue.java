/** 
 * RetrievalQueue.java - FIFO queue for storing retrieval times
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
import java.util.LinkedList;

public class RetrievalQueue implements Serializable {

	private static final long serialVersionUID = 1;

	private final static int MAX_RETRIEVAL_TIMES_DEFAULT = 10;

	private long detailID;
	private int maxRetrievalTimes;
	private LinkedList<Time> retrievalTimes;

	public RetrievalQueue(long detailID) {
		this(detailID, MAX_RETRIEVAL_TIMES_DEFAULT);
	}

	public RetrievalQueue(long detailID, int maxRetrievalTimes) {
		this.detailID = detailID;
		// maxRetrievalTimes is the queue size
		// a negative number means no limit
		this.maxRetrievalTimes = maxRetrievalTimes;
		this.retrievalTimes = new LinkedList<Time>();
	}

	public long getDetailID() {
		return detailID;
	}

	public void addRetrievalTime(Time time) {
		retrievalTimes.add(time);
		if (maxRetrievalTimes >= 0) {
			// queue has a size limit
			if (retrievalTimes.size() > maxRetrievalTimes) {
				retrievalTimes.removeFirst();
			}
		}
	}

	public LinkedList<Time> getRetrievalTimes() {
		return retrievalTimes;
	}

	public String toString() {
		String str = "Retrieval queue for id " + detailID + ":\n";
		for (Time time : retrievalTimes) {
			str += "  " + time + "\n";
		}
		return str;
	}

}
