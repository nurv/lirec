package lirec.personalguide.events;

import java.util.ArrayList;

import ion.Meta.Event;

public class EventTalk extends Event 
{
	private String utterance;
	
	private ArrayList<String> userOptions;
	
	public EventTalk(String utterance, ArrayList<String> userOptions)
	{
		this.utterance = utterance;
		this.userOptions = userOptions;
	}
	
	public String getUtterance()
	{
		return utterance;
	}

	public ArrayList<String> getUserOptions()
	{
		return userOptions;
	}

}
