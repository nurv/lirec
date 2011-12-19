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
		this.competencyName = "Navigation";
		this.competencyType = "Navigation";
		// has to be same as in CompetencyLibraryTeamBuddy.xml, if this is a SamgarCompetency
	}

	@Override
	public void onRead(Bottle bottleIn) {

		if (bottleIn.get(0).isInt()) {
			int location = bottleIn.get(0).asInt();

			finished = true;
			System.out.println("Navigation finished " + location);

			// set location in WorldModel			
			String objectName = "CurrentPlatform";
			String[] locationNames = { "Home", "Desk1", "Desk2", "Desk3", "Desk4", "Desk5", "Desk6", "Door", "RechargePosition", "VisitorPosition" };
			WorldModel wm = getArchitecture().getWorldModel();
			if (wm.hasObject(objectName)) {
				wm.getObject(objectName).requestSetProperty("location", locationNames[location]);
			} else {
				HashMap<String, Object> properties = new HashMap<String, Object>();
				properties.put("location", locationNames[location]);
				wm.requestAddObject(objectName, properties);
			}
		}

	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {

		finished = false;
		String goalPos = parameters.get("GoalPos");
		//int iPos = Integer.parseInt(goalPos);

		for (int i = 0; i < 3; i++) {
			Bottle b = this.prepareBottle();
			b.addInt(-1);
			b.addString(goalPos);
			this.sendBottle();
			System.out.println("Navigate.java: Sending bottle: " + b.toString());
			b.clear();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		// wait until finished or timed out (currently set to 500 sec = 5000*100 ms sleep)
		while (!finished) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
			timeoutcounter++;
			if (timeoutcounter > 5000)
				return false;
		}
		return true;

		
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

}
