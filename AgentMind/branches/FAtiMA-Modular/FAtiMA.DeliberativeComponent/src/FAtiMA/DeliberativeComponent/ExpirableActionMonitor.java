/** 
 * ExpirableActionMonitor.java - Implements a monitor capable of verifying if a given
 * action has been achieved. But this monitor waits only for a limited amount of time.
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 30/12/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 30/12/2004 - File created
 * João Dias: 24/05/2006 - Added comments to each public method's header
 * João Dias: 02/07/2006 - Replaced System's timer by an internal agent simulation timer
 * João Dias: 10/07/2006 - the class is now serializable 
 */

package FAtiMA.DeliberativeComponent;

import FAtiMA.Core.AgentSimulationTime;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.Event;

/**
 * Implements a monitor capable of verifying if a given
 * action has been achieved. But this monitor waits only for 
 * a limited amount of time.
 * 
 * @author João Dias
 */
public class ExpirableActionMonitor extends ActionMonitor {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private long _endTime;
    
    /**
     * Creates a new ActionMonitor that expires after some time
     * @param waitTime - how long should the monitor wait before expiring
     * @param s - the plan's step (action) that we want to monitor
     * @param actionEnd - the event that we should wait for. If this event
     * 					  happens, it means that the action finished
     */
    public ExpirableActionMonitor(long waitTime, Step s, Event actionEnd) {
        super(s,actionEnd);
        this._endTime = AgentSimulationTime.GetInstance().Time() + waitTime;
    }
    
    /**
     * indicates if the ActionMonitor expired and we should wait no more.
     * @return true if the Monitor waited more than specified, false otherwise
     */
    public boolean expired() {
        return  AgentSimulationTime.GetInstance().Time() > _endTime;
    }
}