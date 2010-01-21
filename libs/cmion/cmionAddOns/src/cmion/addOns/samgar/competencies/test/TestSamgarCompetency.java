package cmion.addOns.samgar.competencies.test;

import java.util.HashMap;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;

public class TestSamgarCompetency extends SamgarCompetency {

	public TestSamgarCompetency(IArchitecture architecture) 
	{
		super(architecture);
		this.competencyName ="TestSamgarCompetency";
		this.competencyType ="SamgarTester";
	}

	@Override
	public void onRead(Bottle bottle_in) 
	{
		System.out.println("received bottle :"+bottle_in);
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		// regularly send a bottle with "test cmion" inside
		while (true)
		{
			// obtain a bottle
			Bottle b = this.prepareBottle();
			b.addString("test");
			b.addString("cmion");
			this.sendBottle();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
		}
	}

	// this test competency runs in the background
	@Override
	public boolean runsInBackground() {
		return true;
	}

}
