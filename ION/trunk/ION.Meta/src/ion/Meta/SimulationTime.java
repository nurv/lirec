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

public final class SimulationTime implements Cloneable {

    public enum PhaseID {
        BeforeUpdate,
        ProcessingRequests,
        ProcessingEvents
    }
    
    private Simulation simulation;
    private long step;
    private PhaseID phase;

    public Simulation getSimulation() {
        return this.simulation;
    }

    void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public long getStep() {
        return this.step;
    }

    void setStep(long step) {
        this.step = step;
    }

    public PhaseID getPhase() {
        return this.phase;
    }

    void setPhase(PhaseID phase) {
        this.phase = phase;
    }

    public SimulationTime duplicate() {
        SimulationTime duplicate = null;
        try {
            duplicate = (SimulationTime) super.clone();
        } catch (CloneNotSupportedException ex) {
        }
        return duplicate;
    }
}
