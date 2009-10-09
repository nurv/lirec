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

package lirec.level3;

import lirec.level2.CompetencyExecutionPlan;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** a competency manager rule maps a mind action and the state of available competencies 
 *  to a CompetencyExecutionPlan*/
public class CompetencyManagerRule 
{

	/** the mind action that should be matched, parameter values and action names can be "*"
	 *  to signify a match with anything */
	private MindAction mindAction;
	
	/** the execution plan this rule triggers */
	private CompetencyExecutionPlan executionPlan;
	
	/** the specificity of this rule (see getSpecificity for more details) */
	private int specificity;
	
	/** create a new competency manager rule from a dom node (originating form the parsed 
	 * competeny manager rules xml file). An exception is thrown when xml is malformed */
	public CompetencyManagerRule(Node domNode) throws Exception
	{
		mindAction = null;
		executionPlan = null;
		
		// iterate through the children nodes of the rule node
		NodeList children = domNode.getChildNodes();
		for (int i=0; i< children.getLength(); i++)
		{
			if (children.item(i).getNodeName().equals("MindAction"))
			{
				if (mindAction!=null) throw new Exception("More than one MindAction section in a competency manager rule.");
				else mindAction = new MindAction(children.item(i));
			}
			else if (children.item(i).getNodeName().equals("CompetencyExecutionPlan"))
			{
				if (executionPlan!=null) throw new Exception("More than one Execution Plan section in a competency manager rule.");
				else executionPlan = new CompetencyExecutionPlan(children.item(i));
			}
		}
		
		if (mindAction==null) throw new Exception("No MindAction section in a competency manager rule.");
		if (executionPlan==null) throw new Exception("No Execution Plan section in a competency manager rule.");

		// calculate specificity of this rule
		
		// max specificity is 0
		specificity = 0;
		
		// subtract 1 for every "*" found, where matching is possible (action name and parameters)
		if (mindAction.name.equals("*")) specificity--;
		for (String parameter : mindAction.parameters)
			if (parameter.equals("*")) specificity--;		
	}
	
	/** returns the competency execution plan connected with this rule */
	public CompetencyExecutionPlan getExecutionPlan() {
		return executionPlan;
	}
	
	/** returns the mind action connected with this rule */
	public MindAction getMindAction() {
		return mindAction;
	}
	
	/** returns the specificity of this rule. A rule is more specific, the less 
	 * "*" matches it has. We thus define maximum specificity as 0, and subtract 1 for 
	 * every "*" */
	public int getSpecificity()
	{
		return specificity;
	}
	
}
