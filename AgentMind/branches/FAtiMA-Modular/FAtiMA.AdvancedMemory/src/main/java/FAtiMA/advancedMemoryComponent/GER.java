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
	private String _target;	

	private String _time;
	//private String _location;
	
	private String _desirability;
	private String _praiseworthiness;
	private int _coverage;
	
	public GER()
	{
		this._subject = "";
		this._action = "";
		this._target = "";
		this._desirability = "";
		this._praiseworthiness = "";
		this._time = "";
		this._coverage = 0;
	}
	
	public GER(String subject, String action, String target, String desirability, String praiseworthiness, String time)
	{
		this._subject = subject;
		this._action = action;
		this._target = target;
		this._desirability = desirability;
		this._praiseworthiness = praiseworthiness;
		this._time = time;
	}
	
	public void setSubject(String subject)
	{
		this._subject = subject;
	}
	
	public void setAction(String action)
	{
		this._action = action;
	}
	
	public void setTarget(String target)
	{
		this._target = target;
	}
	
	public void setDesirability(String desirability)
	{
		this._desirability = desirability;
	}
	
	public void setPraiseworthiness(String praiseworthiness)
	{
		this._praiseworthiness = praiseworthiness;
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
	
	public String getTarget()
	{
		return this._target;
	}
	
	public String getDesirability()
	{
		return this._desirability;
	}
	
	public String getPraiseworthiness()
	{
		return this._praiseworthiness;
	}
	
	public String getTime()
	{
		return this._time;
	}
	
	public int getCoverage()
	{
		return this._coverage;
	}
}
