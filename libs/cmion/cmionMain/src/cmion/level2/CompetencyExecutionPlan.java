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

package cmion.level2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cmion.architecture.IArchitecture;
import cmion.level3.MindAction;

/** a plan of competencies that can be executed by the competency execution system,
 * this class is used to represent both instantiated and not instantiated execution plans.
 * Inside the competency manager rules uninstantiated execution plans are stored, while
 * once the competency manager selects a plan, it creates an instantiated copy ($ variables
 * in the competency parameters of the plan steps are replaced by actual values) */
public class CompetencyExecutionPlan {

	/** the different steps this plan is made of, indexed by plan step id */
	private HashMap<String,CompetencyExecutionPlanStep> planSteps; 	
	
	/** during execution of the plan this stores the steps of the plan that
	 *  have already been completed, this is only used for instantiated plans */
	private ArrayList<CompetencyExecutionPlanStep> stepsAlreadyCompleted;

	/** during execution of the plan this stores the steps of the plan that
	 *  are currently being executed, this is only used for instantiated plans */
	private ArrayList<CompetencyExecutionPlanStep> stepsCurrentlyExecuting;

	/** stores whether this plan is instantiated (true) or uninstantiated (false) */
	private boolean instantiated;
	
	/** for instantiated plans this stores whether they are currently executed or not*/
	private boolean currentlyExecuting;
	
	/** the mind action which this plan is executing */
	private MindAction mindAction;
	
	/** refernce to cmion architecture */
	private IArchitecture architecture;
	
	/** creates a new competencyExecutionPlan with no steps */
	public CompetencyExecutionPlan(IArchitecture architecture)
	{
		this.architecture = architecture;
		planSteps = new HashMap<String,CompetencyExecutionPlanStep>();
		instantiated = false;
		currentlyExecuting = false;
		mindAction = null;
	}
	
	/** creates a new competencyExecutionPlan from a DOM node */
	public CompetencyExecutionPlan(Node domNode, IArchitecture architecture) throws Exception
	{
		this(architecture);
		NodeList children = domNode.getChildNodes();
		for (int i=0; i<children.getLength(); i++)
		{
			if (children.item(i).getNodeName().equals("Competency"))
			{
				CompetencyExecutionPlanStep step = new CompetencyExecutionPlanStep(children.item(i),architecture);
				if (!planSteps.containsKey(step.getID()))
					planSteps.put(step.getID(), step);
				else
					throw new Exception("Competency Execution plan is corrupt: ID " 
					+ step.getID()+ " was used more than once.");
			}	
		}
		
		// check for integrity of planSteps preconditions, i.e. no step specifies a 
		// precondition that is not the id of another step
		for (CompetencyExecutionPlanStep step : planSteps.values())
			for (String preCondition : step.getPreconditions())
				if (!planSteps.containsKey(preCondition)) 
					throw new Exception("Competency Execution plan is corrupt: precondition "
						+ preCondition + " could not be resolved");
		
	}
	
	
	/** returns an instantiated copy of this competency execution plan
	 * 
	 * @param mappings the mappings for variables to use for instantiation
	 * @param mindAction the mind action which this plan is executing
	 * @return a new competency execution plan which is an instantiated copy of the 
	 * calling object
	 */
	public CompetencyExecutionPlan getInstantiatedCopy(HashMap<String,String> mappings, MindAction mindAction)
	{
		CompetencyExecutionPlan returnPlan = new CompetencyExecutionPlan(architecture);
		
		returnPlan.mindAction = mindAction;
		
		// iterate over all current plan steps and add an instantiated copy to the new plan
		for (String planStepID : planSteps.keySet())
			returnPlan.planSteps.put(planStepID,planSteps.get(planStepID).getInstantiatedCopy(mappings));		
		
		// set the returned plan to be instantiated
		returnPlan.instantiated = true;
		
		// and initialise the working variables that instantiated plans need
		returnPlan.stepsAlreadyCompleted = new ArrayList<CompetencyExecutionPlanStep>();
		returnPlan.stepsCurrentlyExecuting = new ArrayList<CompetencyExecutionPlanStep>();
		
		return returnPlan;
	}

	/** returns the no of steps in the plan */
	public int getNoOfSteps()
	{
		return planSteps.size();
	}
	
	/** returns all plan steps as a collection, do not modify this collection 
	 * 	and do only use this method on instantiated competencyExecution Plans */
	public Collection<CompetencyExecutionPlanStep> getPlanSteps()
	{
		return planSteps.values();
	}
	
	/** returns the plan step with the given id or null if such a step does not exist */
	public CompetencyExecutionPlanStep getPlanStep(String id)
	{
		return planSteps.get(id);	
	}
	
	/** returns the steps of the plan that have already been completed,
	 *  this list may be modified outside of the class */
	public synchronized ArrayList<CompetencyExecutionPlanStep> getStepsAlreadyCompleted()
	{
		return stepsAlreadyCompleted;
	}

	/** returns the steps of the plan that are currently being executed,
	 *  this list may be modified outside of the class */
	public synchronized ArrayList<CompetencyExecutionPlanStep> getStepsCurrentlyExecuted()
	{
		return stepsCurrentlyExecuting;
	}
	
	/** returns whether this plan is instantiated */
	public synchronized boolean isInstantiated()
	{
		return instantiated;
	}
	
	/** returns whether this plan is currently executing (only relevant for instantiated plans) */
	public synchronized boolean isCurrentlyExecuting()
	{
		return currentlyExecuting;
	}
	
	/** the competency execution system should call this when it starts executing a plan
	 * (only relevant for instantiated plans) */
	public synchronized void startExecution()
	{
		// empty execution working variables
		stepsAlreadyCompleted.clear();
		stepsCurrentlyExecuting.clear();
		
		// this also includes the competencies already tried lists for all plan steps
		for (CompetencyExecutionPlanStep step : planSteps.values())
			step.getCompetenciesAlreadyTried().clear();
		
		currentlyExecuting = true; 
		
	}

	/** the competency execution system should call this when it has finished executing a plan 
	 * (both in case of plan failure and success), (only relevant for instantiated plans) */	
	public synchronized void stopExecution()
	{
		currentlyExecuting = false; 		
	}
	
	/** returns the mind action that this plan is executing */
	public MindAction getMindAction()
	{
		return mindAction;
	}

}
