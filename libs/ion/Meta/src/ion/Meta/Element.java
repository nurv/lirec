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

import java.util.HashSet;
import java.util.LinkedList;

public abstract class Element {

    private final long uid;
    private final Class<?> evtType;
    private boolean wasDestroyed;
    private Simulation simulation;
    private RequestHandlerCollection requestHandlers;
    private EventHandlerCollection eventHandlers;
    private RequestFilterCollection requestFilters;
    private EventFilterCollection eventFilters;
    private LinkedList<Request> requests;
    private int requestsCount;
    private LinkedList<Request> metaRequests;
    private int metaRequestsCount;
    private LinkedList<Event> events;

    Element(long uid) {
        this.evtType = this.getType();
        this.uid = uid;
        this.simulation = null;
        this.wasDestroyed = false;

        // setup request handler collection
        this.requestHandlers = new RequestHandlerCollection(this);

        // setup request filter collection
        this.requestFilters = new RequestFilterCollection(this);

        // setup event handler collection
        this.eventHandlers = new EventHandlerCollection(this);

        // setup event filter collection
        this.eventFilters = new EventFilterCollection(this);

        // setup add/remove handler for request handlers
        this.requestHandlers.executeAdd(this.requestHandlers.new Manager());

        // setup add/remove handler for request filters
        this.requestHandlers.executeAdd(this.requestFilters.new Manager());

        // setup add/remove handler for event handlers
        this.requestHandlers.executeAdd(this.eventHandlers.new Manager());

        // setup add/remove handler for event filters
        this.requestHandlers.executeAdd(this.eventFilters.new Manager());

        // setup requests queue
        this.requests = new LinkedList<Request>();
        this.requestsCount = 0;
        this.metaRequests = new LinkedList<Request>();
        this.metaRequestsCount = 0;

        // setup events queue
        this.events = new LinkedList<Event>();
    }

    protected Element(){
        this(Simulator.generateUID());
    }
    
    public void destroy() {
        if (this.simulation == null) {
            this.wasDestroyed = true;
            this.requestHandlers = null;
            this.requestFilters = null;
            this.eventHandlers = null;
            this.eventFilters = null;
            this.requests = null;
            this.metaRequests = null;
            this.events = null;

            this.onDestroy();
        }
    }

    public abstract void onDestroy();

    public long getUID(){
        return this.uid;
    }
    
    public boolean wasDestroyed(){
        return this.wasDestroyed;
    }

    Class<?> getType(){
        return this.evtType;
    }
    
    public Simulation getSimulation(){
        return this.simulation;
    }
    
    void setSimulation(Simulation simulation){
        this.simulation = simulation;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Requests">
    
    public IRequestHandlerCollection getRequestHandlers(){
        return this.requestHandlers;
    }
    
    public IRequestFilterCollection getRequestFilters() {
        return this.requestFilters;
    }
    
    public void schedule(Request request) {
        
        if (this.wasDestroyed) {
            return;
        }

        if (this.simulation != null) {
        	
        	synchronized(this.simulation)
        	{
        		// add time stamp
        		request.setScheduleTime(this.simulation.getTime());

            	if (request instanceof MetaRequest) {
                	this.metaRequests.add(request);
            	} 	else {
            		this.requests.add(request);
            	}

            	this.simulation.synchronizeProcessRequests(this);
        	}
        } else {
            // process immediatly (not synchronized)
            this.process(request);
        }
    }
    
    private void process(Request request) {
        if (!this.blocks(request)) {
            this.invokeHandlers(request);
        }
    }
    
    void lockRequests() {
        // lock the number of pending requests before processing
        // (new requests scheduled during processing will be processed in the next update)
        this.requestsCount = this.requests.size();
        this.metaRequestsCount = this.metaRequests.size();
    }
    
    void processRequests() {
        Request request;
        HashSet<RequestNode> requestNodes = new HashSet<RequestNode>();

        // process requests
        while (this.requestsCount-- > 0) {
            request = this.requests.remove();
            
            if (!this.blocks(request)) {
                RequestNode node;
                
                if (this.requestHandlers.getNodes().containsKey(request.getClass())) {
                    node = this.requestHandlers.getNodes().get(request.getClass());
                    node.enqueue(request);
                    requestNodes.add(node);
                }
            }
        }

        // process request nodes
        if (requestNodes.size() > 0) {
            HashSet<RequestHandlerInvoker> invokers = new HashSet<RequestHandlerInvoker>();
            
            for (RequestNode<Request> node : requestNodes) {
                for (RequestHandlerInvoker invoker : node.getInvokers()) {
                    invokers.add(invoker);
                }
            }
            
            for (RequestHandlerInvoker invoker : invokers) {
                invoker.process();
            }
            
            for (RequestNode node : requestNodes) {
                node.clearQueue();
            }
        }
    }
    
    void processMetaRequests() {
        Request request;
        HashSet<RequestNode> metaRequestNodes = new HashSet<RequestNode>();

        // process meta requests
        while (this.metaRequestsCount-- > 0) {
            request = this.metaRequests.remove();

            if (!this.blocks(request)) {
                RequestNode node;

                if (this.requestHandlers.getNodes().containsKey(request.getClass())) {
                    node = this.requestHandlers.getNodes().get(request.getClass());
                    node.enqueue(request);
                    metaRequestNodes.add(node);
                }
            }
        }

        // process meta request nodes
        if (metaRequestNodes.size() > 0) {
            HashSet<RequestHandlerInvoker> metaInvokers = new HashSet<RequestHandlerInvoker>();
            for (RequestNode<Request> node : metaRequestNodes) {
                for (RequestHandlerInvoker invoker : node.getInvokers()) {
                    metaInvokers.add(invoker);
                }
            }

            for (RequestHandlerInvoker invoker : metaInvokers) {
                invoker.process();
            }

            for (RequestNode node : metaRequestNodes) {
                node.clearQueue();
            }
        }
    }
    
    private boolean blocks(Request request) {
        
        for (RequestFilter filter : this.requestFilters.get(request.getClass())) {
            if (filter.blocks(request)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void invokeHandlers(Request request) {
        RequestNode<Request> node;
        
        if (this.requestHandlers.getNodes().containsKey(request.getClass())) {
            node = this.requestHandlers.getNodes().get(request.getClass());
            node.enqueue(request);
            for (RequestHandlerInvoker invoker : node.getInvokers()) {
                invoker.process();
            }
            node.clearQueue();
        }
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Events">
    
    public IEventHandlerCollection getEventHandlers(){
        return this.eventHandlers;
    }

    public IEventFilterCollection getEventFilters() {
        return this.eventFilters;
    }
    
    public void raise(Event evt) {
        
        if (this.wasDestroyed) {
            return;
        }

        if (evt.getTrail().add(this)) {
            if (this.simulation != null) {
                
            	synchronized(this.simulation)
            	{
            		// add time stamp
            		evt.setRaiseTime(this.simulation.getTime());

                	// enqueue event
                	this.events.add(evt);

                	// synchronize with the simulation
                	this.simulation.synchronizeProcessEvents(this);

                	// propagate to the simulation
                	this.simulation.raise(evt);
            	}
            } else {
                // process event immediatly (not synchronized)
                this.process(evt);
            }
        }
    }
    
    private void process(Event evt) {
        if (!this.blocks(evt)) {
            this.invokeHandlers(evt);
        }
    }
    
    void processEvents() {
        while (this.events.size() > 0) {
            this.process(this.events.remove());
        }
    }
    
    private boolean blocks(Event evt) {
        return this.blocks(evt, new HashSet<Class<?>>());
    }
    
    private boolean blocks(Event evt, HashSet<Class<?>> previousTypes) {
        
        for (Class<?> evtType : evt.getTypes()) {
            
            if (previousTypes.add(evtType)) {
                for (EventFilter filter : this.eventFilters.get(evtType)) {
                    
                    if (filter.blocks(evt)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void invokeHandlers(Event evt) {
        this.invokeHandlers(evt, new HashSet<Class<?>>());
    }

    private void invokeHandlers(Event evt, HashSet<Class<?>> previousTypes) {
        
        for (Class<?> evtType : evt.getTypes()) {
            
            if (previousTypes.add(evtType)) {
                for (EventHandler handler : this.eventHandlers.get(evtType)) {
                    handler.invoke(evt);
                }
            }
        }
    }
    
    //</editor-fold>
}
