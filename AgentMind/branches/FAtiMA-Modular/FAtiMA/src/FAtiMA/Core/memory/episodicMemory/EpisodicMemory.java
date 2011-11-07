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

	private static final long serialVersionUID = 1L;

	//private String _previousLocation;
	private ShortTermEpisodicMemory _stm;
	private AutobiographicalMemory _am;
	private boolean _newData;

	private ArrayList<ActionDetail> _newRecords;

	// 09/03/11 - Matthias
	// default decay value used for the calculation of activation values
	private static final double DECAY_VALUE = 0.8;

	// 03/05/11 - Matthias
	// Activation-Based Forgetting in Short-Term Episodic Memory?
	private static final boolean AB_FORGETTING_STEM = false;
	// Activation-Based Forgetting in Autobiographic Memory?
	private static final boolean AB_FORGETTING_AM = true;

	// use indexed search?
	private static final boolean INDEXED_SEARCH = true;

	// use cached search?
	private static final boolean CACHED_SEARCH = true;
	private static final long CACHE_TIME = 1000; // milliseconds
	private ArrayList<SearchKey> ContainsPastEventCachedSearchKeys;
	private boolean ContainsPastEventCachedResult;
	private long ContainsPastEventCachedTime;
	private ArrayList<SearchKey> ContainsRecentEventCachedSearchKeys;
	private boolean ContainsRecentEventCachedResult;
	private long ContainsRecentEventCachedTime;
	private ArrayList<SearchKey> SearchForPastEventsCachedSearchKeys;
	private ArrayList<ActionDetail> SearchForPastEventsCachedResult;
	private long SearchForPastEventsCachedTime;
	private ArrayList<SearchKey> SearchForRecentEventsCachedSearchKeys;
	private ArrayList<ActionDetail> SearchForRecentEventsCachedResult;
	private long SearchForRecentEventsCachedTime;

	public static ArrayList<SearchKey> GenerateSearchKeys(Event e) {
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		ArrayList<String> params = new ArrayList<String>();
		Parameter param;

		keys.add(new SearchKey(SearchKey.SUBJECT, e.GetSubject()));

		//Meiyii - 12/01/10
		if (e.GetType() == EventType.ACTION) {
			keys.add(new SearchKey(SearchKey.ACTION, e.GetAction()));
			keys.add(new SearchKey(SearchKey.STATUS, ActionEvent.GetName(e.GetStatus())));
		} else {
			keys.add(new SearchKey(SearchKey.INTENTION, e.GetAction()));
			keys.add(new SearchKey(SearchKey.STATUS, GoalEvent.GetName(e.GetStatus())));
		}

		if (e.GetTarget() != null) {
			keys.add(new SearchKey(SearchKey.TARGET, e.GetTarget()));
		}

		if (e.GetParameters().size() > 0) {
			for (ListIterator<Parameter> li = e.GetParameters().listIterator(); li.hasNext();) {
				param = (Parameter) li.next();
				params.add(param.GetValue().toString());
			}
			keys.add(new SearchKey(SearchKey.PARAMETERS, params));
		}

		return keys;
	}

	public EpisodicMemory() {
		_stm = new ShortTermEpisodicMemory();
		_am = new AutobiographicalMemory();
		_newData = false;
		_newRecords = new ArrayList<ActionDetail>();
	}

	// 07/04/11 Matthias
	public int getNextEventID() {
		return _stm.GetEventID();
	}

	// 07/04/11 Matthias
	public void setNextEventID(int nextEventID) {
		_stm.SetEventID(nextEventID);
	}

	public float AssessGoalFamiliarity(Goal g) {
		float similarEvents = 0;
		float familiarity = 0;

		similarEvents = _am.AssessGoalFamiliarity(g) + _stm.AssessGoalFamiliarity(g);

		// familiarity function f(x) = 1 - 1/(x/2 +1)
		// where x represents the number of similar events founds
		familiarity = 1 - (1 / (similarEvents / 2 + 1));

		return familiarity;
	}

	public Float AssessGoalProbability(Goal g) {
		int numberOfSuccess;
		int numberOfTries;
		ArrayList<SearchKey> searchKeys = GenerateSearchKeys(g.GetActivationEvent());

		numberOfTries = _am.CountEvent(searchKeys) + _stm.CountEvent(searchKeys);
		if (numberOfTries == 0) {
			return null;
		}

		searchKeys = GenerateSearchKeys(g.GetSuccessEvent());
		numberOfSuccess = _am.CountEvent(searchKeys) + _stm.CountEvent(searchKeys);

		return new Float(((float) numberOfSuccess / (float) numberOfTries));
	}

	public void AssociateEmotionToAction(Memory m, ActiveEmotion em, Event cause) {
		Name locationKey = Name.ParseName(Constants.SELF + "(location)");
		String location = (String) m.getSemanticMemory().AskProperty(locationKey);

		if (location == null) {
			location = Constants.EMPTY_LOCATION;
		}

		if (this._stm.GetCount() > 0) {
			synchronized (this) {
				_stm.AssociateEmotionToDetail(m, em, cause, location);
			}
		}
	}

	public void applySubstitution(Substitution s) {
		_stm.applySubstitution(s);
		_am.applySubstitution(s);
	}

	public void ClearNewRecords() {
		this._newRecords.clear();
	}

	public boolean ContainsPastEvent(ArrayList<SearchKey> searchKeys) {

		// check if cached search is enabled
		if (CACHED_SEARCH) {
			// check if search is cached
			if (searchKeys.equals(ContainsPastEventCachedSearchKeys)) {
				// check if cache has not expired
				if (System.currentTimeMillis() < ContainsPastEventCachedTime + CACHE_TIME) {
					// generate retrievals?
					//
					// return cached result
					return ContainsPastEventCachedResult;
				}
			}
		}

		// perform search
		boolean result;
		if (INDEXED_SEARCH)
			result = _am.ContainsPastEventIndexed(searchKeys);
		else
			result = _am.ContainsPastEvent(searchKeys);

		// check if cached search is enabled
		if (CACHED_SEARCH) {
			// update cache
			ContainsPastEventCachedSearchKeys = searchKeys;
			ContainsPastEventCachedResult = result;
			ContainsPastEventCachedTime = System.currentTimeMillis();
		}

		return result;
	}

	public boolean ContainsRecentEvent(ArrayList<SearchKey> searchKeys) {

		// check if cached search is enabled
		if (CACHED_SEARCH) {
			// check if search is cached
			if (searchKeys.equals(ContainsRecentEventCachedSearchKeys)) {
				// check if cache has not expired
				if (System.currentTimeMillis() < ContainsRecentEventCachedTime + CACHE_TIME) {
					// generate retrievals?
					//
					// return cached result
					return ContainsRecentEventCachedResult;
				}
			}
		}

		// perform search		
		boolean result;
		if (INDEXED_SEARCH)
			result = _am.ContainsRecentEventIndexed(searchKeys);
		else
			result = _am.ContainsRecentEvent(searchKeys);
		if (ContainsNewEvent(searchKeys)) {
			result = true;
		}

		// check if cached search is enabled
		if (CACHED_SEARCH) {
			// update cache
			ContainsRecentEventCachedSearchKeys = searchKeys;
			ContainsRecentEventCachedResult = result;
			ContainsRecentEventCachedTime = System.currentTimeMillis();
		}

		return result;
	}

	public boolean ContainsNewEvent(ArrayList<SearchKey> searchKeys) {
		synchronized (this) {
			if (this._stm.GetCount() > 0) {
				return _stm.VerifiesKeys(searchKeys);
			}
			return false;
		}
	}

	public int countMemoryDetails() {
		return _am.countMemoryDetails();
	}

	public ArrayList<MemoryEpisode> GetAllEpisodes() {
		return _am.GetAllEpisodes();
	}

	public ArrayList<ActionDetail> getDetails() {
		return _stm.getDetails();
	}

	public ActionDetail getNewestRecord() {
		return _stm.GetNewestRecord();
	}

	public ArrayList<ActionDetail> GetNewRecords() {
		return this._newRecords;
	}

	public Object GetSyncRoot() {
		return this;
	}

	/**
	 * This methods verifies if any new data was added to the
	 * AutobiographicalMemory since the last time this method was called.
	 * 
	 * @return
	 */
	public boolean HasNewData() {
		boolean aux = this._newData;
		this._newData = false;
		return aux;
	}

	public ArrayList<ActionDetail> SearchForPastEvents(ArrayList<SearchKey> searchKeys) {

		// check if cached search is enabled
		if (CACHED_SEARCH) {
			// check if search is cached
			if (searchKeys.equals(SearchForPastEventsCachedSearchKeys)) {
				// check if cache has not expired
				if (System.currentTimeMillis() < SearchForPastEventsCachedTime + CACHE_TIME) {
					// generate retrievals?
					//
					// return cached result
					return SearchForPastEventsCachedResult;
				}
			}
		}

		// perform search
		ArrayList<ActionDetail> result;
		if (INDEXED_SEARCH)
			result = _am.SearchForPastEventsIndexed(searchKeys);
		else
			result = _am.SearchForPastEvents(searchKeys);

		// check if cached search is enabled
		if (CACHED_SEARCH) {
			// update cache
			SearchForPastEventsCachedSearchKeys = searchKeys;
			SearchForPastEventsCachedResult = result;
			SearchForPastEventsCachedTime = System.currentTimeMillis();
		}

		return result;
	}

	public ArrayList<ActionDetail> SearchForRecentEvents(ArrayList<SearchKey> searchKeys) {

		// check if cached search is enabled
		if (CACHED_SEARCH) {
			// check if search is cached
			if (searchKeys.equals(SearchForRecentEventsCachedSearchKeys)) {
				// check if cache has not expired
				if (System.currentTimeMillis() < SearchForRecentEventsCachedTime + CACHE_TIME) {
					// generate retrievals?
					//
					// return cached result
					return SearchForRecentEventsCachedResult;
				}
			}
		}

		// perform search
		ArrayList<ActionDetail> result;
		if (INDEXED_SEARCH)
			result = _am.SearchForRecentEventsIndexed(searchKeys);
		else
			result = _am.SearchForRecentEvents(searchKeys);
		result.addAll(_stm.GetDetailsByKeys(searchKeys));

		// check if cached search is enabled
		if (CACHED_SEARCH) {
			// update cache
			SearchForRecentEventsCachedSearchKeys = searchKeys;
			SearchForRecentEventsCachedResult = result;
			SearchForRecentEventsCachedTime = System.currentTimeMillis();
		}

		return result;
	}

	public ArrayList<ActionDetail> SearchForNewEvents(ArrayList<SearchKey> searchKeys) {
		synchronized (this) {
			if (this._stm.GetCount() > 0) {
				return _stm.GetDetailsByKeys(searchKeys);
			}
			return new ArrayList<ActionDetail>();
		}
	}

	/* 
	 * Start a new episode depending on the requirements of the different scenarios
	 * Meiyii 13/09/10
	 */
	public void StartEpisode(Memory m) {
		Name locationKey = Name.ParseName(Constants.SELF + "(location)");
		String location = (String) m.getSemanticMemory().AskProperty(locationKey);

		if (location == null) {
			location = Constants.EMPTY_LOCATION;
		}

		// 13/09/2010 - Create a new episode 
		synchronized (this) {
			_am.NewEpisode(location);
		}

	}

	public void StoreAction(Memory m, Event e) {
		Name locationKey = Name.ParseName(Constants.SELF + "(location)");
		String location = (String) m.getSemanticMemory().AskProperty(locationKey);

		if (location == null) {
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
			if (this._stm.GetCount() >= ShortTermEpisodicMemory.MAXRECORDS) {
				ActionDetail detail = _stm.GetOldestRecord();

				// Meiyii 07/01/10				

				// Matthias 09/06/2011
				// reformatted, added condition with retrievals

				boolean storeAction = false;

				// goal status
				if (detail.getIntention() != null) {
					if (detail.getStatus().equals(GoalEvent.GetName(GoalEvent.ACTIVATION)) || detail.getStatus().equals(GoalEvent.GetName(GoalEvent.SUCCESS))
							|| detail.getStatus().equals(GoalEvent.GetName(GoalEvent.FAILURE))) {
						storeAction = true;
					}
				}
				// emotion
				if (detail.getAction() != null) {
					if (detail.getEmotion().getType() != NeutralEmotion.NAME) {
						storeAction = true;
					}
				}
				// retrievals
				if (detail.getRetrievalQueue().getNumRetrievalsInTotal() > 1) {
					storeAction = true;
				}

				if (storeAction) {
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
	public void MoveSTEMtoAM() {
		synchronized (this) {
			for (int i = 0; i < _stm.GetCount(); i++) {
				ActionDetail detail = _stm.getDetails().get(i);

				// Matthias 09/06/2011
				// reformatted, added condition with retrievals

				boolean storeAction = false;

				// goal status
				if (detail.getIntention() != null) {
					if (detail.getStatus().equals(GoalEvent.GetName(GoalEvent.ACTIVATION)) || detail.getStatus().equals(GoalEvent.GetName(GoalEvent.SUCCESS))
							|| detail.getStatus().equals(GoalEvent.GetName(GoalEvent.FAILURE))) {
						storeAction = true;
					}
				}
				// emotion
				if (detail.getAction() != null) {
					if (detail.getEmotion().getType() != NeutralEmotion.NAME) {
						storeAction = true;
					}
				}
				// retrievals
				if (detail.getRetrievalQueue().getNumRetrievalsInTotal() > 1) {
					storeAction = true;
				}

				if (storeAction) {
					_am.StoreAction(detail);
				}
			}
			_stm.getDetails().clear();
		}
	}

	public AutobiographicalMemory getAM() {
		return _am;
	}

	public ShortTermEpisodicMemory getSTEM() {
		return _stm;
	}

	public String SummarizeEpisode(Memory m, String episode) {
		if (episode.equals("STM")) {
			return _stm.GenerateSummary(m);
		}
		int num = Integer.parseInt(episode);
		return _am.SummarizeEpisode(m, num);
	}

	public String toXML() {
		String emStr = "<EpisodicMemory>";
		emStr += _am.toXML();
		emStr += _stm.toXML();
		emStr += "</EpisodeMemory>";
		return emStr;
	}

	/*
	 * Called during loading
	 */
	public void putAutobiographicalMemory(AutobiographicalMemory am) {
		_am = am;
	}

	/*
	 * Called during loading
	 */
	public void putSTEpisodicMemory(ShortTermEpisodicMemory stem) {
		_stm = stem;
	}

	// 09/03/11 - Matthias
	public void calculateActivationValues() {
		calculateActivationValues(new Time(), DECAY_VALUE);
	}

	// 09/03/11 - Matthias
	public void calculateActivationValues(Time timeCalculated, double decayValue) {
		synchronized (this) {

			// calculate values for Autobiographic Memory		
			for (MemoryEpisode episode : _am.GetAllEpisodes()) {
				for (ActionDetail detail : episode.getDetails()) {
					detail.calculateActivationValue(timeCalculated, decayValue);
				}
			}

			// calculate values for Short-Term Memory
			for (ActionDetail detail : _stm.getDetails()) {
				detail.calculateActivationValue(timeCalculated, decayValue);
			}

		}
	}

	// sort by activation value (ascending)
	private void quickSortByActivationValue(ArrayList<ActionDetail> details, int lower, int upper) {
		int i = lower, j = upper;
		ActionDetail pivot = details.get(lower + (upper - lower) / 2);
		while (i <= j) {
			while (details.get(i).getActivationValue().getValue() < pivot.getActivationValue().getValue()) {
				i++;
			}
			while (details.get(j).getActivationValue().getValue() > pivot.getActivationValue().getValue()) {
				j--;
			}
			if (i <= j) {
				ActionDetail temp = details.get(i);
				details.set(i, details.get(j));
				details.set(j, temp);
				i++;
				j--;
			}
		}
		if (lower < j)
			quickSortByActivationValue(details, lower, j);
		if (i < upper)
			quickSortByActivationValue(details, i, upper);
	}

	// 09/03/11 - Matthias
	public ArrayList<ActionDetail> activationBasedSelectionByThreshold(double threshold) {
		synchronized (this) {

			// DEBUG
			System.out.println("Activation-Based Selection by threshold...");

			// hold selected details in one list
			ArrayList<ActionDetail> selected = new ArrayList<ActionDetail>();

			// pre-select details not to be forgotten
			if (!AB_FORGETTING_AM) {
				// add details from Autobiographic Memory
				for (MemoryEpisode episode : _am.GetAllEpisodes()) {
					selected.addAll(episode.getDetails());
				}
				// DEBUG
				System.out.println("selected all details from Autobiographic Memory");
			}
			if (!AB_FORGETTING_STEM) {
				// add details from Short-Term Memory
				selected.addAll(_stm.getDetails());
				// DEBUG
				System.out.println("selected all details from Short-Term Memory");
			}

			if (AB_FORGETTING_AM) {
				// select details from Autobiographic Memory
				for (MemoryEpisode episode : _am.GetAllEpisodes()) {
					for (ActionDetail detail : episode.getDetails()) {
						double value = detail.getActivationValue().getValue();
						if (value > threshold) {
							selected.add(detail);
							// DEBUG
							System.out.println("selected detail " + detail.getID() + " with value " + value);
						}
					}
				}
			}

			if (AB_FORGETTING_STEM) {
				// select details from Short-Term Memory
				for (ActionDetail detail : _stm.getDetails()) {
					double value = detail.getActivationValue().getValue();
					if (value > threshold) {
						selected.add(detail);
						// DEBUG
						System.out.println("selected detail " + detail.getID() + " with value " + value);
					}
				}
			}

			return selected;

		}
	}

	// 09/03/11 - Matthias
	public ArrayList<ActionDetail> activationBasedSelectionByCount(int countMax) {
		synchronized (this) {

			// DEBUG
			System.out.println("Activation-Based Selection by count...");

			// hold selected details in one list
			ArrayList<ActionDetail> selected = new ArrayList<ActionDetail>();

			// pre-select details not to be forgotten
			if (!AB_FORGETTING_AM) {
				// add details from Autobiographic Memory
				for (MemoryEpisode episode : _am.GetAllEpisodes()) {
					selected.addAll(episode.getDetails());
				}
				// DEBUG
				System.out.println("selected all details from Autobiographic Memory");
			}
			if (!AB_FORGETTING_STEM) {
				// add details from Short-Term Memory
				selected.addAll(_stm.getDetails());
				// DEBUG
				System.out.println("selected all details from Short-Term Memory");
			}

			// merge details for applying selection into one list
			ArrayList<ActionDetail> merged = new ArrayList<ActionDetail>();

			// only take into account details which can be forgotten
			if (AB_FORGETTING_AM) {
				// add details from Autobiographic Memory
				for (MemoryEpisode episode : _am.GetAllEpisodes()) {
					merged.addAll(episode.getDetails());
				}
			}
			if (AB_FORGETTING_STEM) {
				// add details from Short-Term Memory
				merged.addAll(_stm.getDetails());
			}

			// sort by activation value (ascending)
			if (merged.size() > 0) {
				quickSortByActivationValue(merged, 0, merged.size() - 1);
			}

			// select tail of list (highest values, to be selected)
			int iMax = Math.min(countMax, merged.size());
			for (int i = merged.size() - 1; i > merged.size() - 1 - iMax; i--) {
				selected.add(merged.get(i));
				// DEBUG
				System.out.println("selected detail " + merged.get(i).getID() + " with value " + merged.get(i).getActivationValue().getValue());
			}

			return selected;

		}
	}

	// 09/03/11 - Matthias
	public ArrayList<ActionDetail> activationBasedSelectionByAmount(double amount) {
		synchronized (this) {

			// DEBUG
			System.out.println("Activation-Based Selection by amount...");

			// validate amount
			if (amount < 0) {
				amount = 0;
			} else if (amount > 1) {
				amount = 1;
			}

			// determine corresponding count
			int countTotal = 0;

			// count only details to be taken into account for selection
			if (AB_FORGETTING_AM) {
				for (MemoryEpisode episode : _am.GetAllEpisodes()) {
					countTotal += episode.getDetails().size();
				}
			}
			if (AB_FORGETTING_STEM) {
				countTotal += _stm.getDetails().size();
			}

			// calculate corresponding maximum count
			int countMax = (int) Math.round(amount * countTotal);
			// DEBUG
			System.out.println(amount + " corresponds to " + countMax + "/" + countTotal + " events");

			// select details by count
			return activationBasedSelectionByCount(countMax);

		}
	}

	public void applyActivationBasedSelection(ArrayList<ActionDetail> selectedDetails) {
		synchronized (this) {

			// DEBUG
			System.out.println("Applying Activation-Based Selection...");

			// loop over episodes
			for (MemoryEpisode episode : _am.GetAllEpisodes()) {
				ArrayList<ActionDetail> details = episode.getDetails();

				// loop over details from episode
				for (int i = 0; i < details.size(); i++) {
					ActionDetail detail = details.get(i);

					// check if detail is selected
					boolean selected = false;
					for (ActionDetail selectedDetail : selectedDetails) {
						// compare pointers here
						// no ArrayList.contains(Object) as equals() is overwritten in ActionDetail						
						if (selectedDetail == detail) {
							// detail is selected
							selected = true;
							break;
						}
					}

					// check if detail needs to be removed
					if (!selected) {
						if (_am.RemoveAction(detail, episode)) {
							i--;
						}
					}
				}
			}

			// loop over details from Short-Term Memory
			ArrayList<ActionDetail> details = _stm.getDetails();
			for (int i = 0; i < details.size(); i++) {
				ActionDetail detail = details.get(i);

				// check if detail is selected
				boolean selected = false;
				for (ActionDetail selectedDetail : selectedDetails) {
					// compare pointers here
					// no ArrayList.contains(Object) as equals() is overwritten in ActionDetail					
					if (selectedDetail == detail) {
						// detail is selected
						selected = true;
						break;
					}
				}

				// check if detail needs to be removed
				if (!selected) {
					if (_stm.RemoveAction(detail)) {
						i--;
					}
				}
			}

		}
	}

	public ArrayList<ActionDetail> activationBasedForgettingByThreshold(double threshold) {
		synchronized (this) {

			// DEBUG
			System.out.println("Activation-Based Forgetting by threshold...");

			// hold forgetable details in one list
			ArrayList<ActionDetail> forget = new ArrayList<ActionDetail>();

			if (AB_FORGETTING_AM) {
				// forget details from Autobiographic Memory
				for (MemoryEpisode episode : _am.GetAllEpisodes()) {
					for (ActionDetail detail : episode.getDetails()) {
						double value = detail.getActivationValue().getValue();
						if (value < threshold) {
							forget.add(detail);
							// DEBUG
							System.out.println("forget detail " + detail.getID() + " with value " + value);
						}
					}
				}
			}

			if (AB_FORGETTING_STEM) {
				// forget details from Short-Term Memory
				for (ActionDetail detail : _stm.getDetails()) {
					double value = detail.getActivationValue().getValue();
					if (value < threshold) {
						forget.add(detail);
						// DEBUG
						System.out.println("forget detail " + detail.getID() + " with value " + value);
					}
				}
			}

			return forget;

		}
	}

	public ArrayList<ActionDetail> activationBasedForgettingByCount(int countMax) {
		synchronized (this) {

			// DEBUG
			System.out.println("Activation-Based Forgetting by count...");

			// hold forgetable details in one list
			ArrayList<ActionDetail> forgetable = new ArrayList<ActionDetail>();

			// select details to be forgetable
			if (AB_FORGETTING_AM) {
				// add details from Autobiographic Memory
				for (MemoryEpisode episode : _am.GetAllEpisodes()) {
					forgetable.addAll(episode.getDetails());
				}
			}
			if (AB_FORGETTING_STEM) {
				// add details from Short-Term Memory
				forgetable.addAll(_stm.getDetails());
			}

			// sort by activation value (ascending)
			if (forgetable.size() > 0) {
				quickSortByActivationValue(forgetable, 0, forgetable.size() - 1);
			}

			// select head of list (lowest values, to be forgotten)
			ArrayList<ActionDetail> forget = new ArrayList<ActionDetail>();
			int iMax = Math.min(countMax, forgetable.size());
			for (int i = 0; i < iMax; i++) {
				forget.add(forgetable.get(i));
				// DEBUG
				System.out.println("forget detail " + forgetable.get(i).getID() + " with value " + forgetable.get(i).getActivationValue().getValue());
			}

			return forget;

		}
	}

	public ArrayList<ActionDetail> activationBasedForgettingByAmount(double amount) {
		synchronized (this) {

			// DEBUG
			System.out.println("Activation-Based Forgetting by amount...");

			// validate amount
			if (amount < 0) {
				amount = 0;
			} else if (amount > 1) {
				amount = 1;
			}

			// determine corresponding count
			int countTotal = 0;

			// count only details to be taken into account for forgetting
			if (AB_FORGETTING_AM) {
				for (MemoryEpisode episode : _am.GetAllEpisodes()) {
					countTotal += episode.getDetails().size();
				}
			}
			if (AB_FORGETTING_STEM) {
				countTotal += _stm.getDetails().size();
			}

			// calculate corresponding maximum count
			int countMax = (int) Math.round(amount * countTotal);
			// DEBUG
			System.out.println(amount + " corresponds to " + countMax + "/" + countTotal + " events");

			// forget details by count
			return activationBasedForgettingByCount(countMax);

		}
	}

	public void applyActivationBasedForgetting(ArrayList<ActionDetail> forgetDetails) {
		synchronized (this) {

			// DEBUG
			System.out.println("Applying Activation-Based Forgetting...");

			// remove details to be forgotten
			for (ActionDetail detail : forgetDetails) {
				// try to remove from Autobiographic Memory 
				if (!_am.RemoveAction(detail)) {
					// try to remove from Short-Term Memory
					_stm.RemoveAction(detail);
				}
			}

		}
	}

}
