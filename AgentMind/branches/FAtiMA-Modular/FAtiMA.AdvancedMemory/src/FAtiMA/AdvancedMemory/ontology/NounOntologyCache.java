/** 
 * NounOntologyCache.java - A class for caching nouns and their common hypernyms.
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
 * Created: 14/12/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 14/12/11 - File created
 * **/

package FAtiMA.AdvancedMemory.ontology;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

public class NounOntologyCache {

	private static final NounOntologyCache instance = new NounOntologyCache();

	private static final int MAX_SIZE = 500;

	private HashMap<String, LinkedList<String>> commonHypernyms;

	private HashMap<String, Long> requestTimes;

	private NounOntologyCache() {
		commonHypernyms = new HashMap<String, LinkedList<String>>();
		requestTimes = new HashMap<String, Long>();
	}

	public static NounOntologyCache getInstance() {
		return instance;
	}

	public LinkedList<String> getCommonHypernyms(String[] nouns, int depthMax) {
		String nounsEncoded = encodeNouns(nouns, depthMax);
		LinkedList<String> hypernyms = commonHypernyms.get(nounsEncoded);
		if (hypernyms != null) {
			requestTimes.put(nounsEncoded, System.currentTimeMillis());
		}
		return hypernyms;
	}

	public void addCommonHypernyms(String[] nouns, int depthMax, LinkedList<String> lemmas) {
		String nounsEncoded = encodeNouns(nouns, depthMax);
		commonHypernyms.put(nounsEncoded, lemmas);
		requestTimes.put(nounsEncoded, System.currentTimeMillis());
		if (commonHypernyms.size() > MAX_SIZE) {

			// DEBUG
			System.out.println("MAX SIZE EXCEEDED");

			long requestTimeOldest = Long.MAX_VALUE;
			String nounsEncodedOldest = null;
			for (String nounsEncodedCurrent : requestTimes.keySet()) {
				long requestTimeCurrent = requestTimes.get(nounsEncodedCurrent);
				if (requestTimeCurrent < requestTimeOldest) {
					requestTimeOldest = requestTimeCurrent;
					nounsEncodedOldest = nounsEncodedCurrent;
				}
			}
			commonHypernyms.remove(nounsEncodedOldest);
			requestTimes.remove(nounsEncodedOldest);
		}
	}

	private String encodeNouns(String[] nouns, int depthMax) {
		Arrays.sort(nouns);
		String nounsEncoded = depthMax + "*";
		for (String noun : nouns) {
			nounsEncoded += "*" + noun;
		}
		return nounsEncoded;
	}

	public void clear() {
		commonHypernyms.clear();
		requestTimes.clear();
	}

}
