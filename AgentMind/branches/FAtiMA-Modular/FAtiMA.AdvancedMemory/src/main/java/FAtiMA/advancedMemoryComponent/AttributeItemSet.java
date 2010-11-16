/** 
 * AttributeItemSet.java - Holds a set of AttributeItems stored in a list. 
 * The list will contain an AttributeItem if it is a one-item-set, two AttributeItems
 * if it is a two-item-set and so on up to the maximum number-item-set applied 
 * in the Apriori Algorithm. The coverage is the frequency the item-set occurs in the EM.
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
import java.util.ArrayList;
import java.util.ListIterator;


public class AttributeItemSet implements Comparable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<AttributeItem> _candidateItemSet;
	private int _coverage;
	
	public AttributeItemSet()
	{
		this._candidateItemSet = new ArrayList <AttributeItem>();
		this._coverage = 0;
	}
	
	public void resetCoverage()
	{
		this._coverage = 0;
	}
	
	public void addCandidateItemSet(AttributeItem attrItem)
	{
		this._candidateItemSet.add(attrItem);
	}
	
	public void increaseCoverage()
	{
		this._coverage++;
	}
	
	public ArrayList<AttributeItem> getCandidateItemSet()
	{
		return this._candidateItemSet;
	}
	
	public int getCoverage()
	{
		return this._coverage;
	}
	
	/*
	 * Check if two attributeItemSets have similar attributes for all the items in 
	 * the candidateItemSets
	 */
	public boolean sameAttributes(AttributeItemSet attrItemSet)
	{
		if (this._candidateItemSet != null)
		{
			for (int i = 0; i < this._candidateItemSet.size(); i++)
			{
				if(!this._candidateItemSet.get(i).sameAttribute(attrItemSet._candidateItemSet.get(i)))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	/*
	 * Check if two attributeItemSets have similar attributes for all the items in 
	 * the candidateItemSets except the last
	 */
	public boolean differentInLastAttribute(AttributeItemSet attrItemSet)
	{
		if (this._candidateItemSet != null)
		{
			for (int i = 0; i < this._candidateItemSet.size()-1; i++)
			{
				if(!this._candidateItemSet.get(i).sameAttribute(attrItemSet._candidateItemSet.get(i)))
				{
					return false;
				}
			}
			if(this._candidateItemSet.get(this._candidateItemSet.size()-1).equals(attrItemSet._candidateItemSet.get(attrItemSet._candidateItemSet.size()-1)))
			{
				return false;
			}			
		}
		return true;
	}
	
	
	public boolean equals(Object o)
	{
		AttributeItemSet attrItemSet;
		
		if(!(o instanceof AttributeItemSet))
		{
			return false;
		}
		
		attrItemSet = (AttributeItemSet) o;

		if (this._candidateItemSet != null)
		{
			if(!this._candidateItemSet.equals(attrItemSet._candidateItemSet))
			{
				return false;
			}
		}
		return true;
	}
	
	public String toString()
	{
		String itemSetStr = "";
		for(int i = 0; i < this._candidateItemSet.size(); i++)
		{
			itemSetStr = itemSetStr + this._candidateItemSet.get(i).toString() + " ";
		}
		return itemSetStr + " coverage: " + this._coverage;
	}

	@Override
	public int compareTo(Object o) {

		AttributeItemSet attrItemSet;
		
		if(!(o instanceof AttributeItemSet))
		{
			return 0;
		}
		
		attrItemSet = (AttributeItemSet) o;
		
		if (this != null)
		{
			for (int i = 0; i < this._candidateItemSet.size(); i++)
			{
				if (this._candidateItemSet.get(i).equals(attrItemSet._candidateItemSet.get(i)))
					;
				else
					return this._candidateItemSet.get(i).compareTo(attrItemSet._candidateItemSet.get(i));
			}
		}
		return 0;
	}
	
	public Object clone()
	{
		AttributeItemSet attrItemSet = new AttributeItemSet();
		attrItemSet._candidateItemSet = new ArrayList<AttributeItem> (this._candidateItemSet.size());
		ListIterator<AttributeItem> li = this._candidateItemSet.listIterator();
		
		while(li.hasNext())
		{
			attrItemSet.addCandidateItemSet((AttributeItem) li.next().clone());
		}
		
		attrItemSet._coverage = this._coverage;
		return attrItemSet;
	}
}
