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
  09/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  ---  
*/

package lirec.level2;

import java.util.HashMap;

import lirec.architecture.Architecture;

/** an abstract class describing a competency, whose main execution is performed remotely,
 *  in a different executable (possibly on a different machine) and that we communicate
 *  with over the network. This should not be subclassed directly by a competency implementation,
 *  but by another abstract class that adds the details of a certain network communication 
 *  technology (e.g. TCP). */
public abstract class RemoteCompetency extends Competency {
		
		
	/** during execution, this variable indicates whether the competency has finished executing */
	private boolean ready;

	/** after has ready has been set,  the return value of the competency execution is read from this variable */	
	private boolean competencySuccess;
	
	/** call this constructor from subclasses */
	protected RemoteCompetency(Architecture architecture) 
	{
		super(architecture);
	}	

	/** the code that is executed when the competency is started*/
	@Override
	protected final boolean competencyCode(HashMap<String, String> parameters) {

		// set ready to false
		ready = false;
		
		// start execution
		startExecution(parameters);
		
		// wait here until ready
		while (!ready)
		{
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {}
		}
		
		return competencySuccess;		
	}
	
	/** this method will be called when execution of the competency is invoked,
	 *  in here a command should be sent over the socket, starting the competency
	 *  remotely*/
	protected abstract void startExecution(HashMap<String, String> parameters);
	
	/** this method will be called whenever a message is received from the remote 
	 *  competency, if the message gives an indication about failure or success of
	 *  the remote competency than returnSuccess or returnFailure should be called
	 *  at the end of this method */
	protected abstract void processMessage(String message);	

	/** this method should not be implemented by a competency but by a direct subclass
	 *  e.g. Remote TCPClient competency */
	protected abstract void sendMessage(String message);	
	
	/** this should be called from the processMessage function whenever the competency has
	 *  returned successfully */
	protected final void returnSuccess()
	{
		if (this.running)
		{
			competencySuccess = true;
			ready = true;
		}
	}

	/** this should be called from the processMessage function whenever the competency has
	 *  returned unsuccessfully */
	protected final void returnFailure()
	{
		if (this.running)
		{
			competencySuccess = false;
			ready = true;
		}
	}	
	

}
