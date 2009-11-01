package FAtiMA.conditions;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.AgentModel;
import FAtiMA.memory.SearchKey;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;

public class NewEventCondition extends RecentEventCondition {

	private boolean _conditionAlreadyVerified = false;  
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected NewEventCondition()
	{
	}
	
	public Object clone() {
		NewEventCondition newEvent = new NewEventCondition();
		
		newEvent._positive = this._positive;
		newEvent._conditionAlreadyVerified = this._conditionAlreadyVerified;
		newEvent._name = (Name) this._name.clone();
		newEvent._subject = (Symbol) this._subject.clone();
		newEvent._action = (Symbol) this._action.clone();
		if(this._target != null)
		{
			newEvent._target = (Symbol) this._target.clone();
		}
		
		newEvent._parameters = new ArrayList<Symbol>(this._parameters.size());
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		
		while(li.hasNext())
		{
			newEvent._parameters.add((Symbol)li.next().clone());
		}
		
		return newEvent;
	}
	
	public NewEventCondition(boolean occurred, Name event)
	{
		super(occurred, event);	
	}
	
	
	
	public NewEventCondition(PastEventCondition cond)
	{
		this._subject = cond._subject;
		this._action = cond._action;
		this._target = cond._target;
		this._name = cond._name;
		this._positive = cond._positive;
		this._parameters = cond._parameters;
	}
	

	public boolean CheckCondition(AgentModel am) {
		boolean conditionVerified;
		

		if(!_name.isGrounded()){
			return false;
		}
		
		if(this._conditionAlreadyVerified){
			return true;
		}
	
		conditionVerified = (_positive == am.getMemory().getSTM().ContainsRecentEvent(GetSearchKeys()));
		
		if(conditionVerified){
			_conditionAlreadyVerified = true;
		}
		
		return conditionVerified;
	}
	
	
	protected ArrayList<SearchKey> GetSearchKeys()
	{
		ArrayList<SearchKey> keys = super.GetSearchKeys();
		
		//we only want to search for events that happened at most 1 second before
		keys.add(new SearchKey(SearchKey.MAXELAPSEDTIME, new Long(4000)));
		
		return keys;
	}
}
