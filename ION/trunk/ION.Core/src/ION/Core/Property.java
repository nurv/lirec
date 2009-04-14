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
package ion.Core;

import ion.Core.Events.ValueChanged;
import ion.Meta.Element;
import ion.Meta.Event;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

/**
 * A Property is an Element that represents an attribute of a specific type.
 * 
 * @author GAIPS
 * @param <TValue> the specific type of the attribute
 */
public class Property<TValue> extends Element {
    
    /**
     * Direcly changes the value of the Property without regarding the synchronization cycle.
     * Only change this value if you know what you are doing!!!
     */
    TValue value;

    /**
     * Creates a Property with the default value null.
     */
    public Property() {
        //Exception is assured to never be thrown when creating a SetValueHandler
        this.getRequestHandlers().add(new PropertyRequestHandler());
        this.value = null;
    }
    
    /**
     * Creates a Property with a particular initial value.
     * 
     * @param value
     */
    public Property(TValue value){
        this();
        this.value = value;
    }
    
    //<editor-fold defaultstate="collapsed" desc="Requests">
    protected final class SetValue extends Request {

        public final TValue newValue;

        public SetValue(TValue newValue) {
            this.newValue = newValue;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Request Handlers">
    
    /**
     * Change value policy that chooses the first request set value.
     * 
     * @param requests the set of request queues to be processed
     */
    protected void changeValueChooseFirst(IReadOnlyQueueSet<Request> requests) {
        
        for (SetValue request : requests.get(SetValue.class)) {
            //Use reflection to instantiate the appropriate event type
            //object[] arguments = { this, this.value };
            Event evt = new ValueChanged<TValue, TValue, Property>(this.value, request.newValue, this);

            this.value = request.newValue;
            this.raise(evt);

            return;
        }
    }
    
    protected final class PropertyRequestHandler extends RequestHandler{

        public PropertyRequestHandler(){
            super(new TypeSet(SetValue.class));
        }

        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            changeValueChooseFirst(requests);
        }
    }
    
    //</editor-fold>
    
    /**
     * Gets the Value of the Property.
     * 
     * @return the current Value of the Property
     */
    public TValue getValue(){
        return this.value;
    }
    
    /**
     * Sets the value of the property.
     * It does not set the value directly, it schedules a request to change the value.
     * 
     * @param value the value to be set
     */
    public void setValue(TValue value){
        this.schedule(new SetValue(value));
    }

    @Override
    public void onDestroy() {
    }
}
