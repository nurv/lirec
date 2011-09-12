package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;
import cmion.storage.WorldModel;

public class WorldModelRemover extends Competency {

	public WorldModelRemover(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "WorldModelRemover";
		this.competencyType = "WorldModelRemover";
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {
		String subContainerName = parameters.get("SubContainerName");
		String propertyName = parameters.get("PropertyName");
		WorldModel wm = architecture.getWorldModel();
		if (wm.hasSubContainer(subContainerName)) {
			wm.getSubContainer(subContainerName).requestRemoveProperty(propertyName);
			return true;
		}
		return false;
	}

	@Override
	public void initialize() {
		this.available = true;
	}

}
