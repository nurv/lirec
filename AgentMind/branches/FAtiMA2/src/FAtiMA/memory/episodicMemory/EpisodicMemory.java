package FAtiMA.memory.episodicMemory;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.deliberativeLayer.goals.Goal;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.memory.Memory;
import FAtiMA.sensorEffector.Event;
import FAtiMA.sensorEffector.Parameter;
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.wellFormedNames.Name;

public class EpisodicMemory {
	
	public static ArrayList<SearchKey> GenerateSearchKeys(Event e)
	{	
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		ArrayList<String> params = new ArrayList<String>();
		Parameter param;
		
		keys.add(new SearchKey(SearchKey.SUBJECT,e.GetSubject()));
		
		keys.add(new SearchKey(SearchKey.ACTION,e.GetAction()));
		
		if(e.GetTarget() != null)
		{
			keys.add(new SearchKey(SearchKey.TARGET, e.GetTarget()));
		}
		
		if(e.GetParameters().size() > 0)
		{
			for(ListIterator<Parameter> li = e.GetParameters().listIterator();li.hasNext();)
			{
				param = (Parameter) li.next();
				params.add(param.GetValue().toString());
			}
			keys.add(new SearchKey(SearchKey.PARAMETERS, params));
		}
		
		return keys;
	}
	private String _previousLocation;
	private ShortTermEpisodicMemory _stm;
	private AutobiographicalMemory _am;
	private boolean _newData;
	
	
	private ArrayList<ActionDetail> _newRecords;	
	
	public EpisodicMemory()
	{
		_stm = new ShortTermEpisodicMemory();
		_am = new AutobiographicalMemory();
		_newData = false;
		_newRecords = new ArrayList<ActionDetail>();
		_previousLocation = Constants.EMPTY_LOCATION;
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
	
	public Float AssessGoalProbability(Goal g)
	{
		int numberOfSuccess;
		int numberOfTries;
		ArrayList<SearchKey> searchKeys = GenerateSearchKeys(g.GetActivationEvent());
		
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
	
	public void AssociateEmotionToAction(Memory m, ActiveEmotion em, Event cause)
	{
		Name locationKey = Name.ParseName(Constants.SELF + "(location)");
		String location = (String) m.getSemanticMemory().AskProperty(locationKey);
		
		if(location == null)
		{
			location = Constants.EMPTY_LOCATION;
		}
		
		if(this._stm.GetCount() > 0)
		{
			synchronized (this)
			{
				_stm.AssociateEmotionToDetail(m, em,cause,location);
			}
		}
	}
	
	public void ClearNewRecords() {
		this._newRecords.clear();
	}
	
	public boolean ContainsPastEvent(ArrayList<SearchKey> searchKeys)
	{
		return _am.ContainsPastEvent(searchKeys);
	}
	
	public boolean ContainsRecentEvent(ArrayList<SearchKey> searchKeys)
	{		
		synchronized (this) {
			if(this._stm.GetCount() > 0)
			{				
				return _stm.VerifiesKeys(searchKeys);
			}
			return false;
		}
	}
	
	public int countMemoryDetails()
	{
		return _am.countMemoryDetails();
	}
	
	public ArrayList<MemoryEpisode> GetAllEpisodes()
	{
		return _am.GetAllEpisodes();
	}
	
	public ArrayList<ActionDetail> getDetails()
	{
		return _stm.getDetails();
	}
	
	
	
	public ArrayList<ActionDetail> GetNewRecords()
	{
		return this._newRecords;
	}
	
	public Object GetSyncRoot()
	{
		return this;
	}
	
	/**
	 * This methods verifies if any new data was added to the AutobiographicalMemory since
	 * the last time this method was called. 
	 * @return
	 */
	public boolean HasNewData()
	{
		boolean aux = this._newData;
		this._newData = false;
		return aux;
	}
	
	public ArrayList<ActionDetail> SearchForPastEvents(ArrayList<SearchKey> keys) 
	{
		return _am.SearchForPastEvents(keys);
	}
	
	public ArrayList<ActionDetail> SearchForRecentEvents(ArrayList<SearchKey> searchKeys)
	{		
		synchronized (this) {
			if(this._stm.GetCount() > 0)
			{
				return _stm.GetDetailsByKeys(searchKeys);
			}
			return new ArrayList<ActionDetail>();
		}
	}
	
	public void StoreAction(Memory m, Event e)
	{
		Name locationKey = Name.ParseName(Constants.SELF + "(location)");
		String location = (String) m.getSemanticMemory().AskProperty(locationKey);
		
		if(location == null)
		{
			location = Constants.EMPTY_LOCATION;
		}
		
		// 31/03/2009 - Create a new episode if the location changes to allow goals reset
		// If this if block is commented, goals decay over time and reset automatically
		if (!location.equals(_previousLocation))
		{
			_am.NewEpisode(location);
			_stm.ResetEventID();
		}

		synchronized (this) {
			if(this._stm.GetCount() >= ShortTermEpisodicMemory.MAXRECORDS)
			{
				ActionDetail detail = _stm.GetOldestRecord();
				
				if((detail.getAction().equals("activate") || 
						detail.getAction().equals("succeed") ||
						detail.getAction().equals("fail")) ||
						((!detail.getAction().equals("activate") && 
						!detail.getAction().equals("succeed") &&
						!detail.getAction().equals("fail")) &&
						(detail.getEmotion().GetType()) != EmotionType.NEUTRAL))
				{
					_am.StoreAction(detail);					
				}
				_stm.DeleteOldestRecord();
			}
			_stm.AddActionDetail(m, e, location);
			_newRecords.add(_stm.GetNewestRecord());
			_previousLocation = location;
			
			this._newData = true;
		}
	}
	
	public String SummarizeLastEvent(Memory m)
	{
		return _am.SummarizeLastEvent(m);
	}
}
