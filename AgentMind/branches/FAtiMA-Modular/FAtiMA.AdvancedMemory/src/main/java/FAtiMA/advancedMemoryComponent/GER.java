/** 
 * GER.java - General Event Representation class - holds an abstraction of events in EM.
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


public class GER implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _subject;
	private String _action;
	private String _intention;
	private String _target;
	private String _object;

	private String _time;
	private String _location;
	
	private String _desirability;
	private String _praiseworthiness;
	private int _coverage;
	
	public GER()
	{
		this._subject = "";
		this._action = "";
		this._intention = "";
		this._target = "";
		this._object = "";
		this._desirability = "";
		this._praiseworthiness = "";
		this._location = "";
		this._time = "";
		this._coverage = 0;
	}
	
	public GER(String subject, String action, String intention, String target, String object, String desirability, String praiseworthiness, String location, String time, int coverage)
	{
		this._subject = subject;
		this._action = action;
		this._intention = intention;
		this._target = target;
		this._object = object;
		this._desirability = desirability;
		this._praiseworthiness = praiseworthiness;
		this._location = location;
		this._time = time;
		this._coverage = coverage;
	}
	
	public void setSubject(String subject)
	{
		this._subject = subject;
	}
	
	public void setAction(String action)
	{
		this._action = action;
	}
	
	public void setIntention(String intention)
	{
		this._intention = intention;
	}
	
	public void setTarget(String target)
	{
		this._target = target;
	}
	
	public void setObject(String object)
	{
		this._object = object;
	}
	
	public void setDesirability(String desirability)
	{
		this._desirability = desirability;
	}
	
	public void setPraiseworthiness(String praiseworthiness)
	{
		this._praiseworthiness = praiseworthiness;
	}	
	
	public void setLocation(String location)
	{
		this._location = location;
	}
	
	public void setTime(String time)
	{
		this._time = time;
	}
	
	public void setCoverage(int coverage)
	{
		this._coverage = coverage;
	}
	
	public String getSubject()
	{
		return this._subject;
	}
	
	public String getAction()
	{
		return this._action;
	}
	
	public String getIntention()
	{
		return this._intention;
	}
	
	public String getTarget()
	{
		return this._target;
	}
	
	public String getObject()
	{
		return this._object;
	}
	
	public String getDesirability()
	{
		return this._desirability;
	}
	
	public String getPraiseworthiness()
	{
		return this._praiseworthiness;
	}
	
	public String getLocation()
	{
		return this._location;
	}
	
	public String getTime()
	{
		return this._time;
	}
	
	public int getCoverage()
	{
		return this._coverage;
	}
	
	public String toString()
	{
		String ger = "";
		
		if(this._subject != "")
		{
			ger += "subject " + this._subject + " ";
		}		
		if(this._action != "")
		{
			ger += "action " + this._action + " ";
		}		
		if(this._intention != "")
		{
			ger += "intention " + this._intention + " ";
		}		
		if(this._target != "")
		{
			ger += "target " + this._target + " ";
		}		
		if(this._object != "")
		{
			ger += "object " + this._object + " ";
		}		
		if(this._desirability != "")
		{
			ger += "desirability " + this._desirability + " ";
		}		
		if(this._praiseworthiness != "")
		{
			ger += "praiseworthiness " + this._praiseworthiness + " ";
		}		
		if(this._location != "")
		{
			ger += "location " + this._location + " ";
		}		
		if(this._time != "")
		{
			ger += "time " + this._time + " ";
		}		
		ger += "coverage " + String.valueOf(this._coverage);
		
		return ger;
	}
	
	public String toXML()
	{
		String ger = "<GER>\n";
		
		if(this._subject != "")
		{
			ger += "<subject>" + this._subject + "</subject>\n";
		}		
		if(this._action != "")
		{
			ger += "<action>" + this._action + "</action>\n";
		}		
		if(this._intention != "")
		{
			ger += "<intention>" + this._intention + "</intention>\n";
		}		
		if(this._target != "")
		{
			ger += "<target>" + this._target + "</target>\n";
		}		
		if(this._object != "")
		{
			ger += "<object>" + this._object + "</object>\n";
		}		
		if(this._desirability != "")
		{
			ger += "<desirability>" + this._desirability + "</desirability>\n";
		}		
		if(this._praiseworthiness != "")
		{
			ger += "<praiseworthiness>" + this._praiseworthiness + "</praiseworthiness>\n";
		}		
		if(this._location != "")
		{
			ger += "<location>" + this._location + "</location>\n";
		}		
		if(this._time != "")
		{
			ger += "<time>" + this._time + "</time>\n";
		}		
		ger += "<coverage>" + String.valueOf(this._coverage) + "</coverage>\n";
		
		ger += "</GER>\n";
		return ger;
	}
}
