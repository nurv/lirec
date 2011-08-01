package cmion.TeamBuddy.competencies;

import java.util.Calendar;
import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;


public class WaitTimeout extends Competency {

	public WaitTimeout(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "WaitTimeout";
		this.competencyType = "WaitTimeout";
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {
		
		// parse timeout time
		// if no request happens within this period, wait will stop
		long waitTimeoutTime = Long.valueOf(parameters.get("WaitTimeoutTime"));

		long differenceTime = 0;
		
		// loop while the last request happened before the timeout
		while(differenceTime < waitTimeoutTime) {

			// check blackboard value only every second 
			// uncomment if thread uses too much CPU resources
			/*
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
			
			// read time of last interface request from blackboard
			Object value = architecture.getBlackBoard().getPropertyValue("INTERFACE_REQUEST_TIME");
			long interfaceRequestTime = 0;
			if(value != null && value instanceof Long) {
				interfaceRequestTime = (Long) value;
			}
			
			// forced exit when interfaceRequestTime is set to 0 
			if(interfaceRequestTime == 0) {
				break;
			}
			
			// calculate difference between now and last interface request			 
			differenceTime = Calendar.getInstance().getTimeInMillis() - interfaceRequestTime; 			
		}
		
		return true;		
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}

}
