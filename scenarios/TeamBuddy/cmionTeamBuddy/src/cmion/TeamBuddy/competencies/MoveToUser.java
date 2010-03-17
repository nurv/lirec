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
		while(true)
		{
			Integer FaceDetected = (Integer) architecture.getBlackBoard().getRTPropertyValue("faceDetected");
			Integer FaceProxemics = (Integer) architecture.getBlackBoard().getRTPropertyValue("faceProxemic");
			Integer FacePosition = (Integer) architecture.getBlackBoard().getRTPropertyValue("facePosition");
			//architecture.getBlackBoard().getR
			//value 1 if true or 0 if false
			
			if(FaceDetected!=null && FaceDetected==1)
			{
				
				if(FaceProxemics==0) //face found and far
				{
					architecture.getBlackBoard().setRTProperty("RobotSpeed", 100);
					architecture.getBlackBoard().setRTProperty("RobotRotation", FacePosition);
				}
				else //face found and close, just rotate
				{
					architecture.getBlackBoard().setRTProperty("RobotSpeed", 0);
					architecture.getBlackBoard().setRTProperty("RobotRotation", FacePosition);
					if (Math.abs(FacePosition)<=25)
					{
						architecture.getBlackBoard().setRTProperty("RobotRotation", 0);
						return true;
					}	
				}
				
			}	
			else //no face, don't move
			{
				architecture.getBlackBoard().setRTProperty("RobotSpeed", 0);
				architecture.getBlackBoard().setRTProperty("RobotRotation", 0);
			} 
			
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
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
