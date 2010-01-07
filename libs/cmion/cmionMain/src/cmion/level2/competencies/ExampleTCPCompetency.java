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

package cmion.level2.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.RemoteTCPCompetency;


/** example of the implementation of a TCP remote competency, with comments 
 *  explaining how to define competencies, this is not actually doing anything serious */
public class ExampleTCPCompetency extends RemoteTCPCompetency {

	/** constructor, every competency, should if possible have a constructor 
	 *  that receives the Architecture as an argument */
	public ExampleTCPCompetency(IArchitecture architecture) 
	{
		// call parent class constructor, always do this first
		// in this case our connection settings are: connecting as a client (hence
		// parameter 3 is false) to a server with address localhost on port 453 
		super(architecture, 453, false, "localhost");
		
		// an alternative to the above would be assuming the role of server,
		// i.e. remote competency connects to us, the following line would do that
		// super(architecture, 453, true, null);
		
		// set the name/identifier for the competency
		competencyName = "ExampleTCPCompetency1";

		// set the type for the competency, for this example lets pretend 
		// our competency is a DetectPerson competency
		competencyType = "DetectPerson";
	}

	/** this competency is invoked directly (does not run in background) */
	@Override
	public boolean runsInBackground() 
	{
		// note: if we return true here, startExecution (below) will be called only once
		// directly at cmion start up and calling returnSuccess should be avoided as this
		// will terminate the competency and background competencies usually don't terminate.

		return false;
	}	
	
	/** we must implement this method. It will be invoked whenever the 
	 * competency is started. */
	@Override
	protected void startExecution(HashMap<String, String> parameters) 
	{
		// assume we have a parameter named "parameter1" that we want to
		// pass on to the remote program
		
		// first check, if we have such a parameter, if not return failure
		if (!parameters.containsKey("parameter1")) returnFailure();
		else
		{
			// send a message over the tcp connection, message protocol must be of course
			// agreed with remote program. The only constraint this implementation poses
			// is that every message ends with a new line "\n". That also means no "\n" 
			// allowed within messages
			sendMessage("please execute now with parameter 1 = " + parameters.get("parameter1"));
		}
	}

	/** we must also implement this method, it will be invoked, whenever we receive
	 *  a message from the remote program */
	@Override
	protected void processMessage(String message) 
	{
		// check what message says
		if (message.equals("I have completed successfully"))
			returnSuccess();
		else if (message.equals("I have completed unsuccessfully"))
			returnFailure();
		
		// other types of messages that could be received here, could includefor example
		// putting something on the blackboard or worldmodel or raising an EventRemoteAction
	}
}
