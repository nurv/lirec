/** 
 * PropertyPerception.java - Represents a property change event that happened in the virtual world
 *  
 * Copyright (C) 2011 GAIPS/INESC-ID 
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
 * Created: November/2011 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: November/2011 - File created
 * 						   
 */
package FAtiMA.Core.perceptions;

import java.io.Serializable;

import FAtiMA.Core.wellFormedNames.Name;

public class PropertyPerception implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String _ToM;
	private Name _property;
	private String _value;
	
	public PropertyPerception(String ToM, Name property, String value)
	{
		this._ToM = ToM;
		this._property = property;
		this._value = value;
	}
	
	public String getToM()
	{
		return this._ToM;
	}
	
	public Name getProperty()
	{
		return this._property;
	}
	
	public String getValue()
	{
		return this._value;
	}
}
