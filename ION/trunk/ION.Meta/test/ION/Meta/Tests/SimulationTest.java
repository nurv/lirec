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

import ion.Meta.Event;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.Simulation;
import ion.Meta.TypeSet;
import ion.Meta.SimulationTime.PhaseID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author LG
 */
public class SimulationTest {

    Simulation simulation;
    
    public SimulationTest() {
        this.simulation = Simulation.instance;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @After
    public void tearDown(){
        Utils.tearDownSimulation(this.simulation);
    }
    
    /**
     * Tests if simulation time changes correctly after an update.
     */
    @Test
    public void simulationStepTest(){
        long startStep = this.simulation.getTime().getStep();
        
        this.simulation.update();
        assertEquals(startStep + 1, this.simulation.getTime().getStep());
        this.simulation.update();
        assertEquals(startStep + 2, this.simulation.getTime().getStep());
    }
    
    /**
     * Tests if the simulation goes through all of its phases.
     * In addition it also tests if requests and events are handled
     * at the correct phase.
     */
    @Test
    public void simulationPhasesTest(){
        
        CheckPhaseRequestHandler requestHandler = new CheckPhaseRequestHandler();
        CheckPhaseEventHandler eventHandler = new CheckPhaseEventHandler();
        
        this.simulation.getRequestHandlers().add(requestHandler);
        this.simulation.getEventHandlers().add(eventHandler);
        this.simulation.update();
        
        this.simulation.schedule(new DummyRequest());
        this.simulation.raise(new DummyEvent());
        this.simulation.update();
        
        assertEquals(PhaseID.ProcessingRequests, requestHandler.phase);
        assertEquals(PhaseID.ProcessingEvents, eventHandler.phase);
        assertEquals(PhaseID.BeforeUpdate, this.simulation.getTime().getPhase());
    }
    
    //<editor-fold defaultstate="collapsed" desc="Auxiliary Classes">
    
    public class DummyRequest extends Request {
    }
    
    public class CheckPhaseRequestHandler extends RequestHandler{
        
        public PhaseID phase;

        public CheckPhaseRequestHandler() {
            super(new TypeSet(DummyRequest.class));
        }

        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            this.phase = simulation.getTime().getPhase();
        }
    }
    
    public class DummyEvent extends Event{
    }
    
    public class CheckPhaseEventHandler extends EventHandler{
        
        public PhaseID phase;

        public CheckPhaseEventHandler() {
            super(DummyEvent.class);
        }

        @Override
        public void invoke(IEvent evt) {
            this.phase = simulation.getTime().getPhase();
        }
    }
    
    //</editor-fold>

}