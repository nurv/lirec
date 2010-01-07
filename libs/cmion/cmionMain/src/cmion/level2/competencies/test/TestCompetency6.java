/*	
    CMION
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
  09/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  Renamed to CMION
  ---  
*/

package cmion.level2.competencies.test;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;


/** this is an example of a competency that is not started as part of an order
 *  issued from the mind, but that continuously runs in the background, in this
 *  case printing out a message every 5 seconds. This type of competency might be used
 *  for sensors that are continuously active instead of being explicitly invoked */ 
public class TestCompetency6 extends Competency {

	/** constructor of the test competency */
	public TestCompetency6(IArchitecture architecture) {
		super(architecture);

		competencyName = "TestCompetency4";
		
		competencyType = "TestType6";
	}

	/** this competency runs in background and is not invoked directly */
	@Override
	public boolean runsInBackground() 
	{
		return true;
	}
	
	
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		System.out.println("Test Competency 6 running ");
		
		// note: since this competency is started together with the architecture, 
		// the parameters hash map will always be empty. If something needs to be passed
		// to this competency this can be done in the constructor
		
		// since this competency runs continuously we put the code in an endless loop
		// that also means we don't need to return anything
		while (true)
		{
			// sleep a while
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
			
			// post a message
			System.out.println("Test Competency 6 is doing something ");
		}
	}

	@Override
	public void initialize() {
		available = true;	
	}

}
