package FAtiMA.Core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import FAtiMA.Core.Display.AgentDisplay;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAdvancedPerceptionsComponent;
import FAtiMA.Core.componentTypes.IAffectDerivationComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IBehaviourComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.componentTypes.IProcessEmotionComponent;
import FAtiMA.Core.componentTypes.IProcessExternalRequestComponent;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.emotionalState.NeutralEmotion;
import FAtiMA.Core.exceptions.ActionsParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.RequiredComponentException;
import FAtiMA.Core.exceptions.UndefinedComponentException;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.goals.GoalLibrary;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.Core.plans.IDetectThreatStrategy;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.IONRemoteAgent;
import FAtiMA.Core.sensorEffector.RemoteAgent;
import FAtiMA.Core.sensorEffector.SpeechAct;
import FAtiMA.Core.sensorEffector.WorldSimulatorRemoteAgent;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.VersionChecker;
import FAtiMA.Core.util.enumerables.AgentPlatform;
import FAtiMA.Core.util.parsers.BinaryStringConverter;
import FAtiMA.Core.util.parsers.CentralXMLParser;
import FAtiMA.Core.util.parsers.EmotionDispositionsLoaderHandler;
import FAtiMA.Core.util.parsers.MemoryLoaderHandler;
import FAtiMA.Core.util.writers.MemoryWriter;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;


public class AgentCore implements Serializable, AgentModel, IGetModelStrategy {

	private static final long serialVersionUID = 1L;
	public static final String MIND_PATH = "data/characters/minds/";
	public static final String MIND_PATH_ANDROID = "sdcard/data/characters/minds/";
	public static final String MEMORY_FILENAME = "XMLMemory";
	
	public static final Name ACTION_CONTEXT = Name.ParseName("ActionContext()");

	//components
	protected HashMap<String, IComponent> _generalComponents;
	protected ArrayList<IProcessEmotionComponent> _processEmotionComponents;
	protected ArrayList<IBehaviourComponent> _behaviourComponents;
	protected ArrayList<IModelOfOtherComponent> _modelOfOtherComponents;
	protected ArrayList<IProcessExternalRequestComponent> _processExternalRequestComponents;
	protected ArrayList<IAdvancedPerceptionsComponent> _processPerceptionsComponents;
	protected ArrayList<IAffectDerivationComponent> _affectDerivationComponents;
	protected ArrayList<IAppraisalDerivationComponent> _appraisalComponents;
	
	//Strategies
	protected IDetectThreatStrategy _detectThreatStrat;
	
	//Data structures
	protected CentralXMLParser _centralParser;
	protected EmotionalState _emotionalState;
	protected Memory _memory;
	protected GoalLibrary _goalLibrary;
	protected ActionLibrary _actionLibrary;
	
	protected boolean _shutdown;
	protected ArrayList<ValuedAction> _actionsForExecution;
	protected ArrayList<Event> _perceivedEvents;
	protected RemoteAgent _remoteAgent;
	protected String _role;
	protected String _name; //the agent's name
	protected String _sex;
	protected String _displayName; 
	protected SpeechAct _speechAct;
	protected String _currentEmotion;
	protected long _numberOfCycles;
	protected long _totalexecutingtime=0;

	protected AgentDisplay _agentDisplay;
	protected boolean _showStateWindow;
	protected Logger _logger;

	private String _saveDirectory;
	private boolean _saveRequest = false;
	private boolean _agentLoaded = false;
	private boolean _saveMemoryRequest = false;
	
	private MemoryWriter _memoryWriter;
	private IGetModelStrategy _strat;


	public AgentCore(String name){
		_name = name;
		_shutdown = false;
		_numberOfCycles = 0;
		_currentEmotion = NeutralEmotion.NAME; //neutral emotion - no emotion
		_actionsForExecution = new ArrayList<ValuedAction>();
		_perceivedEvents = new ArrayList<Event>();
		_saveDirectory = "";
		
		_emotionalState = new EmotionalState();
		_memory = new Memory();
		// creating a new episode when the agent starts 13/09/10
		_memory.getEpisodicMemory().StartEpisode(_memory);
		_memoryWriter = new MemoryWriter(_memory);
		_strat = this;
		_actionLibrary = new ActionLibrary();
		
		_generalComponents = new HashMap<String,IComponent>();
		_processEmotionComponents = new ArrayList<IProcessEmotionComponent>();
		_behaviourComponents = new ArrayList<IBehaviourComponent>();
		_modelOfOtherComponents = new ArrayList<IModelOfOtherComponent>();
		_processExternalRequestComponents = new ArrayList<IProcessExternalRequestComponent>();
		_processPerceptionsComponents = new ArrayList<IAdvancedPerceptionsComponent>();
		_affectDerivationComponents = new ArrayList<IAffectDerivationComponent>();
		_appraisalComponents = new ArrayList<IAppraisalDerivationComponent>();
		
		AgentSimulationTime.GetInstance(); //This call will initialize the timer for the agent's simulation time
	}


	public void initialize(String scenariosFile, String scenarioName, String agentName) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException{
		
		if (VersionChecker.runningOnAndroid()){
			ConfigurationManager.initialize(MIND_PATH_ANDROID,scenariosFile,scenarioName,agentName);
		}else{
			ConfigurationManager.initialize(MIND_PATH,scenariosFile,scenarioName,agentName);
		}
				
		try{
			if (ConfigurationManager.getAgentLoad())
			{
				_agentLoaded = true;
				agentCoreLoad(ConfigurationManager.getPlatform(), ConfigurationManager.getHost(), ConfigurationManager.getPort(), ConfigurationManager.getSaveDirectory(), agentName);
			}
			else
			{
				_agentLoaded = false;
				
				_showStateWindow = ConfigurationManager.getDisplayMode();
				_displayName = ConfigurationManager.getDisplayName();
				_role = ConfigurationManager.getRole();
				_sex = ConfigurationManager.getSex();
				_saveDirectory = ConfigurationManager.getSaveDirectory();
				_memory.setSaveDirectory(_saveDirectory);
				
				AgentLogger.GetInstance().initialize(_name,_showStateWindow);
				
				if(_showStateWindow && !VersionChecker.runningOnAndroid()){
					_agentDisplay = new AgentDisplay(this);
				}
	
				//loading agent memory from xml
				if (ConfigurationManager.getMemoryLoad())
				{
					_memory.setMemoryLoad(true);
					loadAgentMemory(_saveDirectory + MEMORY_FILENAME);
				}
				else
				{
					_memory.setMemoryLoad(false);
				}
				
				// Load Plan Operators
				_actionLibrary.LoadActionsFile(ConfigurationManager.getActionsFile(), this);
	
				// Load GoalLibrary
				_goalLibrary = new GoalLibrary(ConfigurationManager.getGoalsFile());
	
	
				//TODO:PARSETHEGOALS
				loadPersonality(ConfigurationManager.getPersonalityFile(),ConfigurationManager.getPlatform(),new ArrayList<String>());
				//Start the remote agent socket
				
				_remoteAgent = createNewRemoteAgent(ConfigurationManager.getPlatform(), ConfigurationManager.getHost(), ConfigurationManager.getPort(), ConfigurationManager.getAgentProperties());
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			AgentLogger.GetInstance().log("Exception: " + e);
			terminateExecution();
		}
	}


	private RemoteAgent createNewRemoteAgent(short aP, String host, int port, HashMap<String,String> properties) throws IOException{
		if(aP == AgentPlatform.WORLDSIM){
			properties.put("name", _name); 
			properties.put("role", _role);
			properties.put("sex", _sex);	
			return new WorldSimulatorRemoteAgent(host, port, this, properties);
		}else if (aP == AgentPlatform.ION){
			return new IONRemoteAgent(host, port, this);	
		}else{
			throw new RuntimeException("startRemoteAgent: AgentPlatform has an incorrect value:" + aP);
		}
	}
	
	public void agentCoreLoad(short agentPlatform, String host, int port, String directory, String fileName)
	{
		try{
			_shutdown = false;
			_numberOfCycles = 0;
			LoadAgentState(directory + fileName);
						
			AgentLogger.GetInstance().initialize(fileName,_showStateWindow);

			if(_showStateWindow && !VersionChecker.runningOnAndroid()){
				_agentDisplay = new AgentDisplay(this);
				for (IComponent c: this.getComponents())
				{
					AgentDisplayPanel panel = c.createDisplayPanel(this);
					if(panel != null)
					{
						this._agentDisplay.AddPanel(panel, c.name(),"");
					}
				}
				
			}
			
			// creating a new episode when the agent loads 13/09/10
			_memory.getEpisodicMemory().StartEpisode(_memory);
			_remoteAgent = this.createNewRemoteAgent(agentPlatform, host, port, new HashMap<String,String>());
			_remoteAgent.LoadState(directory + fileName + "-RemoteAgent.dat");
		}
		catch (Exception e) {
			e.printStackTrace();
			this.terminateExecution();
		}
	}

	public static Name applyPerspective(Name n, String agentName)
	{
		Name newName = (Name) n.clone();
		ArrayList<Symbol> symbols = newName.GetLiteralList();

		for(int i = 0; i < symbols.size(); i++)
		{
			if(symbols.get(i).getName().equals(agentName))
			{
				symbols.set(i, new Symbol(Constants.SELF));
			}
		}

		return newName;
	}
	
	public static Name removePerspective(Name n, String agentName)
	{
		Name newName = (Name) n.clone();
		ArrayList<Symbol> symbols = newName.GetLiteralList();

		for(int i = 0; i < symbols.size(); i++)
		{
			if(symbols.get(i).getName().equals(Constants.SELF))
			{
				symbols.set(i, new Symbol(agentName));
			}
		}

		return newName;
	}
	
	public boolean isSelf()
	{
		return true;
	}


	public void addComponent(IComponent c)
	{
		for(String dependency : c.getComponentDependencies()){
			if (!this._generalComponents.containsKey(dependency))
				throw new RequiredComponentException(c.name(),dependency);
		}
			
		
		this._generalComponents.put(c.name(), c);
		if(c instanceof IProcessEmotionComponent)
		{
			_processEmotionComponents.add((IProcessEmotionComponent) c);
		}
		if(c instanceof IBehaviourComponent)
		{
			_behaviourComponents.add((IBehaviourComponent) c);
		}
		if(c instanceof IModelOfOtherComponent)
		{
			_modelOfOtherComponents.add((IModelOfOtherComponent) c);
		}
		if(c instanceof IProcessExternalRequestComponent)
		{
			_processExternalRequestComponents.add((IProcessExternalRequestComponent) c);
		}
		if(c instanceof IAdvancedPerceptionsComponent)
		{
			_processPerceptionsComponents.add((IAdvancedPerceptionsComponent) c);
		}
		if(c instanceof IAffectDerivationComponent)
		{
			_affectDerivationComponents.add((IAffectDerivationComponent) c);
		}
		if(c instanceof IAppraisalDerivationComponent)
		{
			_appraisalComponents.add((IAppraisalDerivationComponent) c);
		}
		
		c.initialize(this);
		AgentDisplayPanel panel = c.createDisplayPanel(this);
		if(panel != null & _showStateWindow)
		{
			this._agentDisplay.AddPanel(panel, c.name(),"");
		}
	}
	
	public RemoteAgent getRemoteAgent()
	{
		return this._remoteAgent;
	}

	public IComponent getComponent(String name)
	{
		if(!this._generalComponents.containsKey(name))
		{
			throw new UndefinedComponentException(name);
		}
		else return this._generalComponents.get(name);
	}

	public Collection<IComponent> getComponents()
	{
		return this._generalComponents.values();
	}
	
	public Collection<IProcessExternalRequestComponent> getProcessExternalRequestComponents()
	{
		return this._processExternalRequestComponents;
	}
	
	public ActionLibrary getActionLibrary()
	{
		return _actionLibrary;
	}

	/*public void RemoveComponent(IComponent c)
	{
		this._components.remove(c.name());
	}*/


	public void PerceiveActionFailed(Event e)
	{
		Event e2 = e.ApplyPerspective(_name);
		
		for(IAdvancedPerceptionsComponent c : _processPerceptionsComponents)
		{
			c.actionFailedPerception(e2);
		}
	}

	/**
	 * Gets the agent's name that is displayed externally
	 * @return the agent's external name
	 */
	public String displayName() {
		return _displayName;
	}


	public EmotionalState getEmotionalState()
	{
		return _emotionalState;
	}


	public Memory getMemory()
	{
		return _memory;
	}

	public boolean getAgentLoad()
	{
		return _agentLoaded;
	}
	
	public GoalLibrary getGoalLibrary()
	{
		return _goalLibrary;
	}
	
	/**
	 * Gets the name of the agent
	 * @return the agent's name
	 */
	public String getName() {
		return _name;
	}


	public void setModelStrategy(IGetModelStrategy strat)
	{
		_strat = strat;
	}
	
	public void EnforceCopingStrategy(AgentModel am, String coping)
	{
		Goal g;
		coping = coping.toLowerCase();
		for(ListIterator<Goal> li = _goalLibrary.GetGoals();li.hasNext();)
		{
			g = (Goal) li.next();
			if(g.getName().toString().toLowerCase().startsWith(coping)
					|| (coping.equals("standup") && g.getName().toString().startsWith("ReplyNegatively")))
			{
				AgentLogger.GetInstance().logAndPrint("");
				AgentLogger.GetInstance().logAndPrint("Enforcing coping strategy: " + g.getName());
				AgentLogger.GetInstance().logAndPrint("");
				g.IncreaseImportanceOfFailure(am, 2);
				g.IncreaseImportanceOfSuccess(am, 2);
			}
		}
	}


	@SuppressWarnings("unchecked")
	public void LoadAgentState(String fileName) 
	throws IOException, ClassNotFoundException{


		FileInputStream in = new FileInputStream(fileName);
		ObjectInputStream s = new ObjectInputStream(in);
	
		this._emotionalState = (EmotionalState) s.readObject();
		this._memory = (Memory) s.readObject();
		this._goalLibrary = (GoalLibrary) s.readObject();
		this._actionLibrary = (ActionLibrary) s.readObject();
		//this._dialogManager = (DialogManager) s.readObject();
		this._role = (String) s.readObject();
		this._name = (String) s.readObject();
		this._sex = (String) s.readObject();
		this._speechAct = (SpeechAct) s.readObject();
		this._currentEmotion = ((String) s.readObject());
		this._displayName = (String) s.readObject();
		this._showStateWindow = ((Boolean) s.readObject()).booleanValue();
		this._actionsForExecution = (ArrayList<ValuedAction>) s.readObject();
		this._perceivedEvents = (ArrayList<Event>) s.readObject();
		this._saveDirectory = (String) s.readObject();
		
		Object stratObject = s.readObject();
		if (stratObject instanceof IGetModelStrategy)
		{
			this._strat = (IGetModelStrategy) stratObject;
		}
		else 
		{	
			if (stratObject instanceof String)
			{
				String stratObjectStr = (String) stratObject;
				if (stratObjectStr.equals("SELF")) this._strat = this;
			}
		}
		 
		this._generalComponents = (HashMap<String,IComponent>) s.readObject();
		this._processEmotionComponents = (ArrayList<IProcessEmotionComponent>) s.readObject();
		this._behaviourComponents = (ArrayList<IBehaviourComponent>) s.readObject();
		this._modelOfOtherComponents = (ArrayList<IModelOfOtherComponent>) s.readObject();
		this._processExternalRequestComponents = (ArrayList<IProcessExternalRequestComponent>) s.readObject();
		this._processPerceptionsComponents = (ArrayList<IAdvancedPerceptionsComponent>) s.readObject();
		this._affectDerivationComponents = (ArrayList<IAffectDerivationComponent>) s.readObject();
		this._appraisalComponents = (ArrayList<IAppraisalDerivationComponent>) s.readObject();
		
		s.close();
		in.close();

		AgentSimulationTime.LoadState(fileName+"-Timer.dat");

		//_remoteAgent.LoadState(fileName+"-RemoteAgent.dat");
	}

	private void loadPersonality(String personalityFile, short agentPlatform, ArrayList<String> goalList) 
	throws	ParserConfigurationException, SAXException, IOException, UnknownGoalException{

		AgentLogger.GetInstance().log("LOADING Personality: " + personalityFile);
		EmotionDispositionsLoaderHandler c = new EmotionDispositionsLoaderHandler(_emotionalState);

		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(personalityFile), c);
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
		String auxSubject;
		
		if(subject.equals(_name))
		{
			auxSubject = Constants.SELF;
		}
		else
		{
			auxSubject = subject;
		}

		if(target.equals(_name))
		{
			auxTarget = Constants.SELF;
		}
		else
		{
			auxTarget = target;
		}

		for(IAdvancedPerceptionsComponent c : this._processPerceptionsComponents)
		{
			c.lookAtPerception(this, auxSubject, auxTarget);
		}
	}

	public void PerceivePropertyChanged(String ToM, Name propertyName, String value)
	{
		AgentLogger.GetInstance().logAndPrint("PropertyChanged: " + ToM + " " + propertyName + " " + value);
		
		_memory.getSemanticMemory().Tell(applyPerspective(propertyName, _name), value);
		
		for(IAdvancedPerceptionsComponent c : this._processPerceptionsComponents)
		{
			c.propertyChangedPerception(ToM, applyPerspective(propertyName, _name), value);
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

	public void PerceiveEntityRemoved(String entity)
	{
		for(IAdvancedPerceptionsComponent c : this._processPerceptionsComponents)
		{
			c.entityRemovedPerception(entity);
		}
	}

	/**
	 * Resets the agent's reasoning layers (deliberative + cognitive)
	 *
	 */
	public void Reset() {
		for(IComponent c : this._generalComponents.values())
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
		ValuedAction bestAction;
		float bestActionValue;
		float value;
		AppraisalFrame appraisalFrame;
		
		long updateTime = System.currentTimeMillis();

		while (!_shutdown) {
			try {
				if(_remoteAgent.isShutDown()) {
					_shutdown = true;
				}
				
				updateSimulationTimer();
				long startCycleTime = System.currentTimeMillis();

				if (_remoteAgent.isRunning()) {
					
					_emotionalState.Decay();

					for(IComponent c : this._generalComponents.values())
					{
						c.update(this, AgentSimulationTime.GetInstance().Time());
					}

					//perceives and appraises new events
					synchronized (this)
					{
						for(Event e : this._perceivedEvents)
						{
							AgentLogger.GetInstance().log("appraising event: " + e.toName());
							
							Event e2 = e.ApplyPerspective(_name);
							_memory.getEpisodicMemory().StoreAction(_memory, e2);
							_memory.getSemanticMemory().Tell(ACTION_CONTEXT, e2.toName().toString());
							
							for(IComponent c : this._generalComponents.values())
							{
								c.update(this, e2);
							}
							
							appraisalFrame = new AppraisalFrame(e2);
							for(IAppraisalDerivationComponent c : this._appraisalComponents)
							{
								c.appraisal(this,e2, appraisalFrame);
								updateEmotions(appraisalFrame);
							}
						}
						this._perceivedEvents.clear();
					}
					
					for(IAppraisalDerivationComponent c : this._appraisalComponents)
					{
						appraisalFrame = c.reappraisal(this);
						if(appraisalFrame != null)
						{
							updateEmotions(appraisalFrame);
						}
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


					bestActionValue = -1;
					bestAction = null;
					
					for(IBehaviourComponent c : this._behaviourComponents)
					{
						action = c.actionSelection(this);
						if(action!= null)
						{
							value = action.getValue(_emotionalState);
							if(value > bestActionValue)
							{
								bestActionValue = value;
								bestAction = action;
							}
						}
						
					}

					if(_remoteAgent.FinishedExecuting() && _remoteAgent.isRunning() && bestAction != null) {
						
						_remoteAgent.AddAction(bestAction);
						IBehaviourComponent c = (IBehaviourComponent) getComponent(bestAction.getComponent());
						c.actionSelectedForExecution(bestAction);

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
					if(_saveMemoryRequest)
					{
						_saveMemoryRequest = false;
						SaveAgentMemory();
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
	
	@Override
	public EmotionalState simulateEmotionalState(Event simulatedEvent,IComponent callingComponent) {
		AppraisalFrame appraisalFrame = new AppraisalFrame(simulatedEvent);
		EmotionalState simulatedEmotionalState = null;
		
		for(IAppraisalDerivationComponent c : this._appraisalComponents)
		{
			if(c != callingComponent){ //to prevent infinite loops
				c.appraisal(this,simulatedEvent, appraisalFrame);
			}
		}
		if(appraisalFrame.hasChanged())
		{
			simulatedEmotionalState = this._emotionalState.clone(); 
			simulatedEmotionalState.Clear(); //only want the emotions caused by the simulatedEvent
			
			for(IAffectDerivationComponent ac : this._affectDerivationComponents)
			{
				if(ac != callingComponent){ //again to prevent infinite loops
					ArrayList<BaseEmotion> emotions = ac.affectDerivation(this, appraisalFrame);
					for(BaseEmotion em : emotions)
					{
						simulatedEmotionalState.AddEmotion(em, this);
					}		
				}
			}
			
		}
	
		return simulatedEmotionalState;
	}

	
	
	private void updateSimulationTimer() {
		// //updates the agent's simulation timer
		AgentSimulationTime.GetInstance().Tick();
		_numberOfCycles++;	
	}


	public void RequestAgentSave()
	{
		this._saveRequest = true;
	}

	public void RequestMemorySave()
	{
		this._saveMemoryRequest = true;
	}

	private void SaveAgentMemory() throws ParserConfigurationException, SAXException, IOException
	{
		_memory.getEpisodicMemory().MoveSTEMtoAM();
		_memoryWriter.outputMemorytoXML(_saveDirectory + MEMORY_FILENAME);
		for(IProcessExternalRequestComponent ip: _processExternalRequestComponents)
		{
			ip.processExternalRequest(this,"SAVE_ADV_MEMORY","");
		}
		
	}
	
	private void loadAgentMemory(String memoryFile) throws	ParserConfigurationException, SAXException, IOException
	{
		AgentLogger.GetInstance().log("LOADING Memory: " + memoryFile);
		MemoryLoaderHandler ml = new MemoryLoaderHandler(_memory);
	
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(memoryFile), ml);
		
		// creating a new episode
		_memory.getEpisodicMemory().StartEpisode(_memory);
	}
	
	/* Currently not called 
	 * Call it when there is an AdvancedMemory component and memory is being loaded 
	 * and an AdvancedMemory component previously existed
	 * Meiyii 14/01/11
	 */
	private void loadAgentAdvMemory() throws ParserConfigurationException, SAXException, IOException
	{
		for(IProcessExternalRequestComponent ip: _processExternalRequestComponents)
		{
			ip.processExternalRequest(this,"LOAD_ADV_MEMORY","");
		}
	}
	
	private void SaveAgentState(String agentName)
	{
		String fileName = _saveDirectory + agentName;

		// Moving all the STEM into AM before saving because when it loads the next time
		// a new episode will be started. This is to prevent the rest of the STEM events
		// being stored in the wrong episode
		// Meiyii 13/09/10
		_memory.getEpisodicMemory().MoveSTEMtoAM();

		AgentSimulationTime.SaveState(fileName+"-Timer.dat");
		_remoteAgent.SaveState(fileName+"-RemoteAgent.dat");

		try
		{
			FileOutputStream out = new FileOutputStream(fileName);
			ObjectOutputStream s = new ObjectOutputStream(out);
			
			s.writeObject(_emotionalState);
			s.writeObject(_memory);
			s.writeObject(_goalLibrary);
			s.writeObject(_actionLibrary);
			//s.writeObject(_dialogManager);
			s.writeObject(_role);
			s.writeObject(_name);
			s.writeObject(_sex);
			s.writeObject(_speechAct);
			s.writeObject(_currentEmotion);
			s.writeObject(_displayName);
			s.writeObject(new Boolean(_showStateWindow));
			s.writeObject(_actionsForExecution);
			s.writeObject(_perceivedEvents);
			s.writeObject(_saveDirectory);
			
			// prevent saving of the whole AgentCore which contains _agentDisplay as this would 
			// lead to NonSerializableException
			if (_strat != this)
			{
				s.writeObject(_strat);
			}
			else
			{
				s.writeObject(new String("SELF"));
			}
			s.writeObject(_generalComponents);
			s.writeObject(_processEmotionComponents);
			s.writeObject(_behaviourComponents);
			s.writeObject(_modelOfOtherComponents);
			s.writeObject(_processExternalRequestComponents);
			s.writeObject(_processPerceptionsComponents);
			s.writeObject(_affectDerivationComponents);
			s.writeObject(_appraisalComponents);
			
			s.flush();
			s.close();
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
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
			this.Run();
		}
		catch (Exception e) {
			e.printStackTrace();
			this.terminateExecution();
		}
	}

	private void terminateExecution(){
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

			//s.writeObject(_ToM);
			//s.writeObject(_nearbyAgents);
			s.writeObject(_emotionalState);
			s.writeObject(_memory);
			s.writeObject(_goalLibrary);
			s.writeObject(_actionLibrary);
			//s.writeObject(_motivationalState);
			//s.writeObject(_dialogManager);
			//s.writeObject(_role);
			//s.writeObject(_name);
			//s.writeObject(_sex);
			s.writeObject(_speechAct);
			s.writeObject(_currentEmotion);
			s.writeObject(_displayName);
			//s.writeObject(new Boolean(_showStateWindow));
			s.writeObject(_actionsForExecution);
			s.writeObject(_perceivedEvents);
			
			// prevent saving of the whole AgentCore which contains _agentDisplay as this would 
			// lead to NonSerializableException
			if (_strat != this)
			{
				s.writeObject(_strat);
			}
			else
			{
				s.writeObject(new String("SELF"));
			}	
			
			s.writeObject(_generalComponents);
			s.writeObject(_processEmotionComponents);
			s.writeObject(_behaviourComponents);
			s.writeObject(_modelOfOtherComponents);
			s.writeObject(_processExternalRequestComponents);
			s.writeObject(_processPerceptionsComponents);
			s.writeObject(_affectDerivationComponents);
			s.writeObject(_appraisalComponents);
			
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

	public AgentModel getModelToTest(Symbol ToM)
	{
		AgentModel am = _strat.execute(ToM);
		if(am == null) am = this;
		return am;
	}

	public AgentModel execute(Symbol ToM)
	{
		return this;
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
			this._emotionalState = (EmotionalState) s.readObject();
			this._memory = (Memory) s.readObject();
			// we need to recreate the memory writer because it holds a reference to the memory object
			this._memoryWriter = new MemoryWriter(this._memory);
			this._goalLibrary = (GoalLibrary) s.readObject();
			this._actionLibrary = (ActionLibrary) s.readObject();
			//this._dialogManager = (DialogManager) s.readObject();
			//this._role = (String) s.readObject();
			//this._name = (String) s.readObject();
			//this._sex = (String) s.readObject();
			this._speechAct = (SpeechAct) s.readObject();
			this._currentEmotion = (String) s.readObject();
			this._displayName = (String) s.readObject();
			//this._showStateWindow = ((Boolean) s.readObject()).booleanValue();
			this._actionsForExecution = (ArrayList<ValuedAction>) s.readObject();
			this._perceivedEvents = (ArrayList<Event>) s.readObject();
			
			Object stratObject = s.readObject();
			if (stratObject instanceof IGetModelStrategy)
			{
				this._strat = (IGetModelStrategy) stratObject;
			}
			else 
			{	
				if (stratObject instanceof String)
				{
					String stratObjectStr = (String) stratObject;
					if (stratObjectStr.equals("SELF")) this._strat = this;
				}
			}

			this._generalComponents = (HashMap<String,IComponent>) s.readObject();
			this._processEmotionComponents = (ArrayList<IProcessEmotionComponent>) s.readObject();
			this._behaviourComponents = (ArrayList<IBehaviourComponent>) s.readObject();
			this._modelOfOtherComponents = (ArrayList<IModelOfOtherComponent>) s.readObject();
			this._processExternalRequestComponents = (ArrayList<IProcessExternalRequestComponent>) s.readObject();
			this._processPerceptionsComponents = (ArrayList<IAdvancedPerceptionsComponent>) s.readObject();
			this._affectDerivationComponents = (ArrayList<IAffectDerivationComponent>) s.readObject();
			this._appraisalComponents = (ArrayList<IAppraisalDerivationComponent>) s.readObject();
			
			AgentSimulationTime.LoadState(s);
			s.close();
			
			resetDisplay();
			
			//this._saveDirectory = (String) s.readObject();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// reset the display after having overwritten the agents internal state 
	// (setSerializedState method)
	private void resetDisplay()
	{
		if (_agentDisplay!=null)
		{
			// first remove all panels that are linked to our old components
			_agentDisplay.clearAllComponentTabs();
			
			// now let all the new components create new panels			
			for (IComponent c : _generalComponents.values())
			{
				AgentDisplayPanel panel = c.createDisplayPanel(this);
				if(panel != null & _showStateWindow)
				{
					this._agentDisplay.AddPanel(panel, c.name(),"");
				}
			}
		}
	}
	
	public void updateEmotions(AppraisalFrame af) {
		
		ArrayList<BaseEmotion> emotions;
		ActiveEmotion activeEmotion;
		
			
		if(af.hasChanged())
		{
			for(IAffectDerivationComponent ac : this._affectDerivationComponents)
			{	
				emotions = ac.affectDerivation(this, af);
				for(BaseEmotion em : emotions)
				{
					activeEmotion = _emotionalState.AddEmotion(em, this);
					if(activeEmotion != null)
					{
						for(IProcessEmotionComponent pec : this._processEmotionComponents)
						{
							pec.emotionActivation(this,activeEmotion);
						}
					}
				}
			}
		}	
	}
	
	
	

}