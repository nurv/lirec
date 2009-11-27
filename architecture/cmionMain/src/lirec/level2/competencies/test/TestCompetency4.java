/*	
        Lirec Architecture
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
  ---  
*/

package lirec.level2.competencies.test;

import java.util.HashMap;

import lirec.architecture.IArchitecture;
import lirec.level2.Competency;

/** one of several test competencies to test the execution system */ 
public class TestCompetency4 extends Competency {

	/** constructor of the test competency */
	public TestCompetency4(IArchitecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		competencyName = "TestCompetency4";
		competencyType = "TestType4";

		// with other initialisations wait for the initialize method
	}

	/** perform initialisations in this method */
	@Override
	public void initialize() {
		available = true;
	}

	/** competency code */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		System.out.println("Test Competency 4 running, with parameters below: ");
		for (String parameter : parameters.keySet())
			System.out.println("  "+ parameter + " = " + parameters.get(parameter));
		
		// test competency 4 always fails
		return false;
	}	
		
	
}
