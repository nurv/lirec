package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.deliberativeLayer.goals.Goal;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EmotionType;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.util.enumerables.GoalEvent;
import FAtiMA.Core.wellFormedNames.Name;

public class EpisodicMemory implements Serializable {
	
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	public static ArrayList<SearchKey> GenerateSearchKeys(Event e)
	{	
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		ArrayList<String> params = new ArrayList<String>();
		Parameter param;
		
		keys.add(new SearchKey(SearchKey.SUBJECT,e.GetSubject()));
		
		//Meiyii - 12/01/10
		if (e.GetType() == EventType.ACTION)
		{
			keys.add(new SearchKey(SearchKey.ACTION,e.GetAction()));
			keys.add(new SearchKey(SearchKey.STATUS,ActionEvent.GetName(e.GetStatus())));
		}
		else
		{
			keys.add(new SearchKey(SearchKey.INTENTION,e.GetAction()));
			keys.add(new SearchKey(SearchKey.STATUS,GoalEvent.GetName(e.GetStatus())));
		}
		
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
		
		return new Float(((float)numberOfSuccess/(float)numberOfTries));
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
				_stm.AssociateEmotionToDetail(m,em,cause,location);
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
		return _am.ContainsRecentEvent(searchKeys) || ContainsNewEvent(searchKeys);
	}
	
	public boolean ContainsNewEvent(ArrayList<SearchKey> searchKeys)
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
	
	public ActionDetail getNewestRecord()
	{
		return _stm.GetNewestRecord();
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
	
	public ArrayList<ActionDetail> SearchForRecentEvents(ArrayList<SearchKey> keys)
	{
		ArrayList<ActionDetail> aux = _am.SearchForRecentEvents(keys);
		aux.addAll(_stm.GetDetailsByKeys(keys));
		
		return aux;
	}
	
	public ArrayList<ActionDetail> SearchForNewEvents(ArrayList<SearchKey> searchKeys)
	{		
		synchronized (this) {
			if(this._stm.GetCount() > 0)
			{
				return _stm.GetDetailsByKeys(searchKeys);
			}
			return new ArrayList<ActionDetail>();
		}
	}
	
	/* 
	 * Start a new episode depending on the requirements of the different scenarios
	 * Meiyii 13/09/10
	 */
	public void StartEpisode(Memory m)
	{
		Name locationKey = Name.ParseName(Constants.SELF + "(location)");
		String location = (String) m.getSemanticMemory().AskProperty(locationKey);
		
		if(location == null)
		{
			location = Constants.EMPTY_LOCATION;
		}
		
		// 13/09/2010 - Create a new episode 
		synchronized (this) 
		{
			_am.NewEpisode(location);
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
		// commented 13/09/10
//		if (!location.equals(_previousLocation))
//		{
//			_am.NewEpisode(location);
//			//_stm.ResetEventID(); 06/01/10 not resetting the eventID to support CC matching that returns the ID as results
//		}

		synchronized (this) {
			if(this._stm.GetCount() >= ShortTermEpisodicMemory.MAXRECORDS)
			{
				ActionDetail detail = _stm.GetOldestRecord();
				//Meiyii 07/01/10
				
				/*if((detail.getAction().equals("activate") || 
						detail.getAction().equals("succeed") ||
						detail.getAction().equals("fail")) ||
						((!detail.getAction().equals("activate") && 
						!detail.getAction().equals("succeed") &&
						!detail.getAction().equals("fail")) &&
						(detail.getEmotion().GetType()) != EmotionType.NEUTRAL))*/
				if((detail.getIntention() != null && (detail.getStatus().equals(GoalEvent.GetName(GoalEvent.ACTIVATION)) || 
						detail.getStatus().equals(GoalEvent.GetName(GoalEvent.SUCCESS)) ||
						detail.getStatus().equals(GoalEvent.GetName(GoalEvent.FAILURE)))) ||
						(detail.getAction() != null && (detail.getEmotion().GetType()) != EmotionType.NEUTRAL))
				{
					_am.StoreAction(detail);					
				}
				_stm.DeleteOldestRecord();
			}
			_stm.AddActionDetail(m, e, location);
			_newRecords.add(_stm.GetNewestRecord());
			//_previousLocation = location; Meiyii 13/09/10
			
			this._newData = true;
		}
	}
	
	/* 
	 * Empty STEM into AM before the agent shut down
	 * Meiyii 13/09/10
	 */
	public void MoveSTEMtoAM()
	{
		synchronized (this) {
			for (int i=0; i < _stm.GetCount(); i++)
			{
				ActionDetail detail = _stm.getDetails().get(i);
				if((detail.getIntention() != null && (detail.getStatus().equals(GoalEvent.GetName(GoalEvent.ACTIVATION)) || 
						detail.getStatus().equals(GoalEvent.GetName(GoalEvent.SUCCESS)) ||
						detail.getStatus().equals(GoalEvent.GetName(GoalEvent.FAILURE)))) ||
						(detail.getAction() != null && (detail.getEmotion().GetType()) != EmotionType.NEUTRAL))
				{
					_am.StoreAction(detail);					
				}
			}
			_stm.getDetails().clear();
		}
	}
	
	public String SummarizeEpisode(Memory m, int episode)
	{
		return _am.SummarizeEpisode(m, episode);
	}
}
