package FAtiMA;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.Appraisal;
import FAtiMA.emotionalState.AppraisalVector;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.exceptions.UnknownGoalException;
import FAtiMA.memory.Memory;
import FAtiMA.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.motivationalSystem.MotivationalState;
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
import FAtiMA.util.enumerables.AgentPlatform;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.util.parsers.AgentLoaderHandler;
import FAtiMA.util.parsers.CultureLoaderHandler;
import FAtiMA.util.parsers.ScenarioLoaderHandler;
import FAtiMA.wellFormedNames.Name;

public class Agent implements AgentModel {
	
	 /**
     * The main method
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
     */
	
	static public void main(String args[]) throws ParserConfigurationException, SAXException, IOException  {
		short agentPlatform = 0;
		String platform;
				
		if(args.length == 0){
			System.out.println("ERROR: zero arguments.");
			System.exit(1);	
		}
		
		//Load the arguments from the scenario definition present in scenarios.xml	
		if(args.length == 2){
			ScenarioLoaderHandler scenHandler = new ScenarioLoaderHandler(args[0],args[1]);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(MIND_PATH + "LIRECScenarios.xml"), scenHandler);
			args = scenHandler.getAgentArguments();
		}
		
		platform = args[0];
		
		if(platform.equalsIgnoreCase("ION")){
			agentPlatform = AgentPlatform.ION;
		}else if(platform.equalsIgnoreCase("WORLDSIM")){
			agentPlatform = AgentPlatform.WORLDSIM;
		}else{
			System.err.println("ERROR: The first argument should be 'ion' or 'worldsim' according to the platform in use.");
			System.exit(1);
		}
		
		switch(agentPlatform){
			case AgentPlatform.ION:
				if(args.length == 14){
					
					new Agent(agentPlatform,args[1], Integer.parseInt(args[2]), args[3],Boolean.parseBoolean(args[10]), args[4], args[5], args[6], args[7], args[8], args[9],args[11],args[12],args[13],null,null);
				}
				else if(args.length == 5)
				{
					System.err.println("Creating the agent instance");
					new Agent(agentPlatform,args[1],Integer.parseInt(args[2]), args[3], args[4]);
				}
				else
				{
					System.err.println("Wrong number of arguments!");
				}
				break;
				
			case AgentPlatform.WORLDSIM:
				String saveDirectory = "";
				if (args.length == 4){
					new Agent(agentPlatform, args[1],Integer.parseInt(args[2]),saveDirectory,args[3]);
				}else if(args.length >= 11){
					HashMap<String,String> properties = new HashMap<String,String>();
					ArrayList<String> goals = new ArrayList<String>();
					readPropertiesAndGoals(args, properties, goals);
					new Agent(agentPlatform,args[1], Integer.parseInt(args[2]),saveDirectory,Boolean.parseBoolean(args[3]),args[4],null,null, args[5], args[6], args[7],args[8],args[9],args[10], properties, goals);		
				}else{
					System.err.println("Wrong number of arguments!");
				}
				break;
		}

	}
	
	static private void readPropertiesAndGoals(String args[],HashMap<String,String> properties,ArrayList<String> goals){
		StringTokenizer st;
		String left;
			
		for(int i = 11; i < args.length; i++) {
			st = new StringTokenizer(args[i], ":");
			left = st.nextToken();
			if(left.equals("GOAL")) {
			    goals.add(st.nextToken());
			}
			else properties.put(left, st.nextToken());
		}
	}
	
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

	private String _saveDirectory;
	public static final String MIND_PATH = "data/characters/minds/";
	private static final Name ACTION_CONTEXT = Name.ParseName("ActionContext()");

	public Agent(short agentPlatform, String host, int port, String saveDirectory, boolean displayMode, String name,String lActDatabase, 
			String userLActDatabase, String sex, String role, 
			String displayName, String actionsFile, 
			String goalsFile, String cultureName, HashMap<String,String> properties, ArrayList<String> goalList) {

		_emotionalState = new EmotionalState();
		_memory = new Memory();
		_motivationalState = new MotivationalState();
		
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
			AgentLogger.GetInstance().initialize(name);

			// Load Plan Operators
			ActionLibrary.GetInstance().LoadActionsFile("" + MIND_PATH + actionsFile + ".xml", this);
			EmotionalPlanner planner = new EmotionalPlanner(ActionLibrary.GetInstance().GetActions());

			// Load GoalLibrary
			GoalLibrary goalLibrary = new GoalLibrary(MIND_PATH + goalsFile + ".xml", _name);


			//For efficiency reasons these two are not real processes
			_reactiveLayer = new ReactiveProcess(_name);

			_deliberativeLayer = new DeliberativeProcess(_name,goalLibrary,planner);
	
			String personalityFile = MIND_PATH + "roles/" + role + "/" + role + ".xml";
			loadPersonality(personalityFile,agentPlatform, goalList);
			
			
			loadCulture(cultureName);
			
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

			_remoteAgent.start();

			if(_showStateWindow){
				 _agentDisplay = new AgentDisplay(this);
			}

			this.Run();
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
			
			if(agentPlatform == AgentPlatform.ION)
			{
				_remoteAgent = new IONRemoteAgent(host,port,this);
			}
			else if (agentPlatform == AgentPlatform.WORLDSIM)
			{
				_remoteAgent = new WorldSimulatorRemoteAgent(host,port,this,new HashMap<String,String>());
			}
			 
			LoadAgentState(directory + fileName);
			 
			_remoteAgent.start();
	        
			if(_showStateWindow){
				_agentDisplay = new AgentDisplay(this);
			}
			
			this.Run();
		}
		catch (Exception e) {
			e.printStackTrace();
			this.terminateExecution();
		}
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
		if(agentPlatform == AgentPlatform.ION){
			_deliberativeLayer.RemoveAllGoals();
		}

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
	
	private void loadCulture(String cultureName)
		throws ParserConfigurationException, SAXException, IOException{

		AgentLogger.GetInstance().log("LOADING Culture: " + cultureName);
		
		CultureLoaderHandler culture = new CultureLoaderHandler(this, _reactiveLayer,_deliberativeLayer);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(MIND_PATH + cultureName + ".xml"), culture);
		
		Ritual r;
		ListIterator<Ritual> li = culture.GetRituals(this).listIterator();
		while(li.hasNext())
		{
			r = (Ritual) li.next();
			_deliberativeLayer.AddRitual(r);
			_deliberativeLayer.AddGoal(r);
			AgentLogger.GetInstance().log("Ritual: "+ r.toString());
		}
		
		CulturalDimensions.GetInstance().changeNeedsWeightsAndDecays(this);
	}
	
	

	public void SaveAgentState(String agentName)
	{
		String fileName = _saveDirectory + agentName;

		AgentSimulationTime.SaveState(fileName+"-Timer.dat");
		ActionLibrary.SaveState(fileName+"-ActionLibrary.dat");
		_remoteAgent.SaveState(fileName+"-RemoteAgent.dat");

		try
		{
			FileOutputStream out = new FileOutputStream(fileName);
			ObjectOutputStream s = new ObjectOutputStream(out);

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
	
	@SuppressWarnings("unchecked")
	public void LoadAgentState(String fileName) 
		throws IOException, ClassNotFoundException{
		
		
		FileInputStream in = new FileInputStream(fileName);
		ObjectInputStream s = new ObjectInputStream(in);
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
		
		_remoteAgent.LoadState(fileName+"-RemoteAgent.dat");
	}
	
	private void terminateExecution(){
		_deliberativeLayer.ShutDown();
		_reactiveLayer.ShutDown();
		_remoteAgent.ShutDown();
		if(_showStateWindow && _agentDisplay != null) _agentDisplay.dispose();
	}

	/**
	 * Gets the name of the agent
	 * @return the agent's name
	 */
	public String getName() {
		return _name;
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
	
	public MotivationalState getMotivationalState()
	{
		return _motivationalState;
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
	 * Gets the agent's Deliberative Layer that you can use
	 * to get access to Deliberative structures such as 
	 * the goals and planner
	 * @return the agent's Deliberative Layer
	 */
	public DeliberativeProcess getDeliberativeLayer()
	{
		return this._deliberativeLayer;
	}
	
	/**
	 * Specifies that the agent must give an answer to a received
	 * SpeechAct
	 * @param speechAct - the SpeechAct that needs an answer 
	 */
	public void AnswerToSpeechAct(SpeechAct speechAct) {
	    _speechAct = speechAct;
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
					_emotionalState.Decay();
					_motivationalState.Decay();
					_dialogManager.DecayCauseIDontHaveABetterName(_memory);
					
					//perceives and appraises new events
					synchronized (this)
					{
						for(ListIterator<Event> li = this._perceivedEvents.listIterator(); li.hasNext();)
						{
							Event e = (Event) li.next();
							e = e.ApplyPerspective(_name);
							AgentLogger.GetInstance().log("Perceiving event: " + e.toName());
							//inserting the event in AM
							
							_memory.getEpisodicMemory().StoreAction(_memory, e);
						    //registering an Action Context property in the KB
							_memory.getSemanticMemory().Tell(ACTION_CONTEXT,e.toName().toString());
							
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
					_reactiveLayer.Appraisal(this);
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
	
	public void AppraiseSelfActionFailed(Event e)
	{
		_deliberativeLayer.AppraiseSelfActionFailed(e);
	}
	
	public void SpeechStarted()
	{
		_dialogManager.SpeechStarted();
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
			e = new Event(Constants.SELF, "look-at", name);
			int like = Math.round(LikeRelation.getRelation(Constants.SELF, name).getValue(_memory));
			AppraisalVector v = new AppraisalVector();
			v.setAppraisalVariable(AppraisalVector.LIKE, like);
			em = (BaseEmotion) Appraisal.GenerateEmotions(this, e, v, null).get(0);
			return _emotionalState.DetermineActiveEmotion(em);
		}
		else if(action.equals("ACT_FOR_CHARACTER"))
		{
			if(parameters.size() == 0)
			{
				e = new Event(Constants.SELF,name, null);
			}
			else
			{
				e = new Event(Constants.SELF,name, (String) parameters.get(0));
				for(int i = 1; i < parameters.size(); i++)
				{
					e.AddParameter(new Parameter("param",parameters.get(i)));
				}
			}
			
			Reaction r = _reactiveLayer.Evaluate(this, e);
			emotions = Appraisal.GenerateEmotions(this, e, _reactiveLayer.translateEmotionalReaction(r),r.getOther());
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
	
	public void EnforceCopingStrategy(String coping)
	{
		_deliberativeLayer.EnforceCopingStrategy(this, coping);
		_reactiveLayer.EnforceCopingStrategy(coping);
	}
	
	public void PerceivePropertyChanged(String subject, String property, String value)
	{
		if(subject.equals(_name))
		{
			subject = Constants.SELF;
		}
		
		Name propertyName = Name.ParseName(subject + "(" + property + ")");
		_memory.getSemanticMemory().Tell(propertyName, value);
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


}
