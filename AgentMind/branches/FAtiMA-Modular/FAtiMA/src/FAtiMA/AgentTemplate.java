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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.logging.Logger;



import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import FAtiMA.Display.AgentDisplay;
import FAtiMA.culture.CulturalDimensions;
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
import FAtiMA.util.parsers.CultureLoaderHandler;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;

import FAtiMA.memory.ICompoundCue;
import FAtiMA.memory.ISpreadActivate;
import FAtiMA.memory.ICommonalities;

public class AgentTemplate {
	
	protected HashMap<String, IComponent> _components;
	
	protected EmotionalState _emotionalState;
	
	protected Memory _memory;
	
	
	protected boolean _shutdown;
	protected DeliberativeProcess _deliberativeLayer;
	protected ReactiveProcess _reactiveLayer;
	

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

	private String _saveDirectory;
	public static final String MIND_PATH = "data/characters/minds/";
	public static final String MIND_PATH_ANDROID = "sdcard/data/characters/minds/";	
	
	private boolean _saveRequest = false;

	public AgentTemplate(short agentPlatform, String host, int port, String saveDirectory, String name, boolean displayMode, 
			String sex, String role, 
			String displayName, String actionsFile, 
			String goalsFile, HashMap<String,String> properties, ArrayList<String> goalList) {
		
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
		
		
		_emotionalState = new EmotionalState();
		
		_memory = new Memory();
		// creating a new episode when the agent starts 13/09/10
		_memory.getEpisodicMemory().StartEpisode(_memory);

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
			AddComponent(_reactiveLayer,"Reactive");
			
			_deliberativeLayer = new DeliberativeProcess(_name,goalLibrary,planner);
			AddComponent(_deliberativeLayer,"Deliberative");
	
			String personalityFile = mind_path + "roles/" + role + "/" + role + ".xml";
			loadPersonality(personalityFile,agentPlatform, goalList);
			
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
	
	public AgentTemplate(short agentPlatform, String host, int port, String directory, String fileName)
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
	
	public void AddComponent(IComponent c)
	{
		this._components.put(c.name(), c);
		c.Initialize(this);
	}
	
	public void RemoveComponent(IComponent c)
	{
		this._components.remove(c.name());
	}

	
	public void AppraiseSelfActionFailed(Event e)
	{
		_deliberativeLayer.AppraiseSelfActionFailed(e);
	}
	
	/**
	 * Gets the agent's name that is displayed externally
	 * @return the agent's external name
	 */
	public String displayName() {
	    return _displayName;
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
	
	public EmotionalState getEmotionalState()
	{
		return _emotionalState;
	}

	
	public Memory getMemory()
	{
		return _memory;
	}

	
	/**
	 * Gets the name of the agent
	 * @return the agent's name
	 */
	public String getName() {
		return _name;
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
	
	
	@SuppressWarnings("unchecked")
	public void LoadAgentState(String fileName) 
		throws IOException, ClassNotFoundException{
		
		
		FileInputStream in = new FileInputStream(fileName);
		ObjectInputStream s = new ObjectInputStream(in);
		//this._ToM = (HashMap<String, ModelOfOther>) s.readObject();
		//this._nearbyAgents = (ArrayList<String>) s.readObject();
		this._deliberativeLayer = (DeliberativeProcess) s.readObject();
		this._reactiveLayer = (ReactiveProcess) s.readObject();
		this._emotionalState = (EmotionalState) s.readObject();
		this._memory = (Memory) s.readObject();
		//this._motivationalState = (MotivationalState) s.readObject();
		//this._dialogManager = (DialogManager) s.readObject();
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
		String auxTarget;
		
		if(target.equals(_name))
		{
			auxTarget = Constants.SELF;
		}
		else
		{
			auxTarget = target;
		}
		
		for(IComponent c : this._components.values())
		{
			c.LookAtPerception(subject, auxTarget);
		}
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
	
	public void PerceivePropertyChanged(String ToM, Name propertyName, String value)
	{
		AgentLogger.GetInstance().logAndPrint("PropertyChanged: " + ToM + " " + propertyName + " " + value);
		
		for(IComponent c : this._components.values())
		{
			c.PropertyChangedPerception(ToM, applyPerspective(propertyName, _name), value);
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
		
		PerceivePropertyChanged(ToM,propertyName,value);
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
	
	
	
	
	/**
	 * Resets the agent's reasoning layers (deliberative + cognitive)
	 *
	 */
	public void Reset() {
		for(IComponent c : this._components.values())
		{
			c.reset();
		}
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
					
					long time = AgentSimulationTime.GetInstance().Time();
					
					for(IComponent c : this._components.values())
					{
						c.decay(time);
					}
					
					//perceives new events
					synchronized (this)
					{
						for(ListIterator<Event> li = this._perceivedEvents.listIterator(); li.hasNext();)
						{
							Event e = (Event) li.next();
							AgentLogger.GetInstance().log("Perceiving event: " + e.toName());
							
							
							for(IComponent c : this._components.values())
							{
								c.update(e);
								c.appraisal(e,this);
							}
							
							
									
	
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
				
					
					for(IComponent c : this._components.values())
					{
						c.coping(this);
					}
				
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
	 * Gets the gender of the agent
	 * @return the agent's sex
	 */
	public String sex() 
	{
		return _sex;
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