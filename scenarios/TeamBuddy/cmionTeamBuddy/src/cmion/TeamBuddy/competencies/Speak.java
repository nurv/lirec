package cmion.TeamBuddy.competencies;


import java.util.HashMap;
import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;
import cmion.storage.CmionStorageContainer;
import java.util.*;
import java.io.*;


public class Speak extends SamgarCompetency {
	
	public Speak(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "Speak";
		this.competencyType = "Speak";
		// has to be same as in CompetencyLibraryTeamBuddy.xml
		
		}
	
	
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {
		
		//need to implement your own greet message, may also pass it to SAMGAR module for TTS
		System.out.println("Speak Competency says Hello my name is Sarah ");
	
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//return true after greeting message is said
		return true;

	}
	
	
	@Override
	public boolean runsInBackground() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onRead(Bottle bottle_in) {
		// TODO Auto-generated method stub
		
	}


}

