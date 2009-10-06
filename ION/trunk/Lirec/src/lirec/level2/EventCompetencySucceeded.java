package lirec.level2;

import ion.Meta.Event;

import java.util.HashMap;

/** this event signifies that the execution of a competency has suceeded */
public class EventCompetencySucceeded extends Event {

	/** a reference to the competency that has succeeded */
	private Competency competency;
	
	/** the parameters the competency was running with, when suceeding */
	private HashMap<String, String> parameters;
	
	
	public EventCompetencySucceeded(Competency competency,
			HashMap<String, String> parameters) 
	{
		this.competency = competency;
		this.parameters = parameters;
	}

	/** returns a reference to the competency that has succeeded */	
	public Competency getCompetency()
	{
		return competency;
	}
	
	/** returns the parameters the competency was running with, when suceeding */
	public HashMap<String, String> getParameters()
	{
		return parameters;
	}

	
}
