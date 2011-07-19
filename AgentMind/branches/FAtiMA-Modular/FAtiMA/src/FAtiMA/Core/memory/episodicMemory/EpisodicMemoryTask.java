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

	public EpisodicMemoryTask(Memory memory) {
		this.memory = memory;
	}

	public void run() {

		// move events to LTM
		memory.getEpisodicMemory().MoveSTEMtoAM();
		
		// set date format
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String strTime = sdf.format(cal.getTime());

		// XML Memory export
		System.out.println(strTime + ": saving XML Memory");
		MemoryWriter memoryWriter = new MemoryWriter(memory);
		memoryWriter.outputMemorytoXML(filepath + "XMLMemory_" + strTime + ".xml");

		// Activation-Based Forgetting
		System.out.println(strTime + ": performing AB Forgetting (20%)");
		memory.getEpisodicMemory().calculateActivationValues();
		ArrayList<ActionDetail> selected = memory.getEpisodicMemory().activationBasedSelectionByAmount(0.8);
		ArrayList<Integer> selectedIDs = new ArrayList<Integer>();
		for (ActionDetail actionDetail : selected) {
			selectedIDs.add(new Integer(actionDetail.getID()));
		}
		memory.getEpisodicMemory().activationBasedForgetting(selectedIDs);

		// XML Memory export
		System.out.println(strTime + ": saving XML Memory");
		memoryWriter.outputMemorytoXML(filepath + "XMLMemory_" + strTime + "_AfterABForgetting" + ".xml");
		
		// start a new episode
		memory.getEpisodicMemory().StartEpisode(memory);
		
	}

}
