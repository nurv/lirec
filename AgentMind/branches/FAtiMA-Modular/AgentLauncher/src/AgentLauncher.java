import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.AgentCore;
import FAtiMA.exceptions.ActionsParsingException;
import FAtiMA.exceptions.GoalLibParsingException;
import FAtiMA.exceptions.UnknownGoalException;
import FAtiMA.util.enumerables.AgentPlatform;

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
		// setting the memory mechanisms
		//if (agent != null)
		//{
			//agent.setCompoundCue(new CompoundCue());
			//System.out.println("Compound cue set ");
			//agent.setSpreadActivate(new SpreadActivate());
			//System.out.println("Spread activate set ");
			//agent.setCommonalities(new Commonalities());
			//System.out.println("Commonalities set ");
			//agent.setGeneralisation(new Generalisation());
			//System.out.println("Generalisation set ");
		//}
		
		aG.StartAgent();
	}
	
	static protected AgentCore initializeAgentCore(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException{
		if(args.length != 2){
			System.err.println("ERROR: It is now required to use the xml file to launch the agent.");
			System.exit(1);
		}
		
		String scenarioName = args[0];
		String agentName = args[1];	
		
		AgentCore agent = new AgentCore();
		agent.initialize(scenarioName,agentName);
		
		return agent;
	}
	
	
	
}

