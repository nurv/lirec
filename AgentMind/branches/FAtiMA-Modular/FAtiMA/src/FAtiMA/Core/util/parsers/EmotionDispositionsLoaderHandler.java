/** 
 * AgentLoaderHandler.java - Parses an agent's personality 
 * 
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 17/01/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 17/01/2004 - File created
 * João Dias: 12/07/2006 - Removed the reference to the KB from the class.
 * 						   It is no longer needed.
 * João Dias: 15/07/2006 - Removed the EmotionalState from the Class fields since the 
 * 						   EmotionalState is now a singleton that can be used anywhere 
 * 					       without previous references.
 * João Dias: 17/07/2006 - Removed the class constructor that received an agent. The
 * 						   constructor stopped working because of the changes to the 
 * 						   Agent class
 * João Dias: 21/07/2006 - Now the constructor only receives the reactive and deliberative layers
 * João Dias: 31/08/2006 - Added parsing for RecentEvents as conditions
 * 						 - Added parsing for PastEvents as conditions
 * João Dias: 05/09/2006 - Added parsing for InterpersonalRelations
 * João Dias: 07/09/2006 - Changes the parsing of importanceOfSuccess so that you can parse
 * 						   the definition of goals can be made with the previous version of
 * 						   FearNot! files, wich had a typo and defined the importanceOfSuccess
 * 						   with only one c (importanceOfSucess)
 * João Dias: 18/09/2006 - Added parsing for the attribute other when specifying an emotional
 * 					       reaction that relates to an event considering another character
 * João Dias: 28/09/2006 - Added parsing for EmotionConditions
 * João Dias: 10/02/2007 - Added parsing for MoodConditions
 */

package FAtiMA.Core.util.parsers;

import java.util.Locale;

import org.xml.sax.Attributes;

import FAtiMA.Core.emotionalState.EmotionDisposition;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;

public class EmotionDispositionsLoaderHandler extends ReflectXMLHandler {
	
    private EmotionalState _emotionalState;
    
    public EmotionDispositionsLoaderHandler(EmotionalState es)
    {
    	this._emotionalState = es;
    }
    

    public void EmotionalThreshold(Attributes attributes) throws InvalidEmotionTypeException {
    	String emotionName;
 
    	emotionName = attributes.getValue("emotion").toUpperCase(Locale.ENGLISH);
  
        _emotionalState.AddEmotionDisposition(new EmotionDisposition(emotionName,
                                                              new Integer(attributes.getValue("threshold")).intValue(),
                                                              new Integer(attributes.getValue("decay")).intValue()));
    }
}