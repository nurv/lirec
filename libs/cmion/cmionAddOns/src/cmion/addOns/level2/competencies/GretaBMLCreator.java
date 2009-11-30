package lirec.addOns.level2.competencies;

import java.util.HashMap;

import lirec.architecture.IArchitecture;
import lirec.level2.Competency;

/** this competency translates parameterized mind actions into BML that is realizable by
 *  Greta. Created BML is written to the black board */
public class GretaBMLCreator extends Competency {

	/** create a new Greta BML Creator */
	public GretaBMLCreator(IArchitecture architecture) 
	{
		super(architecture);
		competencyName = "GretaBMLCreator";
		competencyType = "GretaBMLCreator";
	}

	/** initialises the BML Creator */
	@Override
	public void initialize() {
		// nothing to initialize here, so make competency available straight away
		this.available = true;
	}

	/** the code that is executed when the competency is invoked */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		// check if we have a parameter called BmlOutName (variable name to store bml on blackboard on
		if (!parameters.containsKey("BmlOutName")) return false;
		
		String text = "Hello! I am Greta. I talk like a waterfall.";
		
		// generate bml (for now very simple hardcoded bml)
		String bml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + "\n"
        + "<!DOCTYPE bml SYSTEM \"bml/bml.dtd\">" + "\n"
        + "<bml>" + "\n"
        + "<speech id=\"s1\" start=\"0.0\" type=\"application/xml\" voice=\"mary.female\" language=\"english\" text=\""+ text + "\">" +"\n"  
        + "<description level=\"1\" type=\"gretabml\">" + "\n"
		+ "<reference>Scene 1-1</reference>" + "\n" 
		+ "</description>" + "\n"
		+ text + "\n" 
		+ "<tm id=\"tm1\"/>" + "\n"
		+ "</speech>" + "\n"
        + "</bml>";
		
		// write bml to blackboard
		architecture.getBlackBoard().requestSetProperty(parameters.get("BmlOutName"), bml);
		
		// return competency success
		return true;
	}

}
