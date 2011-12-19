
/*	
Wizard of Oz interface for Team Buddy Scenario
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

package cmion.TeamBuddy.gui;

import javax.swing.SwingUtilities;

import cmion.architecture.IArchitecture;
import cmion.level3.AgentMindConnector;
import cmion.level3.MindAction;

public class TBWoz extends AgentMindConnector {

/** reference to the gui */
TBWozFrame wozGUI;

public TBWoz(IArchitecture architecture) {
	// call parent constructor
	super(architecture);
	// create gui
	SwingUtilities.invokeLater(new Runnable() 
	{
		public void run() {
			wozGUI = new TBWozFrame(TBWoz.this);
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

/** print the failure of a woz Action in the log*/	
@Override
protected void processActionFailure(MindAction a) 
{
	String newLogLine = null;
	if (a.getName().equals("wozTalk") && a.getSubject().equals("Sarah"))
		newLogLine = "FAIL!!!!!" + a.getParameters().get(0);
	else if (a.getName().equals("wozEmotion") && a.getSubject().equals("Sarah"))
		newLogLine = "FAIL!!!!! emotion: " + a.getParameters().get(0);
	else if (a.getName().equals("wozNavigate") && a.getSubject().equals("Sarah"))
		newLogLine = "FAIL!!!!! navigate: " + a.getParameters().get(0);

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

/** print the success of a woz Action in the log*/
@Override
protected void processActionSuccess(MindAction a) 
{
	String newLogLine = null;
	if (a.getName().equals("wozTalk") && a.getSubject().equals("Sarah"))
		newLogLine = a.getParameters().get(0);
	else if (a.getName().equals("wozEmotion") && a.getSubject().equals("Sarah"))
		newLogLine = "***** emotion: " + a.getParameters().get(0);
	else if (a.getName().equals("wozNavigate") && a.getSubject().equals("Sarah"))
		newLogLine = "***** navigate: " + a.getParameters().get(0);
	
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

@Override
protected void processPropertyChanged(String entityName, String propertyName, String propertyValue, boolean persistent) {
	// TODO Auto-generated method stub
	
}

@Override
protected void processRawMessage(String message) {
	// TODO Auto-generated method stub
	
}



}

