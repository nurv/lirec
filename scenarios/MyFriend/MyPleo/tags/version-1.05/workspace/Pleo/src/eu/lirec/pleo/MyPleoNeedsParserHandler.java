/** 
 * MyPleoNeedsParserHandler.java - Parser of the needs' xml that stores the read data.
 *  
 * Copyright (C) 2011 GAIPS/INESC-ID 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 * Company: GAIPS/INESC-ID
 * Project: Pleo Scenario
 * @author: Paulo F. Gomes
 * Email to: pgomes@gaips.inesc-id.pt
 */

package eu.lirec.pleo;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class MyPleoNeedsParserHandler extends DefaultHandler{
	private static final String LOG_TAG = "MyPleoNeedsParserHandler";
	private static final String XML_NEED_TAG = "need";
	private static final String XML_NEED_NAME_ATTRIBUTE = "name";
	private static final String XML_NEED_VALUE_ATTRIBUTE = "value";
	private static final String XML_NEED_NAME_CLEANLINESS = "cleanliness";
	private static final String XML_NEED_NAME_ENERGY = "energy";
	private static final String XML_NEED_NAME_PETTING = "petting";
	private static final String XML_NEED_NAME_SKILLS = "skills";
	private static final String XML_NEED_NAME_WATER = "water";
	
	private final MyPleoNeeds _myPleoNeeds;

    public MyPleoNeedsParserHandler(MyPleoNeeds myPleoNeeds) {
		super();
		_myPleoNeeds = myPleoNeeds;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

       if (qName.equalsIgnoreCase(XML_NEED_TAG)) {
    	   String needName = attributes.getValue(XML_NEED_NAME_ATTRIBUTE);
    	   String needValueString = attributes.getValue(XML_NEED_VALUE_ATTRIBUTE);
    	   int needValue;
    	   
    	   try {
    		   needValue = Integer.parseInt(needValueString);
    	   } catch(NumberFormatException e){
    		   Log.e(LOG_TAG,"Unable to parse " + needValueString + " as integer.");
    		   return;
    	   }
    	   
    	   setNeedValue(needName, needValue);    	   
       }
    }

	private void setNeedValue(String needName, int needValue) {
		if(needName.equalsIgnoreCase(XML_NEED_NAME_CLEANLINESS)){
    		   _myPleoNeeds.setNeedCleanliness(needValue);
    	   }else if(needName.equalsIgnoreCase(XML_NEED_NAME_ENERGY)){
    		   _myPleoNeeds.setNeedEnergy(needValue);
    	   }else if(needName.equalsIgnoreCase(XML_NEED_NAME_PETTING)){
    		   _myPleoNeeds.setNeedPetting(needValue);
    	   }else if(needName.equalsIgnoreCase(XML_NEED_NAME_SKILLS)){
    		   _myPleoNeeds.setNeedSkills(needValue);
    	   }else if(needName.equalsIgnoreCase(XML_NEED_NAME_WATER)){
    		   _myPleoNeeds.setNeedWater(needValue);
    	   }
	}
}