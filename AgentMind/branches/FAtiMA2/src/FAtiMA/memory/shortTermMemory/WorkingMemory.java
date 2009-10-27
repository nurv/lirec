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


package FAtiMA.memory.shortTermMemory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import FAtiMA.conditions.Condition;
import FAtiMA.deliberativeLayer.plan.Effect;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.ApplicationLogger;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Symbol;
import FAtiMA.knowledgeBase.*;
import FAtiMA.memory.ActionDetail;
import FAtiMA.memory.KnowledgeSlot;


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
	
	/**
	 * Singleton pattern 
	 */
	private static WorkingMemory _wmInstance = null;
	
	/**
	 * Gets the Agent's WorkingMemory
	 * @return the WorkingMemory
	 */
	public static WorkingMemory GetInstance()
	{
		if(_wmInstance == null)
		{
			_wmInstance = new WorkingMemory();
		}
		return _wmInstance;
	}
	
	/**
	 * Saves the state of the current WorkingMemory to a file,
	 * so that it can be later restored from file
	 * @param fileName - the name of the file where we must write
	 * 		             the WorkingMemory
	 */
	public static void SaveState(String fileName)
	{
		synchronized(_wmInstance)
		{
			try 		
			{
				FileOutputStream out = new FileOutputStream(fileName);
		    	ObjectOutputStream s = new ObjectOutputStream(out);
		    	
		    	s.writeObject(_wmInstance);
	        	s.flush();
	        	s.close();
	        	out.close();
			}
			catch(Exception e)
			{
				AgentLogger.GetInstance().logAndPrint("Exception: " + e);
				ApplicationLogger.Write(e.getMessage());
			}
		}
	}
	
	/**
	 * Loads a specific state of the WorkingMemory from a previously
	 * saved file
	 * @param fileName - the name of the file that contains the stored
	 * 					 WorkingMemory
	 */
	public static void LoadState(String fileName)
	{	
		try
		{
			FileInputStream in = new FileInputStream(fileName);
        	ObjectInputStream s = new ObjectInputStream(in);
        	_wmInstance = (WorkingMemory) s.readObject();
        	s.close();
        	in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	private KnowledgeSlot _wM;
	private ArrayList _factList;
	private boolean _newKnowledge;
	private ArrayList _newFacts;
	private ArrayList _changeList;

	/**
	 * Creates a new Empty WorkingMemory
	 */
	private WorkingMemory() {
		_wM = new KnowledgeSlot("WM");
		_factList = new ArrayList(WorkingMemory.MAXENTRY);
		_newKnowledge = false;
		_newFacts = new ArrayList();
		_changeList = new ArrayList(WorkingMemory.MAXENTRY);
	}
    
	/**
	 * Gets the working memory slots
	 * @return the working memory slots
	 */
	// Added 18/03/09
	public KnowledgeSlot GetWorkingMemory()
	{
		return _wM;
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
    
    public ArrayList GetNewFacts()
    {
    	return this._newFacts;
    }
    
    /**
     *  This method should be called every simulation cycle, and will try to apply InferenceOperators.
     * 	Note, that if new knowledge results from this process, it will be added immediately to the
     *  WM. However, the inference will not continue (by trying to use the new knowledge to activate
     *  more operators) until the method PerformInference is called in next cycle.
     * 
     * @return true if the Inference resulted in new Knowledge being added, false if no
     * new knowledge was inferred
     */
    public boolean PerformInference()
    {
    	Step infOp;
    	Step groundInfOp;
    	ArrayList substitutionSets;
    	SubstitutionSet sSet;
    	
    	_newKnowledge = false;
    	_newFacts.clear();
    	
		for(ListIterator li = KnowledgeBase.GetInstance().GetInferenceOperators().listIterator();li.hasNext();)
		{
			infOp = (Step) li.next();
			substitutionSets = Condition.CheckActivation(infOp.getPreconditions());
			if(substitutionSets != null)
			{
				for(ListIterator li2 = substitutionSets.listIterator();li2.hasNext();)
				{
					sSet = (SubstitutionSet) li2.next();
					groundInfOp = (Step) infOp.clone();
					groundInfOp.MakeGround(sSet.GetSubstitutions());
					InferEffects(groundInfOp);
				}
			}
		}
    	
    	return _newKnowledge;
    }
    
    private void InferEffects(Step infOp)
    {
    	Effect eff;
    	for(ListIterator li = infOp.getEffects().listIterator();li.hasNext();)
    	{
    		eff = (Effect) li.next();
    		if(eff.isGrounded())
    		{
    			Tell(eff.GetEffect().getName(),eff.GetEffect().GetValue().toString());
    			//System.out.println("InferEffects");    			
    		}
    	}
    }
    
	/**
	 * Inserts a Predicate in the WorkingMemory
	 * @param predicate - the predicate to be inserted
	 */
	public void Assert(Name predicate) {
		this.Tell(predicate,new Symbol("True"));
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

	/**
     * Removes a predicate from the WorkingMemory
	 * @param predicate - the predicate to be removed
	 */
	public void Retract(Name predicate) {
		
		KnowledgeSlot aux = _wM;
		KnowledgeSlot currentSlot = _wM;
		ArrayList fetchList = predicate.GetLiteralList();
		ListIterator li = fetchList.listIterator();
		Symbol l = null;
			
		synchronized (this) {
			
			while (li.hasNext()) {
				currentSlot =  aux;
				l = (Symbol) li.next();
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
			li = _factList.listIterator();
			while(li.hasNext())
			{
				ks = (KnowledgeSlot) li.next();
				if(ks.getName().equals(predicate.toString()))
				{
					li.remove();
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
	public void Tell(Name property, Object value) {

		boolean newProperty = false;
		KnowledgeSlot aux = _wM;
		KnowledgeSlot currentSlot = _wM;
		ArrayList fetchList = property.GetLiteralList();
		ListIterator li = fetchList.listIterator();
		Symbol l = null;		

		synchronized (this) {
			while (li.hasNext()) {
				currentSlot = aux;
				l = (Symbol) li.next();
				if (currentSlot.containsKey(l.toString())) {
					aux = currentSlot.get(l.toString());
				} else {
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
				KnowledgeSlot ksAux = new KnowledgeSlot(property.toString());
				ksAux.setValue(value);
				_newFacts.add(ksAux);
				
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
				KnowledgeSlot ks;
				li = _factList.listIterator();
				while(li.hasNext())
				{
					ks = (KnowledgeSlot) li.next();
					if(ks.getName().equals(property.toString()))
					{
						ks.setValue(value);
						//if(!_changeList.contains(ks))
						//	_changeList.add(ks);
						//System.out.println("New property value: " + ks.toString());
					} 
				}
				this.RearrangeWorkingMemory(property);
			}
			
			if(_factList.size() > WorkingMemory.MAXENTRY)
			{
				KnowledgeSlot temp = (KnowledgeSlot) _factList.get(0);
				
				Name tempName = Name.ParseName(temp.getName());
				ArrayList literals = tempName.GetLiteralList();
				li = literals.listIterator();
			
				aux = _wM;
				while (li.hasNext()) {
					currentSlot =  aux;
					l = (Symbol) li.next();
					if (currentSlot.containsKey(l.toString())) {
						aux = currentSlot.get(l.toString());
					} else
						return;
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
				
				KnowledgeBase.GetInstance().Tell(tempName, temp.getValue());
				_factList.remove(temp);		
				_changeList.remove(temp);
			}
		}
	}
	
	/**
	 * Rearrange the working memory entries so that the most current accessed entry comes last
	 */
	public void RearrangeWorkingMemory(Name predicate)
	{
		KnowledgeSlot ks;
		ArrayList tempFactList = (ArrayList) _factList.clone();
		ListIterator li = tempFactList.listIterator();
		synchronized (this) {
			while(li.hasNext())
			{
				ks = (KnowledgeSlot) li.next();
				if(ks.getName().equals(predicate.toString()))
				{
					_factList.remove(ks);
					_factList.add(ks);
					//if(!_changeList.contains(ks))
					//	_changeList.add(ks);
					return;
				}
			}
		}			
	}
	
	public ListIterator GetFactList() {
	    return _factList.listIterator();
	}
	
	public ArrayList GetChangeList() {
	    return _changeList;
	}
	
	public void ClearChangeList() {
	    _changeList.clear();
	}
	
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
		String facts = "<Fact>";
		for(ListIterator li = _factList.listIterator();li.hasNext();)
		{
			slot = (KnowledgeSlot) li.next();
			facts += slot.toString();
		}
		facts += "</Fact>\n";
		
		return facts;
	}
}
