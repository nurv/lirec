package FAtiMA.culture;

import java.util.HashMap;

import FAtiMA.Core.exceptions.DuplicateSymbolTranslatorEntry;

/** @author: Samuel Mascarenhas
*/

public class SymbolTranslator {

	private HashMap<String,String> _symbolToAction; //symbols are Strings
	private HashMap<String,String> _actionToSymbol; //actions are Strings
	
	/**
	 * Singleton pattern 
	 */
	private static SymbolTranslator _symbolLibraryInstance = null;
	
	
	private SymbolTranslator(){
		this._symbolToAction = new HashMap<String,String>();
		this._actionToSymbol = new HashMap<String,String>();
	}
	
	
	public static SymbolTranslator GetInstance()
	{
		if(_symbolLibraryInstance == null)
		{
			_symbolLibraryInstance = new SymbolTranslator();
		}
		return _symbolLibraryInstance;
	}
	
	public void addEntry(String symbol, String action) throws DuplicateSymbolTranslatorEntry{
		
		if(this._symbolToAction.containsKey(symbol) ||
				this._actionToSymbol.containsKey(action)){
			throw new DuplicateSymbolTranslatorEntry();
		}else{
			_symbolToAction.put(symbol, action);
			_actionToSymbol.put(action, symbol);
		}
	}
	
	/**
	 * @param symbolName
	 * @return The action associated to symbolName. If there isn't any action 
	 * associated then it returns the parameter symbolName 
	 */
	public String translateSymbolToAction(String symbolName){
		String result = (String)_symbolToAction.get(symbolName);
		
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
	public String translateActionToSymbol(String actionName/*RemoteAction ra*/){
		
		String result = (String)_actionToSymbol.get(actionName);
		
		if(result == null){
			return actionName;
		}else{
			return result;
		}
	}
	
	
	public void clearAll(){
		this._actionToSymbol.clear();
		this._symbolToAction.clear();
	}
}