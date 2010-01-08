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
package cmion.inTheWild.services;

import java.io.IOException;

import org.smslib.GatewayException;
import org.smslib.SMSLibException;
import org.smslib.Service;
import org.smslib.TimeoutException;
import org.smslib.AGateway.Protocols;
import org.smslib.modem.SerialModemGateway;

/** Singleton class, providing access to the sms service, is used by both the SMS Receiver 
 * and Sender competencies, requires a GSM modem to run */
public class SMSService 
{
    /** singleton access to the SMS service */
    public static final SMSService instance  = new SMSService();

    /** indicates whether the sms service is already initialized */
    private boolean initialized;

    /** indicates whether the sms service is currently initializing */
    private boolean initializing;

    /** the sms lib service object */
    private Service service;
    
    /** create a new SMS service */
    private SMSService()
    {
    	initializing = false;
    	initialized = false;
    }
    
    /** returns a reference to the SMS lib service object that gives access to all sms functionality*/
    public Service getService()
    {
    	return service;
    }

    /** initialize the sms service, if several competencies use this service, only the first call to this method 
     *  will be effective, parameters are the variables to identify the GSM modem */
    public synchronized void initialize(String id, String comPort, int baudRate, String manufacturer, String model)
    {
    	if (!initializing && !initialized)
    	{
    		initializing = true;
    		new InitialisationThread(id,comPort,baudRate,manufacturer,model).start();
    	}
    }
    
    
    /** in this thread the SMS service is initialized */
    private class InitialisationThread extends Thread
    {
    	// variables to identify the GSM modem
    	private String id;
    	private String comPort;
    	private int baudRate;
    	private String manufacturer;
    	private String model;
    	
    	/** creates a new initialisation thread */
    	public InitialisationThread(String id, String comPort, int baudRate, String manufacturer, String model)
    	{
    		this.id = id;
    		this.comPort = comPort;
    		this.baudRate = baudRate;
    		this.manufacturer = manufacturer;
    		this.model = model;
    	}
    	
    	/** run method of the thread */
    	@Override
    	public void run()
    	{
    		try 
    		{
    			service = new Service();
    			SerialModemGateway gateway = new SerialModemGateway(id, comPort, baudRate, manufacturer, model);
 				service.addGateway(gateway);
 				// Do we want the Gateway to be used for Inbound messages?
 				gateway.setInbound(true);
    			// Do we want the Gateway to be used for Outbound messages?
    			gateway.setOutbound(true);
    			// use PDU protocol (recommended for this modem)
    			gateway.setProtocol(Protocols.PDU);
    		
    			service.startService();

    			initialized = true;
    			initializing = false;
			} catch (GatewayException e) 
			{ 
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SMSLibException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

    	}
    }

    /** returns whether the sms service is ready to use*/
	public synchronized boolean isReady() 
	{
		return initialized;
	}
    
    
    
}
