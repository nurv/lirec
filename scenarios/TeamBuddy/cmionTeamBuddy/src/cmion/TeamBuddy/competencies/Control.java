package cmion.TeamBuddy.competencies;
import java.applet.*;

import java.util.ArrayList;
import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level3.MindAction;
import cmion.level3.RequestNewMindAction;

public class Control extends Competency{

	public Control(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "Control";
		this.competencyType = "Control";
		//AudioClip ac = getAudioClip(getCodeBase(), "wavefile.wav");
	}


	

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
		// TODO Auto-generated method stub
		boolean flag = false;
		int robotSpeed;
		int robotRotation;
		int cnt =0;
		while(true)
		{
			flag = false;
			Integer FaceDetected = (Integer) architecture.getBlackBoard().getRTPropertyValue("faceDetected");
			Integer FaceProxemics = (Integer) architecture.getBlackBoard().getRTPropertyValue("faceProxemic");
			Integer FacePosition = (Integer) architecture.getBlackBoard().getRTPropertyValue("facePosition");
			//architecture.getBlackBoard().getR
			//value 1 if true or 0 if false
			
			if(FaceDetected!=null && FaceDetected==1)
			{
				
				if(FaceProxemics==0) //face found and far
				{
					architecture.getBlackBoard().setRTProperty("RobotSpeed", 100);
					architecture.getBlackBoard().setRTProperty("RobotRotation", FacePosition);
					//System.out.println("Control: face found and far");
				}
				else if(FaceProxemics==-1) //face found and very close
				{
					architecture.getBlackBoard().setRTProperty("RobotSpeed", -100);
					architecture.getBlackBoard().setRTProperty("RobotRotation", FacePosition);
				}
				else //face found and close, just rotate
				{
					architecture.getBlackBoard().setRTProperty("RobotSpeed", 0);
					architecture.getBlackBoard().setRTProperty("RobotRotation", FacePosition);
					flag = true;
					cnt++;
					/*
					if (Math.abs(FacePosition)<=25)
					{
						architecture.getBlackBoard().setRTProperty("RobotRotation", 0);
						System.out.println("Control: face found and close, just rotate");
						return true;
					}	*/
				}
				
			}	
			else //no face, don't move
			{
				architecture.getBlackBoard().setRTProperty("RobotSpeed", 0);
				architecture.getBlackBoard().setRTProperty("RobotRotation", 0);
				//System.out.println("Control: no face, don't move");
			} 
			
			
			
			//{
				//ArrayList<String> mindActionParameters = new ArrayList<String>();
				//mindActionParameters.add("neutral");	//"neutral", "tensed", "anger", "disgust", "joy", "distress", "fear", "sadness", "surprise"			
				//MindAction mindAction = new MindAction("Greta", "emotion", mindActionParameters);
				//architecture.getCompetencyManager().schedule(new RequestNewMindAction(mindAction));
			
			//}
			
			
			if (cnt==1)
			{
				flag = false;
				cnt++;
				ArrayList<String> mindActionParameters = new ArrayList<String>();
				mindActionParameters.add("Hello my name is sarah, how are you today");
				mindActionParameters.add("neutral");
				MindAction mindAction = new MindAction("Greta", "talk", mindActionParameters);
				architecture.getCompetencyManager().schedule(new RequestNewMindAction(mindAction));
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void initialize() 
	{
		this.available = true;
	}

}
