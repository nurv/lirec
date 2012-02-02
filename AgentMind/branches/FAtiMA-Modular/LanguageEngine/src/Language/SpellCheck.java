package Language;

import java.io.File;
import java.util.Iterator;
import java.util.List; 

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
//import com.swabunga.spell.engine.Configuration;

/**
 * A spell checking engine.
 * Works by comparing the words to check with words compiled in a lexicon using the Levenshtein
 * distance.
 * The lexicon is just a plain text file with a list of the words.<br>
 * Adapted from CFSpellCheck by Darron Schall (<a href="http://www.darronschall.com">www.darronschall.com</a>) 
 * 
 * @author <a href="mailto:darron@darronschall.com">Darron Schall</a>
 * @author Bettina Conradi
 * @author Eva Loesch
 * @author Thurid Vogt
 *
 */
class SpellCheck {

		/**
		 * The dictionary
		 */
        private SpellDictionary dictionary;
        
        /**
         * The spell checking engine
         */
        private SpellChecker spellChecker;
        
        /**
         * Contains the text to be checked.
         * Must be set for every new run of spell checking.
         */
        private String textToCheck;

        /**
         * constructs a spell checker from the dictionary found in file <code>dict</code>

         * @param dict the name of the dictionary file
         */
        public SpellCheck(String dict) {
        	setDictionary(dict);
//        	spellChecker = new SpellChecker(dictionary);
        }

        /**
         * returns the current text that needs to be checked
         * 
         * @return the current text to be checked
         */
        public String getText() {
                return textToCheck;
        }

        /**
         * start spell checking.
         * The text must be set beforehand with {@link SpellCheck#setText(String)}
         *
         */
        public void checkSpelling() {
                spellChecker = new SpellChecker(dictionary);
//                spellChecker.getConfiguration().setInteger(Configuration.COST_INSERT_CHAR,20);
                spellChecker.addSpellCheckListener(new SpellCheckListener() {
                        public void spellingError(SpellCheckEvent event) {
                            event.replaceWord(event.getInvalidWord(),true);
                                List suggestions = event.getSuggestions();
                                Iterator suggestedWord = suggestions.iterator();
                                if (suggestedWord.hasNext())
                                	textToCheck = textToCheck.replace(event.getInvalidWord(), suggestedWord.next().toString());
                                else
                                	LanguageEngine.Debug(event.getInvalidWord()+" not in dictionary!");
                        }
                });
                spellChecker.checkSpelling(new StringWordTokenizer(textToCheck));
        }

        /**
         * change the dictionary file after creation.
         * 
         * @param dictFile the name of the dictionary file
         */
        public void setDictionary(String dictFile) {
                try {
                        dictionary = new SpellDictionaryHashMap(new File(dictFile));
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        /**
         * set the text to be checked.
         * 
         * @param txt the text to be checked
         */
        public void setText(String txt) {
                textToCheck = txt;
        }

}
