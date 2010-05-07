package lirec.personalguide.events;

import ion.Meta.Event;

public class EventChangeEmotion extends Event {

	private String emotion;
	
	public EventChangeEmotion(String emotion)
	{
		this.emotion = emotion;
	}
	
	public String getEmotion()
	{
		return emotion;
	}
	
}
