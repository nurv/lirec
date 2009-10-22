/*	
        ION Framework - Synchronized Collections Unit Test Classes
	Copyright(C) 2009 GAIPS / INESC-ID Lisboa

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

	Authors:  Pedro Cuba, Marco Vala, Guilherme Raimundo, Rui Prada, Carlos Martinho 

	Revision History:
  ---
  09/04/2009      Pedro Cuba <pedro.cuba@tagus.ist.utl.pt>
  First version.
  ---  
*/
package ion.Meta.Tests;


import ion.Meta.Element;
import ion.Meta.EventFilter;
import ion.Meta.EventHandler;
import ion.Meta.RequestFilter;
import ion.Meta.RequestHandler;
import ion.Meta.Simulation;

import java.util.Iterator;

public class Utils {
    
    public static void tearDownSimulation(Simulation sim){
        //Clean the elements in the simulation
        Iterator<Element> elements = sim.getElements().iterator();
        while(elements.hasNext()){
            elements.next();
            elements.remove();
        }
        
        //Clean the simulation EventFilters
        Iterator<EventFilter> eventFilters = sim.getEventFilters().iterator();
        while(eventFilters.hasNext()){
            eventFilters.next();
            eventFilters.remove();
        }
        
        //Clean the simulation EventHandlers
        Iterator<EventHandler> eventHandlers = sim.getEventHandlers().iterator();
        while(eventHandlers.hasNext()){
            eventHandlers.next();
            eventHandlers.remove();
        }
        
        //Clean the simulation RequestFilters
        Iterator<RequestFilter> requestFilters = sim.getRequestFilters().iterator();
        while(requestFilters.hasNext()){
            requestFilters.next();
            requestFilters.remove();
        }
        
        //Clean the simulation RequestHandlers
        Iterator<RequestHandler> requestHandlers = sim.getRequestHandlers().iterator();
        while(requestHandlers.hasNext()){
            requestHandlers.next();
            requestHandlers.remove();
        }
    }

}
