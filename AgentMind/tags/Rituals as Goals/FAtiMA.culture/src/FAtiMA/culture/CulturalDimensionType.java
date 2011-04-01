package FAtiMA.culture;

import FAtiMA.culture.exceptions.InvalidDimensionTypeException;

public class CulturalDimensionType {
	public static final short POWERDISTANCE = 0;
	public static final short COLLECTIVISM = 1;
	public static final short MASCULINITY = 2;
	public static final short UNCERTAINTYAVOIDANCE = 3;
	public static final short LONGTERMORIENTATION = 4;

	public static int numberOfTypes(){
		return 5;
	}
	
	public static short ParseType(String dimensionType) throws InvalidDimensionTypeException {

		
		if(dimensionType == null) throw new InvalidDimensionTypeException();
		
		if(dimensionType.equalsIgnoreCase("PowerDistance")){
			return POWERDISTANCE;
		}else if(dimensionType.equalsIgnoreCase("Collectivism")){
			return COLLECTIVISM;
		}else if(dimensionType.equalsIgnoreCase("Masculinity")){
			return MASCULINITY;
		}else if(dimensionType.equalsIgnoreCase("UncertaintyAvoidance")){
			return UNCERTAINTYAVOIDANCE;
		}else if(dimensionType.equalsIgnoreCase("LongTermOrientation")){
			return LONGTERMORIENTATION;
		}else{			
			throw new InvalidDimensionTypeException();
		}
	}
}
