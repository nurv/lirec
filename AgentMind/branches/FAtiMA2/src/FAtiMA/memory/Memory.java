/** 
 * Memory.java - Performs operations that involve data from different memories - currently
 * 				AM and STM
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
 * Created: 13/03/09 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 13/03/2009 - File created
 * 
 * **/

package FAtiMA.memory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import FAtiMA.deliberativeLayer.goals.Goal;
import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.memory.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.memory.shortTermMemory.ShortTermMemory;
import FAtiMA.memory.shortTermMemory.WorkingMemory;
import FAtiMA.sensorEffector.Event;
import FAtiMA.sensorEffector.Parameter;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Symbol;


/**
 * Performs operations that involve data from different memories - currently
 * AM and STM
 * 
 * @author Meiyii Lim
 */

public class Memory {

	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	private AutobiographicalMemory _am;
	private ShortTermMemory _stm;
	private WorkingMemory _wm;
	private KnowledgeBase _kb;
	
	public static ArrayList GenerateSearchKeys(Event e)
	{	
		ArrayList keys = new ArrayList();
		ArrayList params = new ArrayList();
		Parameter param;
		
		keys.add(new SearchKey(SearchKey.SUBJECT,e.GetSubject()));
		
		keys.add(new SearchKey(SearchKey.ACTION,e.GetAction()));
		
		if(e.GetTarget() != null)
		{
			keys.add(new SearchKey(SearchKey.TARGET, e.GetTarget()));
		}
		
		if(e.GetParameters().size() > 0)
		{
			for(ListIterator li = e.GetParameters().listIterator();li.hasNext();)
			{
				param = (Parameter) li.next();
				params.add(param.GetValue().toString());
			}
			keys.add(new SearchKey(SearchKey.PARAMETERS, params));
		}
		
		return keys;
	}	
	
	public Memory()
	{
		_am = new AutobiographicalMemory();
		_stm = new ShortTermMemory();
		_wm = new WorkingMemory();
		_kb = new KnowledgeBase();
	}
	
	public Float AssessGoalProbability(Goal g)
	{
		int numberOfSuccess;
		int numberOfTries;
		ArrayList searchKeys = GenerateSearchKeys(g.GetActivationEvent());
		
		numberOfTries = _am.CountEvent(searchKeys) 
						+ _stm.CountEvent(searchKeys);
		if(numberOfTries == 0)
		{
			return null;
		}
		
		searchKeys = GenerateSearchKeys(g.GetSuccessEvent());
		numberOfSuccess = _am.CountEvent(searchKeys) 
							+ _stm.CountEvent(searchKeys);			
		return new Float(numberOfSuccess/numberOfTries);
	}
	
	public float AssessGoalFamiliarity(Goal g)
	{
		float similarEvents = 0;
		float familiarity = 0;
		
		similarEvents = _am.AssessGoalFamiliarity(g)
						+ _stm.AssessGoalFamiliarity(g);
		
		// familiarity function f(x) = 1 - 1/(x/2 +1)
		// where x represents the number of similar events founds
		familiarity = 1 - (1 / (similarEvents/2 + 1));
		
		return familiarity;
	}
	
	/**
	 * This method provides a way to search for properties/predicates in the WorkingMemory 
     * that match with a specified name with unbound variables.
     *
     * In order to understand this method, let’s examine the following example. Suppose that 
     * the memory only contains properties about two characters: Luke and John.
     * Furthermore, it only stores two properties: their name and strength. So the KB will 
     * only store the following objects:
     * - Luke(Name) : Luke
     * - Luke(Strength) : 8
     * - John(Name) : John 
     * - John(Strength) : 4
     * 
     * The next table shows the result of calling the method with several distinct names. 
     * The function works by finding substitutions for the unbound variables, which make 
     * the received name equal to the name of an object stored in memory.
     * 
     * Name	        Substitutions returned
     * Luke([x])	    {{[x]/Name},{[x]/Strength}}
     * [x](Strength)    {{[x]/John},{[x]/Luke}}
     * [x]([y])	        {{[x]/John,[y]/Name},{[x]/John,[y]/Strength},{[x]/Luke,[y]/Name},{[x]/Luke,[y]/Strength}}
     * John(Name)	    {{}}
     * John(Height)	    null
     * Paul([x])	    null
     *
     * In the first example, there are two possible substitutions that make “Luke([x])”
     * equal to the objects stored above. The third example has two unbound variables,
     * so the returned set contains all possible combinations of variable attributions.
     * 
     * If this method receives a ground name, as seen on examples 4 and 5, it checks
     * if the received name exists in memory. If so, a set with the empty substitution is
     * returned, i.e. the empty substitution makes the received name equal to some object
     * in memory. Otherwise, the function returns null, i.e. there is no substitution
     * that applied to the name will make it equal to an object in memory. This same result
     * is returned in the last example, since there is no object named Paul, and therefore no 
     * substitution of [x] will match the received name with an existing object.
	 * @param name - a name (that correspond to a predicate or property)
	 * @return a list of SubstitutionSets that make the received name to match predicates or 
     *         properties that do exist in the WorkingMemory
	 */
	public ArrayList GetPossibleBindings(Name name) {
		ArrayList bindingSets = null;
		
		bindingSets = MatchLiteralList(name.GetLiteralList(), 0, _wm.GetWorkingMemory());
		
		if (bindingSets == null || bindingSets.size() == 0)
			bindingSets = (MatchLiteralList(name.GetLiteralList(), 0, _kb.GetKnowledgeBase()));
		else
		{
			ArrayList bindingSets2 = MatchLiteralList(name.GetLiteralList(), 0, _kb.GetKnowledgeBase());
			if (bindingSets2 != null)
			{
				ListIterator li = bindingSets2.listIterator();

				synchronized (this) {
					while (li.hasNext()) {
						
						SubstitutionSet ss = (SubstitutionSet) li.next();
						if( !bindingSets.contains(ss) )
							bindingSets.add(ss);
					}
				}
			}
		}
		
		return bindingSets;
	}
	
	 /**
     * Asks the Memory the Truth value of the received predicate
     * @param predicate - The predicate to search in the Memory
     * @return Under the Closed World Assumption, the predicate is considered 
     * true if it is stored in the Memory and false otherwise.
     */
    
	public boolean AskPredicate(Name predicate) 
	{
        KnowledgeSlot ks = (KnowledgeSlot) Ask(predicate, _wm.GetWorkingMemory());
        if (ks != null && ks.getValue() != null && ks.getValue().toString().equals("True"))
        {
        	_wm.RearrangeWorkingMemory(predicate);
            return true;
        }
        else
        {
        	ks= (KnowledgeSlot) Ask(predicate, _kb.GetKnowledgeBase());
        	if (ks != null && ks.getValue() != null && ks.getValue().toString().equals("True"))
            {
        		_wm.Tell(this,predicate, ks.getValue());
                return true;
            }
        }
        return false;
	}
	
	/**
	 * Asks the Memory the value of a given property
	 * @param property - the property to search in the Memory
	 * @return the value stored inside the property, if the property exists. If the 
     *         property does not exist, it returns null
	 */
	public Object AskProperty(Name property) {
		KnowledgeSlot prop = (KnowledgeSlot) Ask(property, _wm.GetWorkingMemory());
		if (prop == null)
		{
			prop = (KnowledgeSlot) Ask(property, _kb.GetKnowledgeBase());
			if (prop == null)
				return null;
			else
				_wm.Tell(this, property, prop.getValue());
		}
		else
		{
			_wm.RearrangeWorkingMemory(property);
		}
		return prop.getValue();
	}
	
	private Object Ask(Name name, KnowledgeSlot slots) {
		KnowledgeSlot aux = slots;
		KnowledgeSlot currentSlot;
		ArrayList fetchList = name.GetLiteralList();
		ListIterator li = fetchList.listIterator();
		Symbol l;

		synchronized (this) {
			while (li.hasNext()) {
					currentSlot = aux;
					l = (Symbol) li.next();
					if (currentSlot.containsKey(l.toString())) {
						aux = currentSlot.get(l.toString());
					} 
					else return null;
			}
			return aux;
		}
	}

	private ArrayList MatchLiteralList(ArrayList literals, int index, KnowledgeSlot kSlot) {
		Symbol l;
		String key;
		ArrayList bindingSets;
		ArrayList newBindingSets;
		SubstitutionSet subSet;
		ListIterator li;
		Iterator it;

		newBindingSets = new ArrayList();

		if (index >= literals.size()) {
			newBindingSets.add(new SubstitutionSet());
			return newBindingSets;
		}

		synchronized (this) {
			l = (Symbol) literals.get(index++);

			if (l.isGrounded()) {
				if (kSlot.containsKey(l.toString())) {
					return MatchLiteralList(literals, index, kSlot.get(l
							.toString()));
				} else
					return null;
			}

			it = kSlot.getKeyIterator();
			while (it.hasNext()) {
				key = (String) it.next();
				bindingSets = MatchLiteralList(literals, index, kSlot
						.get(key));
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
	
	/**
     * Removes a predicate from the Semantic Memory
	 * @param predicate - the predicate to be removed
	 */
	public void Retract(Name predicate) 
	{
		_kb.Retract(predicate);
		_wm.Retract(predicate);
	}
	
	public KnowledgeSlot GetObjectDetails(String objectName)
	{
		KnowledgeSlot object = (_wm.GetWorkingMemory()).get(objectName);
		if(object == null)
			object = (_kb.GetKnowledgeBase()).get(objectName);
		return object;
	}
	
	public KnowledgeBase getKB()
	{
		return _kb;
	}
	
	public AutobiographicalMemory getAM()
	{
		return _am;
	}
	
	public ShortTermMemory getSTM()
	{
		return _stm;
	}
	
	public WorkingMemory getWM()
	{
		return _wm;
	}
	
}
