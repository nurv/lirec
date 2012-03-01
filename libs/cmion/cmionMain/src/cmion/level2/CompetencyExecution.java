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

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.Simulation;
import ion.Meta.TypeSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import cmion.architecture.IArchitecture;
import cmion.architecture.CmionComponent;
import cmion.storage.CmionStorageContainer;


/** this component is responsible for executing plans of competencies it has received 
 * (normally from the competency manager) */
public class CompetencyExecution extends CmionComponent {
	
	/** store all plans that we are currently executing */
	private ArrayList<CompetencyExecutionPlan> currentPlans;
	
	/** store all plans that we are currently cancelling */
	private ArrayList<CompetencyExecutionPlan> plansToCancel;
	
	/** store all competencies that we are currently running indexing the plan they belong to */
	private HashMap<Competency, CompetencyExecutionPlan> runningCompetencies;
	
	/** create a new Competency Execution Component */
	public CompetencyExecution(IArchitecture architecture)
	{
		super(architecture);
		currentPlans = new ArrayList<CompetencyExecutionPlan>();
		plansToCancel = new ArrayList<CompetencyExecutionPlan>();
		runningCompetencies = new HashMap<Competency, CompetencyExecutionPlan>();
	}
	
	/** start the execution of a competency execution plan */
	private synchronized void executePlan(CompetencyExecutionPlan cep)
	{
		// check if the plan is instantiated and not already being executed
		if (cep.isInstantiated() && !cep.isCurrentlyExecuting())
		{
			// raise an event that the plan is being executed
			this.raise(new EventCompetencyExecutionPlanStarted(cep));
			
			// set the plan to currently executing 
			cep.startExecution();
			
			// add it to the list of our current plans
			currentPlans.add(cep);
			
			// start the first steps of the plan
			updatePlanProgress(cep);	
		}
	}

	/** cancel the execution of a currently executing competency execution plan */
	private synchronized void cancelPlan(CompetencyExecutionPlan cep)
	{
		// check if the plan is instantiated and already being executed
		if (cep.isInstantiated() && cep.isCurrentlyExecuting())
		{
			// remember this as one of the plans to cancel
			plansToCancel.add(cep);
			
			// cancel all competencies currently executing in this plan 
			for (Competency c: runningCompetencies.keySet())
			{
				// cancel this competency if it belongs to the plan we want to cancel
				if (runningCompetencies.get(c).equals(cep))
					c.cancel();
			}
		}
	}	
	
	/** checks pre conditions of provided plan and if possible either executes the next batch
	 *  of steps or if all steps are finished raises a plan success event */
	private synchronized void updatePlanProgress(CompetencyExecutionPlan cep)
	{		
		if (plansToCancel.contains(cep))
		{
			checkPlanCancelled(cep);
			return;
		}
		
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
		if (plansToCancel.contains(cep))
		{
			checkPlanCancelled(cep);
			return;
		}
		
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
				
				// also remember that this is one of the competencies currently executing
				runningCompetencies.put(competency,cep);
				
				// get parameters for executing the competency and replace black board / world model variables 
				HashMap<String,String> parameters =  replaceVariables(step.getCompetencyParameters());				
				
				// assign an execution id to the step
				step.assignExecutionID();
				
				// request the competency to start
				competency.requestStartCompetency(parameters,cep,step.getExecutionID());
				
				break;
			}

		// since we could not find any suitable competency for this step, we have to consider the whole plan as failed		
		if (!foundSuitableCompetency) planFailed(cep);
	}

	/** replace variables in comptetency execution parameters */
	private HashMap<String, String> replaceVariables(
			HashMap<String, String> parameters) 
	{
		HashMap<String, String> copiedParameters = new HashMap<String, String>();
		
		// copy all parameters 
		for (String parameterName : parameters.keySet())
		{
			// retrieve original value
			String value = parameters.get(parameterName);
			
	
			// Check if there are any blackboard variables to replace
			// this means search for $BB, variables look like this $BB(utterance) reads the value
			// of the blackboard property utterance, $BB(messages.1) reads the value of the property 1
			// in the sub container 1, etc.

			value = replaceStorageContainerVars(value,"$BB",architecture.getBlackBoard());
			
			// do exactly the same for world model variables ($WM)
			
			value = replaceStorageContainerVars(value,"$WM",architecture.getWorldModel());
						
			// add parameter and value
			copiedParameters.put(parameterName, value);			
		}

		
		return copiedParameters;
	}

	/** method for retrieving the String contents of a storage container slot 
	 * @param input the expression to evaluate 
	 * @param varIdentifier  the String used to identify the topContainer
	 * @param topContainer the top container to read the value from 
	 * */
	private String replaceStorageContainerVars(String input, String varIdentifier,
			CmionStorageContainer topContainer) 
	{
		String value = input;
		while (value.contains(varIdentifier))
		{
			int idx = value.indexOf(varIdentifier);
			String substring = "";
			for (int i=idx; value.charAt(i)!=')'; i++)
				substring += value.charAt(i);
			
			// substring should now contain the $BB/WM expression, excluding the closing bracket,
			// next we split on the '.' and '(' characters
			StringTokenizer st = new StringTokenizer(substring,".(");
			ArrayList<String> tokens = new ArrayList<String>();
			if (st.hasMoreTokens()) st.nextToken(); // discard the first token
			while (st.hasMoreTokens()) tokens.add(st.nextToken()); // store the rest in the array list
			
			// now add the closing bracket to the substring, so that when we replace it, it gets replaced completely
			substring += ")";
			
			// if there isnt at least one token this variable is malformed, remove from string
			if (tokens.size()<1) 
				value = value.replace(substring, "");
			else
			{
				// last token is the name of the property
				String propName = tokens.remove(tokens.size()-1);
				CmionStorageContainer csc = topContainer;
				while (tokens.size()>0)
				{
					String containerName = tokens.remove(tokens.size()-1);
					if (csc!=null)
						csc = csc.getSubContainer(containerName);
				}
				if (csc==null)
					value = value.replace(substring, "");
				else
				{
					Object propValue = csc.getPropertyValue(propName);
					if (propValue==null)
						value = value.replace(substring, "");
					else
						value = value.replace(substring, propValue.toString());
				}
			}
		}

		return value;
	}
	
	
	/** this method should be called when an execution plan has failed */
	private synchronized void planFailed(CompetencyExecutionPlan cep)
	{
		// check if the plan is actually still current
		if (currentPlans.contains(cep))
		{
			// mark the plan as not anymore executing
			cep.stopExecution();
		
			// remove this plan from our current plans
			currentPlans.remove(cep);
		
			// raise an event that the plan has failed
			this.raise(new EventCompetencyExecutionPlanFailed(cep));
		}
	}

	
	private synchronized void checkPlanCancelled(CompetencyExecutionPlan cep)
	{
		// check that this is a plan we want to cancel and all its steps have finished
		if (plansToCancel.contains(cep) && (cep.getStepsCurrentlyExecuted().size()==0))
		{
			// ok ready to cancel
			cep.stopExecution();
			
			// remove this plan from our current plans
			currentPlans.remove(cep);
			
			// raise an event that the plan was cancelled
			this.raise(new EventCompetencyExecutionPlanCancelled(cep));		

		}
		
		
	}
	
	/** process the success of a competency*/
	private synchronized void processCompetencySuccess(Competency competency, HashMap<String,String> parameters, long executionID)
	{
		// get the plan that this competency is part of realizing
		CompetencyExecutionPlan plan = runningCompetencies.get(competency);
		
		// if there is no plan, this competency must have been started in another way (e.g. background)
		// in that case we are not interested in it here
		if (plan == null) return;
		
		// mark the competency as not executing anymore
		runningCompetencies.remove(competency);
				
		CompetencyExecutionPlanStep s=null;
		
		// find the plan step that has completed		
		for (CompetencyExecutionPlanStep step : plan.getStepsCurrentlyExecuted())
			if (step.getCompetencyType().equals(competency.getCompetencyType()))
				// compare execution ids to find out this competency was what the plan step required
				if (executionID == step.getExecutionID())
				{
					s = step;
				}
	
		if (s!=null)
		{
			// remove the step from the plan's executed steps list			
			plan.getStepsCurrentlyExecuted().remove(s);					
			plan.getStepsAlreadyCompleted().add(s);
			
			// update the plan progress
			updatePlanProgress(plan);	
		}
	}

	/** process the failure of a competency*/
	private synchronized void processCompetencyFailure(Competency competency, HashMap<String,String> parameters, long executionID)
	{
		// get the plan that this competency is part of realizing
		CompetencyExecutionPlan plan = runningCompetencies.get(competency);
	
		// if there is no plan, this competency must have been started in another way (e.g. background)
		// in that case we are not interested in it here
		if (plan == null) return;
		
		// mark the competency as not executing anymore
		runningCompetencies.remove(competency);
		CompetencyExecutionPlanStep s=null;
		
		// find the plan step that has failed because this competency has failed		
		for (CompetencyExecutionPlanStep step : plan.getStepsCurrentlyExecuted())
			if (step.getCompetencyType().equals(competency.getCompetencyType()))
				// compare execution ids to find out this competency was what the plan step required
				if (executionID == step.getExecutionID())
				{
					s = step;
				}

		if (s!=null)
		{
			// remove the step from the plan's executed steps list			
			plan.getStepsCurrentlyExecuted().remove(s);					

			// check if the plan is still one of our current plans 
			if (currentPlans.contains(plan)) 
				// ok, it is still a current plan, so try if we can execute the step still (through another competency)
				executeStep(s,plan);	
			
		}		
	}

	/** process the cancelation of a competency*/
	private synchronized void processCompetencyCancel(Competency competency, HashMap<String,String> parameters, long executionID)
	{
		// get the plan that this competency is part of realizing
		CompetencyExecutionPlan plan = runningCompetencies.get(competency);
		
		// if there is no plan, this competency must have been started in another way (e.g. background)
		// in that case we are not interested in it here
		if (plan == null) return;
		
		// mark the competency as not executing anymore
		runningCompetencies.remove(competency);
				
		CompetencyExecutionPlanStep s=null;
		
		// find the plan step that was cancelled		
		for (CompetencyExecutionPlanStep step : plan.getStepsCurrentlyExecuted())
			if (step.getCompetencyType().equals(competency.getCompetencyType()))
				// compare execution ids to find out this competency was what the plan step required
				if (executionID == step.getExecutionID())
				{
					s = step;
				}
	
		if (s!=null)
		{
			// remove the step from the plan's executed steps list			
			plan.getStepsCurrentlyExecuted().remove(s);					
			
			// check if we can already cancel the plan or if we need to wait for other competencies 
			checkPlanCancelled(plan);	
		}

	}

	
	/** registers request and event handlers of the competency execution system*/
	@Override
	public final void registerHandlers() {
		// register request handler for new and cancelled competency execution plan requests with this
		this.getRequestHandlers().add(new HandleNewCompetencyExecutionPlan());
		this.getRequestHandlers().add(new HandleCancelCompetencyExecutionPlan());

		// register event handlers for failed, suceeded and cancelled competencies
		// since they could come from many different competencies, register with the whole simulation
		Simulation.instance.getEventHandlers().add(new HandleCompetencySucceeded());
		Simulation.instance.getEventHandlers().add(new HandleCompetencyFailed());
		Simulation.instance.getEventHandlers().add(new HandleCompetencyCancelled());
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

	/** internal event handler class for handling new competency execution plan requests */
	private class HandleCancelCompetencyExecutionPlan extends RequestHandler {

	    public HandleCancelCompetencyExecutionPlan() {
	        super(new TypeSet(RequestCancelCompetencyExecutionPlan.class));
	    }

	    @Override
	    public void invoke(IReadOnlyQueueSet<Request> requests) {
	        // since this is a request handler only for type RequestCancelCompetencyExecutionPlan the following cast always works
	    	for (RequestCancelCompetencyExecutionPlan request : requests.get(RequestCancelCompetencyExecutionPlan.class))
	    	{
	    		cancelPlan(request.getCompetencyExecutionPlan());
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
	    	long executionID = ((EventCompetencySucceeded)evt).getExecutionID();
	    	processCompetencySuccess(competency, parameters,executionID);
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
	    	long executionID = ((EventCompetencyFailed)evt).getExecutionID();
	    	processCompetencyFailure(competency, parameters, executionID);
	    }
	}

	/** internal event handler class for listening to competency cancelled events */
	private class HandleCompetencyCancelled extends EventHandler {

	    public HandleCompetencyCancelled() {
	        super(EventCompetencyCancelled.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type EventCompetencyCancelled the following casts always work
	    	Competency competency = ((EventCompetencyCancelled)evt).getCompetency();
	    	HashMap<String,String> parameters = ((EventCompetencyCancelled)evt).getParameters();
	    	long executionID = ((EventCompetencyCancelled)evt).getExecutionID();
	    	processCompetencyCancel(competency, parameters, executionID);
	    }
	}


}
