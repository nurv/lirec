/** 
 * GeneralMemory.java - The agent's general memory, that is memory that stores 
 * abstraction of events / memory schemata of which events can be reconstructed.
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
 * Created: 23/09/10
 * @author: Meiyii Lim
 * Email to: M.Lim@hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 23/09/10 - File created
 * 
 * **/

package FAtiMA.memory.generalMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.memory.episodicMemory.EpisodicMemory;

public class GeneralMemory implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private ArrayList<GER> _gers;
	private Generalisation _generalisation;
	
	public GeneralMemory()
	{
		this._gers = new ArrayList<GER>();
		this._generalisation = new Generalisation();
	}
	
	/*
	 * Performs generalisation and update the GeneralMemory with frequent item sets
	 */
	public void generalise(EpisodicMemory episodicMemory)
	{
		ArrayList<AttributeItemSet> itemSet = this._generalisation.generalise(episodicMemory);
		this.AddGER(itemSet);		
	}
	
	public void AddGER(ArrayList<AttributeItemSet> itemSet)
	{
		for (int i = 0; i < itemSet.size(); i++)
		{
			AttributeItemSet attrItemSet = itemSet.get(i);
			GER ger = new GER();
			
			// set the coverage (frequency of occurrence) of the item set
			ger.setCoverage(attrItemSet.getCoverage());			
			for (int j = 0; j < attrItemSet.getCandidateItemSet().size(); j++)
			{
				AttributeItem attrItem = attrItemSet.getCandidateItemSet().get(j);				
				
				if (attrItem.getAttrName() == "subject")
					ger.setSubject(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "action")
					ger.setAction(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "target")
					ger.setTarget(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "desirability")
					ger.setDesirability(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "praiseworthiness")
					ger.setPraiseworthiness(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "time")
					ger.setTime(attrItem.getAttrValue());
				
			}		
			this._gers.add(ger);
		}
	}
	
	public ArrayList<GER> getAllGERs()
	{
		return this._gers;
	}
	
	/*
	 * TODO
	 */
	public String toXML()
	{
		String gm  = "<GeneralMemory>";
		for(ListIterator<GER> li = this._gers.listIterator();li.hasNext();)
		{
			
		}
		gm += "</GeneralMemory>";
		return gm; 
	}
}
