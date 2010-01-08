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
import java.util.HashMap;

import org.smslib.GatewayException;
import org.smslib.OutboundMessage;
import org.smslib.TimeoutException;
import cmion.architecture.IArchitecture;
import cmion.inTheWild.services.SMSService;
import cmion.level2.Competency;

/** this competency uses the SMS-Service to send text messages through a connected GSM modem */
public class SMSSender extends Competency {

	// variables for GSM modem initialisation
	private String id;
	private String comPort;
	private int baudRate;
	private String manufacturer;
	private String model;
	
	/** create a new SMS Sender, parameters are responsible for GSM modem initialisation
	 * @throws Exception if parameter baudRate can not be converted to a valid integer */	
	public SMSSender(IArchitecture architecture,String id, String comPort, String baudRate, String manufacturer, String model) throws Exception 
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
		
		competencyName = "SMSSender";
		competencyType = "SMSSender";
	}

	/** the competency code will send an sms message via the connected service */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		if (!parameters.containsKey("To")) return false;
		if (!parameters.containsKey("Content")) return false;
		
		OutboundMessage outBoundMessage = new OutboundMessage(
				parameters.get("To"),parameters.get("Content"));
		
		if (SMSService.instance.isReady())
		{
			try {
				return SMSService.instance.getService().sendMessage(outBoundMessage);
			} catch (TimeoutException e) {
				return false;
			} catch (GatewayException e) {
				return false;
			} catch (IOException e) {
				return false;
			} catch (InterruptedException e) {
				return false;
			}			
		} else return false;		
	}

	@Override
	public void initialize() 
	{
		SMSService.instance.initialize(id, comPort, baudRate, manufacturer, model);
		available = true;
	}

	/** does not run in background (is invoked instead) */
	@Override
	public boolean runsInBackground() 
	{
		return false;
	}
	
	
}
