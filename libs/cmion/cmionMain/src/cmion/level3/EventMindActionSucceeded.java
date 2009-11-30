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

	/** this type of event is raised by the competency manager when a mind action has succeeded 
	 *  the agent mind connector listens for those events and sends them to the agent mind*/
public class EventMindActionSucceeded extends CmionEvent 
{

	EventMindActionSucceeded(MindAction mindAction)
	{
		super();
		this.mindAction = mindAction;
	}
	
	/** the mind action that has succeeded */
	private MindAction mindAction;
	
	/** returns the mind action that this event refers to */
	public MindAction getMindAction()
	{
		return mindAction;
	}
	
	/** displays information about this event */
	@Override
	public String toString()
	{
		String evtString =  "Mind Action succeeded: " + mindAction.getName();
		for (String parameter : mindAction.getParameters())
			evtString += " " + parameter;
		return evtString;		
	}
}
