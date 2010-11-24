import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.exceptions.ActionsParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.enumerables.CulturalDimensionType;
import FAtiMA.culture.CulturalDimensionsComponent;
import FAtiMA.motivationalSystem.MotivationalComponent;
import FAtiMA.socialRelations.SocialRelationsComponent;

public class AgentLauncher {
	
	 /**
     * The main method
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws GoalLibParsingException 
	 * @throws ActionsParsingException 
	 * @throws UnknownGoalException 
     */
	static public void main(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException  {
			
		
		AgentCore aG = initializeAgentCore(args);
		ArrayList<String> extraFiles = new ArrayList<String>();
		String cultureFile = ConfigurationManager.getMindPath() + ConfigurationManager.getAgentProperties().get("cultureName") + ".xml"; 
		
		extraFiles.add(cultureFile);
		aG.addComponent(new CulturalDimensionsComponent(cultureFile));
		aG.addComponent(new SocialRelationsComponent(extraFiles));
		aG.addComponent(new MotivationalComponent(extraFiles));
		
		
		aG.StartAgent();
	}
	
	
	static protected AgentCore initializeAgentCore(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException{
		if(args.length != 2){
			System.err.println("ERROR: It is now required to use the xml file to launch the agent.");
			System.exit(1);
		}
		
		String scenarioName = args[0];
		String agentName = args[1];	
		
		FAtiMA.Core.AgentCore agent = new AgentCore(agentName);
		agent.initialize(scenarioName,agentName);
		
		return agent;
	}
	
	
	
}

