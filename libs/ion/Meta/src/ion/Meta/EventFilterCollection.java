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

final class EventFilterCollection implements IEventFilterCollection {

    private static final Iterable EmptyItems = new LinkedList();
    private final  Element element;
    private final HashMap<Class<?>, HashSet<EventFilter>> filters;

    public EventFilterCollection(Element element) {
        this.element = element;
        this.filters = new HashMap<Class<?>, HashSet<EventFilter>>();
    }

    public void add(EventFilter item){
        this.element.schedule(new AddEventFilter(item, this.element));
    }

    public void remove(EventFilter item) {
        this.element.schedule(new RemoveEventFilter(item, this.element));
    }

    public boolean contains(EventFilter item) {
        return (this.filters.containsKey(item.getType()) && this.filters.get(item.getType()).contains(item));
    }

    public Iterable<EventFilter> get(Class<?> type) {

        if (this.filters.containsKey(type)) {
            return this.filters.get(type);
        } else {
            return EventFilterCollection.EmptyItems;
        }
    }
    
    public Iterable<Class<?>> getTypes() {
        return this.filters.keySet();
    }
 
    public Iterator<EventFilter> iterator() {
        HashSet<EventFilter> itemsMerged = new HashSet<EventFilter>();

        for (HashSet<EventFilter> valueItems : this.filters.values()) {
            itemsMerged.addAll(valueItems);
        }
        return itemsMerged.iterator();
    }
    
    void manageRequests(IReadOnlyQueueSet<Request> requests) {
        HashSet<EventFilter> itemsToRemove = new HashSet<EventFilter>();
        HashSet<EventFilter> itemsToAdd = new HashSet<EventFilter>();
        HashSet<Class<?>> nodesToCleanUp = new HashSet<Class<?>>();

        // validate filters to remove
        for (RemoveEventFilter request : requests.get(RemoveEventFilter.class)) {

            if (this.filters.containsKey(request.item.getType()) && this.filters.get(request.item.getType()).contains(request.item)) {
                itemsToRemove.add(request.item);
                nodesToCleanUp.add(request.item.getType());
            }
        }

        // validade filters to add
        for (AddEventFilter request : requests.get(AddEventFilter.class)) {

            if (!this.filters.containsKey(request.item.getType()) || !this.filters.get(request.item.getType()).contains(request.item)) {
                itemsToAdd.add(request.item);
                nodesToCleanUp.remove(request.item.getType());
            }
        }

        // remove filters
        for (EventFilter item : itemsToRemove) {
            this.executeRemove(item);
        }

        // add filters
        for (EventFilter item : itemsToAdd) {
            this.executeAdd(item);
        }

        // clean up nodes
        for (Class<?> type : nodesToCleanUp) {
            this.executeCleanUp(type);
        }
    }

    boolean executeAdd(EventFilter item) {
        HashSet<EventFilter> items;
        
        if (!this.filters.containsKey(item.getType())) {
            items = new HashSet<EventFilter>();
            this.filters.put(item.getType(), items);
        } else {
            items = this.filters.get(item.getType());
        }

        if (items.add(item)) {
            // raise event Added<>
            this.element.raise(new AddedEventFilter(item, this.element));

            return true;
        }
        return false;
    }
    
    boolean executeRemove(EventFilter item) {
        HashSet<EventFilter> items;
        
        if (this.filters.containsKey(item.getType())) {
            items = this.filters.get(item.getType());
            
            if (items.remove(item)) {
                // raise event Removed<>
                this.element.raise(new RemovedEventFilter(item, this.element));

                return true;
            }
        }
        return false;
    }
    
    void executeCleanUp(Class<?> type) {
        HashSet<EventFilter> items;
        
        if (this.filters.containsKey(type)) {
            items = this.filters.get(type);

            if (items.isEmpty()) {
                this.filters.remove(type);
            }
        }
    }

    class Manager extends RequestHandler {

        public Manager() {
            super(new TypeSet(AddEventFilter.class, RemoveEventFilter.class));
        }

        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            manageRequests(requests);
        }
    }
}
