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

package cmion.addOns.level2;

import cmion.architecture.IArchitecture;
import cmion.level2.RemoteCompetency;

import com.cmlabs.air.JavaAIRPlug;
import com.cmlabs.air.Message;


/** This class implements the base class for remote competencies that connect to their 
 *  remote counterpart via the PsyClone framework (see http://www.mindmakers.org/projects/Psyclone) */
public abstract class RemoteAirCompetency extends RemoteCompetency 
{

	/** the java air plug we use to connect */
	private JavaAIRPlug plug;
	
	/** the name of the host we want to connect to */
	private String hostName;
	
	/** the port we use for connecting */
	private int port;
	
	/** the name we use to identify ourselves in the PsyClone framework */
	private String name;
	
	/** the white board we are connecting to */
	private String whiteBoard;

    /** the types of the messages that we are sending */
	private String [] sendMsgTypes;
    
	/** the types of the messages that we are receiving */
	private String [] rcvMsgTypes;
	
	
	/**
	 * Create a new air competency
	 * @param architecture a reference to the architecture 
	 * @param name the name we use to identify ourselves in the PsyClone framework
	 * @param hostName the name of the host we want to connect to (the machine that runs the psyclone framework)
	 * @param port the port we use for connecting
	 * @param whiteBoard the white board we are connecting to
	 * @param sendMsgTypes the types of the messages that we are sending
	 * @param rcvMsgTypes the types of the messages that we are receiving
	 */
	protected RemoteAirCompetency(IArchitecture architecture, String name, String hostName, int port,
								String whiteBoard, String [] sendMsgTypes, String [] rcvMsgTypes) 
	{
		super(architecture);
		this.name = name;
		this.hostName = hostName;
		this.port = port;
		this.whiteBoard = whiteBoard;
		this.sendMsgTypes = sendMsgTypes;
		this.rcvMsgTypes = rcvMsgTypes;
	}

	/** use this method to send a message to the remotely connected Psyclone framework. Call it typically from 
	 *  the startExecution method or from an event handler
	 *  Note: you should use this if this competency is only registered for one type of send messages, if
	 *  it is registered for more, you should use sendMessage(String message, String msgType) to specify
	 *  the type of the message. If no sendMsgTypes are registered, nothing will be sent*/	
	@Override
	protected void sendMessage(String message) 
	{
		if (sendMsgTypes!=null && sendMsgTypes.length>0)
		{
			Message msg = new Message(name,whiteBoard,sendMsgTypes[0]);
			msg.content = message;
			plug.postMessage(whiteBoard, msg, "");
		}
	}

	/** use this method to send a message of a particular type to the remotely connected Psyclone framework*/
	protected void sendMessage(String message, String msgType) 
	{
		Message msg = new Message(name,whiteBoard,msgType);
		msg.content = message;
		plug.postMessage(whiteBoard, msg, "");
	}
	
	/** this method will be invoked in parallel with processMessage(String message), whenever a 
	 *  new message is received. Use this version if the competency has more than one receive msg
	 *  types and you need to distinguish the type of message. Both methods have to implemented,
	 *  but only one the body of one of them should be programmed out */
	protected abstract void processMessage(String message, String type);
	
	/** initializes the Air Plug */
	@Override
	public void initialize() {
		new ConnectionThread().start();		
	}
	
	/** internal class implementing a thread that connects to the psyclone framework 
	 *  and continuously listens for incoming messages */
	private class ConnectionThread extends Thread
	{
		@Override
		public void run()
		{
			 
			 // keep trying to connect
			 boolean connected = false;
			 while (!connected)
			 {
				// create plug
				plug = new JavaAIRPlug(name, hostName, port);
				// initialize connection
				if (plug.init())
				{
					while (!connected)
					{
						if (plug.openTwoWayConnectionTo(whiteBoard))
							connected = true;				
						else
							try
							{
								Thread.sleep(1000);
							} catch (InterruptedException e) {}						
					}
				}
				else
					try 
					{
						Thread.sleep(1000);
					} catch (InterruptedException e) {}
			 }
			 
			
			 // we can send messages without registering
		     
	    	 // register for receiving messages
		     if (rcvMsgTypes!=null)
		     {
		    	 String xml = "<module name=\"" + name + "\" type=\"external\">";

		    	 for (String rcvMsgType : rcvMsgTypes)
		    		 xml+= "<trigger from=\"" + whiteBoard + "\" type=\"" + rcvMsgType + "\" />";
		     	 xml +=	"</module>";
		     
		    	 if (!plug.sendRegistration(xml))
		    	 {
		    		 // we could not register, initialisation has failed
		    		 System.out.println("Error registering for receiving messages to Psyclone");
		    		 return;
		    	 }
		     }
		     
			// if we reach this point we have established a connection
		    
		    // make the competency available
		    available = true;
		    
		    // start listening
		    Message message;
		    while (true) 
		    {
		       if ( (message = plug.waitForNewMessage(100)) != null)
		       {
		    	   // invoke the standard process message
		    	   processMessage(message.getContent());
		    	   
		    	   // invoke the process message that also submits the type
		    	   processMessage(message.getContent(),message.type);
		    	   
		       }
		    }
		}	
	}

	
	
	
	
	
}
