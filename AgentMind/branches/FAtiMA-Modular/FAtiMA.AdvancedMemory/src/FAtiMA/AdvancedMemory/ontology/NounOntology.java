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
 * Matthias Keysermann: 05/12/11 - Changed generaliseNouns() to iteratively increase the
 *                                 depth limit and stop when common hypernyms are found.
 *                                 This way is more efficient as long as common hypernyms
 *                                 are found at shallow depths. If no common hypernyms are
 *                                 found, this way is less efficient, as previous depths
 *                                 will be searched repeatedly after each increment.
 *                                 The boolean ITERATIVE_DEPTH_LIMIT controls the use.
 * Matthias Keysermann: 06/12/11 - generaliseNouns() returns a list of Strings
 * Matthias Keysermann: 06/12/11 - Fixed a bug with non-dictionary words. No generalised
 *                                 words were returned which led to zero counts.
 *                                 Now the word itself is returned if all given words are
 *                                 the same, i.e. the word itself is the only hypernym.
 * Matthias Keysermann: 08/12/11 - Dictionary is opened when object is created,
 *                                 never closed explicitly.
 *                                 Methods openDict(), closeDict() have been removed.
 * Matthias Keysermann: 08/12/11 - Added stemming.
 * **/

package FAtiMA.AdvancedMemory.ontology;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

public class NounOntology implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final boolean ITERATIVE_DEPTH_LIMIT = false;

	private static final boolean STEMMING = true;

	private int depthMax;

	public int getDepthMax() {
		return depthMax;
	}

	public void setDepthMax(int depthMax) {
		this.depthMax = depthMax;
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
	public LinkedList<String> generaliseNouns(String[] nouns) {

		// check for minimum number of nouns
		if (nouns.length < 2) {
			return null;
		}

		// set starting depth
		int depthLimitStart = depthMax;
		if (ITERATIVE_DEPTH_LIMIT) {
			depthLimitStart = 0;
		}

		// iteratively increase depth limit
		for (int depthLimit = depthLimitStart; depthLimit <= depthMax; depthLimit++) {

			// retrieve hypernyms of first noun
			LinkedList<IWord> hypernymsCommon = getNounHypernyms(nouns[0], 0, depthLimit);

			// check if noun is not in dictionary
			if (hypernymsCommon == null) {
				// check if other nouns are equal
				for (int j = 1; j < nouns.length; j++) {
					if (!nouns[0].equals(nouns[j])) {
						// This noun is not equal to the first noun.
						// As we do not know about any hypernyms, we
						// assume that not hypernyms exist.
						// return an empty list
						LinkedList<String> lemmas = new LinkedList<String>();
						return lemmas;
					}
				}
				// all nouns are equal
				// the noun itself is a hypernym
				// return a list with only this noun
				LinkedList<String> lemmas = new LinkedList<String>();
				lemmas.add(nouns[0]);
				return lemmas;
			}

			// loop over remaining nouns
			for (int i = 1; i < nouns.length; i++) {

				// retrieve hypernyms
				LinkedList<IWord> hypernymsCurrent = getNounHypernyms(nouns[i], 0, depthLimit);

				// check if noun is not in dictionary
				if (hypernymsCurrent == null) {
					// As we reached this part, the first noun must have be
					// in the dictionary. As this noun is not, it cannot be equal
					// to the first noun. As we do not know about any hypernyms, we
					// assume that not hypernyms exist.
					// return an empty list
					LinkedList<String> lemmas = new LinkedList<String>();
					return lemmas;
				}

				// remove hypernyms not contained in both lists 
				for (int j = 0; j < hypernymsCommon.size(); j++) {
					IWord hypernymCommon = hypernymsCommon.get(j);
					if (!hypernymsCurrent.contains(hypernymCommon)) {
						hypernymsCommon.remove(hypernymCommon);
						j--;
					}
				}

			}

			// stop if common hypernyms are found or maximum depth is reached
			if (hypernymsCommon.size() > 0 || depthLimit == depthMax) {
				LinkedList<String> lemmas = new LinkedList<String>();
				for (IWord wordMatching : hypernymsCommon) {
					lemmas.add(wordMatching.getLemma());
				}
				return lemmas;
			}

		}

		return null;
	}

	/**
	 * Returns a list of noun hypernyms, up to a given depth limit, recursive
	 * 
	 * @param noun
	 *            noun to retrieve hypernyms for
	 * @param depth
	 *            current recursion depth, call with 0
	 * @param depthLimit
	 *            depth limit for retrieving hypernyms
	 * @return list of hypernyms
	 */
	public LinkedList<IWord> getNounHypernyms(String noun, int depth, int depthLimit) {
		IDictionary dictionary = WordnetDictionary.getInstance().getDictionary();

		// stemming
		if (STEMMING) {
			WordnetStemmer wnStemmer = new WordnetStemmer(dictionary);
			List<String> stems = wnStemmer.findStems(noun, POS.NOUN);
			if (stems.size() > 0) {
				// use first stem
				noun = stems.get(0);
			}
		}

		// initialise
		LinkedList<IWord> words = new LinkedList<IWord>();

		// get word from dictionary
		IIndexWord indexWord = dictionary.getIndexWord(noun, POS.NOUN);
		if (indexWord == null) {
			// word is not in dictionary
			return null;
		}

		// loop over word IDs
		List<IWordID> wordIDs = indexWord.getWordIDs();
		for (IWordID wordID : wordIDs) {
			IWord word = dictionary.getWord(wordID);
			words.add(word);

			// check for max recursion depth
			if (depth < depthLimit) {

				// fetch hypernyms
				ISynset synset = word.getSynset();
				List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);

				// loop over hypernyms
				for (ISynsetID hypernym : hypernyms) {
					// loop over words
					for (IWord hypernymWord : dictionary.getSynset(hypernym).getWords()) {
						words.addAll(getNounHypernyms(hypernymWord.getLemma(), depth + 1, depthLimit));
					}
				}

			}
		}

		return words;
	}

}
