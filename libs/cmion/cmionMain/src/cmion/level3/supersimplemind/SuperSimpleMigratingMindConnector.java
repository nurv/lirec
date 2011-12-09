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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cmion.architecture.IArchitecture;
import cmion.level2.migration.Migrating;
import cmion.level2.migration.MigrationAware;
import cmion.level2.migration.MigrationUtils;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

/** The connector to a SuperSimpleMind (example implementation of a simple mind interface) */
public class SuperSimpleMigratingMindConnector extends AgentMindConnector implements Migrating, MigrationAware {

	
	/** the mind itself */
	private SuperSimpleMigratingMind mind;
	
	/** creates a new connector for a SuperSimpleMind */ 
	public SuperSimpleMigratingMindConnector(IArchitecture architecture)
	{	
		super(architecture);
		// create the mind, initially sleeping
		mind = new SuperSimpleMigratingMind(this);
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
	protected void processActionCancellation(MindAction a) {
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
			String propertyName, String propertyValue, boolean persistent) {
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


	@Override
	public String getMessageTag() {
		return "mindstate";
	}


	@Override
	public void restoreState(Element message) {

		Element goal = (Element) message.getElementsByTagName("goal").item(0);
		if(goal != null){
			System.out.println("MIND-Received mind with goal: "+goal.getAttribute("value"));
		} else {
			System.out.println("MIND-Received mind without goal.");
		}
		new WaitAndMigrateBack().start();
	}

	private class WaitAndMigrateBack extends Thread
	{
		@Override
		public void run()
		{
			// wait 5 seconds
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
			
			// migrate back
			mind.executeMigrationBackAction();
		}
	}

	@Override
	public Element saveState(Document doc) {
		
		//This is the regular case of  migration, where the component can
		//save it's state immediately.
		Element state = doc.createElement(getMessageTag());
		
		Element goal = doc.createElement("goal");
		goal.setAttribute("value", "Migrate Mind");
		state.appendChild(goal);
		
		return state;
		
		//This simulates a case where you need for information before migrating
		/*
		System.out.println("MIND-Halting migration.");
		MigrationUtils.haltMigration(this);
		DelayedMigration delayedMigration = new DelayedMigration(doc);
		new Thread(delayedMigration).start();
		return null;
		*/
	}


	@Override
	public void onMigrationFailure() {
		System.out.println("MIND-Migration Failed.");
	}


	@Override
	public void onMigrationIn() {
		System.out.println("MIND-Receiving a migration.");
	}


	@Override
	public void onMigrationOut() {
		System.out.println("MIND-Going to migrate.");
	}


	@Override
	public void onMigrationSuccess() {
		System.out.println("MIND-Migration Success.");
	}

	private class DelayedMigration implements Runnable{
		
		private Document doc;
		
		public DelayedMigration(Document doc){
			this.doc = doc;
		}
		
		@Override
		public void run() {
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Element state = doc.createElement(getMessageTag());
			
			Element goal = doc.createElement("goal");
			goal.setAttribute("value", "Migrate Mind with delay");
			state.appendChild(goal);
			
			MigrationUtils.addMigrationData(state);
			MigrationUtils.resumeMigration(SuperSimpleMigratingMindConnector.this);
			
			System.out.println("MIND-Migration resumed.");
		}
	}

	@Override
	protected void processRawMessage(String message) {
		// TODO Auto-generated method stub
		
	}
}
