package FAtiMA.advancedMemoryComponent;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;

public class AdvancedMemoryHandler extends ReflectXMLHandler {

	private AdvancedMemoryComponent _advancedMemory;
	
	public AdvancedMemoryHandler(AdvancedMemoryComponent advancedMemory)
	{
		_advancedMemory = advancedMemory;
	}
	
	public void GER(Attributes attributes) {
		
	    String subject = attributes.getValue("subject");
	    String action = attributes.getValue("action");
	    String intention = attributes.getValue("intention");
	    String target = attributes.getValue("target");
	    String object = attributes.getValue("object");	   
	    String desirability = attributes.getValue("desirability");
	    String praiseworthiness = attributes.getValue("praiseworthiness");	    
	    String location = attributes.getValue("location");
	    String time = attributes.getValue("time");
	    int coverage = Integer.parseInt(attributes.getValue("coverage"));
	    
	    GER ger = new GER(subject, action, intention, target, object, desirability, praiseworthiness, location, time, coverage);
	    _advancedMemory.getGeneralisation().putGER(ger);
	    
	    //System.out.println("GER");
	    //System.out.println(subject + " " + action + " " + intention + " " + target + " " + object + " " + 
	    //		desirability + " " + praiseworthiness + " " + location + " " + time + " " + coverage);
	    //System.out.println(ger.toString());
	}
}
