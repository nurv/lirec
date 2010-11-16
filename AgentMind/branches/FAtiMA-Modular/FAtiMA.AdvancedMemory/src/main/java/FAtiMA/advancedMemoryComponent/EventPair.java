/** 
 * EventPair.java - A temporary structure to hold two events for comparison. 
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

public class EventPair{

	private ActionDetail _actionDetail;
	private int _id2;
	private ArrayList<String> _extension;
	private Hashtable<String, String> _matchingValues;
	
	public EventPair(ActionDetail actionDetail, int id2)
	{
		this._actionDetail = actionDetail;
		this._extension = new ArrayList<String>();
		this._matchingValues = new Hashtable<String, String>();
		this._id2 = id2;
	}
	
	public void setID2(int id2)
	{
		this._id2 = id2;
	}
	
	public void setExtension(String extension)
	{
		this._extension.add(extension);
	}
	
	public void setMatchingValues(String extension, String value)
	{
		if (value != null)
		{
			this._matchingValues.put(extension, value);
		}
		else
		{
			this._matchingValues.put(extension, " ");
		}
		
	}
	
	public void setActionDetail(ActionDetail actionDetail)
	{
		this._actionDetail = actionDetail;
	}
	
	public int getID2()
	{
		return this._id2;
	}
	
	public ActionDetail getActionDetail()
	{
		return this._actionDetail;
	}
	
	public ArrayList<String> getExtension()
	{
		return this._extension;
	}
	
	public Hashtable<String, String> getMatchingValues()
	{
		return this._matchingValues;
	}
	
	public int getNumMatch()
	{
		return this._extension.size();
	}
}
