package FAtiMA.Core.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.util.enumerables.AgentPlatform;
import FAtiMA.Core.util.parsers.ScenarioLoaderHandler;

/*
*  
* A read-only singleton used to get the path of the configuration files 
* author: samuel
*
*/
public class ConfigurationManager {
	private static final String PLATFORM = "platform";
	private static final String SAVE_DIRECTORY = "saveDirectory";
	private static final String HOST = "host";
	private static final String PORT = "port";
	private static final String DISPLAY_MODE = "displayMode";
	private static final String NAME = "name";
	private static final String DISPLAY_NAME = "displayName";	
	private static final String SEX = "sex";
	private static final String ROLE = "role";
	private static final String ACTIONS_FILE = "actionsFile";	
	private static final String GOALS_FILE = "goalsFile";
	private static final String AGENT_LOAD = "agentLoad";
	private static final String MEMORY_LOAD = "memoryLoad";
	
	private String mindPath;
	private HashMap<String,String> agentConfiguration;
	private HashMap<String,String> agentProperties;
	
	//private String saveDirectory, actionsFile, goalsFile, personalityFile;
	
	//singleton pattern
	private ConfigurationManager(){}
	private static ConfigurationManager soleInstance = new ConfigurationManager();
	

	private static ConfigurationManager getInstance(){
		return soleInstance;
	}
	
	public static void initialize(String mindPath, String scenariosFile, String scenarioName, String agentName) throws SAXException, IOException, ParserConfigurationException{
		String scenarioFileName = mindPath + scenariosFile;	
		getInstance().mindPath = mindPath;
		
		loadConfigurationFromScenarioFile(scenarioFileName,scenarioName,agentName);
	}
	
	private static void loadConfigurationFromScenarioFile(String scenarioFileName, String scenarioName, String agentName) throws SAXException, IOException, ParserConfigurationException{
		ScenarioLoaderHandler scenHandler = new ScenarioLoaderHandler(scenarioName,agentName);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(scenarioFileName), scenHandler);
		//side-effects
		scenHandler.checkForAgent();
		
		getInstance().agentProperties = scenHandler.getAgentProperties();
		getInstance().agentConfiguration = scenHandler.getAgentConfiguration();
	}
	
	public static short getPlatform(){
		String platform = getInstance().getConfigurationValue(PLATFORM);
		if(platform.equalsIgnoreCase("ION")){
			return AgentPlatform.ION;
		}else if(platform.equalsIgnoreCase("WORLDSIM")){
			return AgentPlatform.WORLDSIM;
		}else{
			return -1; //invalid platform
		}
	}
	
	public static String getSaveDirectory(){
		return getInstance().getConfigurationValue(SAVE_DIRECTORY);
	}
		
	public static String getHost(){
		return getInstance().getConfigurationValue(HOST);		
	}
	
	public static int getPort(){
		return Integer.parseInt(getInstance().getConfigurationValue(PORT));
	}
	
	public static String getActionsFile(){
		String actionsFile = getInstance().getConfigurationValue(ACTIONS_FILE);		

		return getInstance().mindPath + actionsFile + ".xml";
	}
	
	public static String getGoalsFile(){
		String goalsFile = getInstance().getConfigurationValue(GOALS_FILE);		
		return getInstance().mindPath + goalsFile + ".xml";
	}
	
	public static String getRole(){ 
		return getInstance().getConfigurationValue(ROLE);	
	}
	
	public static String getSex(){ 
		return getInstance().getConfigurationValue(SEX);	
	}
	
	public static String getMindPath()
	{
		if(VersionChecker.runningOnAndroid())
		{
			return AgentCore.MIND_PATH_ANDROID;
		}
		else
		{
			return AgentCore.MIND_PATH;
		}
	}
	
	public static String getName(){ 
		return getInstance().getConfigurationValue(NAME);	
	}
	
	public static String getDisplayName(){ 
		return getInstance().getConfigurationValue(DISPLAY_NAME);	
	}
	
	public static boolean getDisplayMode(){ 
		return Boolean.parseBoolean(getInstance().getConfigurationValue(DISPLAY_MODE));	
	}
	
	public static String getPersonalityFile(){
		return getInstance().mindPath + "roles/" + getRole() + "/" + getRole() + ".xml";
	}

	
	public static boolean getAgentLoad(){
		String agentLoad = getInstance().getOptionalConfigurationValue(AGENT_LOAD);
		if(agentLoad!=null){
			return Boolean.parseBoolean(agentLoad);
		}else{
			return false;
		}
	}
	
	public static boolean getMemoryLoad(){
		String memoryLoad = getInstance().getOptionalConfigurationValue(MEMORY_LOAD);
		if(memoryLoad!=null){
			return Boolean.parseBoolean(memoryLoad);
		}else{
			return false;
		}
	}
	
	public static HashMap<String,String> getAgentProperties(){
		return getInstance().agentProperties; 
	}
	
	
	//Auxiliary Method To Prevent From Getting Inexistent Configurations
	private String getConfigurationValue(String name){
		if(this.agentConfiguration.containsKey(name)){
			return this.agentConfiguration.get(name);
		}else{
			throw new RuntimeException("There is no configuration with such name: " + name);
		}
	}
	
	public static String getOptionalConfigurationValue(String name){
		if(getInstance().agentConfiguration.containsKey(name)){
			return getInstance().agentConfiguration.get(name);
		}else{
			return null;
		}
	}
	
	
	
}
