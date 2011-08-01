package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;


public class TestCommSAM  extends SamgarCompetency {
	
	int outcounter = 0;
	boolean gotReply;
	int incounter=0;
	int newval=0;
	boolean firstreading=false;

	public TestCommSAM(IArchitecture architecture) {
		super(architecture);
		this.competencyName ="Test";
		this.competencyType ="Test";
	}

	@Override
	public void onRead(Bottle bottle_in) {
		// TODO Auto-generated method stub
		//System.out.println("got bootle " + bottle_in.get(0).asInt());
		int incount =  bottle_in.get(0).asInt();
		int sentval =  bottle_in.get(1).asInt();
		
		if(firstreading==false)
		{
			newval=incount;
			firstreading=true;
		}
		else
		{

			if(newval!=(incount-1))
				System.out.println("lost one packet  " + incount + " missed " + newval);
			
			newval=incount;
			
		}
		
		/*if(sentval!=outcounter)
			System.out.println("got error, out count =  " + outcounter + " in count " + sentval);
		else
			gotReply = true;*/
		
		incounter++;
		//bottle_in.clear();
		int buffer = Math.abs(incounter - outcounter);
		
		if(buffer > 0)
			System.out.println("difference in buffer " + buffer + " " + (outcounter-sentval) );
			
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
	{
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {}
		
		// wait until finished or timed out (currently set to 500 sec  = 5000*100 ms sleep)
		while(true)
		{
			gotReply = false;
			Bottle b = this.prepareBottle();
			b.addInt(outcounter);
			
			this.sendBottle();
			//b.clear();
			
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {}
			
			//if (!gotReply)
				//System.out.println("no reply for out count "+outcounter);
			outcounter++;
			
		}
		//return true;
	}

}
