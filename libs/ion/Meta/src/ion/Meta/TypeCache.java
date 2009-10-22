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
package ion.Meta;

import java.util.HashMap;
import java.util.LinkedList;

final class TypeCache {

    // singleton
    public static final TypeCache instance = new TypeCache();
    private final HashMap<Class<?>, LinkedList<Class<?>>> parents;

    private TypeCache() {
        this.parents = new HashMap<Class<?>, LinkedList<Class<?>>>();
    }
    
    public LinkedList<Class<?>> getParents(Class<?> type) {
        // find or create a new parent branch for "type"
        LinkedList<Class<?>> parentBranch;
        if (!this.parents.containsKey(type)) {
            LinkedList<Class<?>> interfs = new LinkedList<Class<?>>();
            
            // create new branch
            parentBranch = new LinkedList<Class<?>>();
            this.parents.put(type, parentBranch);

            // add type
            parentBranch.add(type);

            // add base types
            if (!type.isInterface()) {
                Class<?> baseType = type.getSuperclass();
                while (baseType != null) {
                    parentBranch.add(baseType);
                    for (Class<?> interf : baseType.getInterfaces()) {
                        interfs.add(interf);
                    }
                    baseType = baseType.getSuperclass();
                }
            }

            // add interfaces
            for (Class<?> interfaceType : type.getInterfaces()) {
                interfs.add(interfaceType);
            }
            
            while(!interfs.isEmpty()){
                Class<?> interf = interfs.remove();
                parentBranch.add(interf);
                
                for (Class<?> superInterf : interf.getInterfaces()) {
                    interfs.add(superInterf);
                }
            }

        } else {
            parentBranch = this.parents.get(type);
        }

        // return the parent branch for "type"
        return parentBranch;
    }
}
