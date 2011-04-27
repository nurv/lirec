/** 
 * Time.java - 
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
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 18/Jul/2006 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 18/Jul/2006 - File created
 * Meiyii Lim: 13/03/2009 - Moved the class from FAtiMA.autobiographicalMemory package
 * Matthias Keysermann: 20/04/2011 - Added get/set methods for _eventCounter
 * Matthias Keysermann: 27/04/2011 - Calendar for _realTime, getRealTime() returns milliseconds
 * **/

package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import FAtiMA.Core.AgentSimulationTime;


public class Time implements Serializable {

	private static final long serialVersionUID = 1L;

	private static long _eventCounter = 1;
	
	private long _narrativeTime;
	private Calendar _realTime;
	private SimpleDateFormat simpleDateFormat;
	private long _eventSequence;
	
	public Time()
	{
        simpleDateFormat = new SimpleDateFormat();		
		this._narrativeTime = AgentSimulationTime.GetInstance().Time();
		this._realTime = new GregorianCalendar();
		this._eventSequence = _eventCounter;
		_eventCounter++;
	}
	
	public Time(Long narrativeTime, long realTime, long eventSequence)
	{
        simpleDateFormat = new SimpleDateFormat();		
		this._narrativeTime = narrativeTime;
		this._realTime = new GregorianCalendar();
		this._realTime.setTimeInMillis(realTime);
		this._eventSequence = eventSequence;
	}
	
	public long getNarrativeTime()
	{
		return this._narrativeTime;
	}
	
	public long getRealTime()
	{
		//return this._realTime.get(Calendar.HOUR_OF_DAY);
		return this._realTime.getTimeInMillis();
	}
	
	public String getRealTimeFormatted() {
		return simpleDateFormat.format(this._realTime.getTime());
	}
	
	public String getStrRealTime()
	{
		String strRealTime = "";
		if(this._realTime.get(Calendar.HOUR_OF_DAY) >= 0 && this._realTime.get(Calendar.HOUR_OF_DAY) < 12)
			strRealTime = "Morning";
		else
			strRealTime = "Afternoon";		
		return strRealTime;
	}
	
	public long getEventSequence()
	{
		return this._eventSequence;
	}
	
	public long getElapsedNarrativeTime()
	{
		long currentTime = AgentSimulationTime.GetInstance().Time();
		return currentTime - this._narrativeTime;
	}
	
	public long getElapsedRealTime()
	{
		long currentTime = System.currentTimeMillis();
		return currentTime - this._realTime.getTimeInMillis();
	}
	
	public long getElapsedEvents()
	{
		return _eventCounter - this._eventSequence - 1;
	}
	
	public static long getEventCounter()
	{
		return _eventCounter;
	}
	
	public static void setEventCounter(long eventCounter)
	{
		_eventCounter = eventCounter;
	}
	
	public String toString()
	{        	
		return "(RT) " + this.getRealTimeFormatted() + "\n(NT) " + _narrativeTime + "\n(ES) " + _eventSequence; 
	}
}
