package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;

public class IdleDream extends Competency {

	public IdleDream(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "IdleDream";
		this.competencyType = "IdleDream";
	}

	@Override
	public void initialize() {
		this.available = true;
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException {

		// TODO
		// dreaming process

		return true;
	}

}
