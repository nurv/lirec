package MemoryProcesses;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;


import sun.security.x509.Extension;

import FAtiMA.memory.episodicMemory.ActionDetail;

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
		for(Iterator i = ids.iterator(); i.hasNext(); )
		{
			Integer id = (Integer) i.next();
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
