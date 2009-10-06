package lirec.level2.competencies.test;

import java.util.HashMap;

import lirec.architecture.Architecture;
import lirec.level2.Competency;

/** one of several test competencies to test the execution system */ 
public class TestCompetency1 extends Competency
{

	/** constructor of the test competency */
	public TestCompetency1(Architecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		competencyName = "TestCompetency1";
		competencyType = "TestType1";

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
		System.out.println("Test Competency 1 running, with parameters below: ");
		for (String parameter : parameters.keySet())
			System.out.println("  "+ parameter + " = " + parameters.get(parameter));
		
		// test competency 1 always fails
		return false;
	}	
	
}
