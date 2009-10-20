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

package lirec.level2.competencies;

import java.util.HashMap;

import lirec.architecture.IArchitecture;
import lirec.level2.Competency;

/** example of the implementation of a competency, with comments explaining how
 * to define competencies, this is not actually doing anything serious */
public class ExampleCompetency extends Competency {

	/** constructor, every competency, should if possible have a constructor 
	 *  that receives the Architecture as an argument */
	public ExampleCompetency(IArchitecture architecture)
	{
		// call parent class constructor, always do this first
		super(architecture);
		
		// set the name/identifier for the competency
		competencyName = "ExampleCompetency1";

		// set the type for the competency, for this exampole lets pretend 
		// our competency is a DetectPerson competency
		competencyType = "DetectPerson";

		// with other initialisations wait for the initialize method
	}

	/** perform initialisations in this method */
	@Override
	public void initialize() {
		// if initialisations take time, start a thread here
		
		// set available to true to indicate the initialisation has finished successfully
		// if thread was used as suggested above do this as last command in the thread
		available = true;
	}

	
	/** the custom code of the competence, this is already running in a thread */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		System.out.println("example competency running");
		// what normally would happen here is 
		// a) parse parameters, check if they are correct
		// b) run the code
		// c) maybe update the world model
		// d) read and write from the blackboard
		// e) return success (boolean)
		
		// ended successful
		return true;
	}

}
