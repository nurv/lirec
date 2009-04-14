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
package ion.SyncCollections;

import ion.Meta.Element;
import ion.Meta.Event;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.SyncCollections.Events.Dictionary.IAdded;
import ion.SyncCollections.Events.Dictionary.IRemoved;

/**
 * An implementation of HashDictionary<K,V> where the values must extend Element.
 * All events raised on the elements are also raised on the ElementHashDictionary.
 * Added / Removed / Set Events are raised on both ElementHashDictionary and elements.
 * 
 * @author GAIPS
 * @param <K> the class of the dictionary keys
 * @param <V> the class of the dictionary values
 */
public class ElementHashDictionary<TKey,TValue extends Element> extends HashDictionary<TKey,TValue> {

    public ElementHashDictionary() {
        this.getEventHandlers().add(new IAddedHandler());
        this.getEventHandlers().add(new IRemovedHandler());
    }
    
    private void onElementAdded(IAdded<TKey, TValue> evt) {
        if(evt.getDictionary().equals(this)){
            evt.getValue().raise((Event) evt);
            evt.getValue().getEventHandlers().add(new ElementEventHandler());
        }
    }

    private void onElementRemoved(IRemoved<TKey, TValue> evt) {
        if(evt.getDictionary().equals(this)){
            evt.getValue().raise((Event) evt);
            evt.getValue().getEventHandlers().remove(new ElementEventHandler());
        }
    }

    private void onElementEvent(Event evt) {
        this.raise(evt);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        this.getEventHandlers().remove(new IAddedHandler());
        this.getEventHandlers().remove(new IRemovedHandler());
        
        for (TValue element : this.getValues()) {
            element.getEventHandlers().remove(new ElementEventHandler());
        }
    }
    
    private class IAddedHandler extends EventHandler {

        public IAddedHandler() {
            super(IAdded.class);
        }

        @Override
        public void invoke(IEvent evt) {
            onElementAdded((IAdded) evt);
        }
    }
    
    private class IRemovedHandler extends EventHandler {

        public IRemovedHandler() {
            super(IRemoved.class);
        }

        @Override
        public void invoke(IEvent evt) {
            onElementRemoved((IRemoved)evt);
        }
    }
    
    private class ElementEventHandler extends EventHandler {

        public ElementEventHandler() {
            super(Event.class);
        }

        @Override
        public void invoke(IEvent evt) {
            onElementEvent((Event)evt);
        }
    }
    
}
