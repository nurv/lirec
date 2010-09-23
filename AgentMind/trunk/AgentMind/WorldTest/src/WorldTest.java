import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.util.parsers.ScenarioLoaderHandler;
import FAtiMA.util.parsers.StripsOperatorsLoaderHandler;
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
	
	public static final String SCENARIOS_PATH = "data/characters/minds/LIRECScenarios.xml";
	//public static final String SCENARIOS_PATH = "data/characters/minds/Scenarios.xml";
	private ServerSocket _ss;
	private ServerSocket _ssToGreta;
	private ArrayList _objects;
	private ArrayList _agents;
	private String _scenery;
	private ArrayList _actions;
	private LanguageEngine agentLanguage;
	private UserInterface _userInterface;
	private String _userOptionsFile;
	private GretaAgent _ga;
	
	static public void main(String args[]) throws Exception {
		int i;
		ArrayList objects = new ArrayList();
		
		
		//Load the arguments from the scenario definition present in scenarios.xml
		if (args.length == 1){
			ScenarioLoaderHandler scenHandler = new ScenarioLoaderHandler(args[0]);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			parser.parse(new File(SCENARIOS_PATH), scenHandler);
			args = scenHandler.getWorldSimArguments();
		}
		
		if (args.length < 5) {
			System.out.println("Wrong number of arguments!");
			return;
		}			
		for(i = 5; i < args.length; i++) {
			objects.add(args[i]);
		}
		
		
		WorldTest wt = new WorldTest(new Integer(args[0]).intValue(),args[1],args[2], args[3], args[4],objects);
		wt.run();
		
		
	}
	
	public WorldTest(int port, String scenery, String actionsFile, String agentLanguageFile, String userOptionsFile, ArrayList objects) {
		_scenery = scenery;
		_agents = new ArrayList();
		_objects = new ArrayList();
		_userOptionsFile = userOptionsFile;
		_userInterface = new UserInterface(this);
		
		ListIterator li = objects.listIterator();
		String objName;
		
		while (li.hasNext()) {
			objName = li.next().toString();
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
		
					
		try {
			StripsOperatorsLoaderHandler op = LoadOperators(actionsFile, "[SELF]");
			this._actions = op.getOperators();
			_ss = new ServerSocket(port);
			
			//_ssToGreta = new ServerSocket(100);	
		}
		catch (Exception e){
			e.printStackTrace();
		}
		
		
	}
	
	private StripsOperatorsLoaderHandler LoadOperators(String xmlFile, String self)  throws Exception
	{
		_userInterface.WriteLine("LOAD: " + xmlFile);
		//com.sun.xml.parser.Parser parser;
		//parser = new com.sun.xml.parser.Parser();
		StripsOperatorsLoaderHandler op = new StripsOperatorsLoaderHandler(null);
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
		Socket s1, s2;
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
		ListIterator li = _agents.listIterator();
		RemoteAgent ag;
		String msg = "ENTITY-ADDED " + entityName;
		
		while(li.hasNext()) {
			ag = (RemoteAgent) li.next();
						
			if(!ag.Name().equals(entityName)){
				ag.Send(msg);
			}
		}
	}
	
	public void PerceiveEntities(RemoteAgent agent) {
		ListIterator li = _agents.listIterator();
		RemoteAgent ag;
		String entities = "AGENTS";
		
		while(li.hasNext()) {
			ag = (RemoteAgent) li.next();
			entities = entities + " " + ag.Name();
		}
		
		li = _objects.listIterator();
		
		while(li.hasNext()) {
			entities = entities + " " + ((sObject) li.next()).Name();
		}
		
		agent.Send(entities);
	}
	
	public void SendPerceptionToAll(String perception) {
		ListIterator li = _agents.listIterator();
		RemoteAgent ag;
		
		while(li.hasNext()) {
			ag = (RemoteAgent) li.next();
			ag.Send(perception);
		}
	}
	
	public String GetPropertiesList(String target) {
		
		RemoteAgent ag;
		sObject obj;
		ListIterator li;
		li = _agents.listIterator();
		
		while(li.hasNext()) {
			ag = (RemoteAgent) li.next();
			if(target.equals(ag.Name())) {
				return ag.GetPropertiesList();
			}
		}
		
		li = _objects.listIterator();
		
		while(li.hasNext()) {
			obj = (sObject) li.next();
			if(target.equals(obj.Name())) {
				return obj.GetPropertiesList();
			}
		}
		
		return null;
	}
	
	public ArrayList GetActions()
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
	
	public UserInterface GetUserInterface(){
		return _userInterface;
	}
	
	public void ChangeTime( String time ){
		for( int i = 0, limit = _agents.size(); i != limit; ++i ){
			SendPerceptionToAll( "PROPERTY-CHANGED " + ((RemoteAgent)_agents.get(i)).Name() + " time " + time );
		}
	}
	
	public void ChangePlace( String location ){
		for( int i = 0, limit = _agents.size(); i != limit; ++i ){
			SendPerceptionToAll( "PROPERTY-CHANGED " + ((RemoteAgent)_agents.get(i)).Name() + " location " + location );
			SendPerceptionToAll( "ACTION-FINISHED " + ((RemoteAgent)_agents.get(i)).Name() + " MoveTo " + location );
		}
	}
	
	// Meiyii 11/03/09 
	public void ChangeUser( String previousUser, String user ){
		for( int i = 0, limit = _agents.size(); i != limit; ++i ){
			if(!previousUser.equals(null))
			{
				if(previousUser.equalsIgnoreCase("LukePaulie"))
				{
					SendPerceptionToAll( "PROPERTY-CHANGED Luke(isPresent) False");
					SendPerceptionToAll( "PROPERTY-CHANGED Paulie(isPresent) False");
				}
				else
				{
					SendPerceptionToAll( "PROPERTY-CHANGED " + previousUser + "(isPresent) False");
				}
			}
			
			if(user.equalsIgnoreCase("LukePaulie"))
			{
				SendPerceptionToAll( "PROPERTY-CHANGED Luke(isPresent) True");
				SendPerceptionToAll( "PROPERTY-CHANGED Paulie(isPresent) True");
			}
			else
			{
				SendPerceptionToAll( "PROPERTY-CHANGED " + user + "(isPresent) True");
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
		SendPerceptionToAll( "READY-FOR-NEXT-STEP" );
	}
	
	String knownInfo = "";
	public void AddKnownInfo( String info ){
		knownInfo = knownInfo + info + "*";
	}
	
	public void CCMemory(){
		SendPerceptionToAll("CC-MEMORY");
	}
	
	public void SAMemory(String question){
		SendPerceptionToAll( "SA-MEMORY " + question + "$" + knownInfo );
		knownInfo = "";
	}
	
	public void GMemory(){
		SendPerceptionToAll("G-MEMORY");
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
