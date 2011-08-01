package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;


public class Wait extends Competency {

	public Wait(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "Wait";
		this.competencyType = "Wait";
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {
		
		long waitTime = Long.valueOf(parameters.get("WaitTime"));
		
		try {
			Thread.sleep(waitTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return true;		
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}

}
