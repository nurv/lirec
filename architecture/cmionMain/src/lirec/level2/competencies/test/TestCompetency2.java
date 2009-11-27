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
import java.util.Random;

import lirec.architecture.IArchitecture;
import lirec.level2.Competency;

/** one of several test competencies to test the execution system */ 
public class TestCompetency2 extends Competency 
{

	/** constructor of the test competency */
	public TestCompetency2(IArchitecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		competencyName = "TestCompetency2";
		competencyType = "TestType2";

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
		System.out.println("Test Competency 2 running, with parameters below: ");
		for (String parameter : parameters.keySet())
			System.out.println("  "+ parameter + " = " + parameters.get(parameter));
		
		// test competency 2 generates a random number, writes it to the blackboard
		// in the variable with the name that parameter "RandomOutName" defines
		
		// check if we have a parameter called RandomOutName
		if (!parameters.containsKey("RandomOutName")) return false;
		
		// generate a random number and post it on the blackboard
		Integer random = new Random().nextInt();
		System.out.println("Test Competency 2: wrote random no " + random);
		architecture.getBlackBoard().requestSetProperty(parameters.get("RandomOutName"), random);
		
		// returns competency success
		return true;
	}		
	
}
