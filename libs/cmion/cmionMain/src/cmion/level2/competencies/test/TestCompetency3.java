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


/** one of several test competencies to test the execution system */ 
public class TestCompetency3 extends Competency 
{

	/** constructor of the test competency */
	public TestCompetency3(IArchitecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		competencyName = "TestCompetency3";
		competencyType = "TestType3";

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
		System.out.println("Test Competency 3 running, with parameters below: ");
		for (String parameter : parameters.keySet())
			System.out.println("  "+ parameter + " = " + parameters.get(parameter));
		
		// read random number that TestCompetency2 has created from blackboard
		// in the variable with the name that parameter "RandomInName" defines
		
		// check if we have a parameter called RandomInName
		if (!parameters.containsKey("RandomInName")) return false;
		
		// check if number is posted on the blackboard
		if (! architecture.getBlackBoard().hasProperty(parameters.get("RandomInName"))) return false;
		
		// read it
		Object randomRead = architecture.getBlackBoard().getPropertyValue(parameters.get("RandomInName"));
		
		// test if it is an integer and cast
		if (!(randomRead instanceof Integer)) return false;
		Integer random = (Integer) randomRead;
		
		System.out.println("Test Competency 3: read random no " + random);
		
		// lets also post something to the world model, lets add an agent TestUser
		// with one property test = true
		HashMap<String, Object> initialProperties = new HashMap<String, Object>();
		initialProperties.put("test", "True");
		architecture.getWorldModel().requestAddAgent("TestUser", initialProperties);
		System.out.println("Test Competency 3: has added agent TestUser to world model with property test = true");
		
		
		// returns competency success
		return true;
	}		
	
}
