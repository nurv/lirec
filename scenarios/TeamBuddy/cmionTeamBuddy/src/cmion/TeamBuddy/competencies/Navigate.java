package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;

public class Navigate extends SamgarCompetency {

	public Navigate(IArchitecture architecture) {
		super(architecture);
		this.competencyName ="Navigation";
		this.competencyType ="Navigation";
		// has to be same as in CompetencyLibraryTeamBuddy.xml, if this is a SamgarCompetency
	}

	@Override
	public void onRead(Bottle bottleIn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
		
	    Integer shutflag =0;
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
		}
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return true;
	}

}
