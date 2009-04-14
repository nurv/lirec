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
import java.util.LinkedList;


public final class Simulation extends Element{

    // singleton
    public static final Simulation instance  = new Simulation();
    
    private SimulationTime time;
    private final ElementSet elements;
    private final HashSet<Element> pendingProcessRequests;
    private final HashSet<Element> pendingProcessEvents;
    private final HashSet<Element> pendingRemovals;
    private final LinkedList<Element> elementsToProcess;

    private Simulation(){
        super(Simulator.generateUID());

        // setup time
        this.time = new SimulationTime();
        this.time.setSimulation(this);
        this.time.setStep(0);
        this.time.setPhase(SimulationTime.PhaseID.BeforeUpdate);

        // setup synchronization lists
        this.pendingProcessRequests = new HashSet<Element>();
        this.pendingProcessEvents = new HashSet<Element>();
        this.pendingRemovals = new HashSet<Element>();
        this.elementsToProcess = new LinkedList<Element>();

        // setup elements manager 
        this.elements = new ElementSet(this);
        this.elements.add(this);
    }
    
    @Override
    public void onDestroy() {
    }

    public SimulationTime getTime(){
        return this.time.duplicate();
    }
    
    public IElementSet getElements() {
        return this.elements;
    }
    
    void synchronizeProcessRequests(Element element) {
        this.pendingProcessRequests.add(element);
    }

    void synchronizeProcessEvents(Element element) {
        this.pendingProcessEvents.add(element);
    }

    void synchronizeRemoval(Element element) {
        this.pendingProcessRequests.remove(element);
        this.pendingProcessEvents.remove(element);
        this.pendingRemovals.add(element);
    }
    
    public void update(){
        // update time
        this.time.setPhase(SimulationTime.PhaseID.ProcessingRequests);

        // process requests
        this.simulationProcessRequests();

        // process removals
        this.processRemovals();

        // raise event of simulation updated
        this.raise(new SimulationUpdated(this));

        // update time
        this.time.setPhase(SimulationTime.PhaseID.ProcessingEvents);

        // process events
        this.simulationProcessEvents();

        // update time
        this.time.setStep(this.time.getStep() + 1);
        this.time.setPhase(SimulationTime.PhaseID.BeforeUpdate);
    }
    
    void simulationProcessRequests() {
        // process requests
        if (!this.pendingProcessRequests.isEmpty()) {
            
            this.elementsToProcess.addAll(this.pendingProcessRequests);
            this.pendingProcessRequests.clear();
            
            for ( Element element : this.elementsToProcess) {
                element.lockRequests();
            }
            
            for (Element element : this.elementsToProcess) {
                element.processRequests();
            }
            
            for (Element element : this.elementsToProcess) {
                element.processMetaRequests();
            }
            
            this.elementsToProcess.clear();
        }
    }

    void simulationProcessEvents() {
        // process events
        while (!this.pendingProcessEvents.isEmpty()) {
            
            this.elementsToProcess.addAll(this.pendingProcessEvents);
            this.pendingProcessEvents.clear();
            
            for ( Element element : this.elementsToProcess) {
                element.processEvents();
            }
            
            this.elementsToProcess.clear();
        }
    }

    void processRemovals() {
        // process removals
        if (!this.pendingRemovals.isEmpty()) {
            
            for (Element element : this.pendingRemovals) {
                element.lockRequests();
                element.processRequests();
                element.processEvents();
            }
            
            this.pendingRemovals.clear();
        }
    }
}
