package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;


public class Motion extends SamgarCompetency{

	public Motion(IArchitecture architecture) {
		super(architecture);
		this.competencyName ="MoveRobot";
		this.competencyType ="Motion";
		// has to be same as in CompetencyLibraryTeamBuddy.xml, if this is a SamgarCompetency
		
		//set blackboard variables for motion
		architecture.getBlackBoard().setRTProperty("RobotSpeed", 0);
		architecture.getBlackBoard().setRTProperty("RobotRotation", 0);
	}

	@Override
	public void onRead(Bottle bottleIn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
		
		while (true)
		{
			//get motion values from blackboard and pass it to SAMGAR module
			Integer RobotSpeed = (Integer) architecture.getBlackBoard().getRTPropertyValue("RobotSpeed");
			Integer RobotRotation = (Integer) architecture.getBlackBoard().getRTPropertyValue("RobotRotation");
			
			// obtain a bottle
			Bottle b = this.prepareBottle();
			b.addInt(RobotSpeed);
			b.addInt(RobotRotation);
			this.sendBottle();
			//System.out.println("face found.." + b.toString());
			//System.out.println("face found..rot" + RobotRotation);
			//b.clear();
	
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {}
		}
		
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return true;
	}

}
