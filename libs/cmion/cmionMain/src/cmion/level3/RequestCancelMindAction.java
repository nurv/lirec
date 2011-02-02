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

import ion.Meta.Request;

/** this type of event is raised by the agent mind connector when the mind decides
 *  to cancel a currently executing mind action 
 *  the competency manager listens for those events to stop the execution */
public class RequestCancelMindAction extends Request 
{

	/** creates a new request */
	public RequestCancelMindAction(MindAction mindAction)
	{
		super();
		this.mindAction = mindAction;
	}

	/** the mind action that should be cancelled */
	private MindAction mindAction;

	/** returns the mind action that this request refers to */
	public MindAction getMindAction()
	{
		return mindAction;
	}
}