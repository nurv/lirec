package lirec.personalguide.events;

import ion.Meta.Event;

/** This event signifies that the user has selected an option in the user interface*/
public class EventUserSubmit extends Event {
	
	// the option that the user has selected
	private String userOption;
	
	public EventUserSubmit(String userOption)
	{
		this.userOption = userOption;
	}
	
	public String getUserOption()
	{
		return userOption;
	}
	

}
