/** 
 * NounOntology.java - A class for abstracting nouns using WordNet.
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
 * Created: 02/12/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 02/12/11 - File created
 * Matthias Keysermann: 05/12/11 - Changed generaliseNouns() iteratively increase depth limit
 *                                 and stop when common hypernyms are found.
 *                                 This way is more efficient as long as common hypernyms are
 *                                 found at shallow depths. If no common hypernyms are found,
 *                                 this way is less efficient, as previous depths will be
 *                                 searched repeatedly after each increment.
 *                                 ITERATIVE_DEPTH_LIMIT controls the use. 
 * **/

package FAtiMA.AdvancedMemory.ontology;

import java.io.Serializable;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class NounOntology implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final boolean ITERATIVE_DEPTH_LIMIT = true;

	private static final String DICTIONARY_PATH = "data/characters/minds/wordnet/dict/";

	private IDictionary dict;

	private int depthMax;

	public int getDepthMax() {
		return depthMax;
	}

	public void setDepthMax(int depthMax) {
		this.depthMax = depthMax;
	}

	public void openDict() {
		try {
			URL url = new URL("file", null, DICTIONARY_PATH);
			dict = new Dictionary(url);
			dict.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeDict() {
		if (dict != null) {
			dict.close();
		}
	}

	/**
	 * Generalises nouns by finding a common hypernym, iteratively increasing
	 * the depth limit. Stops if common hypernyms are found for the current
	 * depth limit or the given maximum depth is reached.
	 * 
	 * @param nouns
	 *            nouns to generalise over
	 * @return list of common hypernyms
	 */
	public LinkedList<IWord> generaliseNouns(String[] nouns) {

		// check for minimum number of nouns
		if (nouns.length < 2) {
			return null;
		}

		// initialise
		LinkedList<IWord> wordsMatching = new LinkedList<IWord>();

		// set starting depth
		int depthLimitStart = depthMax;
		if (ITERATIVE_DEPTH_LIMIT) {
			depthLimitStart = 0;
		}

		// iteratively increase depth limit
		for (int depthLimit = depthLimitStart; depthLimit <= depthMax; depthLimit++) {

			// retrieve hypernyms of first noun
			wordsMatching = getNounHypernyms(nouns[0], 0, depthLimit);

			// loop over remaining nouns
			for (int i = 1; i < nouns.length; i++) {

				// retrieve hypernyms
				LinkedList<IWord> words = getNounHypernyms(nouns[i], 0, depthLimit);

				// store new matching words
				LinkedList<IWord> wordsMatchingNew = new LinkedList<IWord>();

				// compare matching words with
				for (IWord word : words) {
					if (wordsMatching.contains(word) && !wordsMatchingNew.contains(word)) {
						wordsMatchingNew.add(word);
					}
				}

				// update matching words
				wordsMatching = wordsMatchingNew;

			}

			// stop if common hypernyms are found
			if (wordsMatching.size() > 0) {
				return wordsMatching;
			}

		}

		return wordsMatching;
	}

	/**
	 * Returns a list of noun hypernyms, up to a given depth limit, recursive
	 * 
	 * @param noun
	 *            noun to retrieve hypernyms for
	 * @param depth
	 *            current recursion depth
	 * @param depthLimit
	 *            depth limit for retrieving hypernyms
	 * @return list of hypernyms
	 */
	public LinkedList<IWord> getNounHypernyms(String noun, int depth, int depthLimit) {
		LinkedList<IWord> words = new LinkedList<IWord>();

		// get word from dictionary
		IIndexWord indexWord = dict.getIndexWord(noun, POS.NOUN);
		if (indexWord == null) {
			// word is not in dictionary
			return words;
		}

		// loop over word IDs
		List<IWordID> wordIDs = indexWord.getWordIDs();
		for (IWordID wordID : wordIDs) {
			IWord word = dict.getWord(wordID);
			words.add(word);

			// check for max recursion depth
			if (depth < depthLimit) {

				// fetch hypernyms
				ISynset synset = word.getSynset();
				List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);

				// loop over hypernyms
				for (ISynsetID hypernym : hypernyms) {
					// loop over words
					for (IWord hypernymword : dict.getSynset(hypernym).getWords()) {
						words.addAll(getNounHypernyms(hypernymword.getLemma(), depth + 1, depthLimit));
					}
				}

			}
		}

		return words;
	}

}
