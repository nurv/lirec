/*
 * MotivatorType.java - Class that implements the Enumerable for PSI built-in motivators
 */

package FAtiMA.motivationalSystem;


/**
 * @author Meiyii Lim
 *
 * Class that implements the Enumerable for PSI built-in motivators
 */
public class MotivatorType {
	public static final short AFFILIATION = 0;
	public static final short INTEGRITY = 1;
	public static final short ENERGY = 2;
	public static final short CERTAINTY = 3;
	public static final short COMPETENCE = 4;
	public static final short NO_MOTIVATORS = 5;
	
	private static final String[] _motivatorTypes = {"Affiliation",
		   											   "Integrity",
													   "Energy",
													   "Certainty",
													   "Competence",};
	
	public static int numberOfTypes(){
		return _motivatorTypes.length;
	}
	
	/**
	 * Parses a string that corresponds to the motivator type and returns the appropriate
	 * motivator type (enumerable)
	 * @param motivatorType - the name of the motivator to search for
	 * @return - the id of the motivator type
	 * 
	 */
	public static short ParseType(String motivatorType) throws InvalidMotivatorTypeException {
		short i;
		
		if(motivatorType == null) throw new InvalidMotivatorTypeException(null);
		
		for(i=0; i < _motivatorTypes.length; i ++) {
			if(_motivatorTypes[i].equals(motivatorType)) return i;
		}
		
		throw new InvalidMotivatorTypeException(motivatorType);
	}
	
	/**
	 * Gets the motivator's name, given its identifier
	 * @param the id of the motivatorType
	 * @return the name of the motivatorType
	 */
	public static String GetName(short motivatorType) {
	    if(motivatorType == - 1) return "Neutral";
		if(motivatorType >= 0 && motivatorType < NO_MOTIVATORS) return _motivatorTypes[motivatorType];
		return null;
	}
}
