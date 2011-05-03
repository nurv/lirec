import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.SpeechAct;
import FAtiMA.Core.util.parsers.ActionsLoaderHandler;
import FAtiMA.Core.util.parsers.ScenarioLoaderHandler;
import Language.LactException;
import Language.LanguageEngine;
import Language.SactException;


/*
 * Created on 4/Fev/2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author João Dias, Samuel Mascarenhas, Meiyii Lim
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * 
 * Meiyii Lim: 31/03/2009 - Added socket connection to Greta
 * 
 */
public class WorldTest {

	public static final String WORLD_PATH = "data/characters/minds/";
	public static final int MINIMUM_NUM_WORLD_SIM_ARGS = 7;


	private ServerSocket _ss;
	private ArrayList<sObject> _objects;
	private ArrayList<RemoteAgent> _agents;
	private String _scenery;
	private ArrayList<Step> _actions;
	private Language.LanguageEngine _agentLanguage;
	private Language.LanguageEngine _userLanguage;
	private static UserInterface _userInterface;
	private String _userOptionsFile;
	private GretaAgent _ga;


	static public void main(String args[]) throws Exception{
		int i;
	
		ArrayList<String> objects = new ArrayList<String>();
		
		//JFileChooser fc = new JFileChooser();
		//fc.showOpenDialog(new JPanel());
	

		if (args.length == 2){

			args = loadArgumentsFromScenarioDefinition(args[0],args[1]);

			if (args.length < MINIMUM_NUM_WORLD_SIM_ARGS) {
				System.out.println("Wrong number of arguments in the scenario definition!");
				return;
			}			
			for(i = MINIMUM_NUM_WORLD_SIM_ARGS; i < args.length; i++) {
				objects.add(args[i]);
			}

			boolean simplifiedVersion = false;
			
			if(args[6] != null){
				simplifiedVersion = new Boolean(args[6]);
			}
			
			WorldTest wt = new WorldTest(new Integer(args[0]).intValue(),args[1],args[2], args[3], args[4],args[5],simplifiedVersion,objects);
			wt.run();
		}
		if (args.length < 2) {
			System.out.println("Expecting 2 arguments: scenarios file, scenario name!");
			return;
		}			
	}




	public WorldTest(int port, String scenery, String actionsFile, String agentLanguageFile, String userLanguageFile, String userOptionsFile, boolean simplifiedVersion, ArrayList<String> objects) throws IOException {
		_scenery = scenery;
		_agents = new ArrayList<RemoteAgent>();
		_objects = new ArrayList<sObject>();
		_userOptionsFile = userOptionsFile;
		
		_userInterface = new UserInterface(this,simplifiedVersion);

		for(String objName : objects)
		{
			_objects.add(sObject.ParseFile(objName));
		}

		try{
			
			if(agentLanguageFile != null){
				_agentLanguage = this.initializeLanguageEngine("name", "victim", "M", agentLanguageFile);
			}
			
			if(userLanguageFile != null){
				_userLanguage = this.initializeLanguageEngine("name", "user", "M", userLanguageFile);
			}
			
			_userInterface.WriteLine("Finished ALE initialization!");
		
			ActionsLoaderHandler op = LoadOperators(actionsFile, "[SELF]");
			this._actions = op.getOperators();
			_ss = new ServerSocket(port);
			//_ssToGreta = new ServerSocket(100);

		}catch(Exception e){
			e.printStackTrace();
			System.exit(-1);
		}

	}

	private LanguageEngine initializeLanguageEngine(String name, String role, String sex, String languageFile) throws SactException, LactException{
		
		if(role.equals("User")){
			_userInterface.WriteLine("Initializing User Language Engine(ALE)... ");		
		}else{
			_userInterface.WriteLine("Initializing Agent Language Engine(ALE)... ");			
		}
		_userInterface.WriteLine("Language File: " + languageFile);
		_userInterface.WriteLine("Sex: " + sex);
		_userInterface.WriteLine("Role: " + role);
		return new LanguageEngine(name,sex,role,new File(languageFile));
	}
	
	private static String[] loadArgumentsFromScenarioDefinition(String scenarioFilename, String scenarioName) throws SAXException, IOException, ParserConfigurationException {
		ScenarioLoaderHandler scenHandler = new ScenarioLoaderHandler(scenarioName);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(WORLD_PATH + scenarioFilename), scenHandler);
		scenHandler.checkScenario();
		return  scenHandler.getWorldSimArguments();
	}
	

	private ActionsLoaderHandler LoadOperators(String xmlFile, String self) throws ParserConfigurationException, SAXException, IOException 
	{
		_userInterface.WriteLine("Loaded actions from: " + xmlFile);
		ActionsLoaderHandler op = new ActionsLoaderHandler(null);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(xmlFile), op);
		return op;		
	}

	public void run() {
		Socket s2;
		RemoteAgent ra;		

		while(true) {
			try {
				/*s1 = _ssToGreta.accept();
				_ga = new GretaAgent(this,s1);
				_ga.start();
				_ga.Send("Connected!");*/

				s2 = _ss.accept();
				ra = new RemoteAgent(this,s2);
				ra.start();
				_agents.add(ra);

				//hack for Mac
				Thread.sleep(150);

				_userInterface.WriteLine(ra.Name() + " enters the " + _scenery);
				NotifyEntityAdded(ra.Name());
				PerceiveEntities(ra);
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}		
		}
	}

	public void NotifyEntityAdded(String entityName) {

		String msg = "ENTITY-ADDED " + entityName;

		for(RemoteAgent ag : _agents)
		{
			if(!ag.Name().equals(entityName)){
				ag.Send(msg);
			}
		}
	}

	public void PerceiveEntities(RemoteAgent agent) {

		String entities = "AGENTS";

		for(RemoteAgent ag : _agents)
		{
			entities = entities + " " + ag.Name();
		}

		for(sObject obj : _objects)
		{
			entities = entities + " " + obj.Name();
		}

		agent.Send(entities);
	}

	public void SendPerceptionToAll(String perception) {

		for(RemoteAgent ag : _agents)
		{
			ag.Send(perception);
		}
	}
	
	public void SendPerceptionTo(String agent, String perception)
	{
		for(RemoteAgent ag : _agents)
		{
			if(ag.Name().equals(agent))
			{
				ag.Send(perception);
			}
		}
	}

	public String GetPropertiesList(String target) {

		for(RemoteAgent ag : _agents)
		{
			if(target.equals(ag.Name())) {
				return ag.GetPropertiesList();
			}
		}

		for(sObject obj : _objects)
		{
			if(target.equals(obj.Name())) {
				return obj.GetPropertiesList();
			}
		}

		return null;
	}

	public ArrayList<Step> GetActions()
	{
		return this._actions;
	}
	
	public String SaySummary(String speech)
	{
		String utterance;
		String aux;
		String[] aux2;

		//System.out.println("Generating SpeechAct:" + speech);
		if(this._agentLanguage!= null)
		{
			try
			{
				aux = this._agentLanguage.Narrate(speech);
				aux2 = aux.split("<Summary>");
				if(aux2.length > 1){
					utterance = aux2[1].split("</Summary>")[0];
					return utterance;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}	
		return null;
	}

	public String Say(String speech)
	{
		String utterance;
		String aux;
		String[] aux2;

		//System.out.println("Generating SpeechAct:" + speech);
		if(this._agentLanguage!= null)
		{
			try
			{
				aux = this._agentLanguage.Say(speech);
				aux2 = aux.split("<Utterance>");
				if(aux2.length > 1){
					utterance = aux2[1].split("</Utterance>")[0];
					return utterance;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}	
		return null;
	}

	public String ProcessInput(String user,String target, String text)
	{
		GetUserInterface().WriteLine(user + " says to " + target + "; " + text);

		SpeechAct userSpeech = new SpeechAct();
		userSpeech.setReceiver(target);
		userSpeech.setSender(user);
		userSpeech.setUtterance(text);
		userSpeech.setActionType(SpeechAct.UserSpeech);

		if(this._userLanguage!= null)
		{
			try
			{
				String speech = this._userLanguage.Input(userSpeech.toLanguageEngine());
				userSpeech = (SpeechAct) SpeechAct.ParseFromXml(speech);
				userSpeech.setActionType(SpeechAct.UserSpeech);
				String perception = "ACTION-FINISHED " + user + " UserSpeech " + target + " " + userSpeech.toXML();
				this.SendPerceptionToAll(perception);

			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}	
		return null;
	}

	public UserInterface GetUserInterface(){
		return _userInterface;
	}

	public void ChangeTime( String time ){
		for( int i = 0, limit = _agents.size(); i != limit; ++i ){
			SendPerceptionToAll( "PROPERTY-CHANGED " + "*" + ((RemoteAgent)_agents.get(i)).Name() + " time " + time );
		}
	}

	public void ChangePlace( String location ){
		for( int i = 0, limit = _agents.size(); i != limit; ++i ){
			SendPerceptionToAll( "PROPERTY-CHANGED " + "*" + ((RemoteAgent)_agents.get(i)).Name() + " location " + location );
			SendPerceptionToAll( "ACTION-FINISHED " + "*" + ((RemoteAgent)_agents.get(i)).Name() + " MoveTo " + location );
		}
	}

	// Meiyii 11/03/09 
	public void ChangeUser( String previousUser, String user ){
		for( int i = 0, limit = _agents.size(); i != limit; ++i ){
			if(!previousUser.equals(null))
			{
				if(previousUser.equalsIgnoreCase("LukePaulie"))
				{
					SendPerceptionToAll( "PROPERTY-CHANGED * Luke(isPresent) False");
					SendPerceptionToAll( "PROPERTY-CHANGED * Paulie(isPresent) False");
				}
				else
				{
					SendPerceptionToAll( "PROPERTY-CHANGED * " + previousUser + "(isPresent) False");
				}
			}

			if(user.equalsIgnoreCase("LukePaulie"))
			{
				SendPerceptionToAll( "PROPERTY-CHANGED * Luke(isPresent) True");
				SendPerceptionToAll( "PROPERTY-CHANGED * Paulie(isPresent) True");
			}
			else
			{
				SendPerceptionToAll( "PROPERTY-CHANGED * " + user + "(isPresent) True");
			}
		}
	}

	// Meiyii 06/04/09 
	public void ChangeExperimentCase( String expCase ){

		if(_scenery.equals("AmyHouse"))
		{
			if(expCase.equals(UserInterface.CASE1))
			{
				_userInterface._locationOptions.setSelectedItem(UserInterface.LIVINGROOM);
				_userInterface._timeOptions.setSelectedItem(UserInterface.MORNING);
			}
			else if(expCase.equals(UserInterface.CASE2))
			{
				_userInterface._locationOptions.setSelectedItem(UserInterface.STUDYROOM);
				_userInterface._timeOptions.setSelectedItem(UserInterface.AFTERNOON);
			}
			else if(expCase.equals(UserInterface.CASE3))
			{
				_userInterface._locationOptions.setSelectedItem(UserInterface.KITCHEN);
				_userInterface._timeOptions.setSelectedItem(UserInterface.MORNING);
			}
		}
		else if(_scenery.equals("Office"))
		{
			if(expCase.equals(UserInterface.CASE1))
			{
				_userInterface._userOptions.setSelectedItem(UserInterface.JOHN);
				_userInterface._locationOptions.setSelectedItem(UserInterface.RECEPTION);
				_userInterface._timeOptions.setSelectedItem(UserInterface.MORNING);
			}
			else if(expCase.equals(UserInterface.CASE2))
			{
				_userInterface._userOptions.setSelectedItem(UserInterface.LUKEPAULIE);
				_userInterface._locationOptions.setSelectedItem(UserInterface.COMMONROOM);
				_userInterface._timeOptions.setSelectedItem(UserInterface.AFTERNOON);
			}
			else if(expCase.equals(UserInterface.CASE3))
			{
				_userInterface._userOptions.setSelectedItem(UserInterface.PAULIE);
				_userInterface._locationOptions.setSelectedItem(UserInterface.OFFICE);
				_userInterface._timeOptions.setSelectedItem(UserInterface.AFTERNOON);
			}	
		}
	}


	public void ReadyForNextStep(){
		//SendPerceptionToAll( "READY-FOR-NEXT-STEP" );
		SendPerceptionToAll( "IDENTIFY-USER Amy Pixota" );
	}

	String knownInfo = "";
	public void AddKnownInfo( String info ){
		knownInfo = knownInfo + info + "*";
	}

	public void CCMemory(){
		SendPerceptionToAll("CC-MEMORY");
	}

	public void SAMemory(String question){
		SendPerceptionToAll("SA-MEMORY " + question + "$" + knownInfo );
		knownInfo = "";
	}

	String gAttributes = "";
	public void AddGAttributes( String attribute ){
		gAttributes = gAttributes + attribute + "*";
	}

	public void GMemory(){
		SendPerceptionToAll("G-MEMORY " + gAttributes);
		System.out.println("WorldTest gAttributes " + gAttributes);
		gAttributes = "";
	}

	public synchronized void removeAgent(RemoteAgent ra){
		_agents.remove(ra);
		_userInterface.WriteLine(ra.Name() + " disconnected\n");
	}

	public synchronized void removeGreta(){
		_userInterface.WriteLine(_ga + " disconnected\n");
		_ga = null;
	}

	public GretaAgent GetGreta(){
		return _ga;
	}

	public String GetUserOptionsFile() {
		return this._userOptionsFile;
	}

	public String GetScenery() {
		return this._scenery;
	}


}
