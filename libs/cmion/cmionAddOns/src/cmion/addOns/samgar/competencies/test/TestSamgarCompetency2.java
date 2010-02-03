package cmion.addOns.samgar.competencies.test;

import java.util.HashMap;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;

public class TestSamgarCompetency2 extends SamgarCompetency {

	public TestSamgarCompetency2(IArchitecture architecture) 
	{
		super(architecture);
		this.competencyName ="TestSamgarCompetency2";
		this.competencyType ="SamgarTester2";
	}

	@Override
	public void onRead(Bottle bottle_in) 
	{
		System.out.println("2 received bottle :"+bottle_in);
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		System.out.println("TestSamgarCompetency2 running");
		// regularly send a bottle with "test cmion" inside
		while (true)
		{
			// obtain a bottle
			Bottle b = this.prepareBottle();
			b.addString("test2");
			b.addString("cmion2");
			this.sendBottle();
			System.out.println("2 sending bottle");
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
