/*	
    CMION
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  09/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  Renamed to CMION
  ---  
*/

package cmion.TeamBuddy.competencies;

import java.util.ArrayList;
import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.RemoteTCPCompetency;
import cmion.level3.MindAction;
import cmion.level3.RequestNewMindAction;


/** example of the implementation of a TCP remote competency, with comments 
 *  explaining how to define competencies, this is not actually doing anything serious */
public class RobotFaceControl extends RemoteTCPCompetency {

	/** constructor, every competency, should if possible have a constructor 
	 *  that receives the Architecture as an argument */
	public RobotFaceControl(IArchitecture architecture, String ip) 
	{
		// call parent class constructor, always do this first
		// in this case our connection settings are: connecting as a client (hence
		// parameter 3 is false) to a server with address localhost on port 453 
		super(architecture, 498, false, ip);
		
		competencyName = "RobotFaceControl";

		competencyType = "RobotFaceControl";
		
		architecture.getBlackBoard().setRTProperty("emotion", "neutral");
		architecture.getBlackBoard().setRTProperty("migrationrequest", 0);
	}

	/** this competency is invoked directly (does not run in background) */
	@Override
	public boolean runsInBackground() 
	{
		return true;
	}	
	
	/** we must implement this method. It will be invoked whenever the 
	 * competency is started. */
	@Override
	protected void startExecution(HashMap<String, String> parameters) 
	{
		String emotion = "neutral";
		while (true)
		{
			if (architecture.getBlackBoard().hasRTProperty("emotion"))
			{
				String newEmotion = architecture.getBlackBoard().getRTPropertyValue("emotion").toString();
				if (! newEmotion.equals(emotion))
				{
					emotion = newEmotion;
					sendMessage(emotion);		
					System.out.println("emotion :"+emotion);
				}
			}	//else sendMessage("happy");		
			
			// sleep
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}

	/** we must also implement this method, it will be invoked, whenever we receive
	 *  a message from the remote program */
	@Override
	protected void processMessage(String message) 
	{
		// check what message says
		/*
		if (message.equals("OK"))
			returnSuccess();
		else if (message.equals("FAIL"))
			returnFailure();
		*/
		// other types of messages that could be received here, could include for example
		// putting something on the blackboard or worldmodel or raising an EventRemoteAction
		if (message.equals("migrate"))
		{
			ArrayList<String> parameters = new ArrayList<String>();
			parameters.add("Screen");
			MindAction mindAction = new MindAction("SARAH","Migration",parameters);
			architecture.getCompetencyManager().schedule(new RequestNewMindAction(mindAction));
			//architecture.getBlackBoard().requestSetProperty("migrationrequest", "true");
			architecture.getBlackBoard().setRTProperty("migrationrequest", 1);
			System.out.println("migrate recieved ");
		}
	}
}
