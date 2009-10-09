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

package lirec.level2;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.Simulation;
import ion.Meta.TypeSet;

import java.util.ArrayList;
import java.util.HashMap;

import lirec.architecture.Architecture;
import lirec.architecture.LirecComponent;

/** this component is responsible for executing plans of competencies it has received from the competency
 *  manager */
public class CompetencyExecution extends LirecComponent {
	
	/** store all plans that we are currently executing */
	private ArrayList<CompetencyExecutionPlan> currentPlans;
	
	/** create a new Competency Execution Component */
	public CompetencyExecution(Architecture architecture)
	{
		super(architecture);
		currentPlans = new ArrayList<CompetencyExecutionPlan>();
	}
	
	/** start the execution of a competency execution plan */
	private synchronized void executePlan(CompetencyExecutionPlan cep)
	{
		// check if the plan is instantiated and not already being executed
		if (cep.isInstantiated() && !cep.isCurrentlyExecuting())
		{
			// set the plan to currently executing 
			cep.startExecution();
			
			// add it to the list of our current plans
			currentPlans.add(cep);
			
			// start the first steps of the plan
			updatePlanProgress(cep);	
		}
	}

	/** checks pre conditions of provided plan and if possible either executes the next batch
	 *  of steps or if all steps are finished raises a plan success event */
	private synchronized void updatePlanProgress(CompetencyExecutionPlan cep)
	{		
		// build a list of plan steps that are neither completed yet nor executed currently
		ArrayList<CompetencyExecutionPlanStep>  stepsLeft = new ArrayList<CompetencyExecutionPlanStep>();
		for (CompetencyExecutionPlanStep step : cep.getPlanSteps())
		{
			if (!cep.getStepsAlreadyCompleted().contains(step) && 
				!cep.getStepsCurrentlyExecuted().contains(step))
				stepsLeft.add(step);
		}
		
		// check if there are any steps left
		if (!stepsLeft.isEmpty())
		{
			// check the preconditions for each step left
			for (CompetencyExecutionPlanStep newStep : stepsLeft)
			{
				boolean fulfilled = true;
				// check for each precondition individually if its fulfilled
				for (String precondition : newStep.getPreconditions())
				{
					CompetencyExecutionPlanStep dependentStep = cep.getPlanStep(precondition);
					if (!cep.getStepsAlreadyCompleted().contains(dependentStep))
						fulfilled = false;							
				}
				
				// all preconditions are fulfilled, we can execute the new step
				if (fulfilled) executeStep(newStep, cep);		
			}
			
		} else
		{
			// no steps left, check if we are still executing some
			if (cep.getStepsCurrentlyExecuted().isEmpty())
			{
				// we are not executing any steps, so this competency execution plan has finished successfully
				
				// mark the plan as not anymore executing
				cep.stopExecution();
				
				// remove this plan from our current plans
				currentPlans.remove(cep);
				
				// raise an event that the plan has succeeded
				this.raise(new EventCompetencyExecutionPlanSucceeded(cep));
			}	
		}		
		
	}
	
	/** find a competency for the given step of the given plan and request its execution,
	 *  or if this could not be accomplished fail the whole plan */
	private synchronized void executeStep(CompetencyExecutionPlanStep step, CompetencyExecutionPlan cep)
	{
		// find a competency from the library that can execute this step
		ArrayList<Competency> competencies = architecture.getCompetencyLibrary().getCompetencies(step.getCompetencyType());
		
		boolean foundSuitableCompetency = false;
		
		// check if there are competetencies to realize this step, that have not been tried yet
		for (Competency competency : competencies)
			if (!step.getCompetenciesAlreadyTried().contains(competency)
				&& competency.isAvailable()
				&& !competency.isRunning()) 
			{
				// we found a suitable competency
				foundSuitableCompetency = true;
				
				// remember we have tried it
				step.getCompetenciesAlreadyTried().add(competency);
				
				// flag that we are currently executing this plan step
				cep.getStepsCurrentlyExecuted().add(step);
				
				// request the competency to start
				competency.requestStartCompetency(step.getCompetencyParameters());
				
				break;
			}

		// since we could not find any suitable competency for this step, we have to consider the whole plan as failed		
		if (!foundSuitableCompetency) planFailed(cep);
	}

	/** this method should be called when an execution plan has failed */
	private synchronized void planFailed(CompetencyExecutionPlan cep)
	{
		// mark the plan as not anymore executing
		cep.stopExecution();
		
		// remove this plan from our current plans
		currentPlans.remove(cep);
		
		// raise an event that the plan has failed
		this.raise(new EventCompetencyExecutionPlanFailed(cep));		
	}
	
	/** process the success of a competency*/
	private synchronized void processCompetencySuccess(Competency competency, HashMap<String,String> parameters)
	{
		
		// find the plans and plan steps that correspond to this competency
		ArrayList<CompetencyExecutionPlan> plans = new ArrayList<CompetencyExecutionPlan>();
		ArrayList<CompetencyExecutionPlanStep> steps = new ArrayList<CompetencyExecutionPlanStep>();
		
		for (CompetencyExecutionPlan cep: currentPlans)
			for (CompetencyExecutionPlanStep step : cep.getStepsCurrentlyExecuted())
				if (step.getCompetencyType().equals(competency.getCompetencyType()))
					// compare parameters to make sure this competency has performed what the plan step required
					if (parameters.equals(step.getCompetencyParameters()))
					{
						// we have found a plan and a step of this plan that was completed through this competency
						plans.add(cep);
						steps.add(step);						
					}
		
		// now process the found matches (note: this is in a seperate loop from above, because the processing 
		// modifies the collection we iterate over above)
		for (int i = 0; i<plans.size(); i++)
		{
			// remove the step from the plans executed steps list and add it to the completed steps list instead
			plans.get(i).getStepsCurrentlyExecuted().remove(steps.get(i));
			plans.get(i).getStepsAlreadyCompleted().add(steps.get(i));
			
			// update the plan progress
			updatePlanProgress(plans.get(i));
		}
	
	}

	/** process the failure of a competency*/
	private synchronized void processCompetencyFailure(Competency competency, HashMap<String,String> parameters)
	{
		// find the plans and plan steps that have (possibly) failed because this competency has failed
		ArrayList<CompetencyExecutionPlan> plans = new ArrayList<CompetencyExecutionPlan>();
		ArrayList<CompetencyExecutionPlanStep> steps = new ArrayList<CompetencyExecutionPlanStep>();
		
		for (CompetencyExecutionPlan cep: currentPlans)
			for (CompetencyExecutionPlanStep step : cep.getStepsCurrentlyExecuted())
				if (step.getCompetencyType().equals(competency.getCompetencyType()))
					// compare parameters to make sure this competency was what the plan step required
					if (parameters.equals(step.getCompetencyParameters()))
					{
						// we have found a plan and a step of this plan that has (possibly) failed through this competency
						plans.add(cep);
						steps.add(step);						
					}
		
		// now process the found matches (note: this is in a seperate loop from above, because the processing 
		// modifies the collection we iterate over above)
		for (int i = 0; i<plans.size(); i++)
		{
			// remove the step from the plans executed steps list
			plans.get(i).getStepsCurrentlyExecuted().remove(steps.get(i));

			// check if the plan is still one of our current plans (this could not be the case if i>0 and
			// one of the previous steps has already failed the plan
			if (currentPlans.contains(plans.get(i))) 
				// ok, it is still a current plan, so try if we can execute the step still (through another competency)
				executeStep(steps.get(i),plans.get(i));			
		}
		
	}
	
	/** registers request and event handlers of the competency execution system*/
	@Override
	public final void registerHandlers() {
		// register request handler for new competency execution plan requests with this
		this.getRequestHandlers().add(new HandleNewCompetencyExecutionPlan());
		// register event handlers for failed and suceeded competencies
		// since they could come from many different competencies, register with the whole simulation
		Simulation.instance.getEventHandlers().add(new HandleCompetencySucceeded());
		Simulation.instance.getEventHandlers().add(new HandleCompetencyFailed());
	}
	
	
	/** internal event handler class for handling new competency execution plan requests */
	private class HandleNewCompetencyExecutionPlan extends RequestHandler {

	    public HandleNewCompetencyExecutionPlan() {
	        super(new TypeSet(RequestNewCompetencyExecutionPlan.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) {
	        // since this is a request handler only for type RequestNewCompetencyExecutionPlan the following cast always works
	    	for (RequestNewCompetencyExecutionPlan request : requests.get(RequestNewCompetencyExecutionPlan.class))
	    	{
	    		executePlan(request.getCompetencyExecutionPlan());
	    	}	
	    }
	}
	
	/** internal event handler class for listening to competency succeeded events */
	private class HandleCompetencySucceeded extends EventHandler {

	    public HandleCompetencySucceeded() {
	        super(EventCompetencySucceeded.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type EventCompetencySucceeded the following casts always work
	    	Competency competency = ((EventCompetencySucceeded)evt).getCompetency();
	    	HashMap<String,String> parameters = ((EventCompetencySucceeded)evt).getParameters();
	    	processCompetencySuccess(competency, parameters);
	    }
	}
	
	/** internal event handler class for listening to competency failed events */
	private class HandleCompetencyFailed extends EventHandler {

	    public HandleCompetencyFailed() {
	        super(EventCompetencyFailed.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type EventCompetencyFailed the following casts always work
	    	Competency competency = ((EventCompetencyFailed)evt).getCompetency();
	    	HashMap<String,String> parameters = ((EventCompetencyFailed)evt).getParameters();
	    	processCompetencyFailure(competency, parameters);
	    }
	}



}
