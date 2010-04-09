package eu.lirec.myfriend.events;

import ion.Meta.Event;

public class MoodChanged extends Event {

	public final int oldMood;
	public final int currentMood;
	
	public MoodChanged(int oldMood, int currentMood){
		this.oldMood = oldMood;
		this.currentMood = currentMood;
	}
}
