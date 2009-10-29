package FAtiMA.conditions;


import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.AgentModel;
import FAtiMA.exceptions.ContextParsingException;
import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.wellFormedNames.Name;

/**
 * Specifies when a ritual can be activated.
 * @author nafonso
 * @see Context
 * @see Ritual
 */
public class TimeCondition extends PropertyCondition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String SELF = "[SELF]";
	private static String KB_TIME_STR = "time";
	private static String ATTRIBUTE_STR = "value";

	public static TimeCondition Parse( Attributes attributes ) throws ContextParsingException{
		if( attributes.getLength() > 1 )
			throw createException("Can only have one argument, '"+ATTRIBUTE_STR+"'");
		String timeValue = attributes.getValue(ATTRIBUTE_STR);
		if( timeValue == null )
			throw createException("Couldn't find attribute '"+ATTRIBUTE_STR+"'. Instead found '"+attributes.getValue(0)+"'" );
		
		if( timeValue.endsWith("ANY") )
			return NullTimeCondition.GetInstance();
		
		TimeCondition time = new TimeCondition();
		
		time._name = Name.ParseName(SELF+"("+KB_TIME_STR+")");
		time._value = Name.ParseName(timeValue);
		
		return time;
	}

	protected TimeCondition(){
		super(null,null);
	}
	
	public boolean CheckCondition(AgentModel am) {
		if( !super.CheckCondition(am) )
			return false;
		
		Object currentTime = _name.evaluate(am.getMemory());
		return currentTime.toString().equals(_value.toString());
	}
	
	private static ContextParsingException createException( String msg ){
		return new ContextParsingException( "TimeCondition: " + msg );
	}
	
	public Object clone() {
		TimeCondition aux = new TimeCondition();
		aux._name = (Name)_name.clone();
		aux._value = (Name)_value.clone();
		return aux;
	}
}
