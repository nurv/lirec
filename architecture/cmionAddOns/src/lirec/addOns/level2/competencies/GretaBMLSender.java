package lirec.addOns.level2.competencies;

import java.util.HashMap;

import lirec.addOns.level2.RemoteAirCompetency;
import lirec.architecture.IArchitecture;

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

	/** create  a new Greta BML Sender
	 * 
	 * @param architecture a reference to the Lirec architecture
	 * @param hostName the name of the machine that runs the psyclone server, over which Greta components communicate 
	 */
	public GretaBMLSender(IArchitecture architecture, String hostName) 
	{
		super(architecture, "SimpleBMLSender", hostName, 10000, "Greta.Whiteboard", "Greta.Data.BML" , "");
		competencyName = "GretaBMLSender";
		competencyType = "GretaBMLSender";
	}

	/** we have received a message from the Greta Whiteboard, which might either indicate success or failure
	 *  of realizing the sent BML */
	@Override
	protected void processMessage(String message) {
		// TODO Auto-generated method stub
		
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
		this.sendMessage(bml);
		
		// for now return success immediately, later wait for a reply from Greta
		this.returnSuccess();
	}

	
}
