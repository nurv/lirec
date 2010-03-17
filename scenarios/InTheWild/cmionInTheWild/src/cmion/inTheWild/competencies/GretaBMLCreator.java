/*	
    CMION classes for "in the wild" scenario
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  ---  
*/

package cmion.inTheWild.competencies;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;


/** this competency translates parameterized mind actions into BML that is realizable by
 *  Greta. Created BML is written to the black board */
public class GretaBMLCreator extends Competency {

	// remembers the emotion currently set
	private String emotion;

	// remembers the gaze direction currently set
	private String gaze;
	
	
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
		// default emotion is neutral
		emotion = "neutral";
		// default gaze is straight "look_at"
		gaze = "look_at";
		// initialisation finished
		this.available = true;
	}
	
	/** returns the bml snippet inside a signal that is responsible for modifying 
	 *  the signal
	 */
	private String getSignalModifier()
	{
		return "<intensity>1.00</intensity>\n"+
		"<FLD.max>1.00</FLD.max>\n"+
		"<FLD.min>-1.00</FLD.min>\n"+
		"<FLD.value>-0.85</FLD.value>\n"+
		"<OAC.max>1.00</OAC.max>\n"+
		"<OAC.min>0.00</OAC.min>\n"+
		"<OAC.value>1.00</OAC.value>\n"+
		"<PWR.max>1.00</PWR.max>\n"+
		"<PWR.min>-1.00</PWR.min>\n"+
		"<PWR.value>0.90</PWR.value>\n"+
		"<REP.max>1.00</REP.max>\n"+
		"<REP.min>-1.00</REP.min>\n"+
		"<REP.value>-0.10</REP.value>\n"+
		"<SPC.max>1.00</SPC.max>\n"+
		"<SPC.min>-1.00</SPC.min>\n"+
		"<SPC.value>0.50</SPC.value>\n"+
		"<TMP.max>1.00</TMP.max>\n"+
		"<TMP.min>-1.00</TMP.min>\n"+
		"<TMP.value>0.55</TMP.value>\n"+
		"<preference.max>1.00</preference.max>\n"+
		"<preference.min>0.00</preference.min>\n"+
		"<preference.value>1.00</preference.value>";	
	}
	
	/** returns bml representing a particular gesture*/
	private String getGestureBML(String gestureName, String gestureClass, String gestureType)
	{
		String gestureBML = 		
		"<"+ gestureType + " id='gesture-1'  start='2.00' end='2.62' stroke='0.68'>\n"+
		"<description level='1' type='gretabml'>\n"+
		"<reference>"+gestureClass+"="+gestureName+"</reference>\n"+
		"<stroke time='2.462' />\n"+
		getSignalModifier()+"\n"+    
		"</description>"+
		"</"+ gestureType +">";	
		return gestureBML;
	}
	
	/** returns bml representing an emotion */
	private String getEmotionBML()
	{
		String emotionBML = 		
		"<face id='emotion-1'  start='0.00' >\n"+
		"<description level='1' type='gretabml'>\n"+
		"<reference>affect="+emotion+"</reference>\n"+
		getSignalModifier()+"\n"+    
		"</description>"+
		"</face>";
		
		return emotionBML;
	}	
	
	/** returns bml representing a gaze */
	private String getGazeBML()
	{
		String gazeBML = 		
		"<gaze id='gaze-1'  start='0.00' >\n"+
		"<description level='1' type='gretabml'>\n"+
		"<reference>gaze="+gaze+"</reference>\n"+
		getSignalModifier()+"\n"+    
		"</description>"+
		"</gaze>";
		
		return gazeBML;
	}	

	/** returns bml representing speech */
	private String getSpeechBML(String text)
	{
		String speechBML =  "<speech id=\"s1\" start='0.0' type=\"application/xml\" voice=\"mary.female\" language=\"english\" text=\""+ text + "\">" +"\n"  
	        + "<description level=\"1\" type=\"gretabml\">" + "\n"
			+ "<reference>Scene 1-1</reference>" + "\n" 
			+ "</description>" + "\n"
			+ text + "\n" 
			+ "</speech>" + "\n"; 
		return speechBML;
	}	
	
	
	/** the code that is executed when the competency is invoked */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		// check if we have a parameter called BmlOutName (variable name to store bml on blackboard on
		if (!parameters.containsKey("BmlOutName")) return false;
		if (!parameters.containsKey("Type")) return false;
		
		String bmlBegin = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + "\n"
        + "<!DOCTYPE bml SYSTEM \"bml/bml.dtd\">" + "\n"
        + "<bml>" + "\n";

		String bmlEnd =  "</bml>";
		
		String bmlCenter;
		
		if (parameters.get("Type").equals("talk"))
		{
			if (!parameters.containsKey("Text")) return false;
			if (parameters.containsKey("Emotion")) emotion = parameters.get("Emotion");
			bmlCenter = getSpeechBML(parameters.get("Text")) + getEmotionBML() + getGazeBML();
		} 
		else if (parameters.get("Type").equals("gesture"))
		{
			if (!parameters.containsKey("GestureClass")) return false;
			if (!parameters.containsKey("GestureName")) return false;
			if (!parameters.containsKey("GestureType")) return false;
			bmlCenter = getGestureBML(parameters.get("GestureName"),
						parameters.get("GestureClass"),
						parameters.get("GestureType")) 
						+ getEmotionBML() + getGazeBML();
		}
		else if (parameters.get("Type").equals("emotion"))
		{	
			if (!parameters.containsKey("Emotion")) return false;
			emotion = parameters.get("Emotion");
			bmlCenter = getEmotionBML() + getGazeBML();
		}
		else if (parameters.get("Type").equals("gaze"))
		{	
			if (!parameters.containsKey("Direction")) return false;
			gaze = parameters.get("Direction");
			bmlCenter = getEmotionBML() + getGazeBML();
		}
		else return false; 
		  
        String bml = bmlBegin + bmlCenter +  bmlEnd;
		
		// write bml to blackboard
		architecture.getBlackBoard().requestSetProperty(parameters.get("BmlOutName"), bml);
		
		// return competency success
		return true;
	}

	/** does not run in background (is invoked instead) */
	@Override
	public boolean runsInBackground() {
		return false;
	}

}
