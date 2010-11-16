/** 
 * CommonEvents.java - A structure to hold events with the same matching values. 
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



public class CommonEvents {
	private ArrayList<Integer> _ids;
	private Hashtable<String, String> _matchingValues;
	
	public CommonEvents(int id1, int id2, Hashtable<String, String> matchingValues)
	{
		this._ids = new ArrayList<Integer>();
		this._ids.add(id1);
		this._ids.add(id2);
		this._matchingValues = matchingValues;
	}
	
	public void setIDs(ArrayList<Integer> ids)
	{
		for(Integer id : ids)
		{
			if(!this._ids.contains(id))
				this._ids.add(id);
		}
	}
	
	public void setMatchingValues(Hashtable<String, String> matchingValues)
	{
		this._matchingValues.putAll(matchingValues);
	}
	
	public ArrayList<Integer> getIDs()
	{
		return this._ids;
	}
	
	public Hashtable<String, String> getMatchingValues()
	{
		return this._matchingValues;
	}
}
