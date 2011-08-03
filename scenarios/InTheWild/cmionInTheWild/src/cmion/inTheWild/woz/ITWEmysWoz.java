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

package cmion.inTheWild.woz;

import javax.swing.SwingUtilities;

import cmion.architecture.IArchitecture;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

public class ITWEmysWoz extends AgentMindConnector {

	/** reference to the gui */
	ITWEmysWozFrame wozGUI;
	
	public ITWEmysWoz(IArchitecture architecture) {
		// call parent constructor
		super(architecture);
		// create gui
		SwingUtilities.invokeLater(new Runnable() 
		{
			public void run() {
				wozGUI = new ITWEmysWozFrame(ITWEmysWoz.this);
				wozGUI.setLocationRelativeTo(null);
				wozGUI.setVisible(true);
			}
		});	
	}

	@Override
	protected void architectureReady() {}


	@Override
	public void awakeMind() {}


	@Override
	public boolean isMindSleeping() 
	{
		return false;
	}

	/** print the failure of a Greta Action in the log*/	
	@Override
	protected void processActionFailure(MindAction a) 
	{
		String newLogLine = null;
		if (a.getName().equals("wozTalk") && a.getSubject().equals("Sarah"))
			newLogLine = "FAIL!!!!!" + a.getParameters().get(0);
		else if (a.getName().equals("wozEmotion") && a.getSubject().equals("Sarah"))
			newLogLine = "FAIL!!!!! emotion: " + a.getParameters().get(0);
		else if (a.getName().equals("wozGaze") && a.getSubject().equals("Sarah"))
			newLogLine = "FAIL!!!!! gaze: " + a.getParameters().get(0);
		else if (a.getName().equals("wozQuestion") && a.getSubject().equals("Sarah"))
			newLogLine = "FAIL!!!!! question: " + a.getParameters().get(0) + " " + a.getParameters().get(1);

		final String finalLogLine = newLogLine;
		
		if (finalLogLine!=null)
		{
			SwingUtilities.invokeLater(new Runnable() 
			{
				public void run() 
				{
					wozGUI.newLogLine("Greta",finalLogLine);
				}
			});		
		}		
	}
	
	/** print the success of a Greta Action in the log*/
	@Override
	protected void processActionSuccess(MindAction a) 
	{
		String newLogLine = null;
		if (a.getName().equals("wozTalk") && a.getSubject().equals("Sarah"))
			newLogLine = a.getParameters().get(0);
		else if (a.getName().equals("wozEmotion") && a.getSubject().equals("Sarah"))
			newLogLine = "***** emotion: " + a.getParameters().get(0);
		else if (a.getName().equals("wozGaze") && a.getSubject().equals("Sarah"))
			newLogLine = "***** gaze: " + a.getParameters().get(0);
		else if (a.getName().equals("wozQuestion") && a.getSubject().equals("Sarah"))
			newLogLine = "***** question: " + a.getParameters().get(0) + " " + a.getParameters().get(1);
		else if (a.getName().equals("wozSendSMS") && a.getSubject().equals("Sarah"))
			newLogLine = "sms: " + a.getParameters().get(1);

		
		final String finalLogLine = newLogLine;
		
		if (finalLogLine!=null)
		{
			SwingUtilities.invokeLater(new Runnable() 
			{
				public void run() 
				{
					wozGUI.newLogLine("Greta",finalLogLine);
				}
			});		
		}
	}

	@Override
	protected void processEntityAdded(String entityName) {}

	@Override
	protected void processEntityRemoved(String entityName) {}

	@Override
	protected void processPropertyChanged(String entityName,
			String propertyName, String propertyValue) {}

	@Override
	protected void processPropertyRemoved(String entityName, String propertyName) {}


	/** this method handles actions by the user (e.g. send an sms) */
	@Override
	protected void processRemoteAction(final MindAction remoteAction) 
	{
		if (remoteAction.getName().equals("sms") && remoteAction.getParameters().size()>0)
		{
			SwingUtilities.invokeLater(new Runnable() 
			{
				public void run() 
				{
					wozGUI.newLogLine(remoteAction.getSubject(),remoteAction.getParameters().get(0));
				}
			});		
		}
		
	}



	@Override
	public void sendMindToSleep() {}

	@Override
	protected void processActionCancellation(MindAction a) {
		// TODO Auto-generated method stub
		
	}
	
	
	
}
