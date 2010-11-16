import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.exceptions.ActionsParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.UnknownGoalException;


public class AgentWithNeedsLauncher extends AgentLauncher{

	static public void main(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException  {
		AgentCore aG = initializeAgentCore(args);
		//aG.AddComponent()
	}
}
