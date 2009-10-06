package lirec.architecture;

import ion.Meta.Element;
import ion.Meta.Simulation;
import lirec.level2.CompetencyExecution;
import lirec.level2.CompetencyLibrary;
import lirec.level3.AgentMindConnector;
import lirec.level3.CompetencyManager;
import lirec.level3.fatima.FAtiMAConnector;
import lirec.storage.LirecStorageContainer;
import lirec.storage.WorldModel;


/** this class is used at the moment as the executable / main entry point for the 
 * Lirec architecture. It is also used for accessing references to all main 
 * architecture components */
public class Architecture {
	
	// components below
	
	/** the world model in which we store high level symbolic information */
	private WorldModel worldModel;

	/** the black board that stores lower level information for competencies to share
	 *  between each other */
	private LirecStorageContainer blackBoard;
	
	/** the competency execution system */
	private CompetencyExecution competencyExecution;

	/** the competency manager */	
	private CompetencyManager competencyManager;
	
	/** the interface to the agent mind */
	private AgentMindConnector agentMindConnector;

	/** the competency library */	
	private CompetencyLibrary competencyLibrary;
	
	/** create a new architecture */
	public Architecture()
	{
		// 1: create data storage components: Worldmodel and Blackboard
		worldModel = new WorldModel("WorldModel",this);
		Simulation.instance.getElements().add(worldModel);
		Simulation.instance.update();
		
		blackBoard = new LirecStorageContainer("BlackBoard","BlackBoard",this);
		Simulation.instance.getElements().add(blackBoard);
		Simulation.instance.update();
		
		// 2: create Competency Library, which will in turn create all competencies
		competencyLibrary = new CompetencyLibrary(this);
		Simulation.instance.getElements().add(competencyLibrary);
		Simulation.instance.update();
		
		// 3: create Competency Execution System
		competencyExecution = new CompetencyExecution(this);
		Simulation.instance.getElements().add(competencyExecution);
		Simulation.instance.update();
		
		// 4: create a competency manager
		competencyManager = new CompetencyManager(this);
		Simulation.instance.getElements().add(competencyManager);
		Simulation.instance.update();
		
		// 5: create an agent mind connector, for a different agent mind, 
		// e.g. Fatima or supersimplemind, change class name here
		agentMindConnector = new FAtiMAConnector(this);
		// alternative mind:
		// agentMindConnector = new SuperSimpleMindConnector(this);
		Simulation.instance.getElements().add(agentMindConnector);		
		Simulation.instance.update();
		
		// now register handlers for all Lirec Components
		for (Element element : Simulation.instance.getElements())
			if (element instanceof LirecComponent)
				((LirecComponent)element).registerHandlers();
		
		System.out.println("Lirec Architecture initialised");
	}
	
	/** function that updates the ion simulation regularly*/
	private void runSimulation()
	{
		System.out.println("Lirec Architecture running");
		
		// run the ion simulation in an endless loop
		while (true)
		{	
			Simulation.instance.update();
			
			// sleep a little while to give other processes some time as well
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {}
		}		
	}
	
	/** returns the world model component */
	public WorldModel getWorldModel()
	{
		return worldModel;
	}
	
	/** returns the black board component */
	public LirecStorageContainer getBlackBoard()
	{
		return blackBoard;
	}
	
	/** returns the competency execution component */
	public CompetencyExecution getCompetencyExecution()
	{
		return competencyExecution;
	}
	
	/** returns the competency manager component */
	public CompetencyManager getCompetencyManager()
	{
		return competencyManager;
	}
	
	/** returns the agent mind connector component */
	public AgentMindConnector getAgentMindConnector()
	{
		return agentMindConnector;
	}
	
	/** returns the competency library component */
	public CompetencyLibrary getCompetencyLibrary()
	{
		return competencyLibrary;
	}
	
	/** main method */
	public static void main(String[] args)
	{
		// initialisation
		Architecture architecture = new Architecture();
		
		// run
		architecture.runSimulation();	
	}
	


}
