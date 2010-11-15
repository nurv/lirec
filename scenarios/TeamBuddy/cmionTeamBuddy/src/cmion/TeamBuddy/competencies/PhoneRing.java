package cmion.TeamBuddy.competencies;

import java.util.HashMap;
import yarp.Bottle;


import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;


public class PhoneRing extends SamgarCompetency {

	public PhoneRing(IArchitecture architecture) {
		super(architecture);
		this.competencyName ="PhoneRing";
		this.competencyType ="PhoneRing";
		// has to be same as in CompetencyLibraryTeamBuddy.xml, if this is a SamgarCompetency
		
		architecture.getBlackBoard().setRTProperty("PhoneRing", 0);
	}

	@Override
	public void onRead(Bottle bottleIn) {
		//value 1 if true(phone rings) or 0 if no call, 2 if picked up
		
		//Integer Ring1;
		Integer PhoneRing = bottleIn.get(0).asInt();
		Integer Emote = bottleIn.get(1).asInt();
		
		
		
		architecture.getBlackBoard().setRTProperty("PhoneRing", PhoneRing);
		
		//if(PhoneRing==0)
		//	architecture.getBlackBoard().setRTProperty("emotion", "neutral");
		
		if(Emote.equals(0))
		{
			architecture.getBlackBoard().setRTProperty("emotion", "neutral");
			//System.out.println("neutral");
		}
		else if(Emote.equals(1))
		{
			architecture.getBlackBoard().setRTProperty("emotion", "happy");
			//System.out.println("happy");
		}
		else if(Emote.equals(-1))
		{
			architecture.getBlackBoard().setRTProperty("emotion", "sad");
			//System.out.println("sad");
		}
		else
			architecture.getBlackBoard().setRTProperty("emotion", "neural");
		
		//System.out.println("phone, emote :"+PhoneRing.toString() + " " +Emote.toString());
		// sleep
		
		
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
		/*
	while (true)
	{
		if (architecture.getBlackBoard().hasRTProperty("migrationrequest"))
		{
			
			Integer ShutDown = (Integer) architecture.getBlackBoard().getRTPropertyValue("migrationrequest");
			if(ShutDown==1)				
			{
				Bottle b = this.prepareBottle();
				b.addInt(1);
				this.sendBottle();
				System.out.println("shut down");
				return true;
			}
			
				
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}
	}*/
		return false;
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return true;
	}

}
