package MemoryProcesses;
/** 
 * CandidateEvent.java - Event structure to hold inference data
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

import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.memory.episodicMemory.ActionDetail;


public class CandidateEvent{

	ActionDetail _actionDetail;
	/*private int _id;
	
	private String _subject;
	private String _action;
	private String _target;
	private Time _time;
	private String _location;
	private BaseEmotion _emotion;*/
	
	private boolean _id;
	private boolean _subject;
	private boolean _action;
	private boolean _target;
	private boolean _location;
	private boolean _time;
	private boolean _emotion;
	
	int _phase;
	ArrayList<String> _extension;
	float _evaluation;
	
	public CandidateEvent(ActionDetail actionDetail)
	{
		/*super(actionDetail.getID(), actionDetail.getSubject(), actionDetail.getAction(), 
				actionDetail.getTarget(), actionDetail.getParameters(), actionDetail.getEvaluation(), 
				actionDetail.getTime(), actionDetail.getLocation(), actionDetail.getEmotion());*/
		
		this._actionDetail = actionDetail;
		this._id = false;
		this._subject = false;
		this._action = false;
		this._target = false;
		this._location = false;
		this._time = false;
		this._emotion = false;
		
		/*this._id = actionDetail.getID();
		this._subject = actionDetail.getSubject();
		this._action = actionDetail.getAction();
		this._target = actionDetail.getTarget();
		this._time = actionDetail.getTime();
		this._location = actionDetail.getLocation();
		this._emotion = actionDetail.getEmotion();*/
		
		this._phase = 0;
		this._extension = new ArrayList<String>();
		this._evaluation = 1;
		
	}
	
	public ActionDetail getActionDetail()
	{
		return this._actionDetail;
	}
	
	public boolean getID()
	{
		return this._id;
	}
	
	public boolean getSubject()
	{
		return this._subject;
	}
	
	public boolean getAction()
	{
		return this._action;
	}
	
	public boolean getTarget()
	{
		return this._target;
	}
	
	public boolean getLocation()
	{
		return this._location;
	}
	
	public boolean getEmotion()
	{
		return this._emotion;
	}
	
	public boolean getTime()
	{
		return this._time;
	}
	
	/*public String getSubject()
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
	
	public String getLocation()
	{
		return this._location;
	}
	
	public BaseEmotion getEmotion()
	{
		return this._emotion;
	}
	
	public int getID()
	{
		return this._id;
	}
	
	public Time getTime()
	{
		return this._time;
	}*/
	
	public int getPhase()
	{
		return this._phase;
	}
	
	public ArrayList getExtension()
	{
		return this._extension;
	}
	
	public float getEvaluation()
	{
		return this._evaluation;
	}
		
	/*public double getEval()
	{
		return this._evalVal;
	}*/
	
	 /*public Hashtable getActionList(){
		 
	    return this._answerList;
	 }*/
	  
	public void setActionDetail(ActionDetail actionDetail)
	{
		this._actionDetail = actionDetail;
	}
	
	public void setSubject(boolean subject)
	{
		this._subject = subject;
	}
	
	public void setAction(boolean action)
	{
		this._action = action;
	}
	
	public void setTarget(boolean target)
	{
		this._target = target;
	}
	
	public void setLocation(boolean location)
	{
		this._location = location;
	}
	
	public void setEmotion(boolean emotion)
	{
		this._emotion = emotion;
	}
	
	public void setID(boolean id)
	{
		this._id = id;
	}
	
	public void setTime(boolean time)
	{
		this._time = time;
	}
	
	/*public void setSubject(String subject)
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
	
	public void setLocation(String location)
	{
		this._location = location;
	}
	
	public void setEmotion(BaseEmotion emotion)
	{
		this._emotion = emotion;
	}
	
	public void getID(int id)
	{
		this._id = id;
	}
	
	public void getTime(Time time)
	{
		this._time = time;
	}*/
	
	public void increasePhase()
	{
		this._phase++;
	}
	
	public void setExtension(String extension)
	{
		this._extension.add(extension);
	}
	
	public void updateEvaluation(float evalFactor)
	{
		this._evaluation += this._evaluation * evalFactor;
	}
	
	/*public void setEvalVal(double evalVal)
	{
		this._evalVal = evalVal;
	}*/
	
    /*private void setAnswerList(String action){
    	if (this._answerList == null || !this._answerList.containsKey(action))
    		this._answerList.put(action, new Integer(1));
    	else
    	{
    		Integer val = (Integer) _answerList.get(action);
    		this._answerList.put(action, ++val);
    	}
    }*/
	
}
