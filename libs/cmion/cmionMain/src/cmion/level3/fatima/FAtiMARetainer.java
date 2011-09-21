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

package cmion.level3.fatima;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import cmion.architecture.IArchitecture;
import cmion.level2.migration.Migrating;
import cmion.level2.migration.MigrationAware;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

/** in this class we pretend to be a fatima for the purpose of retaining the fatima state in a migration scenario*/
public class FAtiMARetainer extends AgentMindConnector implements Migrating, MigrationAware {

	private String state;
	private String successMsg;
	
	public FAtiMARetainer(IArchitecture architecture) 
	{
		super(architecture);
		state = null;
		successMsg = null;
	}


    /** report the failure of an action to FAtiMA */
	@Override
	protected void processActionFailure(MindAction a) 
	{
	}

    /** report the success of an action to FAtiMA */	
	@Override
	protected void processActionSuccess(MindAction a) {

	}
	
	/** FAtiMA is currently not receiving any cancellation feedback messages,
	 *  so we don't need to do anything in here */
	@Override
	protected void processActionCancellation(MindAction a) {}
	
	@Override
	protected void processRemoteAction(MindAction remoteAction) {

	}

	/** send a message to FAtiMA telling the mind to pause */
	@Override
	public void sendMindToSleep() {

	}

	@Override
	protected void processEntityAdded(String entityName) {		
	}

	@Override
	protected void processEntityRemoved(String entityName) {
	}

	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue) {
	}

	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {
	}
	
	@Override
	protected void architectureReady() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMessageTag() 
	{
		return "fatimaconnector";
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

	@Override
	public void restoreState(Element message) 
	{
		System.out.println("MIND-Restoring State.");
		if (message.hasChildNodes())
		{
				// read state
				state = message.getElementsByTagName("state").item(0).getChildNodes().item(0).getNodeValue();

				successMsg = null;
				if (message.getElementsByTagName("successMsg").getLength()==1) 
					successMsg = message.getElementsByTagName("successMsg").item(0).getChildNodes().item(0).getNodeValue();
		} 
	}
	
	
	@Override
	public Element saveState(Document doc) 
	{
		System.out.println("MIND-Saving state.");
		Element parent = doc.createElement(getMessageTag());


		Element stateElement = doc.createElement("state");
		Node stateNode = doc.createTextNode(state);
		stateElement.appendChild(stateNode);
		parent.appendChild(stateElement);

		state = null;

		// also add information about the current fatima mind action (which initiated
		// this migration, so that a success message can be sent on the receiving end)
		if (successMsg!=null)
		{
			Element successMsgElement = doc.createElement("successMsg");
			Node msgNode = doc.createTextNode(successMsg);
			successMsgElement.appendChild(msgNode);
			parent.appendChild(successMsgElement);
		}
		return parent;
	}

	@Override
	public boolean isMindSleeping() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void awakeMind() {
		// TODO Auto-generated method stub
		
	}
}
