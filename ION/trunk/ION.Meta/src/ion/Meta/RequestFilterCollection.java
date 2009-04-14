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

final class RequestFilterCollection implements IRequestFilterCollection {

    private static final Iterable EmptyItems = new LinkedList();
    private final Element element;
    private final HashMap<Class<?>, HashSet<RequestFilter>> filters;

    public RequestFilterCollection(Element element) {
        this.element = element;
        this.filters = new HashMap<Class<?>, HashSet<RequestFilter>>();
    }

    public void add(RequestFilter item){
        this.element.schedule(new AddRequestFilter(item, this.element));
    }

    public void remove(RequestFilter item) {
        this.element.schedule(new RemoveRequestFilter(item, this.element));
    }

    public boolean contains(RequestFilter item) {
        return (this.filters.containsKey(item.getType()) && this.filters.get(item.getType()).contains(item));
    }

    public Iterable<RequestFilter> get(Class<?> type) {

        if (this.filters.containsKey(type)) {
            return this.filters.get(type);
        } else {
            return RequestFilterCollection.EmptyItems;
        }
    }
 
    public Iterable<Class<?>> getTypes() {
        return this.filters.keySet();
    }
    
    public Iterator<RequestFilter> iterator() {
        HashSet<RequestFilter> itemsMerged = new HashSet<RequestFilter>();

        for (HashSet<RequestFilter> valueItems : this.filters.values()) {
            itemsMerged.addAll(valueItems);
        }
        return itemsMerged.iterator();
    }
    
    void manageRequests(IReadOnlyQueueSet<Request> requests) {
        HashSet<RequestFilter> itemsToRemove = new HashSet<RequestFilter>();
        HashSet<RequestFilter> itemsToAdd = new HashSet<RequestFilter>();
        HashSet<Class<?>> nodesToCleanUp = new HashSet<Class<?>>();

        // validate filters to remove
        for (RemoveRequestFilter request : requests.get(RemoveRequestFilter.class)) {

            if (this.filters.containsKey(request.item.getType()) && this.filters.get(request.item.getType()).contains(request.item)) {
                itemsToRemove.add(request.item);
                nodesToCleanUp.add(request.item.getType());
            }
        }

         // validade filters to add
        for (AddRequestFilter request : requests.get(AddRequestFilter.class)) {

            if (!this.filters.containsKey(request.item.getType()) || !this.filters.get(request.item.getType()).contains(request.item)) {
                itemsToAdd.add(request.item);
                nodesToCleanUp.remove(request.item.getType());
            }
        }

        // remove filters
        for (RequestFilter item : itemsToRemove) {
            this.executeRemove(item);
        }

        // add filters
        for (RequestFilter item : itemsToAdd) {
            this.executeAdd(item);
        }

        // clean up nodes
        for (Class<?> type : nodesToCleanUp) {
            this.executeCleanUp(type);
        }
    }

    boolean executeAdd(RequestFilter item) {
        HashSet<RequestFilter> items;
        
        if (!this.filters.containsKey(item.getType())) {
            items = new HashSet<RequestFilter>();
            this.filters.put(item.getType(), items);
        } else {
            items = this.filters.get(item.getType());
        }

        if (items.add(item)) {
            // raise event Added<>
            this.element.raise(new AddedRequestFilter(item, this.element));

            return true;
        }
        return false;
    }
    
    boolean executeRemove(RequestFilter item) {
        HashSet<RequestFilter> items;
        
        if (this.filters.containsKey(item.getType())) {
            items = this.filters.get(item.getType());
            
            if (items.remove(item)) {
                // raise event Removed<>
                this.element.raise(new RemovedRequestFilter(item, this.element));

                return true;
            }
        }
        return false;
    }
    
    void executeCleanUp(Class<?> type) {
        HashSet<RequestFilter> items;
        
        if (this.filters.containsKey(type)) {
            items = this.filters.get(type);

            if (items.isEmpty()) {
                this.filters.remove(type);
            }
        }
    }

    class Manager extends RequestHandler {

        public Manager() {
            super(new TypeSet(AddRequestFilter.class, RemoveRequestFilter.class));
        }

        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            manageRequests(requests);
        }
    }
}
