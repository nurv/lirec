/**
 * Symbol.java - Instantiation of a Simple Well Formed Name composed by just one literal 
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
 * Created: 17/01/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt 
 * 
 * History: 
 * João Dias: 17/01/2004 - File created
 * João Dias: 14/03/2006 - Fixed a bug on Ground Method with List
 * João Dias: 17/05/2006 - Added clone method
 * João Dias: 22/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable 
 * João Dias: 12/07/2006 - Added the evaluate method to names
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 */

package FAtiMA.Core.wellFormedNames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.memory.Memory;



/**
 * Well Formed Name with just one literal The alphabet that makes up the symbols
 * expressions consists of: 
 *  • The set of letters, upper and lowercase.
 *  • The set of digits, 0,1,..,9 
 *  • The symbols “_” and “-” 
 * Symbols expressions begin with a letter and are followed by any sequence of 
 * these legal characters. Well formed names are composed by four types 
 * of symbols:
 *  1. The Truth symbols "True" and "False".
 *  2. Constant symbols, which are simple symbol expressions.
 *  3. Variables symbols, which are symbol expressions enclosed in square
 *     parentheses. Ex: [x] represents the variable x. 
 *  4. The Self symbol [SELF], a reserved special variable which
 *     refers to the agent.
 * 
 * @see Name
 * @author João Dias
 */

public class Symbol extends Name implements Serializable
{
	
	private static final long serialVersionUID = 1L;

    protected String _name;

    /**
     * Creates a new Symbol
     * 
     * @param name -
     *            A String that corresponds to a Well Formed Symbol
     */
    public Symbol(String name)
    {
        super();
        _constant = true;
        _grounded = name.charAt(0) != '[';
        _name = name;
    }

    /**
     * Gets the Name's First Symbol or Literal
     * 
     * @return the first Symbol
     */
    public Symbol GetFirstLiteral()
    {
        return this;
    }
    
    /**
     * Generates a list with all symbols contained in the Name
     * 
     * @return the list with the symbols
     */
    public ArrayList<Symbol> GetLiteralList()
    {
        ArrayList<Symbol> literals = new ArrayList<Symbol>(1);
        literals.add(this);
        return literals;
    }

    /**
     * Gets the String that represents the Symbol's name
     * 
     * @return the Symbol's name
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * Evaluates this symbol
     * @param kb - a reference to the KnowledgeBase
     * @return the Symbol's name
     */
    public Object evaluate(Memory m)
    {
        if (!this._grounded) return null;
        return this._name;
    }
    
    /**
	 * Replaces all unbound variables in the object by applying a numeric 
     * identifier to each one. For example, the variable [x] becomes [x4]
     * if the received ID is 4. 
     * Attention, this method modifies the original object.
     * @param variableID - the identifier to be applied
	 */
    public void ReplaceUnboundVariables(int id)
    {
    	if(this._grounded) 
    		return;
    	this._name = this._name.substring(0, this._name.length() - 1) + id + "]";
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
    	 Substitution b;
         ListIterator<Substitution> li;
         
         if(this._grounded) return;
         
         li = bindings.listIterator();
         
         while (li.hasNext())
         {
             b =  li.next();
             if (this._name.equalsIgnoreCase(b.getVariable()._name))
             {
             	this._name = b.getValue()._name;
             	this._grounded = b.getValue()._grounded;
             	this._constant = b.getValue()._constant;
             }
         }
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
    	if (this._grounded) return;
    	
    	if(this._name.equalsIgnoreCase(subst.getVariable()._name))
    	{
    		this._name = subst.getValue()._name;
    		this._grounded = subst.getValue()._grounded;
    		this._constant = subst.getValue()._constant;
    	}
    }

    /**
	 * Clones this Symbol, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The Symbol's copy.
	 */
    public Object clone()
    {
        return new Symbol(this._name);
    }

    /**
	 * Converts the Symbol to a String
	 * @return the converted String
	 */
    public String toString()
    {
        return _name;
    }
}