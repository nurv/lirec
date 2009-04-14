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
import ion.Meta.Simulation;

import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author LG
 */
public class EventTest {
    
    Simulation simulation;

    public EventTest() {
        this.simulation = Simulation.instance;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp(){
    }
    
    @After
    public void tearDown(){
        Utils.tearDownSimulation(this.simulation);
    }
    
    /**
     * Tests if when an event is raised twice it is treated only once.
     */
    @Test
    public void doubleRaiseEventCatchTest(){
        
        TestEventHandler handler = new TestEventHandler(EventA.class);
        EventA event = new EventA();
        
        this.simulation.getEventHandlers().add(handler);
        this.simulation.update();
        
        this.simulation.raise(event);
        this.simulation.raise(event);
        this.simulation.update();
        
        assertEquals(1, handler.eventsCaught.size());
        assertTrue(handler.eventsCaught.contains(event));
    }
    
    /**
     * Tests if two events of the same class are both treated
     * if raised simultaneously.
     */
    @Test
    public void sameTypeEventCatchTest(){
        
        TestEventHandler handler = new TestEventHandler(EventA.class);
        
        this.simulation.getEventHandlers().add(handler);
        this.simulation.update();
        
        this.simulation.raise(new EventA());
        this.simulation.raise(new EventA());
        this.simulation.update();
        
        assertEquals(2, handler.eventsCaught.size());
    }
    
    @Test
    public void catchSonEventsTest(){
        
        TestEventHandler handler = new TestEventHandler(EventA.class);
        
        this.simulation.getEventHandlers().add(handler);
        this.simulation.update();
        
        this.simulation.raise(new EventB());
        this.simulation.update();
        
        assertEquals(1, handler.eventsCaught.size());
        assertTrue(handler.eventsCaught.get(0) instanceof EventB);
    }
    
    @Test
    public void doNotCatchFatherEventTest(){
        
        TestEventHandler handler = new TestEventHandler(EventB.class);
        
        this.simulation.getEventHandlers().add(handler);
        this.simulation.update();
        
        this.simulation.raise(new EventA());
        this.simulation.update();
        
        assertTrue(handler.eventsCaught.isEmpty());
    }
    
    //<editor-fold defaultstate="collapsed" desc="Auxiliary Classes">
    
    private class TestEventHandler extends EventHandler {

        private ArrayList<IEvent> eventsCaught;
        
        public TestEventHandler(Class<? extends IEvent> type) {
            super(type);
            eventsCaught = new ArrayList<IEvent>();
        }
        
        @Override
        public void invoke(IEvent evt) {
            this.eventsCaught.add(evt);
        }
    }
    
    private class EventA extends Event {
    }
    
    private class EventB extends EventA{
    }

    //</editor-fold>
}