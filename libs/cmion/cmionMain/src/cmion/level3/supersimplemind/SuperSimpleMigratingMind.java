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

import cmion.level3.MindAction;

/** an example mind that does almost nothing except executing random actions,
 * this is just to show how one could interface a different mind then FAtiMA,
 * e.g. a user controlled mind for debugging purposes*/
public class SuperSimpleMigratingMind
{

/** a pointer to the connector object that connects this mind to the competency manager */	
private SuperSimpleMigratingMindConnector connector;	

/** a boolean tracking whether this mind is executing an action at the moment (i.e. waiting for 
 *  results from that action) or not */
private boolean executing;

/** boolean to keep track of whether the mind is awake or sleeping */
private boolean sleeping;

/** the name of the agent */
private String agentName;

/** creates a new super simple mind */
public SuperSimpleMigratingMind(SuperSimpleMigratingMindConnector connector)
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
	if (sleeping)
	{
		// if we are not already executing something, execute a new random action
		sleeping = false;
		if (!executing) executeMigrationAction();
	}
}

/** notify the mind of an action failure*/
public synchronized  void sendFailure(MindAction a) {
	// this super simple mind does not care what happens to the actions it sends
	executing = false;
	
	// if we are not sleeping, execute new random action
	//if (!sleeping) executeMigrationAction();
}

/** notify the mind of an action success*/
public synchronized void sendSuccess(MindAction a) {
	// this super simple mind does not care what happens to the actions it sends
	executing = false;
	
	// if we are not sleeping, execute new random action
	//if (!sleeping) executeMigrationAction();	
}

/** send the mind to sleep */
public synchronized void sendSleep() 
{
	sleeping = true;
}

/** the mind processes remote actions (actions of other agents / users) in this function */
public synchronized void sendRemoteAction(MindAction remoteAction) 
{
	//this super simple mind doesn't do anything with perceptions
}

/** returns whether the mind is sleeping or not*/
public synchronized  boolean isSleeping()
{
	return sleeping;
}

/** creates a Migrating action to a specific device and attempts to execute it */
private void executeMigrationAction()
{
	ArrayList<String> parameters = new ArrayList<String>();
	parameters.add("B");
	
	// modify this for test purposes
	MindAction ma = new MindAction(agentName,"Migration",parameters);
	this.connector.newAction(ma);
	executing = true;
}

/** creates a Migrating action to a specific device and attempts to execute it */
public void executeMigrationBackAction()
{
	ArrayList<String> parameters = new ArrayList<String>();
	parameters.add("A");
	
	// modify this for test purposes
	MindAction ma = new MindAction(agentName,"Migration",parameters);
	this.connector.newAction(ma);
	executing = true;
}

/** the mind processes added entities in this function */
public void sendEntityAdded(String entityName) {
	//this super simple mind doesn't do anything with perceptions
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