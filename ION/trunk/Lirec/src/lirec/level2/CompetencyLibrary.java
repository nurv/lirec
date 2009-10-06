package lirec.level2;

import ion.Meta.Simulation;

import java.util.ArrayList;

import lirec.architecture.Architecture;
import lirec.architecture.LirecComponent;
import lirec.level2.competencies.ExampleCompetency;
import lirec.level2.competencies.test.TestCompetency1;
import lirec.level2.competencies.test.TestCompetency2;
import lirec.level2.competencies.test.TestCompetency3;
import lirec.level2.competencies.test.TestCompetency4;
import lirec.level2.competencies.test.TestCompetency5;

/** the competency library is the component that registers all components */
public class CompetencyLibrary extends LirecComponent {

	/** the list of competencies that are available */
	private ArrayList<Competency> competencies;

	/** Create a new competency library */
	public CompetencyLibrary(Architecture architecture) 
	{
		super(architecture);
		
		// create competencies array list
		competencies = new ArrayList<Competency>();
		
		// at the moment competencies to load are hard coded here
		// could be more sophisticated later
		competencies.add(new ExampleCompetency(architecture));
		competencies.add(new TestCompetency1(architecture));
		competencies.add(new TestCompetency2(architecture));
		competencies.add(new TestCompetency3(architecture));
		competencies.add(new TestCompetency4(architecture));
		competencies.add(new TestCompetency5(architecture));		
		
		// add all competencies to ION and initialise
		for (Competency competency : competencies)
		{	
			Simulation.instance.getElements().add(competency);
			competency.initialize();
		}
	}

	@Override
	public final void registerHandlers() 
	{
		// the competency library needs no handlers at the moment
	}
	
	/** return a List of competencies with the given type, the list will be empty
	 *  if no such competency is in the library */
	public ArrayList<Competency> getCompetencies(String type)
	{
		ArrayList<Competency> returnList = new ArrayList<Competency>();
		for (Competency competency : competencies)
			if (competency.getCompetencyType().equals(type))
				returnList.add(competency);

		return returnList;
	}
	
	

}
