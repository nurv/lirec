package cmion.TeamBuddy.competencies;


import java.util.ArrayList;
import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level3.MindAction;
import cmion.level3.RequestNewMindAction;

public class MoveToUser extends Competency{

	public MoveToUser(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "MoveToUser";
		this.competencyType = "MoveToUser";
		// has to be same as in CompetencyLibraryTeamBuddy.xml
	}


	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
		// TODO Auto-generated method stub
		
		System.out.println("moving to user " );
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return true;
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}

}
