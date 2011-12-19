package cmion.TeamBuddy.competencies;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.level2.migration.Migrating;
import cmion.level2.migration.MigrationAware;
import cmion.level2.migration.MigrationUtils;
import cmion.storage.EventPropertyChanged;

public class EMYSHead extends SamgarCompetency implements MigrationAware, Migrating
{

	public static float JOY_THRESHOLD =  4.0f;
	public static float SAD_THRESHOLD = -2.0f;
	
	private String currentEmotion;
	private String emotionToMigrate;
	private boolean migrateFlag;
	
	public EMYSHead(IArchitecture architecture) {
		super(architecture);
		// TODO Auto-generated constructor stub
		//name and type of the competence
		this.competencyName ="EMYSHead";
		this.competencyType ="EMYSHead";
		currentEmotion = "-1"; // -1 means no emotion is set
		migrateFlag = false;
	}

	@Override
	public void registerHandlers()
	{
		super.registerHandlers();

		// listen to property changes on the blackboard
		HandleBlackBoardProperty propChangeHandler = new HandleBlackBoardProperty();
		architecture.getBlackBoard().getEventHandlers().add(propChangeHandler);
		
		MigrationUtils.registerMigrationAwareComponent(MigrationUtils.getMigration(Simulation.instance), this);
		MigrationUtils.registerMigratingComponent(MigrationUtils.getMigration(Simulation.instance), this);
	}
		
	
	/** internal event handler class for listening to black board property changes */
	private class HandleBlackBoardProperty extends EventHandler {

	    public HandleBlackBoardProperty() {
	        super(EventPropertyChanged.class);
	    }

		@Override
	    public void invoke(IEvent evt) 
	    {
	    	if (evt instanceof EventPropertyChanged)
	    	{
	    		EventPropertyChanged evt1 = (EventPropertyChanged) evt;
	    		if (evt1.getPropertyName().equals("FatimaMood"))
	    		{
	    			float mood = Float.parseFloat(evt1.getPropertyValue().toString());
	    			//setMood(mood);
	    		}	    			    		
	    	}
	    }
	}
	
	
	@Override
	public void onRead(Bottle bottle_in) {
		// TODO Auto-generated method stub
		
	}

	private void setMood(float mood) 
	{
		if ((mood>JOY_THRESHOLD) && (!currentEmotion.equals("3"))) 
			setEmotion("3");
		else if ((mood<SAD_THRESHOLD) && (!currentEmotion.equals("5"))) 
			setEmotion("5");
		else if ((mood>SAD_THRESHOLD) && (mood<JOY_THRESHOLD) && (!currentEmotion.equals("0")))
			setEmotion("0");
	}		
	
	private void setEmotion(String emotion) 
	{
		if(migrateFlag==false)
		{
			System.out.println("emotion set to " + emotion);
			if (emotion.equals("6"))
			{
				migrateFlag=true;
				emotionToMigrate = currentEmotionAsString();
			} 
			currentEmotion = emotion;
			Bottle b = this.prepareBottle();
			b.addString(emotion);
			this.sendBottle();
			b.clear();
		}			
	}

	private void setGaze(String target) 
	{
		Bottle b = this.prepareBottle();
		b.addString("100");
		b.addString(target);
		this.sendBottle();
		b.clear();
	}
	
	
	@Override
	public boolean runsInBackground() 
	{
		return false;
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters){
		
		// first code to check if this was called to set gaze
		
		if (parameters.containsKey("gazeTarget"))
		{
			String gazeTarget = parameters.get("gazeTarget");
			setGaze(gazeTarget);
			return true;
		}	
		
		// if we reach here we are not in gaze mode so assume we are in emotion mode
		
		String emotion="";
		if (parameters.containsKey("emotion"))
		{
				emotion = parameters.get("emotion");
		}
		else if (parameters.containsKey("emotionAsStr"))
		{
				emotion = emotionStringAsInt(parameters.get("emotionAsStr"));
		}
		else return false;
		
		migrateFlag = false;
		setEmotion(emotion);

		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		
		return true;
	}

	//@Override
	public void onMigrationIn() 
	{
		migrateFlag=false;
	}

	//@Override
	public void onMigrationOut() {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onMigrationSuccess() {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public void onMigrationFailure() {
		// TODO Auto-generated method stub
		
	}

	//@Override
	public String getMessageTag() 
	{
		return "currentEmotion";
	}

	//@Override
	public void restoreState(Element message) {
		
		migrateFlag=false;
		
		String emoString = message.getElementsByTagName("emotion").item(0).getChildNodes().item(0).getNodeValue();
		if (emoString.equals("neutral")) setEmotion("0");
		else if (emoString.equals("anger")) setEmotion("1");
		else if (emoString.equals("fear")) setEmotion("2");
		else if (emoString.equals("joy")) setEmotion("3");		
		else if (emoString.equals("surprise")) setEmotion("4");
		else if (emoString.equals("sadness")) setEmotion("5");
		else if (emoString.equals("sleeping")) setEmotion("6");
	}

	//@Override
	public Element saveState(Document doc) {
		Element parent = doc.createElement(getMessageTag());
		
		Element emotion = doc.createElement("emotion");
		Node emotionNode = doc.createTextNode(emotionToMigrate);
		emotion.appendChild(emotionNode);
		parent.appendChild(emotion);
		
		return parent;
	}

	private String currentEmotionAsString() {
		if (currentEmotion.equals("0")) return "neutral";
		if (currentEmotion.equals("1")) return "anger";
		if (currentEmotion.equals("2")) return "fear";
		if (currentEmotion.equals("3")) return "joy";
		if (currentEmotion.equals("4")) return "surprise";
		if (currentEmotion.equals("5")) return "sadness";
		if (currentEmotion.equals("6")) return "sleeping";
		return "neutral";
	}

	private String emotionStringAsInt(String emoString) {
		if (emoString.equals("neutral")) return "0";
		else if (emoString.equals("anger")) return "1";
		else if (emoString.equals("fear")) return "2";
		else if (emoString.equals("joy")) return "3";		
		else if (emoString.equals("surprise")) return "4";
		else if (emoString.equals("sadness")) return "5";
		else if (emoString.equals("sleeping")) return "6";
		return "1";
	}

}
