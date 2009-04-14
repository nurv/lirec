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

import java.util.Map.Entry;

public interface IDictionary<K,V> extends ICollectionValue<Entry<K,V>> {

    boolean contains(K key);
    <H extends K> boolean containsAll(Iterable<H> keys);

    void add(K key, V val);
    <U extends K, W extends V> void addAll(Iterable<Entry<U, W>> entries);

    void remove(K key);
    void removeAll(Iterable<K> keys);
    void removeAll();

    ICollectionValue<K> getKeys();
    ICollectionValue<V> getValues();

    V get(K key);
    void set(K key, V value);
}
