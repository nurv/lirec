/** 
 * MemoryLoaderHandler.java - Loads memory data from an xml file. 
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
 * Company: HWU
 * Project: LIREC
 * Created: 17/12/10
 * @author: Meiyii Lim
 * Email to: M.Lim@hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 17/12/10 - File created
 * Matthias Keysermann: 18/03/11 - Added RetrievalQueue, RetrievalTime
 * Matthias Keysermann: 07/04/11 - Added nextEventID
 * Matthias Keysermann: 12/04/11 - Moved class MemoryBaseEmotion to
 *                                 FAtiMA.Core.memory.episodicMemory
 *                                 in order to fix bug when Serializing
 * Matthias Keysermann: 20/04/11 - added agentSimulationTime
 *                                 agentSimulationTime is used for the narrativeTime
 *                                 narrativeTime is used for calculating activation values
 * Matthias Keysermann: 20/04/11 - added eventCounter
 * Matthias Keysermann: 27/04/11 - real time (Calendar) is imported as milliseconds
 * **/

package FAtiMA.Core.util.parsers;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentSimulationTime;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.AutobiographicalMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.ShortTermEpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.Time;
import FAtiMA.Core.memory.episodicMemory.RetrievalQueue;
import FAtiMA.Core.memory.episodicMemory.MemoryBaseEmotion;
import FAtiMA.Core.memory.semanticMemory.KnowledgeBase;
import FAtiMA.Core.memory.semanticMemory.WorkingMemory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.enumerables.EmotionValence;
import FAtiMA.Core.wellFormedNames.Name;

public class MemoryLoaderHandler extends ReflectXMLHandler {
	
	private Memory _memory;
	private KnowledgeBase _currentKB;
	private WorkingMemory _currentWM;
	private AutobiographicalMemory _currentAM;
	private MemoryEpisode _currentME;
	private ActionDetail _currentAD;
	private ArrayList<Parameter> _currentParameters;
	private BaseEmotion _currentEmotion;
	private Event _currentCause;
	private ShortTermEpisodicMemory _currentSTEM;
	private RetrievalQueue _currentRetrievalQueue;
		
	public MemoryLoaderHandler(Memory memory) {
		_memory = memory;

		_currentKB = new KnowledgeBase();
		_currentWM = new WorkingMemory();
		_currentAM = new AutobiographicalMemory();
		_currentSTEM = new ShortTermEpisodicMemory();		

		_memory.getSemanticMemory().putKnowledgeBase(_currentKB);
		_memory.getSemanticMemory().putWorkingMemory(_currentWM);
		_memory.getEpisodicMemory().putAutobiographicalMemory(_currentAM);
		_memory.getEpisodicMemory().putSTEpisodicMemory(_currentSTEM);
	}
	
	public void EpisodicMemory(Attributes attributes) {		
		int nextEventID = Integer.parseInt(attributes.getValue("nextEventID"));
		_memory.getEpisodicMemory().setNextEventID(nextEventID);
		long agentSimulationTime = Long.parseLong(attributes.getValue("agentSimulationTime"));
		AgentSimulationTime.GetInstance().SetSimulationTime(agentSimulationTime);
		long eventCounter = Long.parseLong(attributes.getValue("eventCounter"));
		Time.setEventCounter(eventCounter);
	}
	
	public void KBSlot(Attributes attributes)
	{
		String name = attributes.getValue("name");
		Object value;
		
		try
		{
			value = Float.parseFloat(attributes.getValue("value")); 
		}
		catch (NumberFormatException nfe)
		{
			value = attributes.getValue("value");			
		}
		
		_currentKB.Tell(Name.ParseName(name), value); 
		
		//System.out.println("KBSlot");
	    //System.out.println(name + " " + value);
	}	
	
	public void WMSlot(Attributes attributes)
	{
		String name = attributes.getValue("name");
		Object value;
		
		try
		{
			value = Float.parseFloat(attributes.getValue("value")); 
		}
		catch (NumberFormatException nfe)
		{
			value = attributes.getValue("value");			
		}
		
		_currentWM.Tell(_currentKB, Name.ParseName(name), value); 
		
		//System.out.println("WMSlot");
	    //System.out.println(name + " " + value);
	}
	
	public void Episode(Attributes attributes) {
	    String locations = attributes.getValue("location");
    	ArrayList<String> location = extractItems(locations);
	    String persons = attributes.getValue("people");
	   	ArrayList<String> people = extractItems(persons);
	    String objects = attributes.getValue("objects");
	   	ArrayList<String> object = extractItems(objects);
	   
	    _currentME = new MemoryEpisode(location, people, object);
	    _currentAM.putEpisode(_currentME);
	}
	
	public void EpisodeTime(Attributes attributes) {
	   
		Long narrativeTime = Long.parseLong(attributes.getValue("narrativeTime"));
	    Long realTime = Long.parseLong(attributes.getValue("realTime"));
	    long eventSequence = Long.parseLong(attributes.getValue("eventSequence"));
	    Time time = new Time(narrativeTime, realTime, eventSequence);	
	    
	    _currentME.setTime(time);
	    
	    //System.out.println("EpisodeTime");
	    //System.out.println(narrativeTime + " " + realTime + " " + eventSequence);
	}
	
	public void RetrievalQueue(Attributes attributes) {	    
		_currentRetrievalQueue = new RetrievalQueue(_currentAD.getID());
		_currentAD.setRetrievalQueue(_currentRetrievalQueue);
		// DEBUG
		//System.out.println("new queue created for id " + _currentAD.getID());
	}
	
	public void RetrievalTime(Attributes attributes) {
		Long narrativeTime = Long.parseLong(attributes.getValue("narrativeTime"));
	    Long realTime = Long.parseLong(attributes.getValue("realTime"));
	    long eventSequence = Long.parseLong(attributes.getValue("eventSequence"));
	    Time retrievalTime = new Time(narrativeTime, realTime, eventSequence);	
	    _currentRetrievalQueue.addRetrievalTime(retrievalTime);
	    // DEBUG
		//System.out.println("added retrieval time");
		//System.out.println(retrievalTime);
	}
	
	public void AMEvent(Attributes attributes) {
	    int eventID = Integer.parseInt(attributes.getValue("eventID"));
	    String subject = attributes.getValue("subject");
	    Short eType = Short.parseShort(attributes.getValue("eType"));
	    String event = attributes.getValue("event");
	    String status = attributes.getValue("status");
	    String target = attributes.getValue("target");
	    String location = attributes.getValue("location");
	    float desirability = Float.parseFloat(attributes.getValue("desirability"));
	    float praiseworthiness = Float.parseFloat(attributes.getValue("praiseworthiness"));		    
	    
	    _currentAD = new ActionDetail(_memory, eventID, subject, eType, event, status, target, location, desirability, praiseworthiness);
	    _currentME.putActionDetail(_currentAD);
	    _currentParameters = new ArrayList<Parameter>();
	    _currentAD.setParameters(_currentParameters);
	    
	    //System.out.println("AMEvent");
	    //System.out.println(eventID + " " + subject + " " + eType + " " + event 
	    //		+ " " + status + " " + target + " " + location + " " + desirability + " " + praiseworthiness);
	}
	
	public void Parameter(Attributes attributes) {
 		String name = attributes.getValue("name");
 		String value = attributes.getValue("value");
	    
 		Parameter param = new Parameter(name, value);
 		_currentParameters.add(param);
 		
	    //System.out.println("Parameter");
	    //System.out.println(name + " " + value);
 	} 	
	
 	public void Emotion(Attributes attributes) {
 		String type = attributes.getValue("type");
 		EmotionValence valence = EmotionValence.valueOf(attributes.getValue("valence").toUpperCase());
 		Name direction = Name.ParseName(attributes.getValue("direction"));
 		String aux = attributes.getValue("appraisalVariables");
 		
 		// Matthias 18/03/11: parse appraisal variables
 		ArrayList<String> auxList = extractItems(aux);
 		String[] appraisalVariables = new String[auxList.size()];
 		for(int i = 0; i < auxList.size(); i++) {
 			appraisalVariables[i] = auxList.get(i);
 		}
 		
	    float potential = Float.parseFloat(attributes.getValue("potential"));
	    	    
	    _currentEmotion = new MemoryBaseEmotion(type,valence,appraisalVariables, potential, null, direction);
	    _currentAD.setEmotion(_currentEmotion);
	    
	    //System.out.println("Emotion");
	    //System.out.println(type + " " + appraisalVariable.toString() + " " +  potential + " " + direction);
 	}
 	
 	public void Cause(Attributes attributes) {
 		String subject = attributes.getValue("subject");
 	    String action = attributes.getValue("action"); 	    
 	    String target = attributes.getValue("target");
 	    Short status = Short.valueOf(attributes.getValue("status"));
 	    Short type = Short.valueOf(attributes.getValue("type"));
 	    Long time = Long.valueOf(attributes.getValue("time"));
 	    
 	    _currentCause = new Event(subject, action, target, type, status, time);
 	    _currentEmotion.SetCause(_currentCause);
 	    
 	    //System.out.println("Cause");
	    //System.out.println(subject + " " + action + " " + target + " " + status + " " + type + " " + time);	    
 	}
 	
 	public void EParameter(Attributes attributes) {
 		String name = attributes.getValue("name");
 		String value = attributes.getValue("value");
	    
 		Parameter param = new Parameter(name, value);
 		_currentCause.AddParameter(param);
 		
	    //System.out.println("Parameter");
	    //System.out.println(name + " " + value);
 	}
 	
 	public void EventTime(Attributes attributes) {
 	   
		Long narrativeTime = Long.parseLong(attributes.getValue("narrativeTime"));
	    Long realTime = Long.parseLong(attributes.getValue("realTime"));
	    long eventSequence = Long.parseLong(attributes.getValue("eventSequence"));
	    Time time = new Time(narrativeTime, realTime, eventSequence);	    

	    _currentAD.setParameters(_currentParameters);
	    _currentAD.setTime(time);                
	    
	    //System.out.println("EventTime");
	    //System.out.println(narrativeTime + " " + realTime + " " + eventSequence);
	}
 	
 	public void STEvent(Attributes attributes) {
	    int eventID = Integer.parseInt(attributes.getValue("eventID"));
	    String subject = attributes.getValue("subject");
	    Short eType = Short.parseShort(attributes.getValue("eType"));
	    String event = attributes.getValue("event");
	    String status = attributes.getValue("status");
	    String target = attributes.getValue("target");
	    String location = attributes.getValue("location");
	    float desirability = Float.parseFloat(attributes.getValue("desirability"));
	    float praiseworthiness = Float.parseFloat(attributes.getValue("praiseworthiness"));	    

	    _currentAD = new ActionDetail(_memory, eventID, subject, eType, event, status, target, location, desirability, praiseworthiness);
	    _currentSTEM.putActionDetail(_currentAD);     
	    _currentParameters = new ArrayList<Parameter>();
	    
	    //System.out.println("STEvent");
	    //System.out.println(eventID + " " + subject + " " + eType + " " + event 
	    //		+ " " + status + " " + target + " " + location + " " + desirability + " " + praiseworthiness);
	}
 	
	/* 
	 * Extract items from a list
	 */
	private ArrayList<String> extractItems(String list)
	{
		ArrayList<String> items = new ArrayList<String>();
	
		if (!list.equals("") && list.length() > 2)
		{
			StringTokenizer st = new StringTokenizer(list.substring(1, list.length()-1), ",");
			
			while(st.hasMoreTokens())
			{
				String item = st.nextToken();
				if (!items.contains(item.trim()))
					items.add(item.trim());
				//System.out.println("Item " + item);
			}
		}
		return items;
	}

}
