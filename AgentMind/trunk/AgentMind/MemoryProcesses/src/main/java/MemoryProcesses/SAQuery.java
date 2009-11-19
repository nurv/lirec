package MemoryProcesses;
/** 
 * SAQuery.java - Query structure for spreading activation
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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class SAQuery {

	private String _question;
	private String _subject;
	private String _target;
	private String _action;
	private String _location;
	
	private int _numKnownVar;
	private Hashtable<String, Integer> _results;
	
	private final PropertyChangeSupport changes  = new PropertyChangeSupport( this );
	 
	public SAQuery(){
		this._question = "";
		this._subject = "";
		this._target = "";
		this._action = "";
		this._location = "";
		this._numKnownVar = 0;
		
		this._results = new Hashtable<String, Integer>();
	}
	    
	public void setQuery( ArrayList<String> info, String question ) {
		String known;		
		StringTokenizer query;
		String queryType;
		
		for (int i = 0; i < info.size(); i++)
		{
			known = (String) info.get(i);
			query = new StringTokenizer(known," ");
			queryType = query.nextToken();
			if (queryType.equals("subject"))
			{
				while(query.hasMoreTokens())
				{
					this._subject = query.nextToken();
				}				
				this._numKnownVar++;
			}
			if (queryType.equals("target"))
			{
				while(query.hasMoreTokens())
				{
					this._target = query.nextToken();
				}
				this._numKnownVar++;
			}
			if (queryType.equals("action"))
			{
				while(query.hasMoreTokens())
				{
					this._action = query.nextToken();
				}
				this._numKnownVar++;
			}
			if (queryType.equals("location"))
			{
				while(query.hasMoreTokens())
				{
					this._location = query.nextToken();
				}
				this._numKnownVar++;
			}
		}
		System.out.println("subject " + _subject + " target " + _target + " action " + _action + " location " + _location);
		_question = question;		
	}
	 
    public String getSubject(){
    	return this._subject;
    }
    
    public String getTarget(){
    	return this._target;
    }
    
    public String getAction(){
    	return this._action;
    }
    
    public String getLocation(){
    	return this._location;
    }
    
    public int getNumKnownVar(){
    	return this._numKnownVar;
    }
    
    public String getQuestion(){
    	return this._question;
    }
    
    public Hashtable<String, Integer> getResults()
	{
    	return this._results;
	}
    
    public void setSubject(String subject){
    	this._subject = subject;
    }
    
    public void setTarget(String target){
    	this._target = target;
    }
    
    public void setAction(String action){
    	this._action = action;
    }
    
    public void setLocation(String location){
    	this._location = location;
    }
    
    public void setQuestion(String question){
    	this._question = question;
    }
	
    public void setResults(String result)
	{
	  	if (this._results == null || !this._results.containsKey(result))
	  	{
	  		this._results.put(result, new Integer(1));
	  	}
	  	else
	  	{
	  		Integer val = (Integer) _results.get(result);
	  		this._results.put(result, ++val);
	  	}
	}
    
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.changes.addPropertyChangeListener( l );
    }

    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.changes.removePropertyChangeListener( l );
    }
}
