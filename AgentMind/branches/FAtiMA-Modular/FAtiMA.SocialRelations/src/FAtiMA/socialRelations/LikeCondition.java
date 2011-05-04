package FAtiMA.socialRelations;

import java.util.ArrayList;

import org.xml.sax.Attributes;


import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.exceptions.ContextParsingException;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;

/**
 * Represents a Like Relation that needs to be fullfiled in order to trigger
 * the condition.
 * The xml should be defined as <LikeRelation SUBJECT_STR="subject_name" TARGET_STR="character_name" OPERATOR_STR="operator" VALUE_STR="relation_value"/>,
 * for example, by default: <LikeRelation subject="Luke" target="John" operator=">" value="3"/>.
 * 		A target must be a character;
 * 		The value must be an integer in the range [-10;10]; 
 * 		Operator can be one of the following < <= = >= > !=
 * @author nafonso
 * @see Context
 * @see Ritual
 */
public class LikeCondition extends Condition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Operator LESS_THAN = new LessThan();
	private static Operator LESS_THAN_OR_EQUAL = new LessThanOrEqual();
	private static Operator EQUAL = new Equal();
	private static Operator MORE_THAN_OR_EQUAL = new MoreThanOrEqual();
	private static Operator MORE_THAN = new MoreThan();
	private static Operator NOT_EQUAL = new NotEqual();
	
	private Operator _operator;
	private Symbol _value;
	
	public static LikeCondition ParseSocialCondition(Attributes attributes) throws InvalidEmotionTypeException, ContextParsingException {
		LikeCondition sc;
		Symbol subject;
		Symbol target = null;
		Operator op;
		Symbol value = new Symbol("0");
		String aux;
		
		
		aux = attributes.getValue("subject");
		if(aux == null)
		{
			subject = Constants.UNIVERSAL;
		}
		else
		{
			subject = new Symbol(aux);
		}
		
		
		aux = attributes.getValue("target");
		if(aux != null)
		{
			target = new Symbol(aux);
		}
	
		aux = attributes.getValue("operator");
		op = LikeCondition.parseOperator(aux);

		aux = attributes.getValue("value");
		if(aux != null)
		{
			value = new Symbol(aux);	
		}
		
		sc = new LikeCondition(subject,target,value,op);
			
		return sc;
	}
	
	protected LikeCondition(Symbol subject, Symbol target, Symbol value, Operator op){
		super(target,subject);
		this._value = value;
		this._operator = op;
	}
	
	protected LikeCondition(LikeCondition lC){
		super(lC);
		_value = lC._value;
		_operator = lC._operator;
	}
	
	public Object clone()
	{
		return new LikeCondition(this);
	}
	
	
	public boolean CheckCondition(AgentModel am) {
		float existingValue;
		String targetName;
		
		if(!this.isGrounded()) return false;
		
		AgentModel perspective = am.getModelToTest(getToM());
		
		if(getToM().toString().equals(Constants.SELF))
		{
			targetName = getName().toString();
		}
		else
		{
			if(getName().toString().equals(Constants.SELF))
			{
				targetName = am.getName();
			}
			else
			{
				targetName = getName().toString();
			}
		}
		
		existingValue = LikeRelation.getRelation(Constants.SELF, targetName).getValue(perspective.getMemory());
		
		return _operator.process(existingValue, Float.parseFloat(_value.toString()));
	}
	
	
	public String toString()
	{
		return getToM() + " like" + _operator + " " + getName() + " " + _value;
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
		public abstract boolean process( float val1, float val2 );
	}
	
	private static class LessThan implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 < val2;
		}
		
		public String toString()
		{
			return "<";
		}
	}
	
	private static class LessThanOrEqual implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 <= val2;
		}
		
		public String toString()
		{
			return "<=";
		}
	}
	
	private static class Equal implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 == val2;
		}
		
		public String toString()
		{
			return "=";
		}
	}
	
	private static class MoreThanOrEqual implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 >= val2;
		}
		
		public String toString()
		{
			return ">=";
		}
	}
	
	private static class MoreThan implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 > val2;
		}
		
		public String toString()
		{
			return ">";
		}
	}
	
	private static class NotEqual implements Operator{
		public boolean process( float val1, float val2 ){
			return val1 != val2;
		}
		
		public String toString()
		{
			return "!=";
		}
	}

	@Override
	public Name GetValue() {
		return new Symbol(String.valueOf(this._value));
	}

	@Override
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		String targetName;
		
		if(!this.getToM().isGrounded()) return null;
		
		if(this.getName().isGrounded())
		{
			if(CheckCondition(am))
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		AgentModel perspective = am.getModelToTest(getToM());
		
		if(getToM().toString().equals(Constants.SELF))
		{
			targetName = getName().toString();
		}
		else
		{
			if(getName().toString().equals(Constants.SELF))
			{
				targetName = am.getName();
			}
			else
			{
				targetName = getName().toString();
			}
		}
	
		Name likeProperty = Name.ParseName("Like(" + Constants.SELF + "," + targetName + ")");
		
		bindingSets = perspective.getMemory().getSemanticMemory().GetPossibleBindings(likeProperty);
		
		return bindingSets;
	}

	@Override
	public void MakeGround(ArrayList<Substitution> bindings) {
		super.MakeGround(bindings);
		this._value.MakeGround(bindings);
	}

	@Override
	public void MakeGround(Substitution subst) {
		super.MakeGround(subst);
		this._value.MakeGround(subst);
	}

	@Override
	public void ReplaceUnboundVariables(int variableID) {
		super.ReplaceUnboundVariables(variableID);
		this._value.ReplaceUnboundVariables(variableID);
	}

	@Override
	public boolean isGrounded() {
		return super.isGrounded() && this._value.isGrounded();
	}

	@Override
	protected ArrayList<Substitution> GetValueBindings(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
	}
}
