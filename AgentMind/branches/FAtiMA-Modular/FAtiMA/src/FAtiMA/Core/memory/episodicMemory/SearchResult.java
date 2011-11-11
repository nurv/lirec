/** 
 * SearchResult.java - A structure holding information about a search on memory events.
 * The structure contains SearchKeys, time (System.currentTimeInMillis) and
 * corresponding ActionDetails.
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
 * Created: 09/10/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 09/10/11 - File created
 * 
 * **/

package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchResult implements Serializable {

	private static final long serialVersionUID = 1;

	private ArrayList<SearchKey> searchKeys; // search keys

	private long time; // time when search was performed

	private ArrayList<ActionDetail> actionDetails; // search result

	public SearchResult(ArrayList<SearchKey> searchKeys, long time, ArrayList<ActionDetail> actionDetails) {
		this.searchKeys = searchKeys;
		this.time = time;
		this.actionDetails = actionDetails;
	}

	public ArrayList<SearchKey> getSearchKeys() {
		return searchKeys;
	}

	public void setSearchKeys(ArrayList<SearchKey> searchKeys) {
		this.searchKeys = searchKeys;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public ArrayList<ActionDetail> getActionDetails() {
		return actionDetails;
	}

	public void setActionDetails(ArrayList<ActionDetail> actionDetails) {
		this.actionDetails = actionDetails;
	}

}
