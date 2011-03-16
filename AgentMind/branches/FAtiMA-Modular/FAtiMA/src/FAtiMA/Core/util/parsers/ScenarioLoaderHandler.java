package FAtiMA.Core.util.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.xml.sax.Attributes;

/**
 * @author Samuel
 */
public class ScenarioLoaderHandler extends ReflectXMLHandler{
	private String _scenarioName;
	private String _characterName;
	private boolean _isPretendedScenario;
	private boolean _isPretendedCharacter;
	private ArrayList<String> _worldSimArgs;
	private HashMap<String,String> _agentProperties;	
	private HashMap<String,String> _agentConfiguration;
	private boolean _foundScenario;
	private boolean _foundAgent;
	
	
	public ScenarioLoaderHandler(String scenarioName){
		this._scenarioName = scenarioName;
		this._characterName = "";
		this._worldSimArgs = new ArrayList<String>();
		this._agentConfiguration = new HashMap<String,String>();
		this._isPretendedScenario = false;
		this._foundScenario = false;
	}	
	
	public ScenarioLoaderHandler(String scenarioName, String characterName){
		this._scenarioName = scenarioName;
		this._characterName = characterName;
		this._agentConfiguration = new HashMap<String,String>();
		this._agentProperties = new HashMap<String,String>();
		this._worldSimArgs = new ArrayList<String>();
		this._agentConfiguration.put("saveDirectory", ""); //default value
		this._isPretendedScenario = false;
		this._isPretendedCharacter = false;
		this._foundAgent = false;
		this._foundScenario = false;
		
	}
	
	public String[] getWorldSimArguments(){
		String[] result = new String[_worldSimArgs.size()];		
		Iterator<String> it = _worldSimArgs.iterator();
		int i = 0;
		
		while(it.hasNext()){
			String arg = it.next();
			result[i] = arg;
			i++;
		}
		return result;
	}
	
	public void Scenario(Attributes attributes){
		if(_scenarioName.equalsIgnoreCase(attributes.getValue("name"))){
			this._isPretendedScenario = true;
			this._foundScenario = true;
		}else{
			this._isPretendedScenario = false;
		}
	}
	

    public void WorldSimulator(Attributes attributes){
    	if(_isPretendedScenario){
    		_worldSimArgs.add(attributes.getValue("port"));
    		_worldSimArgs.add(attributes.getValue("scenery"));
    		_worldSimArgs.add(attributes.getValue("actionsFile"));
    		_worldSimArgs.add(attributes.getValue("agentLanguageFile"));
    		_worldSimArgs.add(attributes.getValue("userLanguageFile"));
    		_worldSimArgs.add(attributes.getValue("userOptionsFile"));
    		_worldSimArgs.add(attributes.getValue("simplifiedVersion"));
    	}
    }
    
    public void checkForAgent() 
    {
    	checkScenario();
    	if(_foundAgent == false)
    	{
    		throw new RuntimeException("Could not find agent: " + this._characterName);
    	}
    }
    
    public void checkScenario()
    {
    	if(_foundScenario == false)
    	{
    		throw new RuntimeException("Could not find scenario: " + this._scenarioName);
    	}
    }
    
    
    public void Object(Attributes attributes){
    	if(_isPretendedScenario){
    		_worldSimArgs.add(attributes.getValue("name"));
    	}
    }    
    
	public void Agent(Attributes attributes){
		if(_isPretendedScenario){
			if(_characterName.equalsIgnoreCase(attributes.getValue("name"))){
				this._isPretendedCharacter = true;
				this._foundAgent = true;
				
				for(int i=0; i < attributes.getLength(); i++ ){
					_agentConfiguration.put(attributes.getQName(i), attributes.getValue(i));
				}
			}else{
				this._isPretendedCharacter = false;
			}    		
    	}
	}
	
	public void Property(Attributes attributes){
		if(_isPretendedScenario && _isPretendedCharacter){
			
			_agentProperties.put(attributes.getValue("name"),attributes.getValue("value"));
		}
	}
	
	public  HashMap<String,String> getAgentProperties(){
		return this._agentProperties;
	}
	
	public HashMap<String, String> getAgentConfiguration(){
		return this._agentConfiguration;
	}
}
