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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.smslib.GatewayException;
import org.smslib.IInboundMessageNotification;
import org.smslib.InboundMessage;
import org.smslib.TimeoutException;
import org.smslib.Message.MessageTypes;

import cmion.architecture.IArchitecture;
import cmion.inTheWild.services.SMSService;
import cmion.level2.Competency;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;


/** this competency uses the SMS-Service to receive text messages through a connected GSM modem,
 * this runs as a background competency.*/
public class SMSReceiver extends Competency implements IInboundMessageNotification {

	// variables for GSM modem initialisation
	private String id;
	private String comPort;
	private int baudRate;
	private String manufacturer;
	private String model;
	
	/** create a new SMS Receiver, parameters are responsible for GSM modem initialisation
	 * @throws Exception if parameter baudRate can not be converted to a valid integer */
	public SMSReceiver(IArchitecture architecture,String id, String comPort, String baudRate, String manufacturer, String model) throws Exception 
	{
		super(architecture);
		this.id = id;
		this.comPort = comPort;
		try
		{
			this.baudRate = Integer.parseInt(baudRate);
		} 
		catch (NumberFormatException e)
		{
			throw new Exception("Error constructing SMS Receiver competency: Parameter baud rate could not be converted to an integer.");
		}
		this.manufacturer = manufacturer;
		this.model = model;
		
		competencyName = "SMSReceiver";
		competencyType = "SMSReceiver";
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{		
		while (true)
		{
			// nothing to do in here but sleeping
			try
			{
				Thread.sleep(5000);
			} 
			catch (InterruptedException e) {}
		}		
	}

	@Override
	public void initialize() 
	{
		// initialize sms service
		SMSService.instance.initialize(id, comPort, baudRate, manufacturer, model);
		
		// register a callback for incoming messages
		boolean callbackSet = false;
		while (!callbackSet)
		{
			if (SMSService.instance.isReady())
			{
				SMSService.instance.getService().setInboundNotification(this);
				callbackSet = true;
			} 
			else try
			{
				// wait a while before trying again
				Thread.sleep(2000);
			} 
			catch (InterruptedException e) {}
		}
	}

	/** SMS lib callback method when an sms is received*/
	@Override
	public void process(String gatewayId, MessageTypes msgType, InboundMessage msg) 
	{
		// raise an event remote action
		ArrayList<String> actionParameters = new ArrayList<String>();
		actionParameters.add(msg.getText());
		MindAction ma = new MindAction(msg.getOriginator(),"sms",actionParameters);
		this.raise(new EventRemoteAction(ma));		
		
		// delete the message from the sim
		try {
			SMSService.instance.getService().deleteMessage(msg);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (GatewayException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
	}

	/** runs in background */
	@Override
	public boolean runsInBackground() 
	{
		return true;
	}

}


