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

package FAtiMA.AdvancedMemory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimerTask;

import FAtiMA.Core.memory.Memory;

public class AdvancedMemoryTask extends TimerTask {

	private AdvancedMemoryComponent advancedMemoryComponent;

	public AdvancedMemoryTask(AdvancedMemoryComponent advancedMemoryComponent, Memory memory) {
		this.advancedMemoryComponent = advancedMemoryComponent;
	}

	public void run() {

		// set date format
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String strTime = sdf.format(cal.getTime());

		// initialise
		/*String attributeNamesStr;
		int minimumCoverage;

		// generalise
		System.out.println(strTime + ": performing Generalisation");
		attributeNamesStr = "subject*action*target";
		minimumCoverage = 1;
		Generalisation generalisation = new Generalisation();
		generalisation.generalise(advancedMemoryComponent.getMemory().getEpisodicMemory(), attributeNamesStr, minimumCoverage);

		// add to results
		advancedMemoryComponent.getResults().add(generalisation);*/

		// write to XML
		String fileName = advancedMemoryComponent.getMemory().getSaveDirectory() + AdvancedMemoryComponent.FILENAME + "_" + strTime;
		advancedMemoryComponent.save(fileName);

	}

}
