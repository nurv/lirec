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
 * Matthias Keysermann: 22/03/2011 - added minimum time interval
 */

package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.util.LinkedList;

public class RetrievalQueue implements Serializable {

	private static final long serialVersionUID = 1L;

	// size of FIFO queue (number of retrieval times)
	// a negative number means no limit
	private final static int MAX_RETRIEVAL_TIMES_DEFAULT = 10;

	// minimum time interval between retrievals (in ms)
	private final static long MIN_TIME_INTERVAL_DEFAULT = 1000;

	private int detailID;
	private int maxRetrievalTimes;
	private LinkedList<Time> retrievalTimes;
	private long minTimeInterval;

	public RetrievalQueue(int detailID) {
		this(detailID, MAX_RETRIEVAL_TIMES_DEFAULT, MIN_TIME_INTERVAL_DEFAULT);
	}

	public RetrievalQueue(int detailID, int maxRetrievalTimes,
			long minTimeInterval) {
		this.detailID = detailID;
		// maxRetrievalTimes is the queue size
		// a negative number means no limit
		this.maxRetrievalTimes = maxRetrievalTimes;
		this.retrievalTimes = new LinkedList<Time>();
		this.minTimeInterval = minTimeInterval;
	}

	public int getDetailID() {
		return detailID;
	}

	public void addRetrievalTime(Time time) {

		// check for minimum time interval
		if (retrievalTimes.size() > 0) {
			long difference = time.getNarrativeTime()
					- retrievalTimes.getLast().getNarrativeTime();
			if (difference < minTimeInterval) {
				// DEBUG
				// System.out
				// .println("RetrievalQueue.java: Minimum time interval deceeded!");
				return;
			}
		}

		// add retrieval time to end of queue
		retrievalTimes.add(time);
		// DEBUG
		// System.out.println("RetrievalQueue.java: added retrieval time for id "
		// + this.getDetailID());

		// non-negative number means queue has a size limit
		if (maxRetrievalTimes >= 0) {
			// check for queue size limit
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
