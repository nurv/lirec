/**
 * PropertyEqual.java - Class that represents a specific property test, in this case checks
 * if two properties are equal
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
 * João Dias: 17/05/2006 - Added clone() method
 * João Dias: 22/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable
 * João Dias: 12/07/2006 - Removed the Reference to a KB stored in conditions. It didn't 
 * 						   make much sense and it causes lots of problems with serialization
 * 						   Because of this, there are additional changes in some of the methods
 * 						   that need to receive a reference to the KB
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 * João Dias: 31/08/2006 - Important conceptual change: Since we have now two types of memory,
 * 						   the KnowledgeBase (Semantic memory) and Autobiographical memory (episodic memory),
 * 						   and we have RecentEvent and PastEvent conditions that are searched in episodic
 * 						   memory (and the old conditions that are searched in the KB), it does not make 
 * 						   sense anymore to receive a reference to the KB in searching methods 
 * 						   (checkCondition, getValidBindings, etc) for Conditions. Since both the KB 
 * 						   and AutobiographicalMemory are singletons that can be accessed from any part of 
 * 						   the code, these methods do not need to receive any argument. It's up to each type
 * 						   of condition to decide which memory to use when searching for information. 
 */

package FAtiMA.Core.conditions;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

/**
 * Test that compares if a property is equal to a given value
 * 
 * @author João Dias
 */

public class PropertyEqual extends PropertyCondition
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new PropertyTest of Type Equal
     * 
     * @param name -
     *            the PropertyTest's name
     * @param value -
     *            the PropertyTest's value
     */
    public PropertyEqual(Name name, Name value, Symbol ToM)
    {
        super(name, value,ToM);
    }
    
    
    protected PropertyEqual(PropertyEqual pE)
    {
        super(pE);
    }
    
    /**
	 * Clones this PropertyTest, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The PropertyTest's copy.
	 */
    public Object clone()
    {
    	return new PropertyEqual(this);
    }


    /**
     * Checks if the Property Condition is verified in the agent's memory (KB + AM)
     * @return true if the condition is verified, false otherwise
     */
    public boolean CheckCondition(AgentModel am)
    {
        Object propertyValue;
        Object value;
        AgentModel perspective = am.getModelToTest(getToM());

        if (!super.CheckCondition(am))
            return false;
        
        propertyValue = this.getName().evaluate(perspective.getMemory());
        value = this.GetValue().evaluate(perspective.getMemory());

        if (propertyValue == null || value == null)
            return false;
        else
            return propertyValue.equals(value);
    }

   
    /**
	 * Prints the PropertyTest to the Standard Output
	 */
    public void Print()
    {
        super.Print();
        AgentLogger.GetInstance().logAndPrint(" Operator: Equal");
    }

    /**
     * Converts the PropertyTest to a String
     * @return the Converted String
     */
    public String toString()
    {
        return getName() + " = " + GetValue();
    }
}