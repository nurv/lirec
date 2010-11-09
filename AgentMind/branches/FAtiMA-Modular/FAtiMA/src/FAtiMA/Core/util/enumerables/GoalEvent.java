/*
 * GoalEvent.java - Class that implements the Enumerable for the different types of 
 * goal events, activation, failure and success
 */

package FAtiMA.Core.util.enumerables;


/**
 * @author João Dias
 *
 * Class that implements the Enumerable for PSI built-in motivators
 */
public class GoalEvent {
	public static final short ACTIVATION = 0;
	public static final short SUCCESS = 1;
	public static final short FAILURE = 2;
	public static final short CANCEL = 3;
	
	private static final String[] _goalEventTypes = {"activate",
		   											   "succeed",
													   "fail", "cancel"};
	
	
	/**
	 * Parses a string that corresponds to the GoalEvent and returns the appropriate
	 * GoalEvent (enumerable)
	 * @param goalEvent - the name of the type of goal event to search for
	 * @return - the id of the goal event type
	 * 
	 */
	public static short ParseType(String goalEvent) {
		short i;
		
		if(goalEvent == null) return -1;
		
		for(i=0; i < _goalEventTypes.length; i ++) {
			if(_goalEventTypes[i].equals(goalEvent)) return i;
		}
		
		return -1;
	}
	
	/**
	 * Gets the Goal's event type, given its identifier
	 * @param the id of the goal's event type
	 * @return the name of the goal's event type
	 */
	public static String GetName(short goalEvent) {
	    if(goalEvent == - 1) return null;
		if(goalEvent >= 0 && goalEvent < _goalEventTypes.length) return _goalEventTypes[goalEvent];
		return null;
	}
}