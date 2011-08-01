package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.storage.WorldModel;

public class Navigate extends SamgarCompetency {
	
	private boolean finished;
	int timeoutcounter = 0;

	public Navigate(IArchitecture architecture) {
		super(architecture);
		this.competencyName ="Navigation";
		this.competencyType ="Navigation";
		// has to be same as in CompetencyLibraryTeamBuddy.xml, if this is a SamgarCompetency
	}

	@Override
	public void onRead(Bottle bottleIn) {
		// TODO Auto-generated method stub
		
		if (bottleIn.get(0).isInt())
		{
			int location = bottleIn.get(0).asInt();
			
			finished = true;
			System.out.println("Navigation finished" );
					
			/*
			// set location in WorldModel			
			String AgentName = "Spirit";			
			String[] locationNames = { "HomePosition", "Desk1", "Desk2", "Desk3", "Desk4", "Desk5", "Desk6", "Door", "RechargePosition", "VisitorPosition" };
			WorldModel wm = getArchitecture().getWorldModel();
			if (wm.hasAgent(AgentName)) {
				wm.getAgent(AgentName).requestSetProperty("location", locationNames[location]);
			} else {
				HashMap<String, Object> properties = new HashMap<String, Object>();
				properties.put("location", locationNames[location]);
				wm.requestAddAgent(AgentName, properties);
			}
			*/
		}
		
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
	
		finished = false;
		String goalPos = parameters.get("GoalPos");
		
		int iPos = Integer.parseInt(goalPos);
		System.out.println("goal location " + iPos);

		Bottle b = this.prepareBottle();
		//b.addInt(iPos);
		b.addString(goalPos);
		this.sendBottle();
		b.clear();
		
		// wait until finished or timed out (currently set to 500 sec  = 5000*100 ms sleep)
		while(! finished)
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			timeoutcounter++;
			if (timeoutcounter>5000) return false;
		}
		return true;
	   /*
		while (true)
		{
			if (architecture.getBlackBoard().hasRTProperty("PhoneRing"))
			{
				//get motion values from blackboard and pass it to SAMGAR module
				Integer PhoneRing = (Integer) architecture.getBlackBoard().getRTPropertyValue("PhoneRing");
			
				// obtain a bottle
				if(PhoneRing==1)
				{	
					Bottle b = this.prepareBottle();
					b.addInt(12);
					
					this.sendBottle();
				}
				
			}
		
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}*/
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return false;
	}

}
