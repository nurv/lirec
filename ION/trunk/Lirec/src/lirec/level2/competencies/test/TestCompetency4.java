package lirec.level2.competencies.test;

import java.util.HashMap;

import lirec.architecture.Architecture;
import lirec.level2.Competency;

/** one of several test competencies to test the execution system */ 
public class TestCompetency4 extends Competency {

	/** constructor of the test competency */
	public TestCompetency4(Architecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		competencyName = "TestCompetency4";
		competencyType = "TestType4";

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
		System.out.println("Test Competency 4 running, with parameters below: ");
		for (String parameter : parameters.keySet())
			System.out.println("  "+ parameter + " = " + parameters.get(parameter));
		
		// test competency 4 always fails
		return false;
	}	
		
	
}
