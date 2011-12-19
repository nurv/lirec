package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;
import cmion.storage.WorldModel;

public class UserMonitor extends Competency {

	public UserMonitor(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "UserMonitor";
		this.competencyType = "UserMonitor";
	}

	@Override
	public boolean runsInBackground() {
		return true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {
		while (true) {

			try {
				Thread.sleep(10 * 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}

			String propertyValue = "False";

			WorldModel wm = getArchitecture().getWorldModel();
			for (String agentName : wm.getAgentNames()) {
				if (wm.hasAgent(agentName)) {
					Object presentObject = wm.getAgent(agentName).getPropertyValue("present");
					if (presentObject != null && presentObject instanceof String) {
						String agentPresent = (String) presentObject;
						if (agentPresent.equals("True")) {
							propertyValue = "True";
						}
					}
				}
			}

			String agentName = "Spirit";
			String propertyName = "usersDetected";
			if (wm.hasAgent(agentName)) {
				wm.getAgent(agentName).requestSetProperty(propertyName, propertyValue);
			}

		}
	}

	@Override
	public void initialize() {
		this.available = true;
	}

}
