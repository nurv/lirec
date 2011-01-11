import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.deliberativeLayer.plan.Step;
import FAtiMA.Core.sensorEffector.SpeechAct;
import FAtiMA.Core.util.parsers.ScenarioLoaderHandler;
import FAtiMA.Core.util.parsers.ActionsLoaderHandler;
import Language.LanguageEngine;


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
	//public static final String SCENARIOS_PATH = "data/characters/minds/Scenarios.xml";
	private ServerSocket _ss;
	private ArrayList<sObject> _objects;
	private ArrayList<RemoteAgent> _agents;
	private String _scenery;
	private ArrayList<Step> _actions;
	private LanguageEngine agentLanguage;
	private LanguageEngine userLanguage;
	private UserInterface _userInterface;
	private String _userOptionsFile;
	private GretaAgent _ga;
	
	static public void main(String args[]) throws Exception {
		int i;
		ArrayList<String> objects = new ArrayList<String>();
		
		
		//Load the arguments from the scenario definition present in scenarios.xml
		if (args.length == 2){
			ScenarioLoaderHandler scenHandler = new ScenarioLoaderHandler(args[1]);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(WORLD_PATH + args[0]), scenHandler);
			
			scenHandler.checkScenario();
			args = scenHandler.getWorldSimArguments();
			
			if (args.length < 6) {
				System.out.println("Wrong number of arguments in the scenario definition!");
				return;
			}			
			for(i = 6; i < args.length; i++) {
				objects.add(args[i]);
			}
			
			
			WorldTest wt = new WorldTest(new Integer(args[0]).intValue(),args[1],args[2], args[3], args[4],args[5],objects);
			wt.run();
		}
		if (args.length < 2) {
			System.out.println("Expecting 2 arguments: scenarios file, scenario name!");
			return;
		}			
	}
	
	public WorldTest(int port, String scenery, String actionsFile, String agentLanguageFile, String userLanguageFile, String userOptionsFile, ArrayList<String> objects) {
		_scenery = scenery;
		_agents = new ArrayList<RemoteAgent>();
		_objects = new ArrayList<sObject>();
		_userOptionsFile = userOptionsFile;
		_userInterface = new UserInterface(this);
		
		for(String objName : objects)
		{
			_objects.add(sObject.ParseFile(objName));
		}
		
		_userInterface.WriteLine("Initializing Agent Language Engine(ALE)... ");
		_userInterface.WriteLine("Language File: " + agentLanguageFile);
		_userInterface.WriteLine("Sex: M");
		_userInterface.WriteLine("Role: Victim");
		try
		{
			this.agentLanguage = new LanguageEngine("name","M","Victim",new File(agentLanguageFile));
			_userInterface.WriteLine("Finished ALE initialization!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		_userInterface.WriteLine("Initializing User Language Engine(ULE)... ");
		_userInterface.WriteLine("Language File: " + userLanguageFile);
		_userInterface.WriteLine("Sex: M");
		_userInterface.WriteLine("Role: Victim");
		try
		{
			this.userLanguage = new LanguageEngine("name","M","User",new File(userLanguageFile));
			_userInterface.WriteLine("Finished ALE initialization!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
					
		try {
			ActionsLoaderHandler op = LoadOperators(actionsFile, "[SELF]");
			this._actions = op.getOperators();
			_ss = new ServerSocket(port);
			
			//_ssToGreta = new ServerSocket(100);	
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	private ActionsLoaderHandler LoadOperators(String xmlFile, String self)  throws Exception
	{
		_userInterface.WriteLine("LOAD: " + xmlFile);
		//com.sun.xml.parser.Parser parser;
		//parser = new com.sun.xml.parser.Parser();
		ActionsLoaderHandler op = new ActionsLoaderHandler(null);
		//parser.setDocumentHandler(op);
		try 
		{
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(xmlFile), op);
			//InputSource input = Resolver.createInputSource(new File(xmlFile));
			//parser.parse(input);
			return op;
		}
		catch (Exception ex) 
		{
			throw new Exception("Error parsing the actions file.",ex);
		}	
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
	
	public String Say(String speech)
	{
		String utterance;
		String aux;
		String[] aux2;
		
		//System.out.println("Generating SpeechAct:" + speech);
		if(this.agentLanguage!= null)
		{
			try
			{
				aux = this.agentLanguage.Say(speech);
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
		//System.out.println("Generating SpeechAct:" + speech);
		
		SpeechAct userSpeech = new SpeechAct();
		userSpeech.setReceiver(target);
		userSpeech.setSender(user);
		userSpeech.setUtterance(text);
		userSpeech.setActionType(SpeechAct.UserSpeech);
		
		if(this.userLanguage!= null)
		{
			try
			{
				String speech = this.userLanguage.Input(userSpeech.toLanguageEngine());
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
		SendPerceptionToAll("AdvancedMemory CC-MEMORY");
	}
	
	public void SAMemory(String question){
		SendPerceptionToAll("AdvancedMemory SA-MEMORY " + question + "$" + knownInfo );
		knownInfo = "";
	}
	
	String gAttributes = "";
	public void AddGAttributes( String attribute ){
		gAttributes = gAttributes + attribute + "*";
	}
	
	public void GMemory(){
		SendPerceptionToAll("AdvancedMemory G-MEMORY " + gAttributes);
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
