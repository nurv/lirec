package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;
import cmion.storage.BlackBoard;

public class BlackBoardRemover extends Competency {

	public BlackBoardRemover(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "BlackBoardRemover";
		this.competencyType = "BlackBoardRemover";
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {
		String subContainerName = parameters.get("SubContainerName");
		String propertyName = parameters.get("PropertyName");
		BlackBoard bb = architecture.getBlackBoard();
		if (bb.hasSubContainer(subContainerName)) {
			bb.getSubContainer(subContainerName).requestRemoveProperty(propertyName);
			return true;
		}
		return false;
	}

	@Override
	public void initialize() {
		this.available = true;
	}

}
