import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.AgentCore;
import FAtiMA.exceptions.ActionsParsingException;
import FAtiMA.exceptions.GoalLibParsingException;
import FAtiMA.exceptions.UnknownGoalException;


public class AgentWithNeedsLauncher extends AgentLauncher{

	static public void main(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, ActionsParsingException, GoalLibParsingException  {
		AgentCore aG = initializeAgentCore(args);
		//aG.AddComponent()
	}
}
