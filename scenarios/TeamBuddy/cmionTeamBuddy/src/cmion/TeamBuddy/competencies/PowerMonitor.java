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
	private double AvgVoltage;
	private int Cntr;

	public PowerMonitor(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "PowerMonitor";
		this.competencyType = "PowerMonitor";
		Cntr = 0;
		this.voltage = 0;
		this.AvgVoltage = 0;
	}

	@Override
	public void onRead(Bottle bottle_in) {
		if (bottle_in.get(0).isString()) {
			String voltageStr = bottle_in.get(0).asString().toString();
			
			double voltage = Double.valueOf(voltageStr);
			//not the same value
			if(voltage != this.voltage) {
				AvgVoltage += voltage;
				Cntr++;
				if(Cntr==5)
				{
					//take average voltage from 5 readings
					this.voltage = AvgVoltage / 5.0;
					String task = "greet";
					String target = "Michael";
					setWMObjectProperty("CurrentPlatform", "avgPower,"+task+","+target, this.voltage);
					architecture.getWorldModel().getObject("CurrentPlatform").getPropertyValue("avgPower,"+task+","+target);
					setWMObjectProperty("CurrentPlatform", "voltage", this.voltage);
					//System.out.println("voltage " + voltage + " this.voltage " + this.voltage + "avg" + AvgVoltage);
					Cntr=0;
					AvgVoltage=0;
				}
			}
			//System.out.println("voltage " + voltage);
		}

	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {
		String TaskName = parameters.get("TaskName");
		String TaskStatus = parameters.get("TaskStatus");

		int iTaskStatus = Integer.parseInt(TaskStatus);
		System.out.println("Power monitor status " + TaskName + " " + iTaskStatus);
		
		for (int i = 0; i < 3; i++) {
			Bottle b = this.prepareBottle();
			b.addString(TaskName);
			b.addString(TaskStatus);

			this.sendBottle();
			b.clear();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return true;
	}

	private void setWMAgentProperty(String agentName, String propertyName, Object propertyValue) {
		WorldModel wm = this.getArchitecture().getWorldModel();
		if (wm.hasAgent(agentName)) {
			wm.getAgent(agentName).requestSetProperty(propertyName, propertyValue);
		} else {
			HashMap<String, Object> properties = new HashMap<String, Object>();
			properties.put(propertyName, propertyValue);
			wm.requestAddAgent(agentName, properties);
		}
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
