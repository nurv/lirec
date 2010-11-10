/*
 * InvalidMotivatorTypeException.java - Exception thrown when an invalid MotivatorType 
 * is parsed in the enumerable Class of MotivatorType
 */

package FAtiMA.motivationalSystem;

/**
 *  Exception thrown when an invalid MotivatorType is parsed in the enumerable Class of MotivatorType
 * 
 *  @author Meiyii Lim
 */

public class InvalidMotivatorTypeException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public InvalidMotivatorTypeException(String motivator) {
        super("ERROR: Invalid motivator type " + motivator);
    }
    
    public InvalidMotivatorTypeException(int num) {
        super("ERROR: invalid motivator type indentifier " + num);
    }
}
