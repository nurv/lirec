/** 
 * WorkingMemory.java - Implements a knowledge structure that stores properties and predicates about the
 * world for current processes (new data or data retrieved from the knowledge base. 
 * Provides a very fast method of searching for the information stored inside it. 
 * This structure also provides a limited kind of Logical Inference that can be
 * applied to generate new properties and predicates. This logical inference is 
 * performed by specific inference operators that must be explicitly added to the WM. 
 * When the WM buffer is full, data will be transfer to the KnowledgeBase (LTM) based 
 * on first in first out mechanism.
 * 
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: HWU
 * Project: LIREC
 * Created: 17/03/09 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 17/03/2009 - File created, a restructure of the previous KnowledgeBase
 *
 * **/


package FAtiMA.Core.memory.semanticMemory;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;


import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;



/**
 * Implements a knowledge structure that stores properties and predicates about the
 * world for current processes (new data or data retrieved from the knowledge base. 
 * Provides a very fast method of searching for the information stored inside it. 
 * This structure also provides a limited kind of Logical Inference that can be
 * applied to generate new properties and predicates. This logical inference is 
 * performed by specific inference operators that must be explicitly added to the WM. 
 * When the WM buffer is full, data will be transfer to the KnowledgeBase (LTM) based 
 * on first in first out mechanism.
 * 
 * @author Meiyii Lim
 */

public class WorkingMemory implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final short MAXENTRY = 28;
	

	private KnowledgeSlot _wM;
	private ArrayList<KnowledgeSlot> _factList;
	private boolean _newKnowledge;
	private ArrayList<KnowledgeSlot> _newFacts;
	private ArrayList<KnowledgeSlot> _changeList;

	/**
	 * Creates a new Empty WorkingMemory
	 */
	public WorkingMemory() {
		_wM = new KnowledgeSlot("WM");
		_factList = new ArrayList<KnowledgeSlot>(WorkingMemory.MAXENTRY);
		_newKnowledge = false;
		_newFacts = new ArrayList<KnowledgeSlot>();
		_changeList = new ArrayList<KnowledgeSlot>(WorkingMemory.MAXENTRY);
	}
	
	public Object Ask(Name name) {
		KnowledgeSlot aux = _wM;
		KnowledgeSlot currentSlot;
		ArrayList<Symbol> fetchList = name.GetLiteralList();
		ListIterator<Symbol> li = fetchList.listIterator();
		Symbol l;

		synchronized (this) {
			while (li.hasNext()) {
					currentSlot = aux;
					l = li.next();
					if (currentSlot.containsKey(l.toString())) {
						aux = currentSlot.get(l.toString());
					} 
					else return null;
			}
			return aux;
		}
	}
    
	/**
	 * Empties the WorkingMemory
	 *
	 */
	public void Clear() {
		synchronized (this) {
			this._wM.clear();
			this._factList.clear();
			this._newFacts.clear();
			this._newKnowledge = false;
			this._changeList.clear();
		}
	}
	
   public void ClearChangeList() {
	    _changeList.clear();
	}
   
   public int Count()
   {
	   return _wM.CountElements();
   }
    
    public ArrayList<KnowledgeSlot> GetChangeList() {
	    return _changeList;
	}
    
    public ArrayList<KnowledgeSlot> GetFactList() {
	    return _factList;
	}

	public ArrayList<KnowledgeSlot> GetNewFacts()
    {
		ArrayList<KnowledgeSlot> aux = this._newFacts;
		this._newFacts = new ArrayList<KnowledgeSlot>();
    	return aux;
    }

	 /*
	  * Called during loading
	  */
	public void putFact(KnowledgeSlot ks)
	{
		_factList.add(ks);
	}

	/**
     * Gets a value that indicates whether new Knowledge has been added to the WorkingMemory since
     * the last inference process
     * @return true if there is new Knowledge in the WM, false otherwise
     */
    public boolean HasNewKnowledge()
    {
    	return this._newKnowledge;
    }
	
	/**
	 * Rearrange the working memory entries so that the most current accessed entry comes last
	 */
	public void RearrangeWorkingMemory(Name predicate, Object value)
	{
		KnowledgeSlot ksNew = new KnowledgeSlot(predicate.toString());
		ksNew.setValue(value);
		KnowledgeSlot ks;
		ArrayList<KnowledgeSlot> tempFactList = new ArrayList<KnowledgeSlot>(_factList); 
		ListIterator<KnowledgeSlot> li = tempFactList.listIterator();
		synchronized (this) {
			while(li.hasNext())
			{
				ks = (KnowledgeSlot) li.next();
				if(ks.getName().equals(predicate.toString()))
				{
					_factList.remove(ks);
					_factList.add(ksNew);
					if(_changeList.contains(ks)) _changeList.remove(ks);
					_changeList.add(ksNew);
					return;
				}
			}
		}			
	}
	
	public void ResetNewKnowledge()
    {
    	this._newKnowledge = false;
    }
	
	/**
     * Removes a predicate from the WorkingMemory
	 * @param predicate - the predicate to be removed
	 */
	public void Retract(Name predicate) {
		
		KnowledgeSlot aux = _wM;
		KnowledgeSlot currentSlot = _wM;
		ArrayList<Symbol> fetchList = predicate.GetLiteralList();
		ListIterator<Symbol> li = fetchList.listIterator();
		ListIterator<KnowledgeSlot> li2;
		Symbol l = null;
			
		synchronized (this) {
			
			while (li.hasNext()) {
				currentSlot =  aux;
				l =  li.next();
				if (currentSlot.containsKey(l.toString())) {
					aux = currentSlot.get(l.toString());
				} else
					return;
			}
			if (aux.CountElements() > 0)
            {
                //this means that there are elements under the aux node, and thus 
                //we cannot delete it, just put it null
                aux.setValue(null);
            }
            else if (l != null)
            {
                currentSlot.remove(l.toString());
            }
			
			KnowledgeSlot ks;
			li2 = _factList.listIterator();
			while(li2.hasNext())
			{
				ks = li2.next();
				if(ks.getName().equals(predicate.toString()))
				{
					li2.remove();
					return;
				}
			}
		}
	}
	
	/**
	 * Adds a new property or sets its value (if already exists) in the WorkingMemory
	 * @param property - the property to be added/changed
	 * @param value - the value to be stored in the property
	 */
	public void Tell(KnowledgeBase kb, Name property, Object value) {

		boolean newProperty = false;
		KnowledgeSlot aux = _wM;
		KnowledgeSlot currentSlot = _wM;
		ArrayList<Symbol> fetchList = property.GetLiteralList();
		ListIterator<Symbol> li = fetchList.listIterator();
		Symbol l = null;		

		synchronized (this) {
			while (li.hasNext()) 
			{
				currentSlot = aux;
				l = li.next();
				if (currentSlot.containsKey(l.toString())) 
				{
					aux = currentSlot.get(l.toString());
				} 
				else 
				{
					newProperty = true;
					_newKnowledge = true;
					aux = new KnowledgeSlot(l.toString());
					currentSlot.put(l.toString(), aux);
				} 
			}
			
			if(aux.getValue() == null || 
					!aux.getValue().equals(value))
			{
				aux.setValue(value);
				_newKnowledge = true;
				//KnowledgeSlot ksAux = new KnowledgeSlot(property.toString());
				//ksAux.setValue(value);
				_newFacts.add(aux);
				
				//System.out.println("New facts: " + ksAux.toString());
			}
			
			if(newProperty)
			{	
				KnowledgeSlot ks = new KnowledgeSlot(property.toString());
				ks.setValue(value);
			 	_factList.add(ks);
				_changeList.add(ks); // new info
				_newFacts.add(ks);
				//System.out.println("New property knowledge: " + ks.toString());
			}
			else
			{
				
				this.RearrangeWorkingMemory(property,value);
			}
			
			if(_factList.size() > WorkingMemory.MAXENTRY)
			{
				KnowledgeSlot temp = (KnowledgeSlot) _factList.get(0);
				_factList.remove(0);
				
				Name tempName = Name.ParseName(temp.getName());
				ArrayList<Symbol> literals = tempName.GetLiteralList();
				li = literals.listIterator();
			
				aux = _wM;
				while (li.hasNext()) {
					currentSlot =  aux;
					l = (Symbol) li.next();
					if (currentSlot.containsKey(l.toString())) 
					{
						aux = currentSlot.get(l.toString());
					} 
					else return;
				}
			
				/*if (aux.CountElements() > 0)
	            {
	                //this means that there are elements under the aux node, and thus 
	                //we cannot delete it, just put it null
	                aux.setValue(null);
	            }
	            else if (l != null)
	            {
	                currentSlot.remove(l.toString());
	            }*/
				currentSlot.remove(l.toString());
				
				kb.Tell(tempName, temp.getValue());		
				//_changeList.remove(temp);
			}
		}
	}
	
	public ArrayList<SubstitutionSet> GetPossibleBindings(Name name) {
		ArrayList<SubstitutionSet> bindingSets = null;

		bindingSets = MatchLiteralList(name.GetLiteralList(), 0, _wM);
		
		if (bindingSets == null || bindingSets.size() == 0)
			return null;
		else
			return bindingSets;
	}
	
	public KnowledgeSlot GetObjectDetails(String objectName)
	{
		return _wM.get(objectName);
	}
	
	// Retrieving an object property value
	public KnowledgeSlot GetObjectProperty(String objectName, String property)
	{
		KnowledgeSlot object = _wM.get(objectName);
		if(object != null)
		{
			return object.get(property);
		}
		else return null;
	}
	
	private ArrayList<SubstitutionSet> MatchLiteralList(ArrayList<Symbol> literals, int index, KnowledgeSlot ks) {
		Symbol l;
		String key;
		ArrayList<SubstitutionSet> bindingSets;
		ArrayList<SubstitutionSet> newBindingSets;
		SubstitutionSet subSet;
		ListIterator<SubstitutionSet> li;
		Iterator<String> it;

		newBindingSets = new ArrayList<SubstitutionSet>();

		if (index >= literals.size()) {
			newBindingSets.add(new SubstitutionSet());
			return newBindingSets;
		}

		synchronized (this) {
			l = (Symbol) literals.get(index++);

			if (l.isGrounded()) {
				if (ks.containsKey(l.toString())) {
					return MatchLiteralList(literals, index, ks.get(l.toString()));
				} else
					return null;
			}

			it = ks.getKeyIterator();
			while (it.hasNext()) {
				key = (String) it.next();
				bindingSets = MatchLiteralList(literals, index, ks.get(key));
				if (bindingSets != null) {
					li = bindingSets.listIterator();
					while (li.hasNext()) {
						subSet = (SubstitutionSet) li.next();
						subSet.AddSubstitution(new Substitution(l, new Symbol(
								key)));
						newBindingSets.add(subSet);
					}
				}
			}
		}

		if (newBindingSets.size() == 0)
			return null;
		else
			return newBindingSets;
	}
	
//	public KnowledgeSlot getMainSlot()
//	{
//	  return _wM;
//	}
	
	/**
	 * Converts the Information stored in the WM to one String
	 * @return the converted String
	 */
	public String toString() {
	    return _wM.toString();
	}
	
	public String toXML()
	{
		KnowledgeSlot slot;
		String facts = "<WMFact>";
		for(ListIterator<KnowledgeSlot> li = _factList.listIterator();li.hasNext();)
		{
			slot = li.next();
			facts += slot.toXML();
		}
		facts += "</WMFact>\n";
		
		return facts;
	}
}
