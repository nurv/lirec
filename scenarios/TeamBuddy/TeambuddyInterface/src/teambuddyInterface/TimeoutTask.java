package teambuddyInterface;

import java.util.HashMap;
import java.util.TimerTask;

import cmion.storage.WorldModel;

public class TimeoutTask extends TimerTask {

	private InterfaceCompetency interfaceCompetency;

	private String wmCurrentPlatform;
	private String wmInterfaceInteraction;

	public TimeoutTask(InterfaceCompetency interfaceCompetency, String wmCurrentPlatform, String wmInterfaceInteraction) {
		this.interfaceCompetency = interfaceCompetency;
		this.wmCurrentPlatform = wmCurrentPlatform;
		this.wmInterfaceInteraction = wmInterfaceInteraction;
	}

	public void run() {
		setWMObjectProperty(wmCurrentPlatform, wmInterfaceInteraction, "False");
		interfaceCompetency.resetAll();
	}

	private void setWMObjectProperty(String objectName, String propertyName, Object propertyValue) {
		if (interfaceCompetency != null) {
			WorldModel wm = interfaceCompetency.getArchitecture().getWorldModel();
			if (wm.hasObject(objectName)) {
				wm.getObject(objectName).requestSetProperty(propertyName, propertyValue);
			} else {
				HashMap<String, Object> properties = new HashMap<String, Object>();
				properties.put(propertyName, propertyValue);
				wm.requestAddObject(objectName, properties);
			}
		}
	}

}
