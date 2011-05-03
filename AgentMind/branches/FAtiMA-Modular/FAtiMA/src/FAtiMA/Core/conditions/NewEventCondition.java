package FAtiMA.Core.conditions;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.SearchKey;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

public class NewEventCondition extends RecentEventCondition {

	private boolean _conditionAlreadyVerified = false;  
	private int TIME_INTERVAL_MS = 5000; 
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected NewEventCondition()
	{
	}
	
	public Object clone() {
		NewEventCondition newEvent = new NewEventCondition();
		
		newEvent._ToM = (Symbol) this._ToM.clone();
		newEvent._positive = this._positive;
		newEvent._conditionAlreadyVerified = this._conditionAlreadyVerified;
		newEvent._name = (Name) this._name.clone();
		newEvent._verifiable = this._verifiable;
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
		
		// Meiyii
		newEvent._type = this._type;
		newEvent._status = this._status;
		
		return newEvent;
	}
	
	public NewEventCondition(boolean occurred, short type, short status, Name event)
	{
		super(occurred, type, status, event);	
	}
	
	
	
	public NewEventCondition(PastEventCondition cond)
	{
		this._ToM = Constants.UNIVERSAL;
		this._subject = cond._subject;
		this._action = cond._action;
		this._target = cond._target;
		this._name = cond._name;
		this._positive = cond._positive;
		this._parameters = cond._parameters;
		
		this._type = cond._type;
		this._status = cond._status;
	}
	

	public boolean CheckCondition(AgentModel am) {
		boolean conditionVerified;
		

		if(!_name.isGrounded()){
			return false;
		}
		
		if(this._conditionAlreadyVerified){
			return true;
		}
	
		conditionVerified = (_positive == am.getMemory().getEpisodicMemory().ContainsNewEvent(GetSearchKeys()));
		
		if(conditionVerified){
			_conditionAlreadyVerified = true;
		}
		
		return conditionVerified;
	}
	
	
	protected ArrayList<SearchKey> GetSearchKeys()
	{
		ArrayList<SearchKey> keys = super.GetSearchKeys();
		
		//we only want to search for events that happened at most 1 second before
		keys.add(new SearchKey(SearchKey.MAXELAPSEDTIME, new Long(TIME_INTERVAL_MS)));
		
		return keys;
	}
}
