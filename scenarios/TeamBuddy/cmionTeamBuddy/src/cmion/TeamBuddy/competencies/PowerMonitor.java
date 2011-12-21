package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.level2.CompetencyCancelledException;
import cmion.storage.WorldModel;

public class PowerMonitor extends SamgarCompetency {

	private double voltageSum;
	private double averageVoltage;
	private int counter;

	public PowerMonitor(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "PowerMonitor";
		this.competencyType = "PowerMonitor";
		voltageSum = 0;
		averageVoltage = 0;
		counter = 0;
	}

	@Override
	public void onRead(Bottle bottle_in) {
		if (bottle_in.get(0).isString()) {
			String voltageStr = bottle_in.get(0).asString().toString();
			double voltage = Double.valueOf(voltageStr);

			voltageSum += voltage;
			counter++;
			if (counter == 5) {
				//take average voltage from 5 readings
				averageVoltage = voltageSum / 5;
				voltageSum = 0;
				counter = 0;
				//String task = "greet";
				//String target = "Michael";
				//setWMObjectProperty("CurrentPlatform", "avgPower,"+task+","+target, this.voltage);
				//architecture.getWorldModel().getObject("CurrentPlatform").getPropertyValue("avgPower,"+task+","+target);

				setWMObjectProperty("CurrentPlatform", "voltage", averageVoltage);

				// write energy to WorldModel
				double minVoltage = 12.0;
				double maxVoltage = 14.0;
				// normalise into range from 0 to 1
				double energy = (averageVoltage - minVoltage) / (maxVoltage - minVoltage);
				// keep value in range
				if (energy < 0)
					energy = 0;
				else if (energy > 1)
					energy = 1;
				// scale to FAtiMA range
				energy *= 10;
				setWMAgentProperty("Spirit", "Energy", String.valueOf(energy));
			}
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

}
