/*	
    CMION classes for "in the wild" scenario
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
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  ---  
*/

package cmion.inTheWild.competencies;

import java.util.HashMap;

import cmion.addOns.level2.RemoteAirCompetency;
import cmion.architecture.IArchitecture;


/** Implementation of a competency that when invoked will send a bml message to the Greta Whiteboard, via
 *  the Psyclone framework. The competency waits to read a message from the Whiteboard that signifies the 
 *  BML has been realized (i.e. the according animation has been played) 
 *  
 *  When requesting execution of this competency, one parameter (BmlInName) has to be specified, which 
 *  contains the name of a variable on the blackboard from which to read the BML.
 *  
 *  */
public class GretaBMLSender extends RemoteAirCompetency 
{
	/** in this boolean we store whether we have sent bml and are currently waiting for a reply*/
	private boolean waitForBMLFeedBack;
	
	/** here we store the time of the greta clock when this bml finishes*/
	private long bmlFinishTime;
	
	/** create  a new Greta BML Sender
	 * 
	 * @param architecture a reference to the Lirec architecture
	 * @param hostName the name of the machine that runs the psyclone server, over which Greta components communicate 
	 */
	public GretaBMLSender(IArchitecture architecture, String hostName) 
	{
		super(architecture, "SimpleBMLSender", hostName, 10000, "Greta.Whiteboard", 
			  new String[] {"Greta.Data.BML"} , 
			  new String[] {"Greta.Data.Clock","Greta.BML.EndsAt","Greta.BML.Error"});
		competencyName = "GretaBMLSender";
		competencyType = "GretaBMLSender";
		waitForBMLFeedBack = false;
	}

	/** we have received a message from the Greta Whiteboard */
	@Override
	protected void processMessage(String message) 
	{	
		// do not process messages here, but rather below where we also can access the message type
	}

	/** we have received a message from the Greta Whiteboard,
	 * this could either be 
	 * a) a message informing us of the current time of the greta clock (msg type Greta.Data.Clock)
	 * b) a message telling us at what time of the greta clock, the bml we have sent will finish (msg type Greta.BML.EndsAt)
	 * c) a message telling us the bml we have sent could not be realized (msg type Greta.BML.Error) (due to some error in Greta,
	 *    or in the BML) */
	@Override
	protected void processMessage(String message, String type) 
	{
		if (type.equals("Greta.Data.Clock"))
		{
			// we have received a clock message, check if we need the clock
			if (waitForBMLFeedBack && bmlFinishTime>-1)
			{
				// read clock time
				long currentTime = Integer.parseInt(message);
				
				// check if currentTime is already later than bmlFinishTime
				if (currentTime >= bmlFinishTime)
				{
					waitForBMLFeedBack = false;
					bmlFinishTime = -1;
					returnSuccess();
				}
			}
		}
		else if (type.equals("Greta.BML.EndsAt"))
		{
			//we get notified of a new ending time for the bml
			bmlFinishTime = Integer.parseInt(message);			
		}
		else if (type.equals("Greta.BML.Error"))
		{
			waitForBMLFeedBack = false;
			bmlFinishTime = -1;
			returnFailure();			
		}
	}
	
	
	/** In this method we read a bml message from the blackboard and post it to the greta whiteboard. */
	@Override
	protected void startExecution(HashMap<String, String> parameters) 
	{
		// check if a parameter was passed that specifies from where on the blackboard to read the BML
		if (!parameters.containsKey("BmlInName")) 
		{
			this.returnFailure();
			return;
		}
			
		// check if BML is posted on the blackboard
		if (! architecture.getBlackBoard().hasProperty(parameters.get("BmlInName")))
		{	
			this.returnFailure();
			return;
		}
		
		// read bml from black board (should be a string, if not toString() will still return something)
		String bml = architecture.getBlackBoard().getPropertyValue(parameters.get("BmlInName")).toString();
		
		// send bml
		waitForBMLFeedBack = true;
		bmlFinishTime = -1;
		this.sendMessage(bml);
	}

	/** does not run in background (is invoked instead) */
	@Override
	public boolean runsInBackground() {
		return false;
	}



	
}
