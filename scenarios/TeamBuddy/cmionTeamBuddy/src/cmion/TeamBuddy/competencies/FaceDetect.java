package cmion.TeamBuddy.competencies;

import java.util.HashMap;
import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.storage.CmionStorageContainer;

public class FaceDetect extends SamgarCompetency {

	private int average;
	private int frameCounter;
	
	public FaceDetect(IArchitecture architecture) {
		super(architecture);
		// TODO Auto-generated constructor stub
	
		//name and type of the competence
		this.competencyName ="FaceDetect";
		this.competencyType ="FaceDetect";
		
		// has to be same as in CompetencyLibraryTeamBuddy.xml, if this is a SamgarCompetency
	}	

	@Override
	public void onRead(Bottle bottleIn) {
		// TODO Auto-generated method stub
		
		//value 1 if true(face detected) or 0 if false
		Integer FaceDetected = bottleIn.get(0).asInt();
		
		//face is in proxemic range,  1 if true or 0 if false
		Integer FaceProxemic = bottleIn.get(1).asInt();
		
		//face position pixel value of face detected on left or right of image
		Integer FacePosition = bottleIn.get(2).asInt();
		
		//System.out.println("face :"+bottleIn.toString());
		
		
		//create 3 variables on blackboard and write on it
		architecture.getBlackBoard().setRTProperty("faceDetected", FaceDetected);
		architecture.getBlackBoard().setRTProperty("faceProxemic", FaceProxemic);
		architecture.getBlackBoard().setRTProperty("facePosition", FacePosition);
	

		//check if the face is present atleast 30% per 100 frames, 
		//this is to find the face consistently for enough time to update world model
		if (frameCounter == 100)
		{	
			//create an object "User" on the world model
			CmionStorageContainer user = architecture.getWorldModel().getAgent("User");
			
			// if we have found a face update world model (user.isPresent -> True)
			if (average>30)
			{
				if (user!=null) user.requestSetProperty("isPresent", "True");
			}
			else
			{
				if (user!=null) user.requestSetProperty("isPresent", "False");
			}
			average = 0;
			frameCounter = 0;
		}
		frameCounter++;
		average += FaceDetected; 

	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{

		//add an object User on world model only if its not present already
		if (!architecture.getWorldModel().hasAgent("User"))
			architecture.getWorldModel().requestAddAgent("User");
		
		//intialise counter
		frameCounter = 0;
		average = 0;

		while (true)
		{
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean runsInBackground() {
		return true;
	}

}
