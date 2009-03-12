package FAtiMA.exceptions;

public class ContextParsingException extends Exception {
	public ContextParsingException( String msg ){
		super( "Error parsing Context - " + msg );
	}
	
	public static ContextParsingException CreateTimeConditionException( String msg ){
		return new ContextParsingException( "TimeCondition: " + msg );
	}
	
	public static ContextParsingException CreatePlaceConditionException( String msg ){
		return new ContextParsingException( "PlaceCondition: " + msg );
	}
	
	public static ContextParsingException CreateSocialConditionException( String msg ){
		return new ContextParsingException( "SocialCondition: " + msg );
	}
}
