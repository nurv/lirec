package FAtiMA.culture;

import org.xml.sax.Attributes;


import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.PropertyCondition;
import FAtiMA.Core.exceptions.ContextParsingException;
import FAtiMA.Core.memory.semanticMemory.KnowledgeBase;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.wellFormedNames.Name;

/**
 * Represents a Social Relation that needs to be fullfiled in order to trigger
 * the ritual.
 * The xml should be defined as <SocialRelation RELATION_NAME_STR="relation_name" TARGET_STR="character_name" OPERATOR_STR="operator" VALUE_STR="relation_value"/>,
 * for example, by default: <SocialRelation name="likes" target="John" operator=">" value="3"/>.
 * 		A target must be a character;
 * 		The value must be an integer in the range [-10;10]; 
 * 		Operator can be one of the following < <= = >= > !=
 * @author nafonso
 * @see Context
 * @see Ritual
 */
public class SocialCondition extends PropertyCondition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static String RELATION_NAME_STR = "name";
	private static String TARGET_STR = "target";
	private static String OPERATOR_STR = "operator";
	private static String VALUE_STR = "value";
	
	private static Operator LESS_THAN = new LessThan();
	private static Operator LESS_THAN_OR_EQUAL = new LessThanOrEqual();
	private static Operator EQUAL = new Equal();
	private static Operator MORE_THAN_OR_EQUAL = new MoreThanOrEqual();
	private static Operator MORE_THAN = new MoreThan();
	private static Operator NOT_EQUAL = new NotEqual();
	
	private Operator _operator;
	
	public static SocialCondition Parse( Attributes attributes ) throws ContextParsingException{
		SocialCondition socialRel = new SocialCondition();
		if( attributes.getLength() != 4 )
			throw createException("There should be exactly 4 attributes in the xml ("+RELATION_NAME_STR+";"+TARGET_STR+";"+OPERATOR_STR+";"+VALUE_STR+")");
		
		String nameStr = attributes.getValue(RELATION_NAME_STR);
		if( nameStr == null )
			throw createException("Missing attribute '"+RELATION_NAME_STR+"'");
		
		String targetStr = attributes.getValue(TARGET_STR);
		if( nameStr == null )
			throw createException("Missing attribute '"+TARGET_STR+"'");
		
		socialRel._operator = parseOperator( attributes.getValue(OPERATOR_STR) );
		
		String valueStr = attributes.getValue(VALUE_STR);
		
		
		try{
			int relationValue = 0;
			relationValue = Integer.parseInt( valueStr );
			
			if( relationValue < -10 || relationValue > 10 )
				throw createException("The '"+VALUE_STR+"' attribute should be an integer between -10 and 10");
		}
		catch(NumberFormatException e){
			
			
			// Its not a number, lets see if it's a bool
			if( !valueStr.equalsIgnoreCase("true") && !valueStr.equalsIgnoreCase("false") ){
				// isn't a boolean, then we assume it is as character!
				valueStr += "("+nameStr+")";
			}
			else{
				// if it is a bool, check if the operator is equal or not equal
				if( !(socialRel._operator instanceof SocialCondition.Equal) && !(socialRel._operator instanceof SocialCondition.NotEqual)){
					AgentLogger.GetInstance().logAndPrint("Received a bool in a SocialCondition but the operator is not '=' nor '!='");
				}
			}
		}

		/// TODO Check if target is a character
		//if( target is not character )
		//	throw createException("The '"+_targetStr+"' attribute should be a character");
		
		socialRel._name = Name.ParseName(targetStr+"("+nameStr+")");
		socialRel._value = Name.ParseName( valueStr );
		
		return socialRel;
	}

	protected SocialCondition(){
		super(null,null,null);
	}
	
	public Object clone() {
		SocialCondition aux = new SocialCondition();
		aux._name = (Name)_name.clone();
		aux._value = (Name)_value.clone();
		aux._operator = _operator;
		return aux;
	}
	
	public boolean CheckCondition(AgentModel am){
		boolean result = false;
		
		if( !super.CheckCondition(am) )
			return result;
		
		Object actualRelationValueStr = _name.evaluate(am.getMemory()); 
		if( actualRelationValueStr == null )
			return result;
		
		Object relationValueStr = _value.evaluate(am.getMemory());
		if( relationValueStr == null ) // i.e. is a constant
			relationValueStr = _value;
		
		
		int actualRelationValue = -1;
		
		if( actualRelationValueStr.toString().equalsIgnoreCase("true") )
			actualRelationValue = 1;
		else if( actualRelationValueStr.toString().equalsIgnoreCase("false") )
			actualRelationValue = 0;
		else
			actualRelationValue = Integer.parseInt(actualRelationValueStr.toString());
		
		int relationValue = -1;
		if( relationValueStr.toString().equalsIgnoreCase("true") )
			relationValue = 1;
		else if( relationValueStr.toString().equalsIgnoreCase("false") )
			relationValue = 0;
		else
			relationValue = Integer.parseInt(relationValueStr.toString());
		
		result = _operator.process( actualRelationValue, relationValue );
		
		return result;
	}
	
	public String toString()
	{
		return _name + " " + _operator + " " + _value;
	}
	
	private static ContextParsingException createException( String msg ){
		return new ContextParsingException("SocialRelationCondition: "+msg);
	}
	
	private static Operator parseOperator( String operator ) throws ContextParsingException{
		if( operator == null )
			throw new ContextParsingException("No operator was found in SocialRelationCondition");
		Operator auxOp;
		if(operator.equals("LesserThan"))
			auxOp = LESS_THAN; //Operator.LESS_THAN;
		else if(operator.equals("LesserEqual"))
			auxOp = LESS_THAN_OR_EQUAL;
		else if(operator.equals("="))
			auxOp = EQUAL;
		else if(operator.equals("GreaterEqual"))
			auxOp = MORE_THAN_OR_EQUAL;
		else if(operator.equals("GreaterThan"))
			auxOp = MORE_THAN;
		else if(operator.equals("!="))
			auxOp = NOT_EQUAL;
		else
			throw new ContextParsingException("Invalid operator '"+operator+"' found in SocialRelationCondition");
		return auxOp;
	}
	
	private interface Operator{
		public abstract boolean process( int val1, int val2 );
	}
	
	private static class LessThan implements Operator{
		public boolean process( int val1, int val2 ){
			return val1 < val2;
		}
		
		public String toString()
		{
			return "<";
		}
	}
	
	private static class LessThanOrEqual implements Operator{
		public boolean process( int val1, int val2 ){
			return val1 <= val2;
		}
		
		public String toString()
		{
			return "<=";
		}
	}
	
	private static class Equal implements Operator{
		public boolean process( int val1, int val2 ){
			return val1 == val2;
		}
		
		public String toString()
		{
			return "=";
		}
	}
	
	private static class MoreThanOrEqual implements Operator{
		public boolean process( int val1, int val2 ){
			return val1 >= val2;
		}
		
		public String toString()
		{
			return ">=";
		}
	}
	
	private static class MoreThan implements Operator{
		public boolean process( int val1, int val2 ){
			return val1 > val2;
		}
		
		public String toString()
		{
			return ">";
		}
	}
	
	private static class NotEqual implements Operator{
		public boolean process( int val1, int val2 ){
			return val1 != val2;
		}
		
		public String toString()
		{
			return "!=";
		}
	}
}
