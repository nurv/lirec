package FAtiMA.Core.conditions;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.SearchKey;
import FAtiMA.Core.wellFormedNames.Name;

public class NewEventCondition extends RecentEventCondition {

	private boolean _conditionAlreadyVerified;  
	private int TIME_INTERVAL_MS = 5000; 
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	
	public NewEventCondition(boolean occurred, short type, short status, Name event)
	{
		super(occurred, type, status, event);	
	}
	

	public NewEventCondition(PastEventCondition cond)
	{
		super(cond);
		_conditionAlreadyVerified = false;
	}
	
	public Object clone() {
		return new NewEventCondition(this);		
	}

	public boolean CheckCondition(AgentModel am) {
		boolean conditionVerified;
		

		if(!getName().isGrounded()){
			return false;
		}
		
		if(this._conditionAlreadyVerified){
			return true;
		}
	
		conditionVerified = (getPositive() == am.getMemory().getEpisodicMemory().ContainsNewEvent(GetSearchKeys()));
		
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
