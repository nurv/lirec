package cmion.TeamBuddy.competencies;

import java.util.HashMap;
import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.storage.CmionStorageContainer;

public class DeskMonitor extends SamgarCompetency {


	private String sUserName;
	boolean bUsrStatus; //present 1 or absent 0
	
	public DeskMonitor(IArchitecture architecture, String UserName) {
		super(architecture);
		// TODO Auto-generated constructor stub
	
		//name and type of the competence
		this.sUserName = UserName;
		this.bUsrStatus = false;
		this.competencyName ="LabMonitor";
		this.competencyType ="LabMonitor";
		
		// has to be same as in CompetencyLibraryTeamBuddy.xml, if this is a SamgarCompetency
	}	

	@Override
	public void onRead(Bottle bottleIn) {
		// TODO Auto-generated method stub
		
		Integer UserID = bottleIn.get(0).asInt(); //user id
		Integer face = bottleIn.get(1).asInt(); // face percentage
		Integer kbdmse = bottleIn.get(2).asInt(); // keyborad mouse percentage
		
		
		
		
		if(face>=25 || kbdmse >= 5)
			bUsrStatus = true;
		else
			bUsrStatus = false;
		
		System.out.println(sUserName+" :"+bottleIn.toString() + " user status"  + bUsrStatus);
		
		//create variables on blackboard and write on it
		architecture.getBlackBoard().setRTProperty(sUserName, bUsrStatus);
	

	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{

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

