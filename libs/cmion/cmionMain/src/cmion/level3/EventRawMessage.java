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

/** this type of event can be raised by any competency. Active agent minds listen
 *  for this event type and will be passed the messaged wrapped by this event. So this 
 *  event is a placeholder for any specialised communication / perception between a 
 *  competency and an agent mind. Was introduced to send memory requests to FAtiMA*/
public class EventRawMessage extends CmionEvent 
{
	/** create a new event raw message*/
	public EventRawMessage(String message)
	{
		super();
		this.message = message;
	}
	
	/** the raw message to be sent to the mind */
	private String message;
	
	/** returns the raw message */
	public String getMessage()
	{
		return message;
	}
	
	/** displays information about this event */
	@Override
	public String toString()
	{
		String evtString =  "Raw Message to mind: " + message;
		return evtString;		
	}
}
