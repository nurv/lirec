package lirec.level2.competencies;

import java.util.HashMap;

import lirec.architecture.Architecture;
import lirec.level2.Competency;

/** example of the implementation of a competency, with comments explaining how
 * to define competencies, this is not actually doing anything serious */
public class ExampleCompetency extends Competency {

	/** constructor, every competency, should if possible have a constructor 
	 *  that receives the Architecture as an argument */
	public ExampleCompetency(Architecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		// set the name/identifier for the competency
		competencyName = "ExampleCompetency1";

		// set the type for the competency, for this exampole lets pretend 
		// our competency is a DetectPerson competency
		competencyType = "DetectPerson";

		// with other initialisations wait for the initialize method
	}

	/** perform initialisations in this method */
	@Override
	public void initialize() {
		// if initialisations take time, start a thread here
		
		// set available to true to indicate the initialisation has finished successfully
		// if thread was used as suggested above do this as last command in the thread
		available = true;
	}

	
	/** the custom code of the competence, this is already running in a thread */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		System.out.println("example competency running");
		// what normally would happen here is 
		// a) parse parameters, check if they are correct
		// b) run the code
		// c) maybe update the world model
		// d) read and write from the blackboard
		// e) return success (boolean)
		
		// ended successful
		return true;
	}

}
