package cmion.personalguide.competencies;

import java.util.HashMap;
import lirec.personalguide.events.EventChangeEmotion;
import cmion.architecture.IArchitecture;
import cmion.level2.Competency;

public class FaceAnimator extends Competency {
	
	public FaceAnimator(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "FaceAnimator";
		this.competencyType = "FaceAnimator";		
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}

	
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		if (parameters.containsKey("emotion"))
		{
			this.raise(new EventChangeEmotion(parameters.get("emotion")));
		}
		return true;
	}

	@Override
	public boolean runsInBackground() 
	{
		return false;
	}

}
