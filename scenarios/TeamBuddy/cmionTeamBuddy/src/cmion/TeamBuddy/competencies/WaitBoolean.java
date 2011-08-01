package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;


public class WaitBoolean extends Competency {

	public static final String EXTEND_WAIT = "EXTEND_WAIT";

	public WaitBoolean(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "WaitBoolean";
		this.competencyType = "WaitBoolean";
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException {
		
		String WaitTime = parameters.get("WaitTime");
		
		int iWaitTime = Integer.parseInt(WaitTime);
		
		
			try {
				Thread.sleep(iWaitTime);
			} catch (InterruptedException e) {}

		Object extendWait = architecture.getBlackBoard().getPropertyValue(EXTEND_WAIT);	
		if ((extendWait!=null) && (extendWait instanceof Boolean))
		{
			long timeNow = System.currentTimeMillis();
			Boolean extend = (Boolean) extendWait;
			while (extend)
			{
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {}
				extendWait = architecture.getBlackBoard().getPropertyValue(EXTEND_WAIT);	
				extend = (Boolean) extendWait;
				// timeout, dont wait forever
				if ((System.currentTimeMillis()-timeNow) > (60*1000)) return true; 
			}
		}
		
		return true;
		
		
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}

}
