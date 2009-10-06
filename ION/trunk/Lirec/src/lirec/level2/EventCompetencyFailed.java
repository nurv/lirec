package lirec.level2;

import ion.Meta.Event;

import java.util.HashMap;

/** this event signifies that the execution of a competency has failed */
public class EventCompetencyFailed extends Event 
{

	/** a reference to the competency that has failed  */
	private Competency competency;
	
	/** the parameters the competency was running with, when failing */
	private HashMap<String, String> parameters;
	
	
	public EventCompetencyFailed(Competency competency,
			HashMap<String, String> parameters) 
	{
		this.competency = competency;
		this.parameters = parameters;
	}

	/** returns a reference to the competency that has failed */	
	public Competency getCompetency()
	{
		return competency;
	}
	
	/** returns the parameters the competency was running with, when failed */
	public HashMap<String, String> getParameters()
	{
		return parameters;
	}	
	
}
