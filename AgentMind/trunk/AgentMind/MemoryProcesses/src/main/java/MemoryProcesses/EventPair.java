package MemoryProcesses;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.memory.episodicMemory.ActionDetail;

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
