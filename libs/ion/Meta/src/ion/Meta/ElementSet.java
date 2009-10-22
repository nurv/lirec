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

final class ElementSet implements IElementSet {

    private final Simulation simulation;
    private final HashMap<Long, Element> elements;

    public ElementSet(Simulation simulation) {
        this.simulation = simulation;
        this.elements = new HashMap<Long, Element>();

        // setup requests manager (add, remove)
        this.simulation.getRequestHandlers().add(new ElementSetRequestHandler());
    }
    
    public void add(Element element) {
        this.simulation.schedule(new AddElement(element, this.simulation));
    }
    
    private void executeAdd(Element element) {
        // add item
        this.elements.put(element.getUID(), element);
        element.setSimulation(this.simulation);

        AddedElement evt = new AddedElement(element, this.simulation);
        this.simulation.raise(evt);
        element.raise(evt);
    }
    
    public void remove(Element element) {
        this.simulation.schedule(new RemoveElement(element, this.simulation));
    }

    private void executeRemove(Element element) {
        // add item
        this.elements.remove(element.getUID());
        element.setSimulation(null);

        RemovedElement evt = new RemovedElement(element, this.simulation);
        this.simulation.raise(evt);
        element.raise(evt);
    }
    
    public boolean contains(Element element) {
        return this.elements.containsKey(element.getUID());
    }

    public Element get(long uid) {
        return this.elements.get(uid);
    }

    public Iterable<Long> getUIDs() {
        return this.elements.keySet();
    }

    public Iterator<Element> iterator() {
        return this.elements.values().iterator();
    }
    
    private void manager(IReadOnlyQueueSet<Request> requests) {
        HashSet<Element> elementsToRemove = new HashSet<Element>();
        HashSet<Element> elementsToAdd = new HashSet<Element>();

        // validate items to remove
        for (RemoveElement request : requests.get(RemoveElement.class)) {
            if (request.item.getSimulation() == this.simulation) {
                elementsToRemove.add(request.item);
            }
        }

        // validate items to add
        for (AddElement request : requests.get(AddElement.class)) {
            if (!request.item.wasDestroyed() && request.item.getSimulation() == null) {
                elementsToAdd.add(request.item);
            }
        }

        // remove items
        for (Element element : elementsToRemove) {
            this.executeRemove(element);
        }

        // add items
        for (Element element : elementsToAdd) {
            this.executeAdd(element);
        }
    }
        
    private final class ElementSetRequestHandler extends RequestHandler {

        public ElementSetRequestHandler(){
            super(new TypeSet(AddElement.class, RemoveElement.class));
        }

        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            manager(requests);
        }
    }
}
