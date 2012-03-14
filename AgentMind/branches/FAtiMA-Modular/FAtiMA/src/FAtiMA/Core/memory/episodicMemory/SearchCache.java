/** 
 * SearchCache.java - A cache structure for searches on memory events.
 * A HashMap maps SearchKeys (as String) to SearchResults.
 * Maximum cache size (number different searches) and expiry time (ms) are adjustable.
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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchCache implements Serializable {

	private static final long serialVersionUID = 1;

	private int maxSize; // number of cached searches

	private static final int MAX_SIZE = 10;

	private long expiryTime; // milliseconds

	private static final long EXPIRY_TIME = 1000;

	private HashMap<String, SearchResult> cachedSearches;

	public SearchCache() {
		this(MAX_SIZE, EXPIRY_TIME);
	}

	public SearchCache(int maxSize, long expiryTime) {
		this.maxSize = maxSize;
		this.expiryTime = expiryTime;
		cachedSearches = new HashMap<String, SearchResult>();
	}

	public void addSearchResult(ArrayList<SearchKey> searchKeys, ArrayList<ActionDetail> actionDetails) {

		// add current search to cache
		SearchResult searchResult = new SearchResult(searchKeys, System.currentTimeMillis(), actionDetails);
		cachedSearches.put(encodeSearchKeys(searchKeys), searchResult);

		// check if maximum cache size is exceeded
		if (cachedSearches.size() > maxSize) {
			// clear oldest entry
			String searchKeysStrOldest = null;
			for (String searchKeysStr : cachedSearches.keySet()) {
				long timeCurrent = cachedSearches.get(searchKeysStr).getTime();
				if (searchKeysStrOldest == null || timeCurrent < cachedSearches.get(searchKeysStrOldest).getTime()) {
					searchKeysStrOldest = searchKeysStr;
				}
			}
			cachedSearches.remove(searchKeysStrOldest);
		}
	}

	public ArrayList<ActionDetail> getSearchResult(ArrayList<SearchKey> searchKeys) {
		String searchKeysStr = encodeSearchKeys(searchKeys);

		// check if the search is cached
		SearchResult cachedSearch = cachedSearches.get(searchKeysStr);
		if (cachedSearch != null) {

			// check if cached search is not expired
			if (System.currentTimeMillis() < cachedSearch.getTime() + expiryTime) {
				// generate retrievals?
				// 
				// return cached result
				return cachedSearch.getActionDetails();
			} else {
				// remove this search from cache
				cachedSearches.remove(searchKeysStr);
			}

		}
		return null;
	}

	public void cleanUp() {
		// check for expired searches
		long currentTimeMillis = System.currentTimeMillis();
		for (String searchKeysStr : cachedSearches.keySet()) {
			if (currentTimeMillis >= cachedSearches.get(searchKeysStr).getTime() + expiryTime) {
				cachedSearches.remove(searchKeysStr);
			}
		}
	}

	public void clear() {
		cachedSearches.clear();
	}

	private String encodeSearchKeys(ArrayList<SearchKey> searchKeys) {
		// encode search keys in one string
		String searchKeysStr = "";
		for (SearchKey searchKey : searchKeys) {
			searchKeysStr += "*" + searchKey.getField() + " " + searchKey.getKey();
		}
		return searchKeysStr;
	}

	private void writeObject(java.io.ObjectOutputStream out) throws IOException {
		// clear cache before serializing to avoid inconsistencies
		clear();
		out.defaultWriteObject();
	}

}
