package lirec.level2;

import ion.Meta.Request;

import java.util.HashMap;

/** this request can be scheduled with competencies to request it to run*/
public class RequestStartCompetency extends Request {

	/** the parameters for running the competence*/
	private HashMap<String, String> parameters;
	
	/** create a new request to start a competency
	 * 
	 * @param parameters the parameters for starting the competency
	 */
	public RequestStartCompetency(HashMap<String, String> parameters) {
		this.parameters = parameters;
	}

	/** returns the parameters */
	public HashMap<String,String> getParameters()
	{
		return parameters;
	}
	
}
