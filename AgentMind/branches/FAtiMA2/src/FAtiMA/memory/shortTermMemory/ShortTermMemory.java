/** 
 * ShortTermMemory.java - A memory structure that stores goal relevant information as 
 * well as recent entries of processing
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
 * Created: 10/03/09 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 10/03/2009 - File created
 * Meiyii Lim: 12/03/2009 - Re-structure the AM. Events are now first stored in the STM before
 *  					  	being transferred to the AM. The STM contains a pre-specified number of
 *  					  	records
 * **/

package FAtiMA.memory.shortTermMemory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.deliberativeLayer.goals.Goal;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.memory.ActionDetail;
import FAtiMA.memory.Memory;
import FAtiMA.memory.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.wellFormedNames.Name;


/**
 * A memory structure that stores goal relevant information as 
 * well as recent entries of processing
 * 
 * @author Meiyii Lim
 */

public class ShortTermMemory implements Serializable {

	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
		
	
		
	private STMemoryRecord _records;
	private ArrayList _newRecords;
	private boolean _newData;	
	private String _previousLocation;
	
	public ShortTermMemory()
	{
		this._records = new STMemoryRecord();
		this._newRecords = new ArrayList();
		this._newData = false;
		this._previousLocation = "";
	}
	
	public void StoreAction(Memory m, Event e, String location)
	{		
		
		// 31/03/2009 - Create a new episode if the location changes to allow goals reset
		// If this if block is commented, goals decay over time and reset automatically
		if (!location.equals(_previousLocation))
		{
			m.getAM().NewEpisode(location);
			_records.ResetEventID();
		}

		synchronized (this) {
			if(this._records.GetCount() >= STMemoryRecord.MAXRECORDS)
			{
				ActionDetail detail = _records.GetOldestRecord();
				
				if((detail.getAction().equals("activate") || 
						detail.getAction().equals("succeed") ||
						detail.getAction().equals("fail")) ||
						((!detail.getAction().equals("activate") && 
						!detail.getAction().equals("succeed") &&
						!detail.getAction().equals("fail")) &&
						(detail.getEmotion().GetType()) != EmotionType.NEUTRAL))
				{
					m.getAM().StoreAction(detail);					
				}
				_records.DeleteOldestRecord();
			}
			_records.AddActionDetail(m, e, location);
			_newRecords.add(_records.GetNewestRecord());
			_previousLocation = location;
			
			this._newData = true;
		}
	}
	
	public void AssociateEmotionToAction(Memory m, ActiveEmotion em, Event cause)
	{
		Name locationKey = Name.ParseName(Constants.SELF + "(location)");	
		
		String newLocation = (String) m.AskProperty(locationKey);
		
		if(this._records.GetCount() > 0)
		{
			synchronized (this)
			{
				_records.AssociateEmotionToDetail(m, em,cause,newLocation);
			}
		}
	}
	
	/**
	 * This methods verifies if any new data was added to the AutobiographicalMemory since
	 * the last time this method was called. 
	 * @return
	 */
	public boolean HasNewData()
	{
		boolean aux = this._newData;
		this._newData = false;
		return aux;
	}
	
	public Object GetSyncRoot()
	{
		return this;
	}
	
	public STMemoryRecord GetAllRecords()
	{
		return this._records;
	}
	
	public ArrayList GetNewRecords()
	{
		return this._newRecords;
	}
	
	public void ClearNewRecords() {
		this._newRecords.clear();
	}
	
	public float AssessGoalFamiliarity(Goal g)
	{
		float similarEvents = 0;

		synchronized(this)
		{
			if(this._records.GetCount() > 0)
			{
				similarEvents = _records.AssessGoalFamiliarity(g);
			}
		}
		return similarEvents;
	}
	
	public int CountEvent(ArrayList searchKeys)
	{
		int count = 0;	
		ListIterator li;
		
		synchronized(this)
		{
			if(this._records.GetCount() > 0)
			{
				count = _records.CountEvent(searchKeys);
			}			
			return count;
		}
	}	
	
	public ArrayList SearchForRecentEvents(ArrayList searchKeys)
	{		
		synchronized (this) {
			if(this._records.GetCount() > 0)
			{
				return _records.GetDetailsByKeys(searchKeys);
			}
			return new ArrayList();
		}
	}
	
	public boolean ContainsRecentEvent(ArrayList searchKeys)
	{		
		synchronized (this) {
			if(this._records.GetCount() > 0)
			{				
				return _records.VerifiesKeys(searchKeys);
			}
			return false;
		}
	}
	
	public String toXML()
	{
		String am  = "<ShortTermMemory>";
		am += this._records.toXML();
		am += "</ShortTermMemory>";
		return am; 
	}
}
