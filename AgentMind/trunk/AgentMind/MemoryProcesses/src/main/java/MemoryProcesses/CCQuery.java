package MemoryProcesses;
/** 
 * CCQuery.java - Query structure for compound cue processing
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Hashtable;

import FAtiMA.memory.episodicMemory.ActionDetail;

public class CCQuery {
	
	private Hashtable<Integer, Float> _results;
	private ActionDetail _actionDetail;
	private int _numField;
	
	private final PropertyChangeSupport changes  = new PropertyChangeSupport( this );
	 
	public CCQuery(){
		this._results = new Hashtable<Integer, Float>();
		this._numField = 5;
	}
	
	public ActionDetail getActionDetail(){
    	return this._actionDetail;
    }
    
	public int getNumField(){
		return this._numField;
	}
	
    public Hashtable<Integer, Float> getCCResults()
	{
    	return this._results;
	}
    
	public void setQuery(ActionDetail actionDetail){
		this._results.clear();
		this._actionDetail = actionDetail;
	}
	
    public void setResults(int id, float evaluation){
    	if (this._results == null || !this._results.containsKey(id))
	  	{
	  		this._results.put(id, evaluation);
	  	}
    }
    
	public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.changes.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.changes.removePropertyChangeListener( l );
    }
}
