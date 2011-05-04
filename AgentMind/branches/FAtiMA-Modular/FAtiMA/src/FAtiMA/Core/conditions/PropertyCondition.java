/**
 * PropertyCondition.java - Abstract class that Represents a property test, used to represent
 * preconditions that refer to properties, success conditions, action effects, etc.
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 16/01/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 16/01/2004 - File created
 * João Dias: 22/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable 
 * João Dias: 12/07/2006 - Removed the Reference to a KB stored in conditions. It didn't 
 * 						   make much sense and it causes lots of problems with serialization
 * 						   Because of this, there are additional changes in some of the methods
 * 						   that need to receive a reference to the KB
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 * João Dias: 31/08/2006 - Class renamed from Property to PropertyCondition
 * 						 - Important conceptual change: Since we have now two types of memory,
 * 						   the KnowledgeBase (Semantic memory) and Autobiographical memory (episodic memory),
 * 						   and we have RecentEvent and PastEvent conditions that are searched in episodic
 * 						   memory (and the old conditions that are searched in the KB), it does not make 
 * 						   sense anymore to receive a reference to the KB in searching methods 
 * 						   (checkCondition, getValidBindings, etc) for Conditions. Since both the KB 
 * 						   and AutobiographicalMemory are singletons that can be accessed from any part of 
 * 						   the code, these methods do not need to receive any argument. It's up to each type
 * 						   of condition to decide which memory to use when searching for information.
 * João Dias: 14/10/2006 - I was wrongly assuming that the result of evaluating a property would always return
 * 						   a symbol. Altough this was true before, it stopped being when we introduced property 
 * 						   values that start with # and evaluate to the same value (constants). This assumption
 * 						   was causing property conditions with # to fail in certain situations. 
 */

package FAtiMA.Core.conditions;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;


/**
 * Represents a test to a property. Used in preconditions, success conditions, etc..
 * This property test is composed by the property name, and a second name that specifies 
 * a comparison value. A PropertyTest can be one of: PropertyEqual, PropertyNotEqual, 
 * PropertyLesser, PropertyGreater.
 * 
 * @author João Dias
 */

public class PropertyCondition extends Condition {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Name _value;
	
	/**
	 * Creates a new Property
	 * @param name - the property's name
	 * @param value - the property's value
	 */
	public PropertyCondition(Name name, Name value,Symbol ToM) {
		super(name,ToM);
		_value = value;
	}
	
	protected PropertyCondition(PropertyCondition pC){
		super(pC);
		_value = (Name) pC._value.clone();
	}
	
	@Override
	public Object clone(){
		return new PropertyCondition(this);
	}
		/**
	 * Parses a PropertyTest given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the PropertyTest Parsed
	 */
	public static PropertyCondition ParseProperty(Attributes attributes) {
		PropertyCondition cond = null;
		Name name;
		Name value;
		Symbol ToM;
		String aux;
		if( attributes.getValue("name") != null ){ // if we're reading a 'normal' property condition
			String op;
			
			name = Name.ParseName(attributes.getValue("name"));
			op = attributes.getValue("operator");
			value = Name.ParseName(attributes.getValue("value"));
			
			aux = attributes.getValue("ToM");
			if(aux == null)
			{
				ToM = Constants.UNIVERSAL;
			}
			else
			{
				ToM = new Symbol(aux);
			}
			
		
			if (op == null || op.equals("="))
				cond = new PropertyEqual(name, value,ToM);
			else if (op.equals("!="))
				cond = new PropertyNotEqual(name, value,ToM);
			else if (op.equalsIgnoreCase("GreaterThan"))
				cond = new PropertyGreater(name, value,ToM);
			else if (op.equalsIgnoreCase("LesserThan"))
				cond = new PropertyLesser(name, value,ToM);
			else if (op.equalsIgnoreCase("GreaterEqual"))
				cond = new PropertyGreaterEqual(name, value,ToM);
			else if (op.equalsIgnoreCase("LesserEqual"))
				cond = new PropertyLesserEqual(name,value,ToM);
			else
				cond = new PropertyEqual(name, value,ToM);
		}
		else{
			
		}
			
		return cond;
	}
	
	


	/**
	 * Checks if the Property Condition is verified in the agent's memory (KB + AM)
	 * @return true if the condition is verified, false otherwise
	 */
	public boolean CheckCondition(AgentModel am) {
		if (!getName().isGrounded() && !_value.isGrounded())
			return false;
		return true;
	}

	/**
	 * Gets the Property's test value
	 * @return the test value of the property
	 */
	public Name GetValue() {
		return _value;
	}
	
	
	/**
	 * Replaces all unbound variables in the object by applying a numeric 
     * identifier to each one. For example, the variable [x] becomes [x4]
     * if the received ID is 4. 
     * Attention, this method modifies the original object.
     * @param variableID - the identifier to be applied
	 */
    public void ReplaceUnboundVariables(int variableID) 
    {
    	super.ReplaceUnboundVariables(variableID);
    	this._value.ReplaceUnboundVariables(variableID);
    }
    
	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void MakeGround(ArrayList<Substitution> bindings)
    {
    	super.MakeGround(bindings);
    	this._value.MakeGround(bindings);
    }

	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void MakeGround(Substitution subst)
    {
    	super.MakeGround(subst);
    	this._value.MakeGround(subst);
    }

	/**
	 * Indicates if the condition is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the condition is grounded, false otherwise
	 */
	public boolean isGrounded() {
		return (super.isGrounded() && _value.isGrounded());
	}

	/**
	 * Prints the PropertyTest to the Standard Output
	 */
	public void Print() {
		AgentLogger.GetInstance().logAndPrint("Property= " + getName() + " value= " + _value);
	}
	
	protected ArrayList<Substitution> GetBindings(AgentModel am, Name groundValue, Name value) {
		
		Object val;
		ArrayList<Substitution> bindings;
		AgentModel perspective = am.getModelToTest(getToM());
		
		if (!groundValue.isGrounded())
			return null;
		if (!value.isGrounded()) {
			val = groundValue.evaluate(perspective.getMemory());
			if (val != null) {
				bindings = new ArrayList<Substitution>();
				if(Unifier.Unify(value, Name.ParseName((String) val), bindings))
					return bindings;
				else return null;
			}
			else return null;
		}
		else if (this.CheckCondition(am)) {
			return new ArrayList<Substitution>();
		}
		else return null;
	}

	/**
	 * Find a set of Substitutions for the second part of the condition, which will 
	 * make it become true. With this method it is possible to test conditions that
	 * have unbound variables in the second part such as: 
     * "Owner(Ball) = [x]" 
     * this condition will be true if there is anyone in the world that owns a Ball.
     * If John owns the ball, the method returns [x]/John
     * @return returns all set of Substitutions that make the condition valid.
     */
	protected ArrayList<Substitution> GetValueBindings(AgentModel am) {
		return GetBindings(am, getName(), _value);
	}
}