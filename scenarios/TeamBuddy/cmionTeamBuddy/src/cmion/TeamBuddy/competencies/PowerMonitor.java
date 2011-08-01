package cmion.TeamBuddy.competencies;

import java.util.ArrayList;
import java.util.HashMap;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.level2.CompetencyCancelledException;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;
import cmion.storage.WorldModel;


public class PowerMonitor extends SamgarCompetency {

	private double voltage;
	
	public PowerMonitor(IArchitecture architecture) {
		super(architecture);
		this.competencyName ="PowerMonitor";
		this.competencyType ="PowerMonitor";
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onRead(Bottle bottle_in) {
		// TODO Auto-generated method stub
		if (bottle_in.get(0).isDouble())
		{
			double voltage = bottle_in.get(0).asDouble();
			if(voltage != this.voltage) {
				this.voltage = voltage;
				setWMObjectProperty("CurrentPlatform", "voltage", voltage);				
			}
			//System.out.println("voltage " + voltage);
		}
		
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {
		// TODO Auto-generated method stub
		String TaskName = parameters.get("TaskName");
		String TaskStatus = parameters.get("TaskStatus");
		
		int iTaskStatus = Integer.parseInt(TaskStatus);
		System.out.println("Power monitor status " + TaskName + " " +  iTaskStatus);

		Bottle b = this.prepareBottle();
		b.addString(TaskName);
		b.addInt(iTaskStatus);
			
		this.sendBottle();
		b.clear();
		return true;
	}

	private void setWMObjectProperty(String objectName, String propertyName, Object propertyValue) {
		WorldModel wm = this.getArchitecture().getWorldModel();
		if (wm.hasObject(objectName)) {
			wm.getObject(objectName).requestSetProperty(propertyName, propertyValue);
		} else {
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put(propertyName, propertyValue);
			wm.requestAddObject(objectName, properties);
		}
	}
}
