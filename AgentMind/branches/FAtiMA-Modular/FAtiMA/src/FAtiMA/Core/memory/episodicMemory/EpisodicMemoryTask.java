/** 
 * EpisodicMemoryTask.java - TimerTask for Episodic Memory  
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
 * Company: HWU
 * Project: LIREC
 * Created: 05/07/2011
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 */

package FAtiMA.Core.memory.episodicMemory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;

import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.util.writers.MemoryWriter;

public class EpisodicMemoryTask extends TimerTask {

	private final static String filepath = "data/characters/minds/state/";

	private Memory memory;
	private boolean start = true;

	public EpisodicMemoryTask(Memory memory) {
		this.memory = memory;
	}

	public void run() {
		
		if(!start) {
			// set date format
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			String strTime = sdf.format(cal.getTime());		
			
			if (cal.get(Calendar.HOUR_OF_DAY) == 16){
				// move events to LTM
				memory.getEpisodicMemory().MoveSTEMtoAM();
				//System.out.println(Calendar.HOUR_OF_DAY + " Moved memory");
				
				//EpisodicMemory tempEM = (EpisodicMemory) memory.getEpisodicMemory().clone();				
				// Activation-Based Forgetting
				//System.out.println(strTime + ": performing AB Forgetting (20%)");
				memory.getEpisodicMemory().calculateActivationValues();
				// forgetting based on threshold
				ArrayList<ActionDetail> forget = memory.getEpisodicMemory().activationBasedForgettingByThreshold(-8);
				
				// log/write details of forgotten events (for forgetting experiment)
				// -> ArrayList<ActionDetail> forget (see above)
				// XML Memory export
				MemoryWriter adWriter = new MemoryWriter(forget);
				System.out.println(strTime + ": saving XML Forgotten Events");
				adWriter.outputForgottenADtoXML(filepath + "XML_" + strTime + "_ForgettingByThreshold" + ".xml");
				
				forget = memory.getEpisodicMemory().activationBasedForgettingByAmount(0.2);
				adWriter.outputForgottenADtoXML(filepath + "XML_" + strTime + "_ForgettingByAmount " + ".xml");
				
				forget = memory.getEpisodicMemory().activationBasedForgettingByCount(20);
				adWriter.outputForgottenADtoXML(filepath + "XML_" + strTime + "_ForgettingByAmount " + ".xml");
			} 
			// XML Memory export		
			MemoryWriter memoryWriter = new MemoryWriter(memory);
			System.out.println(strTime + ": saving XML Memory");
			memoryWriter.outputMemorytoXML(filepath + "XMLMemory_" + strTime + ".xml");
				
			/*
			// Activation-Based Forgetting
			System.out.println(strTime + ": performing AB Forgetting (20%)");
			memory.getEpisodicMemory().calculateActivationValues();
			// TODO:
			// decide about forgetting method (count, ratio or threshold; which parameters settings?)
			ArrayList<ActionDetail> forget = memory.getEpisodicMemory().activationBasedForgettingByAmount(0.2);
			memory.getEpisodicMemory().applyActivationBasedForgetting(forget);
			// TODO:
			// log/write details of forgotten events (for forgetting experiment)
			// -> ArrayList<ActionDetail> forget (see above)
	
			// XML Memory export
			System.out.println(strTime + ": saving XML Memory");
			memoryWriter.outputMemorytoXML(filepath + "XMLMemory_" + strTime + "_AfterABForgetting" + ".xml");
			*/
	
			if (cal.get(Calendar.HOUR_OF_DAY) == 16){
				// start a new episode
				memory.getEpisodicMemory().StartEpisode(memory);
			}
		}
		start = false;
	}

}
