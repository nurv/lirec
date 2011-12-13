package FAtiMA;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.exceptions.ActionsParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.OCCAffectDerivation.OCCAffectDerivationComponent;
import FAtiMA.ReactiveComponent.ReactiveComponent;
import FAtiMA.ToM.ToMComponent;
import FAtiMA.culture.CulturalDimensionsComponent;
import FAtiMA.emotionalIntelligence.EmotionalIntelligenceComponent;
import FAtiMA.motivationalSystem.MotivationalComponent;
import FAtiMA.socialRelations.SocialRelationsComponent;
import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;

public class AgentLauncher {

	/**
	 * The main method
	 * 
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws GoalLibParsingException
	 * @throws ActionsParsingException
	 * @throws UnknownGoalException
	 */
	static public void main(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException {

		AgentCore aG = initializeAgentCore(args);
		ArrayList<String> extraFiles = new ArrayList<String>();
		String cultureFile = ConfigurationManager.getMindPath() + ConfigurationManager.getOptionalConfigurationValue("cultureName") + ".xml";
		String relationsFile = ConfigurationManager.getMindPath() + "relations.xml"; //fixed for now, read it as an optional configuration value latter
		String locationOntologyFile = ConfigurationManager.getMindPath() + "locations/AmyHouse.xml";

		if (!aG.getAgentLoad()) {
			extraFiles.add(cultureFile);

			//FAtiMA Light
			aG.addComponent(new ReactiveComponent());
			aG.addComponent(new OCCAffectDerivationComponent());
			aG.addComponent(new DeliberativeComponent());

			//FAtiMA Advanced Components
			//aG.addComponent(new MotivationalComponent(extraFiles));
			//aG.addComponent(new SocialRelationsComponent(relationsFile, extraFiles));
			//aG.addComponent(new ToMComponent(ConfigurationManager.getName()));
			//aG.addComponent(new CulturalDimensionsComponent(cultureFile));
			//aG.addComponent(new EmotionalIntelligenceComponent(extraFiles));
			//aG.addComponent(new AdvancedMemoryComponent(locationOntologyFile));

		}
		aG.StartAgent();
	}

	static private AgentCore initializeAgentCore(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException {
		if (args.length != 3) {
			System.err.println("ERROR - expecting 3 arguments: Scenarios File, Scenario Name, and Agent Name");
			System.exit(1);
		}

		String scenarioFile = args[0];
		String scenarioName = args[1];
		String agentName = args[2];

		AgentCore agent = new AgentCore(agentName);
		agent.initialize(scenarioFile, scenarioName, agentName);

		return agent;
	}
}
