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

package cmion.level3;
import cmion.architecture.CmionEvent;

/** this type of event can be raised by any sensing competency. It represents someone
 *  other than the companion (e.g. a user) performing an action. The action is represented
 *  as an agent mind action. The agent mind connector listens for those events and sends 
 *  them to the agent mind*/
public class EventRemoteAction extends CmionEvent 
{
	/** create a new event remote action*/
	EventRemoteAction(MindAction remoteAction)
	{
		super();
		this.remoteAction = remoteAction;
	}
	
	/** the remote action that has been registered */
	private MindAction remoteAction;
	
	/** returns the remote action that this event refers to */
	public MindAction getRemoteAction()
	{
		return remoteAction;
	}
	
	/** displays information about this event */
	@Override
	public String toString()
	{
		String evtString =  "Remote Action by " + remoteAction.getSubject() +
		                    ": "  + remoteAction.getName();
		for (String parameter : remoteAction.getParameters())
			evtString += " " + parameter;
		return evtString;		
	}
}
