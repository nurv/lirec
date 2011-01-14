/** 
 * Generalisation.java - Generate an abstraction from all events in the episodic memory
 * using association rules learning - the Apriori algorithm. Users can chose the attributes on which to generalise on. 
 * Currently, a minimum coverage of 3 is used. And since we have not implemented the deletion of events from EM, 
 * every time generalise is called, the new GERs will replace the old one.
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
import java.util.Iterator;
import java.util.Collections;
import java.util.ListIterator;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;

public class Generalisation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<AttributeItemSet> _itemSet;
	private EpisodicMemory _episodicMemory;	
	private static final int MIN_THRESHOLD = 3;
	private int _itemSize;
	
	private ArrayList<String> _gAttributes;
	private ArrayList<GER> _gers;
	
	public Generalisation()
	{	
		this._itemSet = new ArrayList<AttributeItemSet>();
		this._episodicMemory = null;		
		this._gAttributes = new ArrayList<String>();
		this._gers = new ArrayList<GER>();
		this._itemSize = 0;
	}
	
	/*
	 * Applying the Apriori algorithm to events in EM to generate an abstraction
	 * of the agent's experiences
	 */
	public void generalise(ArrayList<String> gAttributes, EpisodicMemory episodicMemory)
	{
		//Setting the attributes to generalise on
		this._gAttributes = gAttributes;
		this._itemSize = gAttributes.size();
		
		this._episodicMemory = episodicMemory;
		this._itemSet.clear();
		
		// Find frequent items - attribute items with more than 3 occurrences
		this.findFrequentItems();
		
		// Generate frequent item sets - sets of attribute items with more than 3 occurrences
		this.combineItemSet();
		
		// Create GERs from the frequent item sets
		this.createGER(this._itemSet);
	}
	
	
	/* 
	 * The first step of the Apriori algorithm - find all the frequent items with 
	 * coverage > MIN_THRESHOLD
	 */
	@SuppressWarnings("unchecked")
	private void findFrequentItems()
	{
		Iterator <MemoryEpisode> it = _episodicMemory.GetAllEpisodes().iterator();
		while (it.hasNext())
		{
			MemoryEpisode event = it.next();
			ArrayList<ActionDetail> details = event.getDetails();
			this.identifyItems(details);						
		}
		
		ArrayList<ActionDetail> records = _episodicMemory.getDetails();
		this.identifyItems(records);
			
		// discard the non-frequent items from the itemSet
		for (int j = 0; j < _itemSet.size(); j++)
		{
			if (_itemSet.get(j).getCoverage() < MIN_THRESHOLD)
			{
				_itemSet.remove(j);
				j--;
			}
		}
		
		Collections.sort(_itemSet);
		
		/*System.out.println("Frequent item sets");
		for (int j = 0; j < _itemSet.size(); j++)
		{
			System.out.println(_itemSet.get(j).toString());
		}	*/	
	}
	
	/*
	 * Obtain all attribute items from the action details list
	 */
	private void identifyItems(ArrayList<ActionDetail> details)
	{
		for (int i = 0; i < details.size(); i++)
		{
			ActionDetail ad = details.get(i);
			
			if (_gAttributes.contains("subject") && (ad.getSubject() != null))
			{	
				// subject field
				AttributeItem subject = new AttributeItem("subject", ad.getSubject());
				this.candidateFrequentItems(subject);
			}
			if (_gAttributes.contains("action") && (ad.getAction() != null))
			{
				// action field
				AttributeItem action = new AttributeItem("action", ad.getAction());
				this.candidateFrequentItems(action);
			}
			if (_gAttributes.contains("intention") && (ad.getIntention() != null))
			{
				// intention field
				AttributeItem intention = new AttributeItem("intention", ad.getAction());
				this.candidateFrequentItems(intention);
			}
			if (_gAttributes.contains("target") && (ad.getTarget() != null))
			{
				// target field
				AttributeItem target = new AttributeItem("target", ad.getTarget());
				this.candidateFrequentItems(target);
			}
			if (_gAttributes.contains("object") && (ad.getObject() != null))
			{
				// object field
				AttributeItem object = new AttributeItem("object", ad.getAction());
				this.candidateFrequentItems(object);
			}
			
			if (_gAttributes.contains("desirability"))
			{
				// desirability				
				AttributeItem desirability = new AttributeItem("desirability", ad.getDesirability()>=0 ? "positive":"negative");
				this.candidateFrequentItems(desirability);
			}
			
			if (_gAttributes.contains("praiseworthiness"))
			{
				// praiseworthiness
				AttributeItem praiseworthiness = new AttributeItem("praiseworthiness", ad.getPraiseworthiness()>=0 ? "positive":"negative");					
				this.candidateFrequentItems(praiseworthiness);	
			}
			
			if (_gAttributes.contains("location") && (ad.getLocation() != null))
			{
				// location field
				AttributeItem location = new AttributeItem("location", ad.getLocation());
				this.candidateFrequentItems(location);
			}
			if (_gAttributes.contains("time") && (ad.getTime() != null))
			{	
				// subject field
				AttributeItem time = new AttributeItem("time", ad.getTime().getStrRealTime());
				this.candidateFrequentItems(time);
			}
		}
	}
	
	/*
	 * Generate the candidate items 
	 */
	private void candidateFrequentItems(AttributeItem attrItem)
	{
		AttributeItemSet attrItemSet = new AttributeItemSet();
		attrItemSet.addCandidateItemSet(attrItem);
		
		if(_itemSet.contains(attrItemSet))
		{
			AttributeItemSet orgItemSet = _itemSet.get(_itemSet.indexOf(attrItemSet));
			orgItemSet.increaseCoverage();
		}
		else
		{
			_itemSet.add(attrItemSet);
		}
	}
	
	/*
	 * Combine two item sets of size k into an item set of size k+1 
	 * The maximum k is set to 5 at the moment
	 */
	private void combineItemSet()
	{
		ArrayList<AttributeItemSet> tempItemSet = new ArrayList<AttributeItemSet>();
		
		for (int k = 1; k < _itemSize ; k++)
		{
			for (int i = 0; i < this._itemSet.size()-1; i++)
			{
				for (int j = i+1; j < this._itemSet.size(); j++)
				{
					if (this._itemSet.get(i).differentInLastAttribute(this._itemSet.get(j)))
					{
						AttributeItemSet tempAttrItemSet1 = (AttributeItemSet) this._itemSet.get(i).clone();
						AttributeItemSet tempAttrItemSet2 = (AttributeItemSet) this._itemSet.get(j).clone();
						tempAttrItemSet1.addCandidateItemSet(tempAttrItemSet2.getCandidateItemSet().get(tempAttrItemSet2.getCandidateItemSet().size()-1));
						tempAttrItemSet1.resetCoverage();
						tempItemSet.add((AttributeItemSet) tempAttrItemSet1);
					}		
					else
					{
						// do nothing when all the attributes are similar or when there 
						// are more than one difference (not only the last) in the two sets 
						;
					}
				}
			}
			
			this.findFrequentItemSets(tempItemSet);
			this._itemSet = (ArrayList<AttributeItemSet>) tempItemSet.clone();
			tempItemSet.clear();
		}
	}
	
	/*
	 * Find frequent item sets
	 */
	private void findFrequentItemSets(ArrayList<AttributeItemSet> tempItemSet)
	{
		ArrayList<AttributeItemSet> frequentItemSet = new ArrayList<AttributeItemSet>();
		
		// count the occurrences in AM
		Iterator <MemoryEpisode> it = _episodicMemory.GetAllEpisodes().iterator();
		while (it.hasNext())
		{
			MemoryEpisode event = it.next();
			ArrayList<ActionDetail> details = event.getDetails();
			this.countOccurences(tempItemSet, details);
		}
		
		// count the occurrences in STEM
		ArrayList<ActionDetail> records = _episodicMemory.getDetails();
		this.countOccurences(tempItemSet, records);
		
		// if coverage is less than the minimum threshold, delete the item set from the tempItemSet list
		for (int i = 0; i < tempItemSet.size(); i++)
		{
			if(tempItemSet.get(i).getCoverage() < Generalisation.MIN_THRESHOLD)
			{
				tempItemSet.remove(i);
				i--;
			}
		}	
		
		/*System.out.println("item sets with frequency > 3");
		for (int j = 0; j < tempItemSet.size(); j++)
		{
			System.out.println(tempItemSet.get(j).toString());
		}*/	
	}
	
	/*
	 * Count the number of occurrences of attributes combination from the item sets in the ActionDetail
	 * list
	 */
	private void countOccurences(ArrayList<AttributeItemSet> tempItemSet, ArrayList<ActionDetail> details)
	{
		for (int i = 0; i < details.size(); i++)
		{
			ActionDetail ad = details.get(i);
			
			for (int j = 0; j < tempItemSet.size(); j++)
			{
				AttributeItemSet tempAttrItemSet = tempItemSet.get(j);
				boolean match = true;
				for (int k = 0; k < tempAttrItemSet.getCandidateItemSet().size() && match == true; k++)
				{
					AttributeItem attrItem = tempAttrItemSet.getCandidateItemSet().get(k);
					if (attrItem.getAttrName() == "subject")
						match = attrItem.getAttrValue().equals(ad.getSubject());
					else if (attrItem.getAttrName() == "action")
						match = attrItem.getAttrValue().equals(ad.getAction());
					else if (attrItem.getAttrName() == "intention")
						match = attrItem.getAttrValue().equals(ad.getIntention());
					else if (attrItem.getAttrName() == "target")
						match = attrItem.getAttrValue().equals(ad.getTarget());
					else if (attrItem.getAttrName() == "object")
						match = attrItem.getAttrValue().equals(ad.getObject());
					else if (attrItem.getAttrName() == "desirability")
						match = attrItem.getAttrValue().equals(ad.getDesirability()>=0 ? "positive":"negative");
					else if (attrItem.getAttrName() == "praiseworthiness")
						match = attrItem.getAttrValue().equals(ad.getPraiseworthiness()>=0 ? "positive":"negative");
					else if (attrItem.getAttrName() == "location")
						match = attrItem.getAttrValue().equals(ad.getLocation());
					else if (attrItem.getAttrName() == "time")
						match = attrItem.getAttrValue().equals(ad.getTime().getStrRealTime());
				}	
				// if all attributes and values in the item set match the values in the action detail, increase 
				// the coverage for the item set
				if (match)
				{
					tempAttrItemSet.increaseCoverage();					
				}
			}
		}	
	}	
	
	private void createGER(ArrayList<AttributeItemSet> itemSet)
	{
		// clearing the list before generalising
		this._gers.clear();
		
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
				else if (attrItem.getAttrName() == "intention")
					ger.setIntention(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "target")
					ger.setTarget(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "object")
					ger.setObject(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "desirability")
					ger.setDesirability(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "praiseworthiness")
					ger.setPraiseworthiness(attrItem.getAttrValue());
				else if (attrItem.getAttrName() == "location")
					ger.setLocation(attrItem.getAttrValue());
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
	
	public void putGER(GER ger)
	{
		this._gers.add(ger);
	}
	
	public Object clone()
	{
		ArrayList<AttributeItemSet> attrItemSetList = new ArrayList<AttributeItemSet>(this._itemSet.size());
		ListIterator<AttributeItemSet> li = this._itemSet.listIterator();
		
		while(li.hasNext())
		{
			attrItemSetList.add((AttributeItemSet) li.next().clone());
		}
		
		return attrItemSetList;
	}
	
	/*
	 * TODO
	 */
	public String toXML()
	{
		String gm  = "<GeneralMemory>\n";
		for(ListIterator<GER> li = this._gers.listIterator();li.hasNext();)
		{
			gm += li.next().toXML();
		}
		gm += "</GeneralMemory>";
		return gm; 
	}
}
