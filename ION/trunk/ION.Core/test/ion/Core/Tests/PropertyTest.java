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
  22/05/2009      Pedro Cuba <pedro.cuba@tagus.ist.utl.pt>
  Added new test to verify if the ChangedValue Event is not raised when the value of a property is set to the value that the property has. 
  ---
*/
package ion.Core.Tests;

import ion.Core.Property;
import ion.Core.Events.IValueChanged;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author GAIPS
 */
public class PropertyTest {
    
    private boolean changedValueEventLaunched;

    public PropertyTest() {
        changedValueEventLaunched = false;
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Tests Value changes with property not in simulation.
     */
    @Test
    public void valueTestOutSimulation() {
        final Integer initialValue = 2;
        final Integer expectedValue = initialValue + 3;

        Property<Integer> property = new Property<Integer>(initialValue);

        property.setValue(expectedValue);
        assertEquals(property.getValue(), expectedValue);
    }
    
    /**
     * Tests Value changes with property in the simulation.
     */
    @Test
    public void valueTestInSimulation() {
        Simulation simulation = Simulation.instance;

        Property<Integer> property = new Property<Integer>(1);
        simulation.getElements().add(property);

        simulation.update();

        int expected = property.getValue();
        property.setValue(expected + 1);
        assertEquals(property.getValue(), expected); //Value is not supposed to change before update

        simulation.update();
        expected = expected + 1;
        assertEquals(property.getValue(), expected); //Value is expected to change after update
    }
    
    @Test
    public void testNullValue() {
        Simulation simulation = Simulation.instance;
        Property<Dummy> property = new Property<Dummy>();
        Dummy dummy = new Dummy();
        simulation.getElements().add(property);

        simulation.update();
        property.setValue(dummy);
        assertNull(property.getValue());

        simulation.update();
        assertEquals(dummy, property.getValue());
        property.setValue(null);

        simulation.update();
        assertNull(property.getValue());
    }
    
    //<editor-fold defaultstate="collapsed" desc="Multiple Requests Policy Test">
    
    @Test
	public void multipleValueSetTest() {
		Simulation simulation = Simulation.instance;

		final int initialValue = 1;

		Property<Integer> property = new Property<Integer>(initialValue);
		simulation.getElements().add(property);
		simulation.update();

		final int expectedValue = initialValue + 1;

		property.setValue(expectedValue);
		property.setValue(expectedValue + 1);
		assertEquals(property.getValue(), initialValue);

		simulation.update();
		assertEquals(property.getValue(), expectedValue);
	}
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Launched Events Testing">
    
    @Test
	public void testPropertyChangedValueEventLaunched() {

		Simulation simulation = Simulation.instance;
		Property<Integer> property = new Property<Integer>();
		property.getEventHandlers().add(new ValueChangedHandler());

		simulation.getElements().add(property);
		simulation.update();

		this.changedValueEventLaunched = false;

		property.setValue(5);
		assertFalse(this.changedValueEventLaunched);

		simulation.update();
		assertTrue(this.changedValueEventLaunched);
	}
    
    /**
	 * Tests if the ValueChanged is not raised when there is a Set with the
	 * value that the property already has.
	 */
    @Test
	public void testPropertyChangedValueOldValueEqualsNewValueEventNotLaunched() {
		Simulation simulation = Simulation.instance;
		int value = 5;
		Property<Integer> property = new Property<Integer>(value);
		property.getEventHandlers().add(new ValueChangedHandler());

		simulation.getElements().add(property);
		simulation.update();

		this.changedValueEventLaunched = false;

		property.setValue(property.getValue());
		assertFalse(this.changedValueEventLaunched);

		simulation.update();
		assertFalse(this.changedValueEventLaunched);
	}

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor Tests">
    
    @Test
    public void propertyConstructorTest1() {
        Property<Integer> property = new Property<Integer>();
        assertEquals(property.getValue(), null);
    }

    @Test
    public void propertyConstructorTest() {
        Property<Integer> property = new Property<Integer>(3);
        assertEquals(property.getValue(), 3);
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Auxiliary Classes">
    
    private class Dummy{
    }
    
    private class ValueChangedHandler extends EventHandler{

        public ValueChangedHandler() {
            super(IValueChanged.class);
        }
        
        @Override
        public void invoke(IEvent evt) {
            changedValueEventLaunched = true;
        }
    }
    
    //</editor-fold>
}