package FAtiMA.Core.memory.episodicMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.NeutralEmotion;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.util.enumerables.GoalEvent;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;

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
	
	// 09/03/11 - Matthias
	// default decay value used for the calculation of activation values
	private static final double DECAY_VALUE = 0.8;
	
	// 03/05/11 - Matthias
	// Activation-Based Forgetting in Short-Term Episodic Memory
	private static final boolean AB_FORGETTING_STEM = true;
	// Activation-Based Forgetting in Autobiographic Memory?	
	private static final boolean AB_FORGETTING_AM = true;
	
	public EpisodicMemory()
	{
		_stm = new ShortTermEpisodicMemory();
		_am = new AutobiographicalMemory();
		_newData = false;
		_newRecords = new ArrayList<ActionDetail>();
		_previousLocation = Constants.EMPTY_LOCATION;
	}
	
	// 07/04/11 Matthias
	public int getNextEventID() {
		return _stm.GetEventID();
	}
	
	// 07/04/11 Matthias
	public void setNextEventID(int nextEventID) {
		_stm.SetEventID(nextEventID);
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
	
	public void applySubstitution(Substitution s)
	{
		_stm.applySubstitution(s);
		_am.applySubstitution(s);
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
						(detail.getAction() != null && (detail.getEmotion().getType()) != NeutralEmotion.NAME))
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
		
		// DEBUG Matthias
		//if(_am.countMemoryDetails() % 20 == 10) {
		//	this.calculateActivationValues();
		//	this.activationBasedSelectionByCount(5);
		//}
		
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
						(detail.getAction() != null && (detail.getEmotion().getType()) != NeutralEmotion.NAME))
				{
					_am.StoreAction(detail);					
				}
			}
			_stm.getDetails().clear();
		}
	}
	
	public AutobiographicalMemory getAM()
	{
		return _am;
	}

	public ShortTermEpisodicMemory getSTEM()
	{
		return _stm;
	}
	
	public String SummarizeEpisode(Memory m, String episode)
	{
		if(episode.equals("STM"))
		{
			return _stm.GenerateSummary(m);
		}
		int num = Integer.parseInt(episode);
		return _am.SummarizeEpisode(m, num);
	}
	
	public String toXML()
	{
		String emStr ="<EpisodicMemory>";
		emStr += _am.toXML();
		emStr += _stm.toXML();
		emStr += "</EpisodeMemory>";
		return emStr;
	}
	
	/*
	 * Called during loading
	 */
	public void putAutobiographicalMemory(AutobiographicalMemory am)
	{
		_am = am;
	}
	
	/*
	 * Called during loading
	 */
	public void putSTEpisodicMemory(ShortTermEpisodicMemory stem)
	{
		_stm = stem;
	}
	
	
	
	// 09/03/11 - Matthias
	public void calculateActivationValues() {
		calculateActivationValues(new Time(), DECAY_VALUE);
	}
		
	// 09/03/11 - Matthias
	public void calculateActivationValues(Time timeCalculated, double decayValue) {
		
		// calculate values for Autobiographic Memory		
		for(MemoryEpisode episode : _am.GetAllEpisodes()) {
			for(ActionDetail detail : episode.getDetails()) {
				detail.calculateActivationValue(timeCalculated, decayValue);
			}			
		}
		
		// calculate values for Short-Term Memory
		for(ActionDetail detail : _stm.getDetails()) {
			detail.calculateActivationValue(timeCalculated, decayValue);
		}
		
	}
		
	// 09/03/11 - Matthias
	public ArrayList<ActionDetail> activationBasedSelectionByThreshold(double threshold) {	

		// DEBUG
		System.out.println("Activation-Based Selection by threshold...");

		// hold selected details in one list
		ArrayList<ActionDetail> selected = new ArrayList<ActionDetail>();

		// select details not to be forgotten
		if(!AB_FORGETTING_AM) {
			// add details from Autobiographic Memory
			for(MemoryEpisode episode : _am.GetAllEpisodes()) {
				selected.addAll(episode.getDetails());			
			}
			// DEBUG
			System.out.println("selected all details from Autobiographic Memory");			
		}
		if(!AB_FORGETTING_STEM) {
			// add details from Short-Term Memory
			selected.addAll(_stm.getDetails());
			// DEBUG
			System.out.println("selected all details from Short-Term Memory");			
		}
		
		if(AB_FORGETTING_AM) {
			// select details from Autobiographic Memory
			for(MemoryEpisode episode : _am.GetAllEpisodes()) {
				for(ActionDetail detail : episode.getDetails()) {
					double value = detail.getActivationValue().getValue();
					if(value > threshold) {
						selected.add(detail);
						// DEBUG
						System.out.println("selected detail " + detail.getID() + " with value " + value);					
					}
				}			
			}
		}

		if(AB_FORGETTING_STEM) {
			// select details from Short-Term Memory
			for(ActionDetail detail : _stm.getDetails()) {					
				double value = detail.getActivationValue().getValue();
				if(value > threshold) {
					selected.add(detail);
					// DEBUG
					System.out.println("selected detail " + detail.getID() + " with value " + value);					
				}
			}
		}
		
		return selected;		
	}
	
	// 09/03/11 - Matthias
	public ArrayList<ActionDetail> activationBasedSelectionByCount(int countMax) {

		// DEBUG
		System.out.println("Activation-Based Selection by count...");

		// hold selected details in one list
		ArrayList<ActionDetail> selected = new ArrayList<ActionDetail>();
		
		// select details not to be forgotten
		if(!AB_FORGETTING_AM) {
			// add details from Autobiographic Memory
			for(MemoryEpisode episode : _am.GetAllEpisodes()) {
				selected.addAll(episode.getDetails());			
			}
			// DEBUG
			System.out.println("selected all details from Autobiographic Memory");			
		}
		if(!AB_FORGETTING_STEM) {
			// add details from Short-Term Memory
			selected.addAll(_stm.getDetails());
			// DEBUG
			System.out.println("selected all details from Short-Term Memory");			
		}
		
		// merge details for applying selection into one list
		ArrayList<ActionDetail> merged = new ArrayList<ActionDetail>();
		
		// only take into account details which can be forgotten
		if(AB_FORGETTING_AM) {
			// add details from Autobiographic Memory
			for(MemoryEpisode episode : _am.GetAllEpisodes()) {
				merged.addAll(episode.getDetails());			
			}
		}
		if(AB_FORGETTING_STEM) {
			// add details from Short-Term Memory
			merged.addAll(_stm.getDetails());
		}

		// sort by value (ascending)
		// bubble sort
		// TODO: faster sort algorithm
		boolean swapped;
		do {
			swapped = false;
			for(int i = 0; i < merged.size() - 1; i++) {
				ActionDetail detailA = merged.get(i);
				double valueA = detailA.getActivationValue().getValue();
				ActionDetail detailB = merged.get(i + 1);				
				double valueB = detailB.getActivationValue().getValue();
				if(valueA > valueB) {
					merged.set(i, detailB);
					merged.set(i + 1, detailA);
					swapped = true;
				}
			}
		} while (swapped);

		// select tail of list (highest values)
		int iMax = Math.min(countMax, merged.size());
		for(int i = merged.size()-1; i > merged.size()-1-iMax; i--) {
			selected.add(merged.get(i));
			// DEBUG
			System.out.println("selected detail " + merged.get(i).getID() + " with value " + merged.get(i).getActivationValue().getValue());
		}
		
		return selected;
	}

	// 09/03/11 - Matthias
	public ArrayList<ActionDetail> activationBasedSelectionByAmount(double amount) {
		
		// DEBUG
		System.out.println("Activation-Based Selection by amount...");
		
		// validate amount
		if(amount < 0) {
			amount = 0;
		}
		else if(amount > 1) {
			amount = 1;
		}
		
		// determine corresponding count
		int countTotal = 0;
		
		// count only details to be taken into account for selection
		if(AB_FORGETTING_AM) {
			for(MemoryEpisode episode : _am.GetAllEpisodes()) {
				countTotal += episode.getDetails().size();
			}
		}
		if(AB_FORGETTING_STEM) {
			countTotal += _stm.getDetails().size();
		}
		
		// calculate corresponding maximum count
		int countMax = (int) Math.round(amount * countTotal);
		// DEBUG
		System.out.println(amount + " corresponds to " + countMax + "/" + countTotal + " events");
		
		// select details by count
		ArrayList<ActionDetail> selected = activationBasedSelectionByCount(countMax);
		
		return selected;
	}
	
	// 02/05/11 - Matthias
	public void activationBasedForgetting(ArrayList<Integer> selectedIDs) {
		
		// DEBUG
		System.out.println("Activation-Based Forgetting...");
		
		// workaround for java.util.ConcurrentModificationException:
		// create temporary copy of episode list
		ArrayList<MemoryEpisode> episodes = new ArrayList<MemoryEpisode>();
		episodes.addAll(_am.GetAllEpisodes());
		
		// forget in each episode		
		for(MemoryEpisode episode : episodes) {
			
			ArrayList<ActionDetail> selectedEpisodeDetails = new ArrayList<ActionDetail>();
			for(ActionDetail detail : episode.getDetails()) {
				if(selectedIDs.contains(detail.getID())) {
					selectedEpisodeDetails.add(detail);
				}
			}
			
			// TODO: implementation decision
			// a) delete old summary, regenerate summary when adding details (summary consistent with details)
			// b) keep old summary with all values, forget only details (summary can be inconsistent with details)
			episode.getPeople().clear();
			episode.getLocation().clear();
			episode.getObjects().clear();
			
			// temporarily remove details
			episode.getDetails().clear();
			for(ActionDetail detail : selectedEpisodeDetails) {
				// add selected details (adds corresponding values to summary)
				episode.AddActionDetail(detail);
			}
			
			// TODO: implementation decision
			// a) delete empty episodes
			// b) keep empty episodes (and summary contains all values)
			if(episode.getDetails().size() == 0) {
				_am.GetAllEpisodes().remove(episode);
			}
			
		}
		
		// forget in Short-Term Memory
		ArrayList<ActionDetail> selectedShortTermDetails = new ArrayList<ActionDetail>();
		for(ActionDetail detail : _stm.getDetails()) {
			if(selectedIDs.contains(detail.getID())) {
				selectedShortTermDetails.add(detail);
			}
		}
		// temporarily remove details
		_stm.getDetails().clear();
		// add selected details
		_stm.getDetails().addAll(selectedShortTermDetails);
	}
	
}
