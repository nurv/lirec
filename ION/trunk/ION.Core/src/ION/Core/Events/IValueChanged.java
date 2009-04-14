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
package ion.Core.Events;

import ion.Core.Property;
import ion.Meta.IEvent;

/**
 * Indicates that a Property has changed its value.
 * 
 * @author GAIPS
 * @param <TProperty> the type of the property which changed
 * @param <TValue>    the type of the value of the property which changed
 */
public interface IValueChanged<TOldValue, TNewValue, TProperty extends Property> extends IEvent {
    
    /**
     * @return the Property which value has changed
     */
    TProperty getProperty();

    /**
     * @return the value before the change occurred
     */
    TOldValue getOldValue();
    
    /**
     * @return the value after the change occurred
     */
    TNewValue getNewValue();
}
