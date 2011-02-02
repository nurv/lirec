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

package cmion.level3.supersimplemind;

import cmion.architecture.IArchitecture;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

/** The connector to a SuperSimpleMind (example implementation of a simple mind interface) */
public class SuperSimpleMindConnector extends AgentMindConnector {

	
	/** the mind itself */
	private SuperSimpleMind mind;
	
	/** creates a new connector for a SuperSimpleMind */ 
	public SuperSimpleMindConnector(IArchitecture architecture)
	{	
		super(architecture);
		// create the mind, initially sleeping
		mind = new SuperSimpleMind(this);
	}
	
	
	@Override
	public synchronized void awakeMind() {
		mind.sendAwake();
	}

	@Override
	public synchronized boolean isMindSleeping() {
		return mind.isSleeping();
	}

	@Override
	public synchronized void processActionFailure(MindAction a) {
		mind.sendFailure(a);
	}

	@Override
	public synchronized void processActionSuccess(MindAction a) {
		mind.sendSuccess(a);
	}

	@Override
	protected void processActionCancellation(MindAction a) 
	{
		mind.sendCancel(a);
	}
	
	/** pause the mind */
	@Override
	public synchronized void  sendMindToSleep() {
		mind.sendSleep();
	}

	@Override
	public synchronized void processRemoteAction(MindAction remoteAction) {
		mind.sendRemoteAction(remoteAction);		
	}


	@Override
	protected void processEntityAdded(String entityName) {
		mind.sendEntityAdded(entityName);
	}


	@Override
	protected void processEntityRemoved(String entityName) {
		mind.sendEntityRemoved(entityName);
	}


	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue) {
		mind.sendPropertyChanged(entityName,propertyName,propertyValue);
	}


	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {
		mind.sendPropertyRemoved(entityName,propertyName);
	}


	@Override
	protected void architectureReady() 
	{	
		mind.sendAwake();
	}

}
