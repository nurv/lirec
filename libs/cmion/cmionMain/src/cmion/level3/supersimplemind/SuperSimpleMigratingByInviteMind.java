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

import java.util.ArrayList;

import cmion.level2.migration.InviteMigration;
import cmion.level3.MindAction;

/** an example mind for demonstrating and testing migration by invite*/
public class SuperSimpleMigratingByInviteMind
{

/** a pointer to the connector object that connects this mind to the competency manager */	
private SuperSimpleMigratingByInviteMindConnector connector;	

/** a boolean tracking whether this mind is executing an action at the moment (i.e. waiting for 
 *  results from that action) or not */
private boolean executing;

/** boolean to keep track of whether the mind is awake or sleeping */
private boolean sleeping;

/** the name of the agent */
private String agentName;

/** creates a new super simple mind */
public SuperSimpleMigratingByInviteMind(SuperSimpleMigratingByInviteMindConnector connector)
{
	this.connector = connector;
	executing = false;
	sleeping = true;
	agentName = "SimpleMind";
}

/** returns the name of the agent */
public String getAgentName()
{
	return agentName;
}


/** this is called to awake the mind */
public synchronized void sendAwake() 
{
	System.out.println("Awaking");
	sleeping = false;
}

/** notify the mind of an action failure*/
public synchronized  void sendFailure(MindAction a) {
	// this super simple mind does not care what happens to the actions it sends
	executing = false;
}

/** notify the mind of an action success*/
public synchronized void sendSuccess(MindAction a) {
	// this super simple mind does not care what happens to the actions it sends
	executing = false;
}

/** send the mind to sleep */
public synchronized void sendSleep() 
{
	sleeping = true;
}

/** the mind processes remote actions (actions of other agents / users) in this function */
public synchronized void sendRemoteAction(MindAction remoteAction) 
{
	// we act if we perceive the remote action of invitation and if we are not sleeping
	if (!sleeping && remoteAction.getName().equals("MigrationInvitation")) 
	{
		// receiving an invitation causes us to migrate to the inviting device
		executeMigrationAction(remoteAction.getSubject());
	}
}

/** returns whether the mind is sleeping or not*/
public synchronized  boolean isSleeping()
{
	return sleeping;
}

/** creates a Migrating action to a specific device and attempts to execute it */
private void executeMigrationAction(String targetDevice)
{
	ArrayList<String> parameters = new ArrayList<String>();
	parameters.add(targetDevice);
	MindAction ma = new MindAction(agentName,"Migration",parameters);
	this.connector.newAction(ma);
	executing = true;
}

/** the mind processes added entities in this function */
public void sendEntityAdded(String entityName) 
{
	// if a new entity is added then invite an migration
	connector.raise(new InviteMigration());
}

/** the mind processes removed entities in this function */
public void sendEntityRemoved(String entityName) {
	//this super simple mind doesn't do anything with perceptions	
}

/** the mind processes changed properties in this function */
public void sendPropertyChanged(String entityName, String propertyName,
		String propertyValue) 
{
	//this super simple mind doesn't do anything with perceptions}
}

/** the mind processes removed properties in this function */
public void sendPropertyRemoved(String entityName, String propertyName) {
	//this super simple mind doesn't do anything with perceptions}	
}

}