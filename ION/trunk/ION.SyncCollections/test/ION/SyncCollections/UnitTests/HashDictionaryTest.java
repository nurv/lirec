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
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;
import ion.SyncCollections.HashDictionary;
import ion.SyncCollections.ICollectionValue;
import ion.SyncCollections.Events.Dictionary.IAddedDictionary;
import ion.SyncCollections.Events.Dictionary.ICleared;
import ion.SyncCollections.Events.Dictionary.IRemovedDictionary;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map.Entry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author GAIPS
 */
public class HashDictionaryTest {
    
    private boolean addedRaised;
    private boolean removedRaised;
    private IAddedDictionary<String, Dummy, HashDictionary<String, Dummy>> addedEvent;
    private IRemovedDictionary<String, Dummy, HashDictionary<String, Dummy>> removedEvent;
    private boolean clearedRaised;
    private ICleared<HashDictionary<String, Dummy>> clearedEvent;

    public HashDictionaryTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    //<editor-fold defaultstate="collapsed" desc="Simple Tests">
    
    @Test
    public void valuesTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key1, value1);
        dictionary.add(key2, value2);
        simulation.update();

        ICollectionValue<Dummy> values = dictionary.getValues();
        assertEquals(2, values.count());

        ArrayList<Dummy> listValues = new ArrayList<Dummy>();
        for (Dummy dummy : values) {
            listValues.add(dummy);
        }
        
        assertTrue(listValues.contains(value1));
        assertTrue(listValues.contains(value2));
    }
    
    @Test
    public void keysTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key1, value1);
        dictionary.add(key2, value2);
        simulation.update();

        ICollectionValue<String> keys = dictionary.getKeys();
        assertEquals(2, keys.count());

        ArrayList<String> listKeys = new ArrayList<String>();
        for (String key : keys) {
            listKeys.add(key);
        }
        assertTrue(listKeys.contains(key1));
        assertTrue(listKeys.contains(key2));
    }
    
    @Test
    public void itemTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key = "dummy";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.set(key, value1);
        assertFalse(dictionary.contains(key));
        simulation.update();
        assertEquals(value1, dictionary.get(key));

        dictionary.set(key, value2);
        assertFalse(dictionary.equals(value2));
        assertEquals(value1, dictionary.get(key));

        simulation.update();
        assertEquals(value2, dictionary.get(key));
    }
    
    @Test
    public void countTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key1, value1);
        dictionary.add(key2, value2);
        simulation.update();

        assertEquals(2, dictionary.count());

        dictionary.remove(key1);
        assertEquals(2, dictionary.count());
        simulation.update();

        assertEquals(1, dictionary.count());
    }
    
    @Test
    public void getIteratorTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();
        Entry<String, Dummy> expected1 = new SimpleEntry<String, Dummy>(key1, value1);
        Entry<String, Dummy> expected2 = new SimpleEntry<String, Dummy>(key2, value2);
        boolean expected1Found = false;
        boolean expected2Found = false;

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key1, value1);
        dictionary.add(key2, value2);
        simulation.update();

        for (Entry<String, Dummy> pair : dictionary) {
            if (pair.equals(expected1)) {
                expected1Found = true;
            } else if (pair.equals(expected2)) {
                expected2Found = true;
            }
        }

        assertTrue(expected1Found);
        assertTrue(expected2Found);
    }
    
    @Test
    public void toArrayTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();
        Entry<String, Dummy> pair1 = new SimpleEntry<String, Dummy>(key1, value1);
        Entry<String, Dummy> pair2 = new SimpleEntry<String, Dummy>(key2, value2);

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key1, value1);
        dictionary.add(key2, value2);
        simulation.update();

        Entry<String, Dummy>[] array = dictionary.toArray();
        ArrayList<Entry<String, Dummy>> list = new ArrayList<Entry<String, Dummy>>();
        for (Entry<String, Dummy> entry : array) {
            list.add(entry);
        }

        assertTrue(list.contains(pair1));
        assertTrue(list.contains(pair2));
        assertEquals(2, array.length);
    }
    
    @Test
    public void copyToTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();
        Entry<String, Dummy>[] arr = new Entry[3];
        Entry<String, Dummy> expected1 = new SimpleEntry<String, Dummy>(key1, value1);
        Entry<String, Dummy> expected2 = new SimpleEntry<String, Dummy>(key2, value2);

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key1, value1);
        dictionary.add(key2, value2);
        simulation.update();

        dictionary.copyTo(arr, 1);

        ArrayList<Entry<String, Dummy>> list = new ArrayList<Entry<String, Dummy>>();
        for (Entry<String, Dummy> entry : arr) {
            list.add(entry);
        }

        assertTrue(list.contains(expected1));
        assertTrue(list.contains(expected2));
    }
    
    @Test
    public void containsAllTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        ArrayList<String> list = new ArrayList<String>();

        list.add(key1);
        list.add(key2);

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key1, value1);
        dictionary.add(key2, value2);
        simulation.update();

        assertTrue(dictionary.containsAll(list));
    }
    
    @Test
    public void containsTest1() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key = "dummy";
        Dummy value = new Dummy();

        simulation.getElements().add(dictionary);
        simulation.update();


        dictionary.add(key, value);
        simulation.update();

        assertTrue(dictionary.contains(new SimpleEntry<String, Dummy>(key, value)));
    }
    
    @Test
    public void containsTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key = "dummy";
        Dummy value = new Dummy();

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key, value);
        simulation.update();

        assertTrue(dictionary.contains(key));
    }
    
    @Test
    public void removeAllTest1() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        final String key3 = "dummy3";
        Dummy value = new Dummy();
        LinkedList<String> keys = new LinkedList<String>();

        keys.add(key1);
        keys.add(key2);
        keys.add(key3);

        dictionary.add(key1, value);
        dictionary.add(key2, value);
        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.removeAll(keys);
        assertTrue(dictionary.contains(key1));
        assertTrue(dictionary.contains(key2));
        assertFalse(dictionary.contains(key3));
        simulation.update();

        assertFalse(dictionary.contains(key1));
        assertFalse(dictionary.contains(key2));
        assertFalse(dictionary.contains(key3));
    }
    
    @Test
    public void removeAllTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();

        dictionary.add(key1, value1);
        dictionary.add(key2, value2);
        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.removeAll();
        assertTrue(dictionary.contains(key1));
        assertTrue(dictionary.contains(key2));
        simulation.update();

        assertFalse(dictionary.contains(key1));
        assertFalse(dictionary.contains(key2));
    }
    
    @Test
    public void removeTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy dummy = new Dummy();
        final String key = "dummy";

        simulation.getElements().add(dictionary);
        dictionary.add(key, dummy);
        simulation.update();

        dictionary.remove(key);
        assertTrue(dictionary.contains(key));
        simulation.update();
        assertFalse(dictionary.contains(key));
    }
    
    @Test
    public void addAllTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        ArrayList<Entry<String, Dummy>> list = new ArrayList<Entry<String, Dummy>>();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();

        list.add(new SimpleEntry<String, Dummy>(key1, value1));
        list.add(new SimpleEntry<String, Dummy>(key2, value2));

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.addAll(list);
        assertEquals(0, dictionary.count());

        simulation.update();
        assertEquals(2, dictionary.count());
        assertEquals(value1, dictionary.get(key1));
        assertEquals(value2, dictionary.get(key2));
    }
    
    @Test
    public void addTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy dummy1 = new Dummy();
        Dummy dummy2 = new Dummy();
        final String key = "dummy";

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key, dummy1);
        assertFalse(dictionary.contains(key));
        simulation.update();
        assertTrue(dictionary.contains(key));
        assertEquals(dummy1, dictionary.get(key));

        dictionary.add(key, dummy2);
        simulation.update();
        assertEquals(dummy1, dictionary.get(key));

        assertEquals(1, dictionary.count());
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Constructor Tests">
    
    @Test
    public void hashDictionaryConstructorTest() {
        HashDictionary<String, Element> dictionary = new HashDictionary<String, Element>();
        assertEquals(0, dictionary.count());
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Multiple Requests Tests">
    
    /**
     * Tests that the first add has priority over the subsequent add requests.
     */
    @Test
    public void multipleAddTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();
        final String key1 = "dummy1";
        final String key2 = "dummy2";

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key1, value1);
        dictionary.add(key1, value2);
        dictionary.add(key2, value2);
        dictionary.add(key2, value1);
        assertEquals(0, dictionary.count());

        simulation.update();
        assertEquals(value1, dictionary.get(key1));
        assertEquals(value2, dictionary.get(key2));
    }
    
    /**
     * Tests if multiple Removes don't give rise to inconsistent states.
     */
    @Test
    public void multipleRemoveTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value = new Dummy();
        final String key1 = "dummy1";
        final String key2 = "dummy2";
        dictionary.add(key1, value);
        dictionary.add(key2, value);

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.remove(key1);
        dictionary.remove(key1);
        dictionary.remove(key2);
        dictionary.remove(key2);
        assertTrue(dictionary.contains(key1));
        assertTrue(dictionary.contains(key2));

        simulation.update();
        assertFalse(dictionary.contains(key1));
        assertFalse(dictionary.contains(key2));
    }
    
    /**
     * Tests that the first set has priority over the subsequent set requests.
     */
    @Test
    public void multipleSetTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();
        final String key = "dummy1";

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.set(key, value1);
        dictionary.set(key, value2);
        assertFalse(dictionary.contains(key));

        simulation.update();
        assertEquals(value1, dictionary.get(key));
    }
    
    /**
     * Tests if:
     *  1) Adds have priority over Removes when the element was not in the collection.
     *  2) Removes have priority over Adds when the element was in the collection.
     */
    @Test
    public void addRemoveTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value = new Dummy();
        final String key = "dummy";

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key, value);
        dictionary.remove(key);
        assertEquals(0, dictionary.count());

        simulation.update();
        assertEquals(value, dictionary.get(key));

        dictionary.add(key, value);
        dictionary.remove(key);

        simulation.update();
        assertFalse(dictionary.contains(key));
    }
    
    /**
     * Tests if set has priority over Adds.
     */
    @Test
    public void setAddTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy valueSet = new Dummy();
        Dummy valueAdded = new Dummy();
        final String keySet1stAdd2nd = "keySet1stAdd2nd";
        final String keyAdd1stSet2nd = "keyAdd1stSet2nd";

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.set(keySet1stAdd2nd, valueSet);
        dictionary.add(keySet1stAdd2nd, valueAdded);
        assertFalse(dictionary.contains(keySet1stAdd2nd));

        simulation.update();
        assertEquals(valueSet, dictionary.get(keySet1stAdd2nd));

        dictionary.add(keyAdd1stSet2nd, valueAdded);
        dictionary.set(keyAdd1stSet2nd, valueSet);
        assertFalse(dictionary.contains(keyAdd1stSet2nd));

        simulation.update();
        assertEquals(valueSet, dictionary.get(keyAdd1stSet2nd));
    }
    
    /**
     * Tests if Sets has priority over Removes.
     */
    @Test
    public void setRemoveTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy valueInit = new Dummy();
        Dummy valueSet = new Dummy();
        final String keySet1stRemove2nd = "keySet1stRemove2nd";
        final String keyRemove1stSet2nd = "keyRemove1stSet2nd";

        assertFalse(valueInit.equals(valueSet));

        dictionary.set(keySet1stRemove2nd, valueInit);
        dictionary.set(keyRemove1stSet2nd, valueInit);
        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.set(keySet1stRemove2nd, valueSet);
        dictionary.remove(keySet1stRemove2nd);
        assertEquals(valueInit, dictionary.get(keySet1stRemove2nd));

        simulation.update();
        assertEquals(valueSet, dictionary.get(keySet1stRemove2nd));


        dictionary.remove(keyRemove1stSet2nd);
        dictionary.set(keyRemove1stSet2nd, valueSet);
        assertEquals(valueInit, dictionary.get(keyRemove1stSet2nd));

        simulation.update();
        assertEquals(valueSet, dictionary.get(keyRemove1stSet2nd));
    }
    
    /**
     * Tests if Sets has priority over Removes and Adds.
     */
    @Test
    public void setAddRemoveTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key = "key";
        Dummy valueAdd = new Dummy();
        Dummy valueSet1 = new Dummy();
        Dummy valueSet2 = new Dummy();

        simulation.getElements().add(dictionary);
        simulation.update();

        dictionary.add(key, valueAdd);
        dictionary.set(key, valueSet1);
        dictionary.remove(key);
        assertFalse(dictionary.contains(key));

        simulation.update();
        assertEquals(valueSet1, dictionary.get(key));
        assertFalse(dictionary.get(key).equals(valueAdd));

        dictionary.add(key, valueAdd);
        dictionary.set(key, valueSet2);
        dictionary.remove(key);

        simulation.update();
        assertEquals(valueSet2, dictionary.get(key));
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Event Tests">
    
    /**
     * Tests if the added event is raised by the add function.
     */
    @Test
    public void addAddedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value = new Dummy();
        final String key = "key";

        dictionary.getEventHandlers().add(new OnAdded());

        simulation.getElements().add(dictionary);
        simulation.update();

        this.addedRaised = false;
        dictionary.add(key, value);
        assertFalse(this.addedRaised);

        simulation.update();
        assertTrue(this.addedRaised);
        assertEquals(this.addedEvent.getKey(), key);
        assertEquals(this.addedEvent.getValue(), value);
        assertEquals(this.addedEvent.getDictionary(), dictionary);
    }
    
    /**
     * Test if Added event is not raised when trying to add an element that is already in the dictionary.
     */
    @Test
    public void secondAddAddedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value = new Dummy();
        final String key = "key";
        dictionary.add(key, value);

        dictionary.getEventHandlers().add(new OnAdded());

        simulation.getElements().add(dictionary);
        simulation.update();

        assertTrue(dictionary.contains(key));
        this.addedRaised = false;
        dictionary.add(key, value);
        assertFalse(this.addedRaised);

        simulation.update();
        assertFalse(this.addedRaised);
    }
    
    /**
     * Tests if the removed event is raised by the remove function.
     */
    @Test
    public void removeRemovedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value = new Dummy();
        final String key = "key";

        dictionary.add(key, value);
        dictionary.getEventHandlers().add(new OnRemoved());

        simulation.getElements().add(dictionary);
        simulation.update();

        this.removedRaised = false;
        dictionary.remove(key);
        assertTrue(dictionary.contains(key));
        assertFalse(this.removedRaised);
        simulation.update();
        assertTrue(this.removedRaised);
        assertEquals(key, this.removedEvent.getKey());
        assertEquals(value, this.removedEvent.getValue());
        assertEquals(dictionary, this.removedEvent.getDictionary());
    }
    
    /**
     * Tests if Removing an inexisting item does not raise the removed event.
     */
    @Test
    public void invalidRemoveRemovedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        final String key = "key";

        dictionary.getEventHandlers().add(new OnRemoved());

        simulation.getElements().add(dictionary);
        simulation.update();

        //Test if false remove launches remove event
        this.removedRaised = false;
        dictionary.remove(key);
        assertFalse(dictionary.contains(key));
        simulation.update();
        assertFalse(this.removedRaised);
    }
    
    /**
     * Tests if Setting an unbound key raises the added event.
     */
    @Test
    public void setAddedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value = new Dummy();
        final String key = "key";

        dictionary.getEventHandlers().add(new OnAdded());
        dictionary.getEventHandlers().add(new OnRemoved());

        simulation.getElements().add(dictionary);
        simulation.update();

        this.addedRaised = false;
        this.removedRaised = false;
        dictionary.set(key, value);
        assertFalse(this.addedRaised);
        assertFalse(this.removedRaised);

        simulation.update();
        assertTrue(this.addedRaised);
        assertEquals(key, this.addedEvent.getKey());
        assertEquals(value, this.addedEvent.getValue());
        assertFalse(this.removedRaised);
    }
    
    /**
     * Tests if Setting an bounded key raises the added event.
     */
    @Test
    public void setChangeAddedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value1 = new Dummy();
        Dummy value2 = new Dummy();
        final String key = "key";
        dictionary.set(key, value1);

        dictionary.getEventHandlers().add(new OnAdded());
        dictionary.getEventHandlers().add(new OnRemoved());

        simulation.getElements().add(dictionary);
        simulation.update();

        this.addedRaised = false;
        this.removedRaised = false;
        dictionary.set(key, value2);
        assertFalse(this.addedRaised);
        assertFalse(this.removedRaised);

        simulation.update();
        assertTrue(this.addedRaised);
        assertEquals(key, this.addedEvent.getKey());
        assertEquals(value2, this.addedEvent.getValue());
        assertEquals(dictionary, this.addedEvent.getDictionary());

        assertTrue(this.removedRaised);
        assertEquals(key, this.removedEvent.getKey());
        assertEquals(value1, this.removedEvent.getValue());
        assertEquals(dictionary, this.removedEvent.getDictionary());
    }
    
    /**
     * Tests if setting the same key/value does not raise neither added nor removed event.
     */
    @Test
    public void setSameAddedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        Dummy value = new Dummy();
        final String key = "key";
        dictionary.set(key, value);

        dictionary.getEventHandlers().add(new OnAdded());
        dictionary.getEventHandlers().add(new OnRemoved());

        simulation.getElements().add(dictionary);
        simulation.update();

        this.addedRaised = false;
        this.removedRaised = false;
        dictionary.set(key, value);
        assertEquals(value, dictionary.get(key));
        assertFalse(this.addedRaised);
        assertFalse(this.removedRaised);

        simulation.update();
        assertFalse(this.addedRaised);
        assertFalse(this.removedRaised);
    }
    
    /**
     * Tests if the cleared event is raised when a dictionary no longer has items.
     */
    @Test
    public void clearedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        dictionary.set("key1", new Dummy());
        dictionary.set("key2", new Dummy());

        dictionary.getEventHandlers().add(new OnCleared());

        simulation.getElements().add(dictionary);
        simulation.update();

        this.clearedRaised = false;
        dictionary.removeAll();
        assertTrue(dictionary.count() > 0);
        assertFalse(this.clearedRaised);

        simulation.update();
        assertEquals(0, dictionary.count());
        assertTrue(this.clearedRaised);
        assertEquals(dictionary, this.clearedEvent.getDictionary());
    }
    
    /**
     * Tests if the cleared event is not raised when a dictionary is requested to remove all and added an element simultaneously.
     */
    @Test
    public void notClearedEventTest() {
        Simulation simulation = Simulation.instance;
        HashDictionary<String, Dummy> dictionary = new HashDictionary<String, Dummy>();
        dictionary.set("key1", new Dummy());
        dictionary.set("key2", new Dummy());

        dictionary.getEventHandlers().add(new OnCleared());

        simulation.getElements().add(dictionary);
        simulation.update();

        this.clearedRaised = false;
        dictionary.removeAll();
        dictionary.add("key3", new Dummy());
        assertTrue(dictionary.count() > 0);
        assertFalse(this.clearedRaised);

        simulation.update();
        assertTrue(dictionary.count() > 0);
        assertFalse(this.clearedRaised);
    }
    
    
    //<editor-fold defaultstate="collapsed" desc="Auxiliary Classes">
    
    private class Dummy{
    }
    
    private class OnAdded extends EventHandler{

        public OnAdded() {
            super(IAddedDictionary.class);
        }

        @Override
        public void invoke(IEvent evt) {
            addedRaised = true;
            addedEvent = (IAddedDictionary<String, Dummy, HashDictionary<String, Dummy>>) evt;
        }
    }
    
    private class OnRemoved extends EventHandler{

        public OnRemoved() {
            super(IRemovedDictionary.class);
        }

        @Override
        public void invoke(IEvent evt) {
            removedRaised = true;
            removedEvent = (IRemovedDictionary<String, Dummy, HashDictionary<String, Dummy>>) evt;
        }
    }
    
    private class OnCleared extends EventHandler{

        public OnCleared() {
            super(ICleared.class);
        }

        @Override
        public void invoke(IEvent evt) {
            clearedRaised = true;
            clearedEvent = (ICleared<HashDictionary<String, Dummy>>) evt;
        }
    }
    
    //</editor-fold>
}