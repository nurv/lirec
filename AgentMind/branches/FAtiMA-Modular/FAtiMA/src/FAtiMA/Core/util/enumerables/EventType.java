/*
 * EventType.java - Class that implements the Enumerable for the different types of event 
 * - goal or action
 */


package FAtiMA.Core.util.enumerables;


/**
 * @author Meiyii Lim
 * 07/01/10
 * 
 * Class that implements the Enumerable for the different types of event - goal or action
 */

public class EventType {
	public static final short GOAL = 0;
	public static final short ACTION = 1;
	
	private static final String[] _eventTypes = {"goal",
		   										 "action"};
	
	/**
	 * Parses a string that corresponds to the EventType and returns the appropriate
	 * EventType (enumerable)
	 * @param eventType - the name of the type of goal event to search for
	 * @return - the id of the event type
	 * 
	 */
	public static short ParseType(String eventType) {
		short i;
		
		if(eventType == null) return -1;
		
		for(i=0; i < _eventTypes.length; i ++) {
			if(_eventTypes[i].equals(eventType)) return i;
		}
		
		return -1;
	}
	
	/**
	 * Gets the event type, given its identifier
	 * @param the id of the event type
	 * @return the name of the event type
	 */
	public static String GetName(short eventType) {
	    if(eventType == - 1) return null;
		if(eventType >= 0 && eventType < _eventTypes.length) return _eventTypes[eventType];
		return null;
	}
}
