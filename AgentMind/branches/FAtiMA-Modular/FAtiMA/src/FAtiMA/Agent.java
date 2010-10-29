package FAtiMA;

import java.io.File;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;



import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import FAtiMA.Display.AgentDisplay;
import FAtiMA.culture.Ritual;
import FAtiMA.deliberativeLayer.DeliberativeProcess;
import FAtiMA.deliberativeLayer.EmotionalPlanner;
import FAtiMA.deliberativeLayer.goals.GoalLibrary;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.emotionalIntelligence.ActionTendencyOperatorFactory;
import FAtiMA.emotionalIntelligence.OCCAppraisalRules;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.Appraisal;
import FAtiMA.emotionalState.AppraisalVector;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.exceptions.UnknownGoalException;
import FAtiMA.memory.Memory;
import FAtiMA.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.reactiveLayer.Action;
import FAtiMA.reactiveLayer.ActionTendencies;
import FAtiMA.reactiveLayer.EmotionalReactionTreeNode;
import FAtiMA.reactiveLayer.Reaction;
import FAtiMA.reactiveLayer.ReactiveProcess;
import FAtiMA.sensorEffector.Event;
import FAtiMA.sensorEffector.IONRemoteAgent;
import FAtiMA.sensorEffector.Parameter;
import FAtiMA.sensorEffector.RemoteAgent;
import FAtiMA.sensorEffector.SpeechAct;
import FAtiMA.sensorEffector.WorldSimulatorRemoteAgent;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.util.VersionChecker;
import FAtiMA.util.enumerables.ActionEvent;
import FAtiMA.util.enumerables.AgentPlatform;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.util.enumerables.EventType;
import FAtiMA.util.parsers.AgentLoaderHandler;
import FAtiMA.util.parsers.BinaryStringConverter;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;

import FAtiMA.memory.ICompoundCue;
import FAtiMA.memory.ISpreadActivate;
import FAtiMA.memory.ICommonalities;

public class Agent implements AgentModel {
	
	protected HashMap<String,ModelOfOther> _ToM;
	protected ArrayList<String> _nearbyAgents;
	
	protected EmotionalState _emotionalState;
	protected MotivationalState _motivationalState;
	protected Memory _memory;
	
	
	protected boolean _shutdown;
	protected DeliberativeProcess _deliberativeLayer;
	protected ReactiveProcess _reactiveLayer;
	protected DialogManager _dialogManager;

	protected ArrayList<ValuedAction> _actionsForExecution;
	protected ArrayList<Event> _perceivedEvents;

	protected RemoteAgent _remoteAgent;
	protected String _role;
	protected String _name; //the agent's name
	protected String _sex;
	protected String _displayName; 
	protected SpeechAct _speechAct;
	protected short _currentEmotion;
	protected long _numberOfCycles;
	protected long _totalexecutingtime=0;

	protected AgentDisplay _agentDisplay;
	protected boolean _showStateWindow;
	protected Logger _logger;

	protected ICompoundCue _compoundCue;
	protected ISpreadActivate _spreadActivate;
	protected ICommonalities _commonalities;

	private String _saveDirectory;
	public static final String MIND_PATH = "data/characters/minds/";
	public static final String MIND_PATH_ANDROID = "sdcard/data/characters/minds/";	
	private static final Name ACTION_CONTEXT = Name.ParseName("ActionContext()");
	
	private boolean _saveRequest = false;

	public Agent(short agentPlatform, String host, int port, String saveDirectory, String name, boolean displayMode, 
			String sex, String role, 
			String displayName, String actionsFile, 
			String goalsFile, String cultureName, HashMap<String,String> properties, ArrayList<String> goalList) {

		_emotionalState = new EmotionalState();
		_memory = new Memory();
		// creating a new episode when the agent starts 13/09/10
		_memory.getEpisodicMemory().StartEpisode(_memory);
		_motivationalState = new MotivationalState();
		
		_ToM = new HashMap<String, ModelOfOther>();
		_nearbyAgents = new ArrayList<String>();
		
		_saveDirectory = saveDirectory;
		_shutdown = false;
		_numberOfCycles = 0;
		_name = name;
		_role = role;
		_sex = sex;
		_displayName = displayName;
		_showStateWindow = displayMode;
		_currentEmotion = EmotionType.NEUTRAL;//neutral emotion - no emotion
		_actionsForExecution = new ArrayList<ValuedAction>();
		_perceivedEvents = new ArrayList<Event>();
		_dialogManager = new DialogManager();

		if(agentPlatform == AgentPlatform.WORLDSIM){
			properties.put("name", _name);
			properties.put("role", _role);
			properties.put("sex", _sex);	
		}


		try{
			AgentLogger.GetInstance().initialize(name,displayMode);

			String mind_path = MIND_PATH;
			if (VersionChecker.runningOnAndroid()) mind_path = MIND_PATH_ANDROID;
			
			// Load Plan Operators
			ActionLibrary.GetInstance().LoadActionsFile("" + mind_path + actionsFile + ".xml", this);
			EmotionalPlanner planner = new EmotionalPlanner(ActionLibrary.GetInstance().GetActions());
			
			

			// Load GoalLibrary
			GoalLibrary goalLibrary = new GoalLibrary(mind_path + goalsFile + ".xml");


			//For efficiency reasons these two are not real processes
			_reactiveLayer = new ReactiveProcess(_name);
			

			_deliberativeLayer = new DeliberativeProcess(_name,goalLibrary,planner);
	
			String personalityFile = mind_path + "roles/" + role + "/" + role + ".xml";
			loadPersonality(personalityFile,agentPlatform, goalList);
			
			ArrayList<Step> occRules = OCCAppraisalRules.GenerateOCCAppraisalRules(this);
			for(Step s : occRules)
			{
				planner.AddOperator(s);
			}
			
			for(Action at: _reactiveLayer.getActionTendencies().getActions())
			{
				planner.AddOperator(ActionTendencyOperatorFactory.CreateATOperator(this, at));
			}
				
			if(agentPlatform == AgentPlatform.WORLDSIM){
				_remoteAgent = new WorldSimulatorRemoteAgent(host, port, this, properties);
			}else if (agentPlatform == AgentPlatform.ION){
				_remoteAgent = new IONRemoteAgent(host, port, this);	
			}
			 
			/*
			 * This call will initialize the timer for the agent's
			 * simulation time
			 */
			AgentSimulationTime.GetInstance();
		}
		catch (Exception e) {
			e.printStackTrace();
			AgentLogger.GetInstance().log("Exception: " + e);
			terminateExecution();
		}

	}
	
	public Agent(short agentPlatform, String host, int port, String directory, String fileName)
	{
		try{
			_shutdown = false;
			_numberOfCycles = 0;
			
			LoadAgentState(directory + fileName);
			AgentLogger.GetInstance().initialize(fileName,_showStateWindow);
			
			// creating a new episode when the agent loads 13/09/10
			_memory.getEpisodicMemory().StartEpisode(_memory);
			
			if(agentPlatform == AgentPlatform.ION)
			{
				_remoteAgent = new IONRemoteAgent(host,port,this);
			}
			else if (agentPlatform == AgentPlatform.WORLDSIM)
			{
				_remoteAgent = new WorldSimulatorRemoteAgent(host,port,this,new HashMap<String,String>());
			}			
			_remoteAgent.LoadState(fileName+"-RemoteAgent.dat");
		}
		catch (Exception e) {
			e.printStackTrace();
			this.terminateExecution();
		}
	}
	
	public void AddNearByAgent(String agent)
	{
		if(!agent.equals(this._name) && !_nearbyAgents.contains(agent))
		{
			this._nearbyAgents.add(agent);
		}
	}
	
	
	/**
	 * Specifies that the agent must give an answer to a received
	 * SpeechAct
	 * @param speechAct - the SpeechAct that needs an answer 
	 */
	public void AnswerToSpeechAct(SpeechAct speechAct) {
	    _speechAct = speechAct;
	}
	
	private Name applyPerspective(Name n, String agentName)
	{
		Name newName = (Name) n.clone();
		ArrayList<Symbol> symbols = newName.GetLiteralList();
		
		//I'm changing directly the received name; not a good thing to do
		for(int i = 0; i < symbols.size(); i++)
		{
			if(symbols.get(i).getName().equals(agentName))
			{
				symbols.set(i, new Symbol(Constants.SELF));
			}
		}
		
		return newName;
	}
	
	public void AppraiseSelfActionFailed(Event e)
	{
		_deliberativeLayer.AppraiseSelfActionFailed(e);
	}
	
	@Override
	public void clearEvents() {
		_reactiveLayer.clearEvents();	
	}
	
	/**
	 * Gets the agent's name that is displayed externally
	 * @return the agent's external name
	 */
	public String displayName() {
	    return _displayName;
	}

	public void EnforceCopingStrategy(String coping)
	{
		_deliberativeLayer.EnforceCopingStrategy(this, coping);
		_reactiveLayer.EnforceCopingStrategy(coping);
	}
	
	private ValuedAction FilterSpeechAction(ValuedAction action)
	{
		ValuedAction aux=null;
		
		if(action != null)
		{
			String actionName = action.GetAction().GetFirstLiteral().toString();
			if(_dialogManager.CanSpeak() || !SpeechAct.isSpeechAct(actionName))
			{
				aux = action;
			}
		}
		
		return aux;
	}
	
	@Override
	public ActionTendencies getActionTendencies() {
		return _reactiveLayer.getActionTendencies();
	}
	
	/** 
	 * Gets the compound cue mechanism of the agent
	 * @return the compound cue mechanism
	 * added by Meiyii 19/11/09
	 */	
	public ICompoundCue getCompoundCue() {
		return _compoundCue;
	}
	
	/**
	 * Gets the agent's Deliberative Layer that you can use
	 * to get access to Deliberative structures such as 
	 * the goals and planner
	 * @return the agent's Deliberative Layer
	 */
	public DeliberativeProcess getDeliberativeLayer()
	{
		return this._deliberativeLayer;
	}
	
	public EmotionalReactionTreeNode getEmotionalReactions()
	{
		return _reactiveLayer.getEmotionalReactions();
	}
	
	public EmotionalState getEmotionalState()
	{
		return _emotionalState;
	}
	
	@Override
	public Collection<Event> getEvents() {
		return _reactiveLayer.getEvents();
	}
	
	public Memory getMemory()
	{
		return _memory;
	}
	
	public MotivationalState getMotivationalState()
	{
		return _motivationalState;
	}
	
	/**
	 * Gets the name of the agent
	 * @return the agent's name
	 */
	public String getName() {
		return _name;
	}
	
	public Collection<String> getNearByAgents()
	{
		return this._nearbyAgents;
	}
	
	/**
	 * Gets the agent's Reactive Layer that you can use
	 * to get access to reactive structures such as 
	 * ActionTendencies and  EmotionalReactions
	 * @return the agent's Reactive Layer
	 */
	public ReactiveProcess getReactiveLayer()
	{
		return this._reactiveLayer;
	}
	
	
	/** 
	 * Gets the spread activate mechanism of the agent
	 * @return the spread activate mechanism
	 *  added by Meiyii 19/11/09
	 */	
	public ISpreadActivate getSpreadActivate() {
		return _spreadActivate;
	}
	
	/** 
	 * Gets the commonalities mechanism of the agent
	 * @return the commonalities mechanism
	 *  added by Meiyii 18/03/10
	 */	
	public ICommonalities getCommonalities() {
		return _commonalities;
	}
	
	public HashMap<String,ModelOfOther> getToM()
	{
		return this._ToM;
	}
	
	public void initializeModelOfOther(String name)
	{
		if(!_ToM.containsKey(name))
		{
			ModelOfOther model = new ModelOfOther(name, this);
			_ToM.put(name, model);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void LoadAgentState(String fileName) 
		throws IOException, ClassNotFoundException{
		
		
		FileInputStream in = new FileInputStream(fileName);
		ObjectInputStream s = new ObjectInputStream(in);
		this._ToM = (HashMap<String, ModelOfOther>) s.readObject();
		this._nearbyAgents = (ArrayList<String>) s.readObject();
		this._deliberativeLayer = (DeliberativeProcess) s.readObject();
		this._reactiveLayer = (ReactiveProcess) s.readObject();
		this._emotionalState = (EmotionalState) s.readObject();
		this._memory = (Memory) s.readObject();
		this._motivationalState = (MotivationalState) s.readObject();
		this._dialogManager = (DialogManager) s.readObject();
		this._role = (String) s.readObject();
		this._name = (String) s.readObject();
		this._sex = (String) s.readObject();
		this._speechAct = (SpeechAct) s.readObject();
		this._currentEmotion = ((Short) s.readObject()).shortValue();
		this._displayName = (String) s.readObject();
		this._showStateWindow = ((Boolean) s.readObject()).booleanValue();
		this._actionsForExecution = (ArrayList<ValuedAction>) s.readObject();
		this._perceivedEvents = (ArrayList<Event>) s.readObject();
		this._saveDirectory = (String) s.readObject();
		s.close();
		in.close();
		
		AgentSimulationTime.LoadState(fileName+"-Timer.dat");
		ActionLibrary.LoadState(fileName+"-ActionLibrary.dat");
		
		//_remoteAgent.LoadState(fileName+"-RemoteAgent.dat");
	}
	
	private void loadPersonality(String personalityFile, short agentPlatform, ArrayList<String> goalList) 
		throws	ParserConfigurationException, SAXException, IOException, UnknownGoalException{
		
		AgentLogger.GetInstance().log("LOADING Personality: " + personalityFile);
		AgentLoaderHandler c = new AgentLoaderHandler(this,_reactiveLayer,_deliberativeLayer,_emotionalState);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(personalityFile), c);

		//The ION Agent does not load the goals initially from the personality file, therefore we
		//must clear all the goals loaded.
		//if(agentPlatform == AgentPlatform.ION){
		//	_deliberativeLayer.RemoveAllGoals();
		//}

		//The WorldSimulator Agent loads additional goals provided in the starting goal list
		if(agentPlatform == AgentPlatform.WORLDSIM){
			ListIterator<String> lt = goalList.listIterator();
			String goal;
			String goalName;
			StringTokenizer st;
			float impOfSuccess;
			float impOfFailure;
			while(lt.hasNext()) {
				goal = (String) lt.next();
				st = new StringTokenizer(goal, "|");
				goalName = st.nextToken();
				impOfSuccess = Float.parseFloat(st.nextToken());
				impOfFailure = Float.parseFloat(st.nextToken());

				_deliberativeLayer.AddGoal(this, goalName, impOfSuccess, impOfFailure);   
			}	
		}
	}
	
	private void UpdateModelOfOthers(Event e)
	{
		ModelOfOther m;
		String subject;
		BaseEmotion perceivedEmotion;
		BaseEmotion predictedEmotion;
		AppraisalVector v;
		Reaction r;
		
		
		subject = e.GetSubject();
		if(!subject.equals(_name))
		{
			m = _ToM.get(subject);
			if(m != null)
			{
				//if the perceived action corresponds to an emotion expression of other, we 
				//should update its action tendencies accordingly
				perceivedEmotion = m.getActionTendencies().RecognizeEmotion(m, e.toStepName());
				if(perceivedEmotion != null)
				{
					predictedEmotion = m.getEmotionalState().GetEmotion(perceivedEmotion.GetHashKey());
					if(predictedEmotion == null)
					{
						v = Appraisal.InverseOCCAppraisal(perceivedEmotion, m.getEmotionalState());
						//updating other's emotional state
						m.getEmotionalState().UpdateEmotionalState(perceivedEmotion, m);
						//upating other's emotional reactions
						if(v.getAppraisalVariable(AppraisalVector.LIKE) != 0)
						{
							LikeRelation.getRelation(subject, e.GetTarget()).setValue(m.getMemory(), 
									v.getAppraisalVariable(AppraisalVector.LIKE));
						}
						if(v.getAppraisalVariable(AppraisalVector.DESIRABILITY) != 0 ||
								v.getAppraisalVariable(AppraisalVector.PRAISEWORTHINESS) != 0)
						{
							r = new Reaction(perceivedEmotion.GetCause());
							r.setDesirability(v.getAppraisalVariable(AppraisalVector.DESIRABILITY));
							r.setPraiseworthiness(v.getAppraisalVariable(AppraisalVector.PRAISEWORTHINESS));
							m.getEmotionalReactions().AddEmotionalReaction(r);
						}
					}
				}
			}
		}
	}
	
	private void UpdateMemory(AgentModel am, Event e)
	{
		Event e2 = e.ApplyPerspective(am.getName());
		am.getMemory().getEpisodicMemory().StoreAction(am.getMemory(), e2);
		am.getMemory().getSemanticMemory().Tell(ACTION_CONTEXT, e2.toName().toString());
	}
	
	/**
	 * Perceives a given event from the virtual world
	 * @param e - the Event to perceive
	 */
	public void PerceiveEvent(Event e) 
	{   
		synchronized (this)
		{	
			_perceivedEvents.add(e);
		}
	}
	
	public void PerceiveLookAt(String subject, String target)
	{
		KnowledgeSlot knownInfo;
		KnowledgeSlot property;
		Name propertyName;
		String auxTarget;
		
		if(target.equals(_name))
		{
			auxTarget = Constants.SELF;
		}
		else
		{
			auxTarget = target;
		}
		
		for(String other : _nearbyAgents)
		{
			if(other.equals(subject))
			{
				ModelOfOther m = _ToM.get(other);
				knownInfo = _memory.getSemanticMemory().GetObjectDetails(auxTarget);
				if(knownInfo!= null)
				{
					for(String s : knownInfo.getKeys())
					{	
						property = knownInfo.get(s);
						propertyName = Name.ParseName(target + "(" + property.getName() + ")");
						m.getMemory().getSemanticMemory().Tell(applyPerspective(propertyName,other), property.getValue());
					}
				}		
			}	
		}
	}
	
	public void PerceivePropertyChanged(String ToM, Name propertyName, String value)
	{
		AgentLogger.GetInstance().logAndPrint("PropertyChanged: " + ToM + " " + propertyName + " " + value);
		_memory.getSemanticMemory().Tell(applyPerspective(propertyName, _name), value);
		
		if(ToM.equals(Constants.UNIVERSAL.toString()))
		{
			for(String other : _nearbyAgents)
			{
				ModelOfOther m = _ToM.get(other);
				m.getMemory().getSemanticMemory().Tell(applyPerspective(propertyName,other), value);
			}
		}
		else if(!ToM.equals(_name))
		{
			ModelOfOther m = _ToM.get(ToM);
			if(m != null)
			{
				m.getMemory().getSemanticMemory().Tell(applyPerspective(propertyName,ToM), value);
			}
		}
		
	}

	public void PerceivePropertyChanged(String ToM,String subject, String property, String value)
	{
		String newSubject = subject;
		Name propertyName;
		
		if(subject.equals(_name))
		{
			newSubject = Constants.SELF;
		}
		
		propertyName = Name.ParseName(newSubject + "(" + property + ")");
		
		AgentLogger.GetInstance().logAndPrint("PropertyChanged: " + ToM + " " + propertyName + " " + value);
		
		_memory.getSemanticMemory().Tell(propertyName, value);
		
		if(ToM.equals(Constants.UNIVERSAL.toString()))
		{
			for(String other : _nearbyAgents)
			{
				if(subject.equals(other))
				{
					newSubject = Constants.SELF; 
				}
				else
				{
					newSubject = subject;
				}
				ModelOfOther m = _ToM.get(other);
				propertyName = Name.ParseName(newSubject + "(" + property + ")");
				m.getMemory().getSemanticMemory().Tell(propertyName, value);
			}
		}
		else if(!ToM.equals(_name))
		{
			if(subject.equals(ToM))
			{
				newSubject = Constants.SELF;
			}
			else
			{
				newSubject = subject; 
			}
			
			ModelOfOther m = _ToM.get(ToM);
			propertyName = Name.ParseName(newSubject + "(" + property + ")");
			m.getMemory().getSemanticMemory().Tell(propertyName, value);
			
		}
	}
	
	public void PerceivePropertyRemoved(String subject, String property)
	{
		if(subject.equals(_name))
		{
			subject = Constants.SELF;
		}
		
		Name propertyName = Name.ParseName(subject + "(" + property + ")");
		_memory.getSemanticMemory().Retract(propertyName);
		
	}
	
	
	
	public void RemoveNearByAgent(String entity)
	{
		this._nearbyAgents.remove(entity);
	}
	
	/**
	 * Resets the agent's reasoning layers (deliberative + cognitive)
	 *
	 */
	public void Reset() {
		//_emotionalState.Clear();
		_dialogManager.Reset();
		_reactiveLayer.Reset();
		_deliberativeLayer.Reset();
		_perceivedEvents.clear();
	}
	
	/**
	 * Gets the agent's role
	 * @return the role of the agent (Victim, Bully, etc)
	 */
	public String role() {
		return _role;
	}
	
	/**
	 * Runs the agent, endless loop until there is a shutdown
	 */
	public void Run() {
		ValuedAction action;
		long updateTime = System.currentTimeMillis();
		
		while (!_shutdown) {
			try {
				
			    if(_remoteAgent.isShutDown()) {
				    _shutdown = true;
			    }
				
			    //updates the agent's simulation timer
			    AgentSimulationTime.GetInstance().Tick();
			    
			    _numberOfCycles++;
			    long startCycleTime = System.currentTimeMillis();
			    
				if (_remoteAgent.isRunning()) {
					//decay the agent's emotional state
					//self
					_emotionalState.Decay();
					_motivationalState.Decay();
					_dialogManager.DecayCauseIDontHaveABetterName(_memory);
					
					//others
					//TODO question: apply decay to all models or only to the agents nearby?
					for(String other: _nearbyAgents)
					{
						ModelOfOther m = _ToM.get(other);
						m.getEmotionalState().Decay();
						m.getMotivationalState().Decay();
					}
					
					//perceives new events
					synchronized (this)
					{
						for(ListIterator<Event> li = this._perceivedEvents.listIterator(); li.hasNext();)
						{
							Event e = (Event) li.next();
							AgentLogger.GetInstance().log("Perceiving event: " + e.toName());
							
							
							UpdateModelOfOthers(e);
							
							//updating the memory of all agents that perceived the event
							//self 
							UpdateMemory(this,e);
							//ToM of Others
							for(String other : _nearbyAgents)
							{
								ModelOfOther m = _ToM.get(other);
								UpdateMemory(m, e);
							}
							
							if(SpeechAct.isSpeechAct(e.GetAction()))
							{
								_dialogManager.UpdateDialogState(e, _memory);
							}
									
							//adds the event to the deliberative and reactive layers so that they can appraise
							//the events
							
							_reactiveLayer.AddEvent(e);
							_deliberativeLayer.AddEvent(e);
						}
						this._perceivedEvents.clear();
					}
					
					//if there was new data or knowledge added we must apply inference operators
					//update any inferred property to the outside and appraise the events
					if(_memory.getEpisodicMemory().HasNewData() ||
							_memory.getSemanticMemory().HasNewKnowledge())
					{
						
						//calling the KnowledgeBase inference process
						_memory.getSemanticMemory().PerformInference(this);
						
						synchronized (_memory.getSemanticMemory())
						{
							ArrayList<KnowledgeSlot> facts = _memory.getSemanticMemory().getNewFacts();
							
							for(ListIterator<KnowledgeSlot> li = facts.listIterator();li.hasNext();)
							{
								KnowledgeSlot ks = (KnowledgeSlot) li.next();
								if(ks.getName().startsWith(Constants.SELF))
								{
									_remoteAgent.ReportInternalPropertyChange(this._name,Name.ParseName(ks.getName()),
											ks.getValue());
								}
							}
						}
					}
					
					//Appraise the events and changes in data
					for(Event e : _reactiveLayer._eventPool)
					{
						_reactiveLayer.Appraisal(e,this);
						
						for(ModelOfOther m : _ToM.values())
						{
							_reactiveLayer.Appraisal(e,m);
						}
					}
					
					_reactiveLayer._eventPool.clear();
					
					for(Event e : _deliberativeLayer._eventPool)
					{	
						for(ModelOfOther m : _ToM.values())
						{
							_deliberativeLayer.AppraiseForOthers(e,m);
						}
						
					}
					
					_deliberativeLayer.Appraisal(this);
					
					
					
				    _reactiveLayer.Coping(this);
					_deliberativeLayer.Coping(this);
				
					if(_remoteAgent.FinishedExecuting() && _remoteAgent.isRunning()) {
						
						//action = FilterSpeechAction(_reactiveLayer.GetSelectedAction());
						action = _reactiveLayer.GetSelectedAction();
						
						if(action != null) 
						{
							_reactiveLayer.RemoveSelectedAction();
							_remoteAgent.AddAction(action);
						}
						else
						{
							action = FilterSpeechAction(_deliberativeLayer.GetSelectedAction());
							if(action != null)
							{
								_deliberativeLayer.RemoveSelectedAction();
								_remoteAgent.AddAction(action);
							}
						}
		
						_remoteAgent.ExecuteNextAction(this);
					}
					
					if(System.currentTimeMillis() - updateTime > 1000)
					{
						if(_showStateWindow && _agentDisplay != null) 
						{
							_agentDisplay.update();
						}
						
						_remoteAgent.ReportInternalState(_emotionalState);
						
						/*ActiveEmotion auxEmotion = EmotionalState.GetInstance().GetStrongestEmotion();
						short nextEmotion;
						if(auxEmotion != null) {
						    nextEmotion = auxEmotion.GetType(); 
						}
						else nextEmotion = EmotionType.NEUTRAL;
						
						if(_currentEmotion != nextEmotion) {
						    _currentEmotion = nextEmotion;
						    _remoteAgent.ExpressEmotion(EmotionType.GetName(_currentEmotion));
						}*/
						
						updateTime = System.currentTimeMillis();
					}
					
					if(_saveRequest)
					{
						_saveRequest = false;
						SaveAgentState(this.getName());
					}
				}
				
				long cycleExecutionTime = System.currentTimeMillis() - startCycleTime;
				_totalexecutingtime += cycleExecutionTime;
				//System.out.println("Cycle execution (in Millis): " + cycleExecutionTime);
				//System.out.println("Average time per cycle (in Millis): " + _totalexecutingtime / _numberOfCycles);
				Thread.sleep(10);
			}
			catch (Exception ex) {
			    //_shutdown = true;
			    ex.printStackTrace();
			    //System.out.println(ex);
			}
		}
	}
	
	public void RequestAgentSave()
	{
		this._saveRequest = true;
	}
	
	
	private void SaveAgentState(String agentName)
	{
		String fileName = _saveDirectory + agentName;

		// Moving all the STEM into AM before saving because when it loads the next time
		// a new episode will be started. This is to prevent the rest of the STEM events
		// being stored in the wrong episode
		// Meiyii 13/09/10
		//_memory.getEpisodicMemory().MoveSTEMtoAM();
		
		AgentSimulationTime.SaveState(fileName+"-Timer.dat");
		ActionLibrary.SaveState(fileName+"-ActionLibrary.dat");
		_remoteAgent.SaveState(fileName+"-RemoteAgent.dat");

		try
		{
			FileOutputStream out = new FileOutputStream(fileName);
			ObjectOutputStream s = new ObjectOutputStream(out);

			s.writeObject(_ToM);
			s.writeObject(_nearbyAgents);
			s.writeObject(_deliberativeLayer);
			s.writeObject(_reactiveLayer);
			s.writeObject(_emotionalState);
			s.writeObject(_memory);
			s.writeObject(_motivationalState);
			s.writeObject(_dialogManager);
			s.writeObject(_role);
			s.writeObject(_name);
			s.writeObject(_sex);
			s.writeObject(_speechAct);
			s.writeObject(new Short(_currentEmotion));
			s.writeObject(_displayName);
			s.writeObject(new Boolean(_showStateWindow));
			s.writeObject(_actionsForExecution);
			s.writeObject(_perceivedEvents);
			s.writeObject(_saveDirectory);
			s.flush();
			s.close();
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected ValuedAction SelectBestAction() {
		
		ValuedAction bestAction = null;
		ValuedAction action;
		int removeHere=-1;
		
		for(int i=0; i < _actionsForExecution.size(); i++)
		{
			action = (ValuedAction) _actionsForExecution.get(i);
			if(bestAction == null || action.GetValue(_emotionalState) > bestAction.GetValue(_emotionalState))
			{
				bestAction = action;
				removeHere = i;
			}
		}
		
		if(bestAction != null)
		{
			_actionsForExecution.remove(removeHere);
		}
		return bestAction;
	}
	
	/** 
	 * Set the compound cue mechanism object of the agent
	 * @param compoundCue
	 *  added by Meiyii 19/11/09
	 */	
	public void setCompoundCue(ICompoundCue compoundCue) {
		this._compoundCue = compoundCue;
	}
	
	/** 
	 * Set the spread activate mechanism object of the agent
	 * @param spreadActivate
	 *  added by Meiyii 19/11/09
	 */	
	public void setSpreadActivate(ISpreadActivate spreadActivate) {
		this._spreadActivate = spreadActivate;
	}
		
	/** 
	 * Set the commonalities mechanism object of the agent
	 * @param commonalities
	 *  added by Meiyii 18/03/10
	 */	
	public void setCommonalities(ICommonalities commonalities) {
		this._commonalities = commonalities;
	}
	
	/**
	 * Gets the gender of the agent
	 * @return the agent's sex
	 */
	public String sex() 
	{
		return _sex;
	}

	public ActiveEmotion simulateAppraisal(String action, String name, ArrayList<String> parameters)
	{
		ArrayList<BaseEmotion> emotions;
		BaseEmotion em;
		Event e;
		ActiveEmotion aem;
		ActiveEmotion maxEmotion = null;
		
		if(action.equals("INSERT_CHARACTER")||action.equals("INSERT_OBJECT"))
		{
			e = new Event(Constants.SELF, "look-at", name, EventType.ACTION, ActionEvent.SUCCESS);
			int like = Math.round(LikeRelation.getRelation(Constants.SELF, name).getValue(_memory));
			AppraisalVector v = new AppraisalVector();
			v.setAppraisalVariable(AppraisalVector.LIKE, like);
			em = (BaseEmotion) Appraisal.GenerateSelfEmotions(this, e, v).get(0);
			return _emotionalState.DetermineActiveEmotion(em);
		}
		else if(action.equals("ACT_FOR_CHARACTER"))
		{
			if(parameters.size() == 0)
			{
				e = new Event(Constants.SELF,name,null);
			}
			else
			{
				e = new Event(Constants.SELF,name, (String) parameters.get(0));
				for(int i = 1; i < parameters.size(); i++)
				{
					e.AddParameter(new Parameter("param",parameters.get(i)));
				}
			}
			
			Reaction r = ReactiveProcess.Evaluate(this, e);
			emotions = Appraisal.GenerateSelfEmotions(this, e, ReactiveProcess.translateEmotionalReaction(r));
			ListIterator<BaseEmotion> li = emotions.listIterator();
			
			while(li.hasNext())
			{
				em = (BaseEmotion) li.next();
				aem = _emotionalState.DetermineActiveEmotion(em);
				if(aem != null && (maxEmotion == null || aem.GetIntensity() > maxEmotion.GetIntensity()))
				{
					maxEmotion = aem;
				}	
			}
			
			return maxEmotion;
		}
		else return null;
	}
	
	public void SpeechStarted()
	{
		_dialogManager.SpeechStarted();
	}

	/**
	 * Starting the agent
	 * added by Meiyii 19/11/09
	 */
	public void StartAgent()
	{
		try{
			_remoteAgent.start();
	        
			if(_showStateWindow && !VersionChecker.runningOnAndroid()){
				_agentDisplay = new AgentDisplay(this);
			}
			
			this.Run();
		}
		catch (Exception e) {
			e.printStackTrace();
			this.terminateExecution();
		}
	}

	private void terminateExecution(){
		_deliberativeLayer.ShutDown();
		_reactiveLayer.ShutDown();
		_remoteAgent.ShutDown();
		if(_showStateWindow && _agentDisplay != null) _agentDisplay.dispose();
	}

	/** returns a String that contains the serialized internal state of the agent
	 *  in a String format*/
	public String getSerializedState() 
	{
		try
		{
		
			ByteArrayOutputStream b = new ByteArrayOutputStream();		
			ObjectOutputStream s = new ObjectOutputStream(b);

			s.writeObject(_ToM);
			//s.writeObject(_nearbyAgents);
			s.writeObject(_deliberativeLayer);
			s.writeObject(_reactiveLayer);
			s.writeObject(_emotionalState);
			s.writeObject(_memory);
			s.writeObject(_motivationalState);
			s.writeObject(_dialogManager);
			//s.writeObject(_role);
			//s.writeObject(_name);
			//s.writeObject(_sex);
			s.writeObject(_speechAct);
			s.writeObject(new Short(_currentEmotion));
			s.writeObject(_displayName);
			//s.writeObject(new Boolean(_showStateWindow));
			s.writeObject(_actionsForExecution);
			s.writeObject(_perceivedEvents);
			//s.writeObject(_saveDirectory);
			AgentSimulationTime.SaveState(s);
			s.flush();
			s.close();
			return BinaryStringConverter.encodeBinaryToString(b.toByteArray());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** sets the internal state of the agent to the state which is contained
	 *  in the passed string */
	public void setSerializedState(String state) 
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(
				BinaryStringConverter.decodeStringToBinary(state));
		
			ObjectInputStream s = new ObjectInputStream(in);
			this._ToM = (HashMap<String, ModelOfOther>) s.readObject();
			//this._nearbyAgents = (ArrayList<String>) s.readObject();
			this._deliberativeLayer = (DeliberativeProcess) s.readObject();
			this._reactiveLayer = (ReactiveProcess) s.readObject();
			this._emotionalState = (EmotionalState) s.readObject();
			this._memory = (Memory) s.readObject();
			this._motivationalState = (MotivationalState) s.readObject();
			this._dialogManager = (DialogManager) s.readObject();
			//this._role = (String) s.readObject();
			//this._name = (String) s.readObject();
			//this._sex = (String) s.readObject();
			this._speechAct = (SpeechAct) s.readObject();
			this._currentEmotion = ((Short) s.readObject()).shortValue();
			this._displayName = (String) s.readObject();
			//this._showStateWindow = ((Boolean) s.readObject()).booleanValue();
			this._actionsForExecution = (ArrayList<ValuedAction>) s.readObject();
			this._perceivedEvents = (ArrayList<Event>) s.readObject();
			AgentSimulationTime.LoadState(s);
			//this._saveDirectory = (String) s.readObject();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}


}
