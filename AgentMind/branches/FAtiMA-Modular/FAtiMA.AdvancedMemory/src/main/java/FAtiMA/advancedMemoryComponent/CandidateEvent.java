package FAtiMA.advancedMemoryComponent;
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

import FAtiMA.Core.memory.episodicMemory.ActionDetail;


public class CandidateEvent{
	
	private ActionDetail _actionDetail;
	
	private int _phase;
	private ArrayList<String> _extension;
    private	float _evaluation;
	
	public CandidateEvent(ActionDetail actionDetail, String extension, float evaluation)
	{
		this._actionDetail = actionDetail;		
		this._phase = 1;
		this._extension = new ArrayList<String>();
		this._extension.add(extension);
		this._evaluation = evaluation;
		
	}
	
	public ActionDetail getActionDetail()
	{
		return this._actionDetail;
	}
	
	public int getPhase()
	{
		return this._phase;
	}
	
	public ArrayList<String> getExtension()
	{
		return this._extension;
	}
	
	public float getEvaluation()
	{
		return this._evaluation;
	}
		
	public void setActionDetail(ActionDetail actionDetail)
	{
		this._actionDetail = actionDetail;
	}
	
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
}