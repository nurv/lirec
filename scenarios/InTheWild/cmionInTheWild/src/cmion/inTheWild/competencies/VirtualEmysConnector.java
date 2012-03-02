package cmion.inTheWild.competencies;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.util.HashMap;
import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.level2.migration.MigrationAware;
import cmion.level2.migration.MigrationUtils;
import cmion.storage.CmionStorageContainer;
import cmion.storage.EventSubContainerAdded;

public class VirtualEmysConnector extends SamgarCompetency implements MigrationAware 
{
	
	private CmionStorageContainer multipleChoiceContainer;
	
	public static String CMD_SETVISIBLE = "SetVisible";
	public static String CMD_SETINVISIBLE = "SetInvisible";	
	public static String CMD_CANCELQUESTION = "CancelQuestion";
	public static String CMD_SETSUBTITLE = "SetSubtitle";
	public static String CMD_REMOVESUBTITLE = "RemoveSubtitle";
	public static String PARAMETER_SUBTITLE = "Subtitle";	
	public static String CMD_CMD = "Command";
	
	
	public VirtualEmysConnector(IArchitecture architecture) {
		super(architecture);
	
		//name and type of the competence
		this.competencyName ="VirtualEmys";
		this.competencyType ="VirtualEmys";		
		multipleChoiceContainer = null;
	}	
	
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();
		
		// listen to sub containers being added to the black board
		HandleBlackBoardAddSC scAddedHandler = new HandleBlackBoardAddSC();
		architecture.getBlackBoard().getEventHandlers().add(scAddedHandler);
		
		MigrationUtils.registerMigrationAwareComponent(MigrationUtils.getMigration(Simulation.instance),this);
	}
		
	/** internal event handler class for listening to blackboard sub containers being added */
	private class HandleBlackBoardAddSC extends EventHandler {

	    public HandleBlackBoardAddSC() {
	        super(EventSubContainerAdded.class);
	    }

		@Override
	    public void invoke(IEvent evt) 
	    {
	    	if (evt instanceof EventSubContainerAdded)
	    	{
	    		EventSubContainerAdded evt1 = (EventSubContainerAdded) evt;
	    		if (evt1.getSubContainer().getContainerName().equals(EmysAskQuestion.CONTAINER_NAME))
	    		{
	    			multipleChoiceContainer = evt1.getSubContainer();
	    			String[] choices = (String[]) evt1.getInitialProperties().get(EmysAskQuestion.CHOICES);
	    			sendChoiceRequest(choices);	
	    		}
	    	}
	    }
	}
	
	private void sendVisible(boolean visible) 
	{
		Bottle b = prepareBottle();
		b.addInt(6); // id 6 signifies setVisible 
		if (visible)
			b.addInt(1);
		else
			b.addInt(0);
		sendBottle();		
	}
		
	public void sendChoiceRequest(String[] choices)
	{
		Bottle b = prepareBottle();
		b.addInt(4); // id 4 signifies multiple choice request
		// add all choices in the array to the bottle
		for (String choice : choices) b.addString(choice);
		sendBottle();
	}
	
	public void sendCancelChoiceDisplay()
	{
		Bottle b = prepareBottle();
		b.addInt(8); // id 8 signifies cancelling of multiple choice display
		sendBottle();
		if (multipleChoiceContainer!=null)
		{
			multipleChoiceContainer.requestSetProperty(EmysAskQuestion.CANCELLED, new Boolean(true));
		}
	}

	private void sendSubtitle(String subtitle) {
		Bottle b = prepareBottle();
		b.addInt(9); // id 9 signifies subtitle display
		b.addString(subtitle);
		sendBottle();
	}	
	
	private void sendRemoveSubtitle() {
		Bottle b = prepareBottle();
		b.addInt(10); // id 10 signifies remove subtitle
		sendBottle();
	}		
	
	/** messages we get back from virtual emys are received here */
	@Override
	public void onRead(Bottle bottleIn) 
	{
		if (bottleIn.size()>0)
		{
			int msgType = bottleIn.get(0).asInt();
			if (msgType == 4) // type 4 signifies the user has chosen an answer to the question he was asked
			{
				String answer = bottleIn.get(1).asString().toString();
				if (multipleChoiceContainer!=null)
				{
					multipleChoiceContainer.requestSetProperty(EmysAskQuestion.ANSWER, answer);
					// we dont want the reference to the container anymore
					multipleChoiceContainer = null;
				}
			}
			
		}
	}


	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		if (!parameters.containsKey(CMD_CMD)) return false;
		String command = parameters.get(CMD_CMD);
		if (command.equals(CMD_CANCELQUESTION)) sendCancelChoiceDisplay();
		else if (command.equals(CMD_SETVISIBLE)) sendVisible(true);
		else if (command.equals(CMD_SETINVISIBLE)) sendVisible(false);
		else if (command.equals(CMD_SETSUBTITLE))
		{	
			if (!parameters.containsKey(PARAMETER_SUBTITLE)) return false;
			String subtitle = parameters.get(PARAMETER_SUBTITLE);
			sendSubtitle(subtitle);
		}
		else if (command.equals(CMD_REMOVESUBTITLE)) sendRemoveSubtitle();
		
		else return false;		
		return true;
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}

	@Override
	public void onMigrationIn() 
	{
		sendVisible(true);
	}

	@Override
	public void onMigrationOut() 
	{
		sendVisible(false);		
	}

	@Override
	public void onMigrationSuccess() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMigrationFailure() {
		// TODO Auto-generated method stub
		
	}

}
