/**
 * PropertyGreaterEqual.java - Class that represents a specific property test, in this case checks
 * if one property is bigger or equal than another value (only works with numeric properties)
 *  
 * Copyright (C) 2007 GAIPS/INESC-ID 
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
 * Created: 12/02/2007 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 12/02/2007 - File created
 */

package FAtiMA.Core.conditions;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * Test that compares if a property is bigger than a given value. Only works with numeric values.
 * 
 * @author João Dias
 */

public class PropertyGreaterEqual extends PropertyCondition {

	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new PropertyTest of Type GreaterEqual
     * 
     * @param name -
     *            the PropertyTest's name
     * @param value -
     *            the PropertyTest's value
     */
	public PropertyGreaterEqual(Name name, Name value, Symbol ToM) {
		super(name, value, ToM);
	}
	
	protected PropertyGreaterEqual(PropertyGreaterEqual pGE)
	{
		super(pGE);
	}

	/**
	 * Clones this PropertyTest, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The PropertyTest's copy.
	 */
	public Object clone()
	{
		return new PropertyGreaterEqual(this);
	}
	
	/**
     * Checks if the Property Condition is verified in the agent's memory (KB + AM)
     * @return true if the condition is verified, false otherwise
     */
	public boolean CheckCondition(AgentModel am) {
		Object propertyValue;
		Object value;
		Float aux;
		Float aux2;

        AgentModel perspective = am.getModelToTest(getToM());

        if (!super.CheckCondition(am))
            return false;
        
        propertyValue = this.getName().evaluate(perspective.getMemory());
        value = this.GetValue().evaluate(perspective.getMemory());

		if (propertyValue == null || value == null)
			return false;
		aux = new Float( propertyValue.toString());
		aux2 = new Float(value.toString());
		return aux.floatValue() >= aux2.floatValue();
	}
	

	

	/**
	 * Prints the PropertyTest to the Standard Output
	 */
	public void Print() {
		super.Print();
		AgentLogger.GetInstance().logAndPrint(" Operator: GreaterEqual");
	}

	/**
     * Converts the PropertyTest to a String
     * @return the Converted String
     */
	public String toString() {
		return getName() + " >= " + GetValue();
	}
}