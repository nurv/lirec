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

	Authors:  Pedro Cuba, Guilherme Raimundo, Marco Vala, Rui Prada, Carlos Martinho 

	Revision History:
  ---
  09/04/2009      Pedro Cuba <pedro.cuba@tagus.ist.utl.pt>
  First version.
  ---  
*/
package ion.SyncCollections.UnitTests;

import ion.Meta.Element;
import ion.Meta.Event;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;
import ion.SyncCollections.ElementHashDictionary;
import ion.SyncCollections.Events.Dictionary.IAddedDictionary;
import ion.SyncCollections.Events.Dictionary.IRemovedDictionary;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author LG
 */
public class ElementHashCollectionTest {
    
    private boolean elementEventRaised;
    private IEvent elementEvent;

    public ElementHashCollectionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //<editor-fold defaultstate="collapsed" desc="Constructor Tests">
    
    @Test
    public void elementHashDictionaryConstructorTest() {
        ElementHashDictionary<String, DummyElement> elementHashDictionary =
                new ElementHashDictionary<String, DummyElement>();
        
        assertEquals(0, elementHashDictionary.count());
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Test Events">
    
    /**
     * Tests if added event is raised on added elements.
     */
    @Test
    public void addedEventTest() {
        Simulation simulation = Simulation.instance;
        ElementHashDictionary<String, DummyElement> dictionary = new ElementHashDictionary<String, DummyElement>();
        DummyElement value = new DummyElement();
        final String key = "key";

        simulation.getElements().add(dictionary);
        simulation.getElements().add(value);
        simulation.update();

        dictionary.add(key, value);
        assertFalse(value.addedRaised);

        simulation.update();
        assertTrue(value.addedRaised);
        assertEquals(key, value.addedEvent.getKey());
        assertEquals(value, value.addedEvent.getValue());
        assertEquals(dictionary, value.addedEvent.getDictionary());
    }
    
    /**
     * Tests if removed event is raised on removed elements.
     */
    @Test
    public void removedEventTest() {
        Simulation simulation = Simulation.instance;
        ElementHashDictionary<String, DummyElement> dictionary = new ElementHashDictionary<String, DummyElement>();
        DummyElement value = new DummyElement();
        final String key = "key";
        dictionary.add(key, value);

        simulation.getElements().add(dictionary);
        simulation.getElements().add(value);
        simulation.update();

        dictionary.remove(key);
        assertTrue(dictionary.contains(key));
        assertFalse(value.removeRaised);

        simulation.update();
        assertTrue(value.removeRaised);
        assertEquals(key, value.removedEvent.getKey());
        assertEquals(value, value.removedEvent.getValue());
        assertEquals(dictionary, value.removedEvent.getDictionary());
    }
    
    @Test
    public void eventOnItemTest() {
        Simulation simulation = Simulation.instance;
        ElementHashDictionary<String, DummyElement> dictionary = new ElementHashDictionary<String, DummyElement>();
        DummyElement value = new DummyElement();
        final String key = "key";
        Event evt = new TestEvent();
        dictionary.set(key, value);

        dictionary.getEventHandlers().add(new OnEventRaised());

        simulation.getElements().add(dictionary);
        simulation.getElements().add(value);
        simulation.update();

        this.elementEventRaised = false;
        value.raise(evt);
        assertFalse(this.elementEventRaised);
        simulation.update();
        assertTrue(this.elementEventRaised);
        assertEquals(evt, this.elementEvent);
    }
    
    //<editor-fold defaultstate="collapsed" desc="Auxiliary Classes">
    
    private class DummyElement extends Element {

        public boolean removeRaised;
        public IRemovedDictionary<String, DummyElement, ElementHashDictionary<String, DummyElement>> removedEvent;
        public boolean addedRaised;
        public IAddedDictionary<String, DummyElement, ElementHashDictionary<String, DummyElement>> addedEvent;

        public DummyElement() {
            this.getEventHandlers().add(new OnAdded());
            this.getEventHandlers().add(new OnRemoved());
        }

        @Override
        public void onDestroy() {
            throw new UnsupportedOperationException();
        }

        private class OnAdded extends EventHandler {

            public OnAdded() {
                super(IAddedDictionary.class);
            }

            @Override
            public void invoke(IEvent evt) {
                addedRaised = true;
                addedEvent = (IAddedDictionary) evt;
            }
        }

        private class OnRemoved extends EventHandler {

            public OnRemoved() {
                super(IRemovedDictionary.class);
            }

            @Override
            public void invoke(IEvent evt) {
                removeRaised = true;
                removedEvent = (IRemovedDictionary) evt;
            }
        }
    }
    
    private class TestEvent extends Event {
    }

    private class OnEventRaised extends EventHandler {

        public OnEventRaised() {
            super(IEvent.class);
        }

        @Override
        public void invoke(IEvent evt) {
            elementEventRaised = true;
            elementEvent = evt;
        }
    }
    
    //</editor-fold>
}