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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

final class CollectionValueWrapper<T> implements ICollectionValue<T> {
    
    private final Collection<T> collectionValue;
    
    public CollectionValueWrapper(Collection<T> collectionValue) {
        this.collectionValue = Collections.unmodifiableCollection(collectionValue);
    }

    //<editor-fold defaultstate="collapsed" desc="ICollectionValue<T> Members">
    
    public int count() {
        return this.collectionValue.size();
    }

    public void copyTo(T[] arr, int startIndex) {
        if (startIndex < 0 || arr.length < startIndex + this.collectionValue.size()) {
            throw new ArrayIndexOutOfBoundsException("Array does not have sufficient space starting from index " + startIndex);
        }
        
        Iterator<T> iter = this.collectionValue.iterator();
        for (int i = startIndex; iter.hasNext(); i++) {
            arr[i] = iter.next();
        }
    }

    public T[] toArray() {
        return (T[]) this.collectionValue.toArray();
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Iterable<T> Members">
    
    public Iterator<T> iterator() {
        return this.collectionValue.iterator();
    }
    
    //</editor-fold>
}
