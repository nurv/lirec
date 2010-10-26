package FAtiMA.conditions;

import org.xml.sax.Attributes;

import FAtiMA.AgentModel;
import FAtiMA.exceptions.ContextParsingException;
import FAtiMA.wellFormedNames.Name;

/**
 * Represents a specific place in which a character needs to be for
 * the ritual to take place.
 * @author nafonso
 * @see Context
 * @see Ritual
 */
public class PlaceCondition extends PropertyCondition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static String SELF = "[SELF]";
	private static String KB_PLACE_STR = "place";
	private static String ATTRIBUTE_STR = "value";
	
	public static PlaceCondition Parse( Attributes attributes ) throws ContextParsingException {
		if( attributes.getLength() > 1 )
			throw createException("Can only have one argument, '"+ATTRIBUTE_STR+"'");
		String placeValue = attributes.getValue(ATTRIBUTE_STR);
		if( placeValue == null )
			throw createException("Couldn't find attribute '"+ATTRIBUTE_STR+"'. Instead found '"+attributes.getValue(0)+"'" );
		
		if( placeValue.equals("ANY") )
			return NullPlaceCondition.GetInstance();
		
		PlaceCondition place = new PlaceCondition();
		place._name = Name.ParseName(SELF+"("+KB_PLACE_STR+")");
		place._value = Name.ParseName(placeValue);
		
		return place;
	}
	
	protected PlaceCondition(){
		super(null,null,null);
	}
	
	public Object clone() {
		PlaceCondition aux = new PlaceCondition();
		aux._name = (Name)_name.clone();
		aux._value = (Name)_value.clone();
		return aux;
	}
	
	private static ContextParsingException createException( String msg ){
		return new ContextParsingException("PlaceCondition: "+msg);
	}
	
	public boolean CheckCondition(AgentModel am){
		if( !super.CheckCondition(am) )
			return false;
		
		Object currentPlace = _name.evaluate(am.getMemory());
		return currentPlace.equals(_value.toString());
	}
}
