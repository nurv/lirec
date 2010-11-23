import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.exceptions.ActionsParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.UnknownGoalException;
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
		aG.addComponent(new SocialRelationsComponent());
		aG.addComponent(new MotivationalComponent());
		
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

