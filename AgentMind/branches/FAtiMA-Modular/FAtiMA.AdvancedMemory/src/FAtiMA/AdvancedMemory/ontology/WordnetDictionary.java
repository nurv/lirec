package FAtiMA.AdvancedMemory.ontology;

import java.net.URL;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;

public class WordnetDictionary {

	private static final String DICTIONARY_PATH = "data/characters/minds/wordnet/dict/";

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
