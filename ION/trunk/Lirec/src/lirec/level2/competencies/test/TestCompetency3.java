package lirec.level2.competencies.test;

import java.util.HashMap;

import lirec.architecture.Architecture;
import lirec.level2.Competency;

/** one of several test competencies to test the execution system */ 
public class TestCompetency3 extends Competency 
{

	/** constructor of the test competency */
	public TestCompetency3(Architecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		competencyName = "TestCompetency3";
		competencyType = "TestType3";

		// with other initialisations wait for the initialize method
	}

	/** perform initialisations in this method */
	@Override
	public void initialize() {
		available = true;
	}

	/** competency code */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		System.out.println("Test Competency 3 running, with parameters below: ");
		for (String parameter : parameters.keySet())
			System.out.println("  "+ parameter + " = " + parameters.get(parameter));
		
		// read random number that TestCompetency2 has created from blackboard
		// in the variable with the name that parameter "RandomInName" defines
		
		// check if we have a parameter called RandomOutName
		if (!parameters.containsKey("RandomInName")) return false;
		
		// check if number is posted on the blackboard
		if (! architecture.getBlackBoard().hasProperty(parameters.get("RandomInName"))) return false;
		
		// read it
		Object randomRead = architecture.getBlackBoard().getPropertyValue(parameters.get("RandomInName"));
		
		// test if it is an integer and cast
		if (!(randomRead instanceof Integer)) return false;
		Integer random = (Integer) randomRead;
		
		System.out.println("Test Competency 3: read random no " + random);
		
		// lets also post something to the world model, lets add an agent TestUser
		// with one property test = true
		HashMap<String, Object> initialProperties = new HashMap<String, Object>();
		initialProperties.put("test", "True");
		architecture.getWorldModel().requestAddAgent("TestUser", initialProperties);
		System.out.println("Test Competency 3: has added agent TestUser to world model with property test = true");
		
		
		// returns competency success
		return true;
	}		
	
}
