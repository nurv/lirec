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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

final class EventHandlerCollection implements IEventHandlerCollection {

    private static final Iterable EmptyItems = new LinkedList();
    private final Element element;
    private final HashMap<Class<?>, HashSet<EventHandler>> handlers;

    public EventHandlerCollection(Element element) {
        this.element = element;
        this.handlers = new HashMap<Class<?>, HashSet<EventHandler>>();
    }

    public void add(EventHandler item){
        this.element.schedule(new AddEventHandler(item, this.element));
    }

    public void remove(EventHandler item) {
        this.element.schedule(new RemoveEventHandler(item, this.element));
    }

    public boolean contains(EventHandler item) {
        return (this.handlers.containsKey(item.getType()) && this.handlers.get(item.getType()).contains(item));
    }

    public Iterable<EventHandler> get(Class<?> type) {

        if (this.handlers.containsKey(type)) {
            return this.handlers.get(type);
        } else {
            return EventHandlerCollection.EmptyItems;
        }
    }
    
    public Iterable<Class<?>> getTypes() {
        return this.handlers.keySet();
    }
 
    public Iterator<EventHandler> iterator() {
        HashSet<EventHandler> itemsMerged = new HashSet<EventHandler>();

        for (HashSet<EventHandler> valueItems : this.handlers.values()) {
            itemsMerged.addAll(valueItems);
        }
        return itemsMerged.iterator();
    }
    
    void manageRequests(IReadOnlyQueueSet<Request> requests) {
        HashSet<EventHandler> itemsToRemove = new HashSet<EventHandler>();
        HashSet<EventHandler> itemsToAdd = new HashSet<EventHandler>();
        HashSet<Class<?>> nodesToCleanUp = new HashSet<Class<?>>();

        // validade handlers to remove
        for (RemoveEventHandler request : requests.get(RemoveEventHandler.class)) {

            if (this.handlers.containsKey(request.item.getType()) && this.handlers.get(request.item.getType()).contains(request.item)) {
                itemsToRemove.add(request.item);
                nodesToCleanUp.add(request.item.getType());
            }
        }

        // validade handlers to add
        for (AddEventHandler request : requests.get(AddEventHandler.class)) {

            if (!this.handlers.containsKey(request.item.getType()) || !this.handlers.get(request.item.getType()).contains(request.item)) {
                itemsToAdd.add(request.item);
                nodesToCleanUp.remove(request.item.getType());
            }
        }

        // remove handlers
        for (EventHandler item : itemsToRemove) {
            this.executeRemove(item);
        }

        // add handlers
        for (EventHandler item : itemsToAdd) {
            this.executeAdd(item);
        }

        // clean up nodes
        for (Class<?> type : nodesToCleanUp) {
            this.executeCleanUp(type);
        }
    }

    boolean executeAdd(EventHandler item) {
        HashSet<EventHandler> items;
        
        if (!this.handlers.containsKey(item.getType())) {
            items = new HashSet<EventHandler>();
            this.handlers.put(item.getType(), items);
        } else {
            items = this.handlers.get(item.getType());
        }

        if (items.add(item)) {
            // raise event Added<>
            this.element.raise(new AddedEventHandler(item, this.element));

            return true;
        }
        return false;
    }
    
    boolean executeRemove(EventHandler item) {
        HashSet<EventHandler> items;
        
        if (this.handlers.containsKey(item.getType())) {
            items = this.handlers.get(item.getType());
            
            if (items.remove(item)) {
                // raise event Removed<>
                this.element.raise(new RemovedEventHandler(item, this.element));

                return true;
            }
        }
        return false;
    }
    
    void executeCleanUp(Class<?> type) {
        HashSet<EventHandler> items;
        
        if (this.handlers.containsKey(type)) {
            items = this.handlers.get(type);

            if (items.isEmpty()) {
                this.handlers.remove(type);
            }
        }
    }

    class Manager extends RequestHandler {

        public Manager() {
            super(new TypeSet(AddEventHandler.class, RemoveEventHandler.class));
        }

        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            manageRequests(requests);
        }
    }
}
