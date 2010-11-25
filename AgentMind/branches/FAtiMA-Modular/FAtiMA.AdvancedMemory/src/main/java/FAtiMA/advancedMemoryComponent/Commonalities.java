/** 
 * Commonalities.java - Finding the similarities between events in memory that are 
 * returned by SA and group the events with the number of highest matches together
 * according to matching values. 
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
 * Created: 18/03/10
 * @author: Meiyii Lim
 * Email to: M.Lim@hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 18/03/10 - File created
 * 
 * **/

package FAtiMA.advancedMemoryComponent;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;

public class Commonalities extends RuleEngine {
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String _gRulePath = "Commonalities.drl";
	
	private Hashtable<String, Float> _results;
	private CommonQuery _gQuery;
	
	public Commonalities()
	{
		super(_gRulePath);
		_gQuery = new CommonQuery();		
	}
	
	/**
	 * Generalise on a set of events
	 */
	//public void Match(ActionDetail queryEvent, ArrayList<MemoryEpisode> episodes, ArrayList<ActionDetail> records)
	public void eventCommonalities(ArrayList<ActionDetail> actionDetails)
	{			
		ActionDetail actionDetail;
		try {		
			System.out.println("Commonalities");
			
			// assert events from memory
			for (int j = 0; j < actionDetails.size(); j++)
			{
				actionDetail = (ActionDetail) actionDetails.get(j);
				System.out.println("ID" + actionDetail.getID());
				_ksession.insert(actionDetail);
			}				
			
			_ksession.insert(_gQuery);
			// fire all CC rules
			_ksession.fireAllRules();	
			//_ksession.retract(queryHandle);
			
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
	
	public Hashtable<ArrayList<Integer>, Hashtable<String, String>> getMatch()
	{
		return _gQuery.getMatch();
	}
}
