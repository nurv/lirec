/** 
 * AttributeItem.java - Holds an attribute item, i.e. the attribute name and its value 
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
 * Company: HWU
 * Project: LIREC
 * Created: 16/09/10
 * @author: Meiyii Lim
 * Email to: M.Lim@hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 16/09/10 - File created
 * 
 * **/

package FAtiMA.advancedMemoryComponent;

import java.io.Serializable;

public class AttributeItem implements Comparable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String _attrName;
	private String _attrValue;
	
	public AttributeItem()
	{
		this._attrName = "";
		this._attrValue = "";
	}
	
	public AttributeItem(String attrName, String attrValue)
	{
		this._attrName = attrName;
		this._attrValue = attrValue;
	}
	
	public String getAttrName()
	{
		return this._attrName;
	}
	
	public String getAttrValue()
	{
		return this._attrValue;
	}
	
	public boolean sameAttribute(AttributeItem attrItem)
	{		
		if(this._attrName != null)
		{
			if(!this._attrName.equals(attrItem._attrName))
			{
				return false;
			}
		}
		
		return true;
	}	
	
	public boolean equals(Object o)
	{
		AttributeItem attrItem;
		
		if(!(o instanceof AttributeItem))
		{
			return false;
		}
		
		attrItem = (AttributeItem) o;		
	
		if(this._attrName != null)
		{
			if(!this._attrName.equals(attrItem._attrName))
			{
				return false;
			}
		}
		
		if (this._attrValue != null)
		{
			if(!this._attrValue.equals(attrItem._attrValue))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public String toString()
	{
		return this._attrName + " " + this._attrValue;
	}

	@Override
	public int compareTo(Object o) 
	{
		AttributeItem attrItem;
		
		if(!(o instanceof AttributeItem))
		{
			return 0;
		}
		
		attrItem = (AttributeItem) o;
		
		if (this._attrName != null)
		{
			if (this._attrName.equals(attrItem._attrName))
			{
				return this._attrValue.compareTo(attrItem._attrValue);
			}
			else
			{
				return this._attrName.compareTo(attrItem._attrName);
			}				
		}
		return 0;
	}
	
	public Object clone()
	{
		AttributeItem attrItem = new AttributeItem(this._attrName, this._attrValue);
		return attrItem;
	}
}
