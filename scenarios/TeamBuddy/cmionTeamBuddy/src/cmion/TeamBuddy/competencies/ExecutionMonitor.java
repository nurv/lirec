package cmion.TeamBuddy.competencies;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.util.HashMap;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.CompetencyCancelledException;
import cmion.level2.CompetencyEvent;
import cmion.level2.CompetencyExecutionPlanEvent;
import cmion.level2.EventCompetencyCancelled;
import cmion.level2.EventCompetencyExecutionPlanCancelled;
import cmion.level2.EventCompetencyExecutionPlanFailed;
import cmion.level2.EventCompetencyExecutionPlanStarted;
import cmion.level2.EventCompetencyExecutionPlanSucceeded;
import cmion.level2.EventCompetencyFailed;
import cmion.level2.EventCompetencyStarted;
import cmion.level2.EventCompetencySucceeded;

public class ExecutionMonitor extends Competency {

	public ExecutionMonitor(IArchitecture architecture) {
		super(architecture);
		this.competencyName = "ExecutionMonitor";
		this.competencyType = "ExecutionMonitor";
	}
	
	@Override
	public void initialize() {
		this.available = true;
	}	
	
	@Override
	public boolean runsInBackground() 
	{	
		return true;
	}
	
	/** we don't do anything in here, all processing is done via ion event callbacks 
	 *  on the simulation thread */
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters)
			throws CompetencyCancelledException {
		while (true)
		{
			try
			{
				Thread.sleep(10000);
			}
			catch (InterruptedException e)
			{}
		}
	}

	/** register the additional event handlers so ion knows to notify us when competency
	 *  related events occur */
	@Override
	public void registerHandlers()
	{
		super.registerHandlers();

		// listen to events related to comeptency
		HandleCompetencyEvent competencyEventHandler = new HandleCompetencyEvent();
		Simulation.instance.getEventHandlers().add(competencyEventHandler);

		// listen to events related to whole plans
		HandleCompetencyExecutionPlanEvent competencyPlanEventHandler = new HandleCompetencyExecutionPlanEvent();
		Simulation.instance.getEventHandlers().add(competencyPlanEventHandler);		
	}	
	
	
	/** internal event handler class for listening to competency related events */
	private class HandleCompetencyEvent extends EventHandler 
	{

	    public HandleCompetencyEvent() {
	        super(CompetencyEvent.class);
	    }

		@Override
	    public void invoke(IEvent evt) 
	    {
	    	if (evt instanceof EventCompetencyStarted)
	    	{
	    		EventCompetencyStarted csEvt = (EventCompetencyStarted) evt;
	    		String competencyName = csEvt.getCompetency().getCompetencyName();
	    		boolean runsInBackground = csEvt.getCompetency().runsInBackground();
	    		if (!runsInBackground)
	    		{		
	    			String actionName = csEvt.getPlan().getMindAction().getName();	  		
	    			System.out.println("competency started " + competencyName + ", action name: "+actionName);
	    			//a little example below on how to access the parameters of an action
	    			//for (String parameter : csEvt.getPlan().getMindAction().getParameters()) 
	    			//{
	    			//	System.out.println(parameter);
	    			//}
	    		}
	    	}
	    	else if (evt instanceof EventCompetencySucceeded)
	    	{
	    		EventCompetencySucceeded csEvt = (EventCompetencySucceeded) evt;
	    		String competencyName = csEvt.getCompetency().getCompetencyName();
	    		boolean runsInBackground = csEvt.getCompetency().runsInBackground();
	    		if (!runsInBackground)
	    		{		
	    			String actionName = csEvt.getPlan().getMindAction().getName();
	    			System.out.println("competency succeeded " + competencyName + ", action name: "+actionName);
	    		}
	    	}
	    	else if (evt instanceof EventCompetencyFailed)
	    	{
	    		EventCompetencyFailed cfEvt = (EventCompetencyFailed) evt;
	    		String competencyName = cfEvt.getCompetency().getCompetencyName();
	    		boolean runsInBackground = cfEvt.getCompetency().runsInBackground();
	    		if (!runsInBackground)
	    		{		
	    			String actionName = cfEvt.getPlan().getMindAction().getName();
	    			System.out.println("competency failed " + competencyName + ", action name: "+actionName);
	    		}
	    	}
	    	else if (evt instanceof EventCompetencyCancelled)
	    	{
	    		EventCompetencyCancelled ccEvt = (EventCompetencyCancelled) evt;
	    		String competencyName = ccEvt.getCompetency().getCompetencyName();
	    		boolean runsInBackground = ccEvt.getCompetency().runsInBackground();
	    		if (!runsInBackground)
	    		{		
	    			String actionName = ccEvt.getPlan().getMindAction().getName();
	    			System.out.println("competency cancelled " + competencyName + ", action name: "+actionName);
	    		}
	    	}

	    }
	}

	/** internal event handler class for listening to competency execution plan related events */
	private class HandleCompetencyExecutionPlanEvent extends EventHandler 
	{

	    public HandleCompetencyExecutionPlanEvent() {
	        super(CompetencyExecutionPlanEvent.class);
	    }

		@Override
	    public void invoke(IEvent evt) 
	    {
	    	if (evt instanceof EventCompetencyExecutionPlanStarted)
	    	{
	    		EventCompetencyExecutionPlanStarted cepsEvt = (EventCompetencyExecutionPlanStarted) evt;
    			String actionName = cepsEvt.getCompetencyExecutionPlan().getMindAction().getName();
    			System.out.println("competency plan started, action: " + actionName);
	    	}
	    	if (evt instanceof EventCompetencyExecutionPlanSucceeded)
	    	{
	    		EventCompetencyExecutionPlanSucceeded cepsEvt = (EventCompetencyExecutionPlanSucceeded) evt;
    			String actionName = cepsEvt.getCompetencyExecutionPlan().getMindAction().getName();
    			System.out.println("competency plan succeded, action: " + actionName);
	    	}
	    	if (evt instanceof EventCompetencyExecutionPlanFailed)
	    	{
	    		EventCompetencyExecutionPlanFailed cepfEvt = (EventCompetencyExecutionPlanFailed) evt;
    			String actionName = cepfEvt.getCompetencyExecutionPlan().getMindAction().getName();
    			System.out.println("competency plan failed, action: " + actionName);
	    	}
	    	if (evt instanceof EventCompetencyExecutionPlanCancelled)
	    	{
	    		EventCompetencyExecutionPlanCancelled cepcEvt = (EventCompetencyExecutionPlanCancelled) evt;
    			String actionName = cepcEvt.getCompetencyExecutionPlan().getMindAction().getName();
    			System.out.println("competency plan cancelled, action: " + actionName);
	    	}
	    }
	}
	
	
}
