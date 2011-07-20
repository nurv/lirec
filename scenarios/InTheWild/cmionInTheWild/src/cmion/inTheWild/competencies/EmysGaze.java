package cmion.inTheWild.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;


public class EmysGaze extends Competency {

	public static final String GAZE_TARGET = "gazeTarget";

	public EmysGaze(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "EmysGaze";
		this.competencyType = "EmysGaze";
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}	
	
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException {

		// check if all required parameters are there
		if (!parameters.containsKey(GAZE_TARGET)) return false;
		architecture.getBlackBoard().requestSetProperty(GAZE_TARGET, parameters.get(GAZE_TARGET));
		
		return true;
	}


}
