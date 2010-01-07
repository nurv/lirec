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


public class TestCompetency5 extends Competency 
{

	/** remember the 1st parameter that was passed to us in the constructor in this field */
	private String someParameter1;
	
	/** remember the 2nd parameter that was passed to us in the constructor in this field */
	private String someParameter2;
	
	/** constructor of the test competency, this one has an additional parameter, purely
	 *  for the purpose whether the constructor is found and the parameter passed correctly */
	public TestCompetency5(IArchitecture architecture, String someParameter1, String someParameter2)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		this.someParameter1 = someParameter1;
		this.someParameter2 = someParameter2;
		
		competencyName = "TestCompetency5";
		// this is also of type 4, purpose is to test whether this competency will be
		// selected after TestCompetency4 fails
		competencyType = "TestType4";

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

	/** competency code */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		System.out.println("Test Competency 5 running, with parameters below: ");
		for (String parameter : parameters.keySet())
			System.out.println("  "+ parameter + " = " + parameters.get(parameter));
		
		
		System.out.println("These are the 2 parameters that Test Competency 5 was constructed with: ");
		System.out.println(someParameter1);
		System.out.println(someParameter2);
		
		// test competency 5 always succeeds
		return true;
	}		
	
}
