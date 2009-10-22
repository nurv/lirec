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

import ion.Meta.Exceptions.TypeSetArgumentException;


public abstract class RequestHandler implements IHashed<TypeSet> {
    
    private final TypeSet type;
    
    protected RequestHandler(TypeSet type) {
        if (!type.isSetOf(Request.class)) {
            throw new TypeSetArgumentException("Failed to create RequestHandler. At least one type of the TypeSet in not a subclass of Request.", "type", type);
        }
        this.type = type;
    }
    
    public TypeSet getType(){
        return this.type;
    }
    
    public abstract void invoke(IReadOnlyQueueSet<Request> requests);
}
