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


import ion.Meta.Element;
import ion.Meta.IReadOnlyQueue;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

public class HashDictionary<TKey, TValue> extends Element implements IDictionary<TKey,TValue> {

    private HashMap<TKey,TValue> dictionary;
    
    public HashDictionary() {
        this.getRequestHandlers().add(new DictionaryRequestHandler());
    }
    
    //<editor-fold defaultstate="collapsed" desc="Requests">
    
    protected abstract class ModifyElementRequest extends Request {

        public final TKey key;

        protected ModifyElementRequest(TKey key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object obj) {
            ModifyElementRequest request;
            
            if(obj instanceof HashDictionary.ModifyElementRequest){
                request = (ModifyElementRequest) obj;
            } else {
                return false;
            }
            
            return this.key.equals(request.key);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + (this.key != null ? this.key.hashCode() : 0);
            return hash;
        }
    }

    protected final class AddRequest extends ModifyElementRequest {

        public final TValue value;

        public AddRequest(TKey key, TValue value) {
            super(key);
            this.value = value;
        }
    }

    protected final class SetRequest extends ModifyElementRequest {

        public final TValue value;

        public SetRequest(TKey key, TValue value) {
            super(key);
            this.value = value;
        }
    }

    protected final class RemoveRequest extends ModifyElementRequest {

        public RemoveRequest(TKey key) {
            super(key);
        }
    }
    
    @Override
    public void onDestroy() {
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Request Handlers">
    
    /**
     * Handles every request made to modify this Dictionary.
     * Sets have maximum priority. I.E. All elements on setRequests are removed from the add and remove requests list.
     * 
     * @param requests
     */
    protected void handleModificationRequests(IReadOnlyQueueSet<Request> requests) {
        boolean hadElements = !this.getDictionary().isEmpty();

        HashSet<HashDictionary.ModifyElementRequest>[] requestsVerified = new HashSet[3];

        this.selectRequests(requests, requestsVerified);

        this.executeSet(requestsVerified[0]);
        this.executeAdd(requestsVerified[1]);
        this.executeRemove(requestsVerified[2]);


        if (hadElements && this.getDictionary().isEmpty()) {
            this.raise(new Cleared<HashDictionary>(this));
        }
    }
    
    /**
     * Changes the received collections by cleaning requests. It has the following policy:
     *  if an object is Set then it is not Added
     *  if an object is Set then it is not Removed
     *  if an object is Added then it is not Removed
     * 
     * @param allRequests
     * @param requests array where [0] contains set requests, [1] contains add requests and [2] contains remove requests
     */
    private void selectRequests(IReadOnlyQueueSet<Request> allRequests,
            HashSet<HashDictionary.ModifyElementRequest>[] requests) {
        
        HashSet<HashDictionary.ModifyElementRequest> setRequestsVerified = selectAndRemoveRepeated(allRequests, SetRequest.class);
        HashSet<HashDictionary.ModifyElementRequest> addRequestsVerified = selectAndRemoveRepeated(allRequests, AddRequest.class);
        HashSet<HashDictionary.ModifyElementRequest> removeRequestsVerified = selectAndRemoveRepeated(allRequests, RemoveRequest.class);

        for (HashDictionary.ModifyElementRequest setRequest : setRequestsVerified) {
            addRequestsVerified.remove(setRequest); // if an object is to be Set then it is not Added
            removeRequestsVerified.remove(setRequest); // if an object is to be Set then it is not Removed 
        }

        for (HashDictionary.ModifyElementRequest addRequest : addRequestsVerified) {
            if (!this.getDictionary().containsKey(addRequest.key)) {
                removeRequestsVerified.remove(addRequest); // if an object is to be Added and is not already
            // in the collection then it is not Removed
            }
        }

        requests[0] = setRequestsVerified;
        requests[1] = addRequestsVerified;
        requests[2] = removeRequestsVerified;
    }
    
    private static <TRequest extends HashDictionary.ModifyElementRequest> HashSet<HashDictionary.ModifyElementRequest>
            selectAndRemoveRepeated(IReadOnlyQueueSet<Request> allRequests, Class<TRequest> type) {
        
        HashSet<HashDictionary.ModifyElementRequest> requestsVerified;
        IReadOnlyQueue<TRequest> requestsTemp = allRequests.get(type);

        if (requestsTemp.count() > 0) {
            requestsVerified = new HashSet<HashDictionary.ModifyElementRequest>(requestsTemp.count());
            for (TRequest request : requestsTemp) {
                requestsVerified.add(request); // Removes requests with same key
            }
        } else {
            requestsVerified = new HashSet<HashDictionary.ModifyElementRequest>();
        }
        return requestsVerified;
    }
    
    /**
     * Assumes there are no duplicate requests. i.e. requests with the same key.
     * 
     * @param requests
     */
    private void executeSet(Iterable<HashDictionary.ModifyElementRequest> requests) {

        for (HashDictionary<TKey, TValue>.ModifyElementRequest request : requests) {
            SetRequest setRequest = (SetRequest) request;
            TValue oldValue;

            if (this.getDictionary().containsKey(request.key)) {
                oldValue = this.getDictionary().get(request.key);
                
                //When setting the same key/value do nothing
                if (oldValue.equals(setRequest.value)) {
                    continue;
                }

                this.raise(new Removed<TKey, TValue, HashDictionary<TKey, TValue>>(setRequest.key, oldValue, this));
            }

            this.getDictionary().put(setRequest.key, setRequest.value);
            this.raise(new Added<TKey, TValue, HashDictionary<TKey, TValue>>(setRequest.key, setRequest.value, this));
        }
    }
    
    /**
     * Assumes there are no duplicate requests. i.e. requests with the same key.
     * 
     * @param requests
     */
    private void executeAdd(Iterable<HashDictionary.ModifyElementRequest> requests) {

        for (HashDictionary<TKey, TValue>.ModifyElementRequest request : requests) {

            AddRequest addRequest = (AddRequest) request;
            TValue reqValue = addRequest.value;

            if (!this.getDictionary().containsKey(addRequest.key)) {

                this.getDictionary().put(addRequest.key, reqValue);
                this.raise(new Added<TKey, TValue, HashDictionary<TKey, TValue>>(addRequest.key, addRequest.value, this));
            }
        }
    }
    
    /**
     * Assumes there are no duplicate requests. i.e. requests with the same key.
     * 
     * @param requests
     */
    private void executeRemove(Iterable<HashDictionary.ModifyElementRequest> requests) {
        
        for (HashDictionary<TKey, TValue>.ModifyElementRequest request : requests) {

            RemoveRequest removeRequest = (RemoveRequest) request;
            TValue value;

            if (this.getDictionary().containsKey(removeRequest.key)) {
                value = this.getDictionary().remove(removeRequest.key);
                this.raise(new Removed<TKey, TValue, HashDictionary<TKey, TValue>>(removeRequest.key, value, this));
            }
        }
    }
    
    private class DictionaryRequestHandler extends RequestHandler{

        public DictionaryRequestHandler() {
            super(new TypeSet(AddRequest.class, RemoveRequest.class, SetRequest.class));
        }
        
        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            handleModificationRequests(requests);
        }
        
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="IDictionary<K,V> Members">
    
    //<editor-fold defaultstate="collapsed" desc="Modifiers">
    
    public void add(TKey key, TValue val) {
        this.schedule(new AddRequest(key, val));
    }
    
    public <U extends TKey, W extends TValue> void addAll(Iterable<Entry<U, W>> entries) {
        for (Entry<U, W> pair : entries) {
            this.schedule(new AddRequest(pair.getKey(), pair.getValue()));
        }
    }
    
    public void remove(TKey key) {
        this.schedule(new RemoveRequest(key));
    }
    
    public void removeAll(Iterable<TKey> keys) {
        for (TKey key : keys) {
            this.schedule(new RemoveRequest(key));
        }
    }
    
    public void removeAll() {
        for (TKey key : this.getDictionary().keySet()) {
            this.schedule(new RemoveRequest(key));
        }
    }
    
    //</editor-fold>
    
    public boolean contains(TKey key) {
        return this.getDictionary().containsKey(key);
    }

    public <H extends TKey> boolean containsAll(Iterable<H> keys) {
       
        for (H singleKey : keys) {
            if (!this.getDictionary().containsKey(singleKey)) {
                return false;
            }
        }
        return true;
    }
       
    public ICollectionValue<TKey> getKeys() {
        return new CollectionValueWrapper<TKey>(this.getDictionary().keySet());
    }

    public ICollectionValue<TValue> getValues() {
        return new CollectionValueWrapper<TValue>(this.getDictionary().values());
    }

    public TValue get(TKey key) {
        return this.getDictionary().get(key);

    }

    public void set(TKey key, TValue value) {
        this.schedule(new SetRequest(key, value));
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="ICollectionValue<KeyValuePair<K,V>> Members">
    
    public boolean contains(Entry<TKey, TValue> item) {
        
        if (this.getDictionary().containsKey(item.getKey())) {
            TValue value = this.getDictionary().get(item.getKey());
            return value.equals(item.getValue());
        }
        return false;
    }

    public int count() {
        return this.getDictionary().size();
    }
    
    public void copyTo(Entry<TKey, TValue>[] arr, int startIndex) {
        Set<Entry<TKey,TValue>> entries = this.getDictionary().entrySet();
        
        if ((arr.length - startIndex) < entries.size()) {
            throw new ArrayIndexOutOfBoundsException("Array does not have sufficient space starting from index " + startIndex);
        }

        Entry<TKey, TValue>[] nativeArr = entries.toArray(new Entry[entries.size()]);

        for (int i = startIndex, j = 0; j < nativeArr.length; i++, j++) {
            arr[i] = nativeArr[j];
        }
    }
    
    public Entry<TKey, TValue>[] toArray() {
        Set<Entry<TKey,TValue>> entries = this.getDictionary().entrySet();
        return entries.toArray(new Entry[entries.size()]);
        //return (Entry<TKey, TValue>[]) convertList.toArray();
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="IEnumerable<KeyValuePair<K,V>> Members">
    
    public Iterator<Entry<TKey, TValue>> iterator() {
        LinkedList<Entry<TKey, TValue>> convertList = new LinkedList<Entry<TKey, TValue>>();

        for (Entry<TKey, TValue> pair : this.getDictionary().entrySet()) {
            convertList.add(new SimpleEntry<TKey, TValue>(pair.getKey(), pair.getValue()));
        }

        return convertList.iterator();
    }
    
    //</editor-fold>
    
    protected HashMap<TKey, TValue> getDictionary() {

        if (this.dictionary == null) {
            this.dictionary = new HashMap<TKey, TValue>(); //lazy creation
        }
        return this.dictionary;
    }
}
