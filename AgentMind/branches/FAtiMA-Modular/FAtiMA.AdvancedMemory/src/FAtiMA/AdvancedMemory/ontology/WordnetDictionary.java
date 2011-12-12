/** 
 * WordnetDictionary.java - A class for holding an instance of the WordNet dictionary.
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
 * Created: 09/12/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 09/12/11 - File created
 * **/

package FAtiMA.AdvancedMemory.ontology;

import java.net.URL;

import FAtiMA.Core.util.ConfigurationManager;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;

public class WordnetDictionary {

	private static final String DICTIONARY_PATH = ConfigurationManager.getMindPath() + "wordnet/dict/";

	private static final WordnetDictionary instance = new WordnetDictionary();

	private IDictionary dictionary;

	private WordnetDictionary() {
		try {
			URL url = new URL("file", null, DICTIONARY_PATH);
			dictionary = new Dictionary(url);
			dictionary.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static WordnetDictionary getInstance() {
		return instance;
	}

	public IDictionary getDictionary() {
		return dictionary;
	}

}
