package cmion.TeamBuddy.competencies;

import java.util.ArrayList;
import java.util.HashMap;
import yarp.Bottle;


import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;


public class DoorMotion extends SamgarCompetency{
	
	boolean bVisitorEntry ;

	public DoorMotion(IArchitecture architecture) {
		super(architecture);
		// TODO Auto-generated constructor stub
		this.competencyName ="DoorMotion";
		this.competencyType ="DoorMotion";
		// has to be same as in CompetencyLibraryTeamBuddy.xml, if this is a SamgarCompetency
		
		this.bVisitorEntry = true;
		architecture.getBlackBoard().setRTProperty("DoorMotion", 0);
		architecture.getBlackBoard().setRTProperty("MotionColor", 0);
	}

	@Override
	public void onRead(Bottle bottle_in) {
		// TODO Auto-generated method stub
		//Integer MotionType = bottle_in.get(0).asInt();
		Double MotionColor = bottle_in.get(0).asDouble();
		
		
		//architecture.getBlackBoard().setRTProperty("DoorMotion", MotionType);
		architecture.getBlackBoard().setRTProperty("MotionColor", MotionColor);
		
		
		//System.out.println("Door motion type " + MotionType);
		if(this.bVisitorEntry && MotionColor>=0.0)
		{
			System.out.println("Door motion color " + MotionColor);
			this.bVisitorEntry=false;
			ArrayList<String> actionParameters = new ArrayList<String>();
			// if you have any parameters add them below
			// actionParameters.add();
			MindAction ma = new MindAction("Visitor","enterOffice",actionParameters);
			this.raise(new EventRemoteAction(ma));	
		}
		
		bottle_in.clear();
		
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
	{
		// TODO Auto-generated method stub
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

}

