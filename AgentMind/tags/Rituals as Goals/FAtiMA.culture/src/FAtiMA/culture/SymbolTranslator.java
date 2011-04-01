package FAtiMA.culture;

import java.util.HashMap;

import FAtiMA.culture.exceptions.DuplicateSymbolTranslatorEntry;

/** @author: Samuel Mascarenhas
*/

public class SymbolTranslator {

	private HashMap<String,String> _symbolToMeaning; //symbols are Strings
	private HashMap<String,String> _meaningToSymbol; //actions are Strings
	
	/**
	 * Singleton pattern 
	 */
	private static SymbolTranslator _symbolLibraryInstance = null;
	
	
	private SymbolTranslator(){
		this._symbolToMeaning = new HashMap<String,String>();
		this._meaningToSymbol = new HashMap<String,String>();
	}
	
	
	public static SymbolTranslator GetInstance()
	{
		if(_symbolLibraryInstance == null)
		{
			_symbolLibraryInstance = new SymbolTranslator();
		}
		return _symbolLibraryInstance;
	}
	
	public void addEntry(String symbol, String meaning) throws DuplicateSymbolTranslatorEntry{
		
		if(this._symbolToMeaning.containsKey(symbol) ||
				this._meaningToSymbol.containsKey(meaning)){
			throw new DuplicateSymbolTranslatorEntry();
		}else{
			_symbolToMeaning.put(symbol, meaning);
			_meaningToSymbol.put(meaning, symbol);
		}
	}
	
	/**
	 * @param symbolName
	 * @return The action associated to symbolName. If there isn't any action 
	 * associated then it returns the parameter symbolName 
	 */
	public String translateSymbolToMeaning(String symbolName){
		String result = (String)_symbolToMeaning.get(symbolName);
		
		if(result == null){
			return symbolName;
		}else{
			return result;
		}
	}
	
	/**
	 * @param actionName
	 * @return The symbol associated to actionName. If there isn't any symbol 
	 * associated then it returns the parameter actionName 
	 */
	public String translateMeaningToSymbol(String actionName/*RemoteAction ra*/){
		
		String result = (String)_meaningToSymbol.get(actionName);
		
		if(result == null){
			return actionName;
		}else{
			return result;
		}
	}
	
	
	public void clearAll(){
		this._meaningToSymbol.clear();
		this._symbolToMeaning.clear();
	}
}