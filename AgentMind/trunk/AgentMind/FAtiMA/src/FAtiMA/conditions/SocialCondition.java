package FAtiMA.conditions;

import java.util.ArrayList;

import org.xml.sax.Attributes;


import FAtiMA.AgentModel;
import FAtiMA.exceptions.ContextParsingException;
import FAtiMA.memory.semanticMemory.KnowledgeBase;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;

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
public class SocialCondition extends Condition {
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
	
	private Symbol _subject;
	private Operator _operator;
	private Symbol _target;
	private float _value;
	
	/**
	 * Checks if the Predicate is verified in the agent's KnowledgeBase
	 * @return true if the Predicate is verified, false otherwise
	 * @see KnowledgeBase
	 */
	
	private SocialCondition()
	{
		
	}

	protected SocialCondition(Symbol subject, Symbol target, float value, Operator op){
		this._name = subject;
		this._target = target;
		this._value = value;
		this._operator = op;
	}
	
	public Object clone()
	{
		SocialCondition cond = new SocialCondition();
		cond._name = (Symbol) this._name.clone();
		cond._value = this._value;
		cond._target = (Symbol) this._target.clone();
		cond._operator = this._operator;
		return cond;
	}
	
	
	public boolean CheckCondition(AgentModel am) {
		AgentModel modelToTest;
		float existingValue;
		if(!_subject.isGrounded() && !_target.isGrounded()) return false;

		if(_subject.equals(Constants.SELF))
		{
			modelToTest = am;
		}
		else
		{
			modelToTest = am.getToM().get(_subject);
		}
		
		existingValue = LikeRelation.getRelation(_subject.toString(), _target.toString()).getValue(modelToTest.getMemory());
		
		return _operator.process(existingValue, _value);
	}
	
	
	public String toString()
	{
		return _subject + " like" + _operator + " " + _target + " " + _value;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ArrayList<Substitution> GetValueBindings(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object GenerateName(int id) {
		SocialCondition cond = (SocialCondition) this.clone();
		cond.ReplaceUnboundVariables(id);
		return cond;
	}

	@Override
	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		SocialCondition cond = (SocialCondition) this.clone();
		cond.MakeGround(bindingConstraints);
		return cond;
	}

	@Override
	public Object Ground(Substitution subst) {
		SocialCondition cond = (SocialCondition) this.clone();
		cond.MakeGround(subst);
		return cond;
	}

	@Override
	public void MakeGround(ArrayList<Substitution> bindings) {
		this._subject.MakeGround(bindings);
		this._target.MakeGround(bindings);	
	}

	@Override
	public void MakeGround(Substitution subst) {
		this._subject.MakeGround(subst);
		this._target.MakeGround(subst);	
	}

	@Override
	public void ReplaceUnboundVariables(int variableID) {
		this._subject.ReplaceUnboundVariables(variableID);
		this._target.ReplaceUnboundVariables(variableID);
	}

	@Override
	public boolean isGrounded() {
		return this._subject.isGrounded() && this._target.isGrounded();
	}
}
