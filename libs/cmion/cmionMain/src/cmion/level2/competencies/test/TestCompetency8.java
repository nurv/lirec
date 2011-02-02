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
import cmion.level2.CompetencyCancelledException;


/** one of several test competencies to test the execution system */ 
public class TestCompetency8 extends Competency
{

	private boolean shouldCancel;
	
	/** constructor of the test competency */
	public TestCompetency8(IArchitecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		competencyName = "TestCompetency8";
		competencyType = "TestType8";

		// with other initialisations wait for the initialize method
	}

	/** perform initialisations in this method */
	@Override
	public void initialize() {
		available = true;
	}

	/** this competency is invoked directly (does not run in background) */
	@Override
	public boolean runsInBackground() 
	{
		return false;
	}	
	
	/** implementing cancel method to give this competency the ability to be cancelled */
	@Override
	public void cancel()
	{
		shouldCancel = true;
	}
	
	/** competency code 
	 * @throws CompetencyCancelledException */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) throws CompetencyCancelledException 
	{
		System.out.println("TestCompetency8 running");
		
		shouldCancel = false;

		/** this is a competency that takes a very long time to execute and that 
		 *  can be cancelled, if it is not cancelled it will take at least 10 minutes 
		 *  (600 seconds) */
		for (int i=0; i<60; i++)
		{
			try
			{
				Thread.sleep(10000);
			}
			catch (InterruptedException e) {}
			
			if (shouldCancel) throw new CompetencyCancelledException();
		}
				
		// test competency 8 always succeeds if it is not cancelled
		return false;
	}
	
}
