package FAtiMA.advancedMemoryComponent;
/** 
 * SpreadActivate.java - A class to perform spreading activation in AM forming connections 
 * between different elements of memory. Useful for story generation, finding an answer 
 * to a query (involving some guessing), etc.
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
 * Company: HWU
 * Project: LIREC
 * Created: 18/11/09
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 18/11/09 - File created
 * 
 * **/

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.Core.memory.episodicMemory.*;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;

public class SpreadActivate extends RuleEngine {
	
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String _saRulePath = "SpreadActivate.drl";
	private SAQuery _saQuery;
	
	public SpreadActivate()
	{
		super(_saRulePath);
		_saQuery = new SAQuery();		
	}
	
	/**
	 * Spread activate through the memory
	 */
	//public void Spread(String question, ArrayList<String> knownInfo, ArrayList<MemoryEpisode> episodes, ArrayList<ActionDetail> records)
	public void Spread(String question, ArrayList<String> knownInfo, EpisodicMemory episodicMemory)
	{			
		try {					
			System.out.println("Spreading Activation");
			
			// assert events from memory
			this.AssertData(episodicMemory);		
			
			// set and assert query
			_saQuery.setQuery(knownInfo, question);
			_ksession.insert(_saQuery);
			
			// fire all SA rule
			_ksession.fireAllRules(); 
			
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
	
	/**
	 * Return the result of spreading activation
	 * currently take into consideration the frequency of appearance
	 * @return a list of answer to the query
	 */
	public Hashtable<String, Integer> getSAResults()
	{
		return this._saQuery.getResults();
	}
	
	public String getSABestResult()
	{
		return this._saQuery.getBestResult();
	}
	
	public ArrayList<ActionDetail> getDetails()
	{
		return this._saQuery.getDetails();
	}
}
