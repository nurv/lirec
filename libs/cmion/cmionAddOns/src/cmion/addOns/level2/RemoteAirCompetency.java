/*	
        Lirec Architecture
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
  20/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  ---  
*/

package lirec.addOns.level2;

import com.cmlabs.air.JavaAIRPlug;
import com.cmlabs.air.Message;

import lirec.architecture.IArchitecture;
import lirec.level2.RemoteCompetency;

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

    /** the type of the messages that we are sending */
	private String sendMsgType;
    
	/** the type of the messages that we are receiving */
	private String rcvMsgType;
	
	
	/**
	 * Create a new air competency
	 * @param architecture a reference to the Lirec architecture 
	 * @param name the name we use to identify ourselves in the PsyClone framework
	 * @param hostName the name of the host we want to connect to (the machine that runs the psyclone framework)
	 * @param port the port we use for connecting
	 * @param whiteBoard the white board we are connecting to
	 * @param sendMsgType the type of the messages that we are sending
	 * @param rcvMsgType the type of the messages that we are receiving
	 */
	protected RemoteAirCompetency(IArchitecture architecture, String name, String hostName, int port,
								String whiteBoard, String sendMsgType, String rcvMsgType) 
	{
		super(architecture);
		this.name = name;
		this.hostName = hostName;
		this.port = port;
		this.whiteBoard = whiteBoard;
		this.sendMsgType = sendMsgType;
		this.rcvMsgType = rcvMsgType;
	}

	/** use this method to send a message to the remotely connected Psyclone framework. Call it typically from 
	 *  the startExecution method or from an event handler */	
	@Override
	protected void sendMessage(String message) {
		Message msg = new Message(name,whiteBoard,sendMsgType);
		msg.content = message;
		plug.postMessage(whiteBoard, msg, "");
	}

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
			 
			
			 // register for send messages
		     String xml = "<module name=\"" + name + "\" type=\"external\">" + 
		     			  "<post to=\"" + whiteBoard + "\" type=\"" + sendMsgType + "\" /></module>";
		     
		     if (!plug.sendRegistration(xml))
		     {
		    	 // we could not register, initialisation has failed
		    	 System.out.println("Error registering for sending messages to Psyclone");
		    	 return;
		     }
		     
			 // register for receiving messages
		     xml = "<module name=\"" + name + "\" type=\"external\">" + 
		     			  "<trigger from=\"" + whiteBoard + "\" type=\"" + rcvMsgType + "\" /></module>";
		     
		     if (!plug.sendRegistration(xml))
		     {
		    	 // we could not register, initialisation has failed
		    	 System.out.println("Error registering for receiving messages to Psyclone");
		    	 return;
		     }
			 
			// if we reach this point we have established a connection
		    
		    // make the competency available
		    available = true;
		    
		    // start listening
		    Message message;
		    while (true) 
		    {
		       if ( (message = plug.waitForNewMessage(100)) != null) 
		    	   processMessage(message.getContent());
		    }
		}	
	}

	
	
	
	
	
}
