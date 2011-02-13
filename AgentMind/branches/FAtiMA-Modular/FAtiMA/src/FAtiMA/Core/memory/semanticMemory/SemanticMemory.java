package FAtiMA.Core.memory.semanticMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;

public class SemanticMemory implements Serializable {
	
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	private KnowledgeBase _kb;
	private WorkingMemory _stm;
	
	public SemanticMemory()
	{
		_kb = new KnowledgeBase();
		_stm = new WorkingMemory();
	}
	
	public void AddInferenceOperator(Step op)
	{
		synchronized (this) {
			_kb.AddInferenceOperator(op);
		}
	}
	 
	
	/**
     * Asks the Memory the Truth value of the received predicate
     * @param predicate - The predicate to search in the Memory
     * @return Under the Closed World Assumption, the predicate is considered 
     * true if it is stored in the Memory and false otherwise.
     */
    
	public boolean AskPredicate(Name predicate) 
	{
		synchronized (this) {
			KnowledgeSlot ks = (KnowledgeSlot) _stm.Ask(predicate);
	        if (ks != null && ks.getValue() != null && ks.getValue().toString().equalsIgnoreCase("True"))
	        {
	        	_stm.RearrangeWorkingMemory(predicate,ks.getValue());
	            return true;
	        }
	        else
	        {
	        	ks= (KnowledgeSlot) _kb.Ask(predicate);
	        	if (ks != null && ks.getValue() != null && ks.getValue().toString().equalsIgnoreCase("True"))
	            {
	        		_stm.Tell(_kb,predicate, ks.getValue());
	                return true;
	            }
	        }
	        return false;
		}   
	}
	
	/**
	 * Asks the Memory the value of a given property
	 * @param property - the property to search in the Memory
	 * @return the value stored inside the property, if the property exists. If the 
     *         property does not exist, it returns null
	 */
	public Object AskProperty(Name property) {
		synchronized(this)
		{
			KnowledgeSlot prop = (KnowledgeSlot) _stm.Ask(property);
			if (prop == null)
			{
				prop = (KnowledgeSlot) _kb.Ask(property);
				if (prop == null)
					return null;
				else
					_stm.Tell(_kb, property, prop.getValue());
			}
			else
			{
				_stm.RearrangeWorkingMemory(property,prop.getValue());
			}
			return prop.getValue();
		}
	}
	
	/**
	 * Inserts a Predicate in the WorkingMemory
	 * @param predicate - the predicate to be inserted
	 */
	public void Assert(Name predicate) {
		synchronized(this)
		{
			_stm.Tell(_kb, predicate,new Symbol("True"));
		}
	}
	
	public void ClearChangeList() {
		synchronized(this)
		{
			_stm.ClearChangeList();
		}
	}
	
	public int Count()
	{
		return _stm.Count();
	}

	
	/*
	 * Return changeList from the WorkingMemory
	 */
	public ArrayList<KnowledgeSlot> GetChangeList() {
	    return _stm.GetChangeList();
	}
	
	/*
	 * Return factList from the WorkingMemory
	 */
	public ArrayList<KnowledgeSlot> GetFactList() {
	    return _stm.GetFactList();
	}
	
	/*
	 * Return factList from the KnowledgeBase
	 */
	public ArrayList<KnowledgeSlot> GetKnowledgeBaseFacts()
	{
		return _kb.GetFactList();
	}
	
	/*
	 * Return new facts from the WorkingMemory
	 */
	public ArrayList<KnowledgeSlot> getNewFacts()
    {
    	return _stm.GetNewFacts();
    }
	
	public KnowledgeSlot GetObjectDetails(String objectName)
	{
		synchronized(this)
		{
			KnowledgeSlot object = _stm.GetObjectDetails(objectName);
			if(object == null)
				object = _kb.GetObjectDetails(objectName);
			return object;
		}
	}
	
	public KnowledgeSlot GetObjectProperty(String objectName, String property)
	{
		synchronized(this)
		{
			KnowledgeSlot object = _stm.GetObjectProperty(objectName, property);
			if(object == null)
				object = _kb.GetObjectProperty(objectName, property);
			return object;
		}
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
     *         
	 */
	
	

	public ArrayList<SubstitutionSet> GetPossibleBindings(Name name) {
		ArrayList<SubstitutionSet> bindingSets = null;
		
		synchronized(this)
		{
			
		
			bindingSets = _stm.GetPossibleBindings(name);
			
			if (bindingSets == null)
				bindingSets = _kb.GetPossibleBindings(name);
			else
			{
				ArrayList<SubstitutionSet> bindingSets2 = _kb.GetPossibleBindings(name);
				if (bindingSets2 != null)
				{
					ListIterator<SubstitutionSet> li = bindingSets2.listIterator();
	
					synchronized (this) {
						while (li.hasNext()) {
							
							SubstitutionSet ss = li.next();
							if( !bindingSets.contains(ss) )
								bindingSets.add(ss);
						}
					}
				}
			}
			
			return bindingSets;
		}
	}

	/**
     * Gets a value that indicates whether new Knowledge has been added to the WorkingMemory since
     * the last inference process
     * @return true if there is new Knowledge in the WM, false otherwise
     */
    public boolean HasNewKnowledge()
    {
    	return _stm.HasNewKnowledge();
    }
	
	private void InferEffects(Step infOp, AgentModel am)
    {
		
    	Effect eff;
    	Condition cond;
    	AgentModel perspective;
    	
    	for(ListIterator<Effect> li = infOp.getEffects().listIterator();li.hasNext();)
    	{
    		eff = (Effect) li.next();
    		if(eff.isGrounded())
    		{
    			cond = eff.GetEffect();
    			perspective = am.getModelToTest(cond.getToM());
    			perspective.getMemory().getSemanticMemory().Tell(cond.getName(), cond.GetValue().toString());
    			//System.out.println("InferEffects");    			
    		}
    	}
    }
	
	public void InitializeProperty(Name property, Object value)
	{
		synchronized(this)
		{
			_kb.Tell(property, value);
		}
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
    public boolean PerformInference(AgentModel am)
    {
    	Step infOp;
    	Step groundInfOp;
    	ArrayList<SubstitutionSet> substitutionSets;
    	SubstitutionSet sSet;

    	synchronized(this)
    	{
	    	_stm.ResetNewKnowledge();
	    	
			for(ListIterator<Step> li = _kb.GetInferenceOperators().listIterator();li.hasNext();)
			{
				infOp = (Step) li.next();
				substitutionSets = Condition.CheckActivation(am, infOp.getPreconditions());
				if(substitutionSets != null)
				{
					for(ListIterator<SubstitutionSet> li2 = substitutionSets.listIterator();li2.hasNext();)
					{
						sSet = li2.next();
						groundInfOp = (Step) infOp.clone();
						groundInfOp.MakeGround(sSet.GetSubstitutions());
						InferEffects(groundInfOp,am);
					}
				}
			}
	    	
	    	return _stm.HasNewKnowledge();
    	}
    }
	
    /**
     * Removes a predicate from the Semantic Memory
	 * @param predicate - the predicate to be removed
	 */
	public void Retract(Name predicate) 
	{
		synchronized(this)
		{
			_kb.Retract(predicate);
			_stm.Retract(predicate);
		}
	}
    
	public void Tell(Name property, Object value) {
		synchronized(this)
		{
			_stm.Tell(_kb, property, value);
		}	
	}
	
	/*
	 * Called during loading
	 */
	public void putKnowledgeBase(KnowledgeBase kb)
	{
		_kb = kb;
	}
	
	/*
	 * Called during loading
	 */
	public void putWorkingMemory(WorkingMemory wm)
	{
		_stm = wm;
	}
	
//	public KnowledgeSlot getKBMainSlot()
//	{
//		return _kb.getMainSlot();
//	}
//	
//	public KnowledgeSlot getWMMainSlot()
//	{
//		return _stm.getMainSlot();
//	}

}
