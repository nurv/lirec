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
package ion.Meta;

import java.util.HashSet;


public abstract class Event implements IEvent {

    private SimulationTime raiseTime;
    private HashSet<Element> targets = new HashSet<Element>();
    private final Class<?> type;
    private Event baseEvent;
    private HashSet<Element> trail;
    
    public Event() {
        this.type = this.getClass();
        this.baseEvent = null;
    }
    
    public SimulationTime getRaiseTime() {
        if (this.baseEvent == null) {
            return this.raiseTime.duplicate();
        } else {
            return this.baseEvent.raiseTime.duplicate();
        }
    }

    void setRaiseTime(SimulationTime raiseTime) {
        if (this.baseEvent == null && this.raiseTime == null) {
            this.raiseTime = raiseTime.duplicate();
        }
    }
    
    Iterable<Class<?>> getTypes() {
        return TypeCache.instance.getParents(this.type);
    }
    
    HashSet<Element> getTrail() {

        if (this.baseEvent == null) {
            if (this.trail == null) {
                this.trail = new HashSet<Element>();
            }
            return this.trail;
        } else {
            return this.baseEvent.trail;
        }
    }

    HashSet<Element> getTargets() {
        return this.targets;
    }
}
