package lirec.level2.competencies.test;

import java.util.HashMap;

import lirec.architecture.Architecture;
import lirec.level2.Competency;

public class TestCompetency5 extends Competency 
{

	/** constructor of the test competency */
	public TestCompetency5(Architecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		competencyName = "TestCompetency5";
		// this is also of type 4, purpose is to test whether this competency will be
		// selected after TestCompetency4 fails
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
		System.out.println("Test Competency 5 running, with parameters below: ");
		for (String parameter : parameters.keySet())
			System.out.println("  "+ parameter + " = " + parameters.get(parameter));
		
		// test competency 5 always succeeds
		return true;
	}		
	
}
