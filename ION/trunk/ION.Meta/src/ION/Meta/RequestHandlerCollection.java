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

final class RequestHandlerCollection implements IRequestHandlerCollection {

    private static final Iterable EmptyItems = new LinkedList();
    private final Element element;
    private final HashMap<TypeSet, HashSet<RequestHandler>> handlers;
    private final HashMap<Class<?>, RequestNode> nodes;

    public RequestHandlerCollection(Element element) {
        this.element = element;
        this.handlers = new HashMap<TypeSet, HashSet<RequestHandler>>();
        this.nodes = new HashMap<Class<?>, RequestNode>();
    }

    public void add(RequestHandler item){
        this.element.schedule(new AddRequestHandler(item, this.element));
    }

    public void remove(RequestHandler item) {
        this.element.schedule(new RemoveRequestHandler(item, this.element));
    }

    public boolean contains(RequestHandler item) {
        return (this.handlers.containsKey(item.getType()) && this.handlers.get(item.getType()).contains(item));
    }

    public Iterable<RequestHandler> get(TypeSet type) {

        if (this.handlers.containsKey(type)) {
            return this.handlers.get(type);
        } else {
            return RequestHandlerCollection.EmptyItems;
        }
    }
    
    public Iterable<TypeSet> getTypes() {
        return this.handlers.keySet();
    }
 
    public Iterator<RequestHandler> iterator() {
        HashSet<RequestHandler> itemsMerged = new HashSet<RequestHandler>();

        for (HashSet<RequestHandler> valueItems : this.handlers.values()) {
            itemsMerged.addAll(valueItems);
        }
        return itemsMerged.iterator();
    }
    
    void manageRequests(IReadOnlyQueueSet<Request> requests) {
        HashSet<RequestHandler> itemsToRemove = new HashSet<RequestHandler>();
        HashSet<RequestHandler> itemsToAdd = new HashSet<RequestHandler>();
        HashSet<TypeSet> nodesToCleanUp = new HashSet<TypeSet>();

        // validade handlers to remove
        for (RemoveRequestHandler request : requests.get(RemoveRequestHandler.class)) {

            if (this.handlers.containsKey(request.item.getType()) && this.handlers.get(request.item.getType()).contains(request.item)) {
                itemsToRemove.add(request.item);
                nodesToCleanUp.add(request.item.getType());
            }
        }

        // validade handlers to add
        for (AddRequestHandler request : requests.get(AddRequestHandler.class)) {

            if (!this.handlers.containsKey(request.item.getType()) || !this.handlers.get(request.item.getType()).contains(request.item)) {
                itemsToAdd.add(request.item);
                nodesToCleanUp.remove(request.item.getType());
            }
        }

        // remove handlers
        for (RequestHandler item : itemsToRemove) {
            this.executeRemove(item);
        }

        // add handlers
        for (RequestHandler item : itemsToAdd) {
            this.executeAdd(item);
        }

        // clean up nodes
        for (TypeSet type : nodesToCleanUp) {
            this.executeCleanUp(type);
        }
    }

    boolean executeAdd(RequestHandler item) {
        HashSet<RequestHandler> items;
        
        if (!this.handlers.containsKey(item.getType())) {
            items = new HashSet<RequestHandler>();
            this.handlers.put(item.getType(), items);
        } else {
            items = this.handlers.get(item.getType());
        }

        if (items.add(item)) {
            // raise event Added<>
            this.element.raise(new AddedRequestHandler(item, this.element));

            // create invoker
            RequestHandlerInvoker invoker = new RequestHandlerInvoker(item);

            RequestNode node;
            for (Class<?> type : item.getType()) {
                if (!this.nodes.containsKey(type)) {
                    node = new RequestNode(type);
                    this.nodes.put(type, node);
                } else {
                    node = this.nodes.get(type);
                }
                node.add(item, invoker);
            }
                
            return true;
        }
        return false;
    }
    
    boolean executeRemove(RequestHandler item) {
        HashSet<RequestHandler> items;
        
        if (this.handlers.containsKey(item.getType())) {
            items = this.handlers.get(item.getType());
            
            if (items.remove(item)) {
                // raise event Removed<>
                this.element.raise(new RemovedRequestHandler(item, this.element));
                
                RequestNode node;

                for (Class<?> type : item.getType()) {
                    if (this.nodes.containsKey(type)) {
                        node = this.nodes.get(type);
                        node.remove(item);
                    }
                }

                return true;
            }
        }
        return false;
    }
    
    void executeCleanUp(TypeSet type) {
        HashSet<RequestHandler> items;
        
        if (this.handlers.containsKey(type)) {
            items = this.handlers.get(type);

            if (items.isEmpty()) {
                this.handlers.remove(type);
            }
        }
    }

    public HashMap<Class<?>, RequestNode> getNodes(){
        return this.nodes;
    }

    class Manager extends RequestHandler {

        public Manager() {
            super(new TypeSet(AddRequestHandler.class, RemoveRequestHandler.class));
        }

        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            manageRequests(requests);
        }
    }
}
