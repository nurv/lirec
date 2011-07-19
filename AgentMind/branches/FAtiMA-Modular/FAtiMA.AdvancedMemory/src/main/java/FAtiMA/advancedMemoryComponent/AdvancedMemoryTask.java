/** 
 * AdvancedMemoryTask.java - TimerTask for Advanced Memory  
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

package FAtiMA.advancedMemoryComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;

import FAtiMA.Core.memory.Memory;

public class AdvancedMemoryTask extends TimerTask {

	private final static String filepath = "data/characters/minds/state/";

	private AdvancedMemoryComponent advancedMemoryComponent;
	private Memory memory;

	public AdvancedMemoryTask(AdvancedMemoryComponent advancedMemoryComponent, Memory memory) {
		this.advancedMemoryComponent = advancedMemoryComponent;
		this.memory = memory;
	}

	public void run() {

		// set date format
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String strTime = sdf.format(cal.getTime());

		// initialise
		ArrayList<String> gAttributes;
		AdvancedMemoryWriter advancedMemoryWriter;

		// generalise
		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("action");
		gAttributes.add("target");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubActTar" + ".xml");

		// generalise
		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("action");
		gAttributes.add("location");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubActLoc" + ".xml");

		// generalise
		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("action");
		gAttributes.add("time");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubActTim" + ".xml");

		// generalise
		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("action");
		gAttributes.add("target");
		gAttributes.add("location");
		gAttributes.add("time");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubActTarLocTim" + ".xml");

		// generalise
		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("location");
		gAttributes.add("time");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubLocTim" + ".xml");

	}

}
