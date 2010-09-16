package FAtiMA.util.parsers;

import java.util.ArrayList;
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
	private ArrayList<String> _agentArgs;
	
	public ScenarioLoaderHandler(String scenarioName){
		this._scenarioName = scenarioName;
		this._characterName = "";
		this._worldSimArgs = new ArrayList<String>();
		this._agentArgs = new ArrayList<String>();
		this._isPretendedScenario = false;	
	}	
	
	public ScenarioLoaderHandler(String scenarioName, String characterName){
		this._scenarioName = scenarioName;
		this._characterName = characterName;
		this._agentArgs = new ArrayList<String>();
		this._worldSimArgs = new ArrayList<String>();
		this._isPretendedScenario = false;
		this._isPretendedCharacter = false;
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
	
	public String[] getAgentArguments(){
		String[] result = new String[_agentArgs.size()];		
		Iterator<String> it = _agentArgs.iterator();
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
    		_worldSimArgs.add(attributes.getValue("userOptionsFile"));
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
				_agentArgs.add(attributes.getValue("platform"));
				_agentArgs.add(attributes.getValue("host"));
				_agentArgs.add(attributes.getValue("port"));
				_agentArgs.add(attributes.getValue("saveDirectory"));
				_agentArgs.add(attributes.getValue("name"));
				_agentArgs.add(attributes.getValue("displayMode"));
				_agentArgs.add(attributes.getValue("sex"));
				_agentArgs.add(attributes.getValue("role"));
				_agentArgs.add(attributes.getValue("displayName"));
				_agentArgs.add(attributes.getValue("actionsFile"));
				_agentArgs.add(attributes.getValue("goalsFile"));
				_agentArgs.add(attributes.getValue("cultureName"));	
				_agentArgs.add(attributes.getValue("load"));
			}else{
				this._isPretendedCharacter = false;
			}    		
    	}
	}
	
	public void Property(Attributes attributes){
		if(_isPretendedScenario && _isPretendedCharacter){
			_agentArgs.add(attributes.getValue("name")+":"+attributes.getValue("value"));
		}
	}
	
}
