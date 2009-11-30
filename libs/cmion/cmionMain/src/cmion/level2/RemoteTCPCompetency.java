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

package cmion.level2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import cmion.architecture.IArchitecture;


/** A class encapsulating tcp communication with another process on the network. */
public abstract class RemoteTCPCompetency extends RemoteCompetency {

	/** indicates whether this side (the Java ION side) is the server (true) or
	 *  the client (false) in the tcp connection. In other words, do we wait
	 *  here for a client to connect or do we assume the role of a client and connect
	 *  to the server. This distinction is only important in establishing the connection.
	 *  After that the communication is the same, whether we are server or client. */
	private boolean serverMode;	
	
	/** the port on which to establish the tcp connection */
	protected int port;	
		
	/** the address of the server that we want to connect to, i.e. the machine on which the
	 * remote competency is running (not used if serverMode == true) */
	private String serverAddress;
	
	/** the reader used to receive data from the remotely connected competency */
	private BufferedReader reader;

	/** the writer used to send data to the remotely connected competency*/	
	private BufferedWriter writer;
	
	/** create a new remote TCP competency
	 * 
	 * @param architecture a reference to the architecture object
	 * @param port the port on which we want to establish a connection
	 * @param serverMode if true, we act as a server and listen for a remote program to connect,
	 * if false, we assume the role of a client and connect to a server on the network
	 * @param serverAddress if serverMode is false this tells us the server address we have to connect
	 * to, if serverMode is true this parameter is not used (can be null) 
	 */
	protected RemoteTCPCompetency(IArchitecture architecture, int port, boolean serverMode, String serverAddress) {
		super(architecture);
		this.port = port;
		this.serverAddress = serverAddress;
		this.serverMode = serverMode;
	}	
	
	/** initialises the tcp connection */
	@Override
	public final void initialize() 
	{
		new ConnectionThread().start();
	}

	/** use this method to send a message to the remotely connected competency. Call it typically from 
	 *  the startExecution method or from an event handler */
	@Override
	protected final void sendMessage(String message) 
	{
		try
		{
			writer.write(message + "\n");
			writer.flush();
		} 
		catch (IOException e)
		{
			// we have lost our connection, make the competency unavailable and return 
			// failure if it should be currently running
			available = false;
			returnFailure();
		}	
	}
	
	
	/** internal class implementing a thread that connects to a remote server through 
	 *  a tcp connection and continuously listens on that socket */
	private class ConnectionThread extends Thread
	{
		@Override
		public void run()
		{
			Socket s = null;
			
			// keep trying to connect
			while (s == null)
			{
				try 
				{
					if (serverMode)
					{
						s = new ServerSocket(port).accept();
					}
					else
					{
						s = new Socket(serverAddress,port);						
					}
				} 
				catch (Exception e) 
				{
					try { Thread.sleep(200); } 
					catch (InterruptedException e1) {}
				} 
			}
			
			// if we reach this point we have been assigned a socket and established
			// a connection
			try {
				reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
				writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
				available = true;
				// main connection loop
				while (true)
				{
					processMessage(reader.readLine());
				}			
			}
			catch (IOException e)
			{
				// we have lost our connection, make the competency unavailable and return 
				// failure, if it should be currently running
				available = false;
				returnFailure();
			}	
		}	
	}
		
}
