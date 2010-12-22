/** 
 * MemoryWriter.java - Writes memory content into an xml file. 
 * Uses the org.znerd.xmlenc package - jar file in the FAtiMA main directory.
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
 * Created: 15/12/10
 * @author: Meiyii Lim
 * Email to: M.Lim@hw.ac.uk
 * 
 * History: 
 * Meiyii Lim: 15/12/10 - File created
 * 
 * **/

package FAtiMA.Core.util.writers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import org.znerd.xmlenc.LineBreak;
import org.znerd.xmlenc.XMLOutputter;

import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.Time;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.enumerables.EmotionType;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.wellFormedNames.Name;


public class MemoryWriter implements Serializable{

	final String encoding = "iso-8859-1";
	private Writer _writer;
	private XMLOutputter _outputter;
	private Memory _memory;
	
	public MemoryWriter(Memory memory)
	{
		_outputter = new XMLOutputter();	
		_memory = memory;
	}
	
	public void outputMemoryInXML(String file)
	{
		try{
			_writer = new FileWriter(file); // new BufferedWriter(new FileWriter(file));
			//_writer = new OutputStreamWriter(System.out, encoding);
		    _outputter = new XMLOutputter(_writer, encoding);
			_outputter.startTag("EpisodicMemory");
			_outputter.setLineBreak(LineBreak.DOS);
			_outputter.setIndentation("   ");
			
			// AutobiographicalMemory entries
			_outputter.startTag("AutobiographicalMemory");			
			for (MemoryEpisode me: _memory.getEpisodicMemory().GetAllEpisodes())
			{				
				_outputter.startTag("Episode");		
				if(me.getLocation() != null)
					_outputter.attribute("location", me.getLocation().toString());	//ArrayList
				if(me.getPeople() != null)
					_outputter.attribute("people", me.getPeople().toString());		//ArrayList
				if(me.getObjects() != null)
					_outputter.attribute("objects", me.getObjects().toString());	//ArrayList	
				
				//episode time
				_outputter.startTag("EpisodeTime");
				_outputter.attribute("narrativeTime", Long.toString(me.getTime().getNarrativeTime()));
				_outputter.attribute("realTime", Long.toString(me.getTime().getRealTime()));
				_outputter.attribute("eventSequence", Integer.toString(me.getTime().getEventSequence()));
				_outputter.endTag();	//EpisodeTime
				
				// episode events
				for (ActionDetail ad: me.getDetails())
				{
					_outputter.startTag("AMEvent");
					actionDetailtoXML(ad);
					_outputter.endTag();	//Event
				}
				_outputter.endTag(); //Episode
			}
			_outputter.endTag(); //AutobiographicMemory			
			_outputter.whitespace("\n");
			
			// ShortTermEpisodicMemory entries
			_outputter.startTag("STEpisodicMemory");					
			for (ActionDetail ad: _memory.getEpisodicMemory().getDetails())
			{
				_outputter.startTag("STEvent");
				actionDetailtoXML(ad);				
				_outputter.endTag();	//STEvent
			}
			_outputter.endTag(); //STEpisodicMemory			
			_outputter.endTag(); //EpisodicMemory
			_outputter.endDocument(); 
		    _outputter.getWriter().flush();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void actionDetailtoXML(ActionDetail ad)
	{
		try{
			_outputter.attribute("eventID", Integer.toString(ad.getID()));	
			if(ad.getSubject() != null)
				_outputter.attribute("subject", ad.getSubject());	
			if(ad.getIntention() != null)
			{
				_outputter.attribute("eType", Short.toString(EventType.GOAL));
				_outputter.attribute("event", ad.getIntention());
			}
			else if(ad.getAction() != null)
			{
				_outputter.attribute("eType", Short.toString(EventType.ACTION));
				_outputter.attribute("event", ad.getAction());
			}
			if(ad.getStatus() != null)
				_outputter.attribute("status", ad.getStatus());	
			if(ad.getTarget() != null)
				_outputter.attribute("target", ad.getTarget());
			if(ad.getLocation() != null)
				_outputter.attribute("location", ad.getLocation());
			
			_outputter.attribute("desirability", Float.toString(ad.getDesirability()));	
			_outputter.attribute("praiseworthiness", Float.toString(ad.getPraiseworthiness()));		
			
			if(ad.getParameters() != null)
			{						
				for (Parameter p: ad.getParameters())
				{
					_outputter.startTag("Parameter");
					_outputter.attribute("name", p.GetName()); 
					_outputter.attribute("value", p.GetValue().toString());
					_outputter.endTag();
				}					
			}
			
			// event emotion
			if(ad.getEmotion() != null)
			{
				_outputter.startTag("Emotion");
				_outputter.attribute("type", Short.toString(ad.getEmotion().GetType()));
				if(ad.getEmotion().GetDirection() != null)
					_outputter.attribute("direction", ad.getEmotion().GetDirection().toString());
				if(ad.getEmotion().GetAppraisalVariables() != null)
					_outputter.attribute("appraisalVariables", ad.getEmotion().GetAppraisalVariables().toString());	//ArrayList
				_outputter.attribute("potential", Float.toString(ad.getEmotion().GetPotential()));
				
				//cause event
				if(ad.getEmotion().GetCause() != null)
				{							
					_outputter.startTag("Cause");
					if(ad.getEmotion().GetCause().GetAction() != null)
						_outputter.attribute("action", ad.getEmotion().GetCause().GetAction());
					if(ad.getEmotion().GetCause().GetSubject() != null)
						_outputter.attribute("subject", ad.getEmotion().GetCause().GetSubject());
					if(ad.getEmotion().GetCause().GetTarget() != null)
						_outputter.attribute("target", ad.getEmotion().GetCause().GetTarget());
				
					_outputter.attribute("status", Short.toString(ad.getEmotion().GetCause().GetStatus()));
					_outputter.attribute("type", Short.toString(ad.getEmotion().GetCause().GetType()));
					_outputter.attribute("time", Long.toString(ad.getEmotion().GetCause().GetTime()));
					
					if(ad.getEmotion().GetCause().GetParameters() != null)
					{						
						for (Parameter p: ad.getEmotion().GetCause().GetParameters())
						{
							_outputter.startTag("EParameter");
							_outputter.attribute("name", p.GetName()); 
							_outputter.attribute("value", p.GetValue().toString());
							_outputter.endTag();
						}					
					}					
					_outputter.endTag();	//Cause
				}						
				_outputter.endTag(); //Emotion
			}
			
			// event time
			_outputter.startTag("EventTime");
			_outputter.attribute("narrativeTime", Long.toString(ad.getTime().getNarrativeTime()));
			_outputter.attribute("realTime", Long.toString(ad.getTime().getRealTime()));
			_outputter.attribute("eventSequence", Integer.toString(ad.getTime().getEventSequence()));
			_outputter.endTag();	//EventTime
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
