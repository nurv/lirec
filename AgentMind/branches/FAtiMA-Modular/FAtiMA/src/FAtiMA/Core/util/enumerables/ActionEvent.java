/*
 * ActionEvent.java - Class that implements the Enumerable for the different types of action event 
 * - success or failure
 */

package FAtiMA.Core.util.enumerables;

/**
 * @author Meiyii Lim
 * 07/01/10
 * 
 * Class that implements the Enumerable for the different types of action event - success or failure
 */

public class ActionEvent {

	public static final short SUCCESS = 0;
	public static final short FAILURE = 1;
	
	private static final String[] _actionEventTypes = {"succeed",
													   "fail"};
	
	/**
	 * Parses a string that corresponds to the ActionEvent and returns the appropriate
	 * ActionEvent (enumerable)
	 * @param actionEvent - the name of the type of goal event to search for
	 * @return - the id of the action event type
	 * 
	 */
	public static short ParseType(String actionEvent) {
		short i;
		
		if(actionEvent == null) return -1;
		
		for(i=0; i < _actionEventTypes.length; i ++) {
			if(_actionEventTypes[i].equals(actionEvent)) return i;
		}
		
		return -1;
	}
	
	/**
	 * Gets the Action's event type, given its identifier
	 * @param the id of the action's event type
	 * @return the name of the action's event type
	 */
	public static String GetName(short actionEvent) {
	    if(actionEvent == - 1) return null;
		if(actionEvent >= 0 && actionEvent < _actionEventTypes.length) return _actionEventTypes[actionEvent];
		return null;
	}
}
