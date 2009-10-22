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
import java.util.Iterator;

public final class TypeSet implements Iterable<Class<?>> {

    private final HashSet<Class<?>> types;

    public TypeSet(Class<?>... types) {
        // check types
        if (types.length < 1) {
            throw new IllegalArgumentException("Failed to create types. At least one type is needed.");
        }

        this.types = new HashSet<Class<?>>();
        for (Class<?> type : types) {
            this.types.add(type);
        }
    }
    
    public boolean isSetOf(Class<?> baseType) {
        
         for (Class<?> type : this.types) {
            try {
                type.asSubclass(baseType);
            } catch (ClassCastException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return this.types.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {

        if (obj instanceof TypeSet) {
            return this.types.equals(((TypeSet) obj).types);
        } else {
            return super.equals(obj);
        }
    }
    
    public Iterator<Class<?>> iterator() {
        return this.types.iterator();
    }
    
}
