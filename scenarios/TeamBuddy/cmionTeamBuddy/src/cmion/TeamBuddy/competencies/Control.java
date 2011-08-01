package cmion.TeamBuddy.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;

public class Control extends Competency {

	public Control(IArchitecture architecture) {
		super(architecture);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException {
		Double MotionColor = Double.parseDouble(architecture.getBlackBoard().getRTPropertyValue("MotionColor").toString());
		boolean UserFlag = Boolean.parseBoolean(architecture.getBlackBoard().getRTPropertyValue("User3").toString());
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

}
