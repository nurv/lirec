/*	
        ION Framework - Synchronized Collections Unit Test Classes
	Copyright(C) 2009 GAIPS / INESC-ID Lisboa

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

	Authors:  Pedro Cuba, Guilherme Raimundo, Marco Vala, Rui Prada, Carlos Martinho 

	Revision History:
  ---
  09/04/2009      Pedro Cuba <pedro.cuba@tagus.ist.utl.pt>
  First version.
  ---
  22/05/2009      Pedro Cuba <pedro.cuba@tagus.ist.utl.pt>
  Changed Step Request scheduling method to pass through the process Events phase.
  Stepped Event -> New Step Request | Started Event -> New Step Request | Resumed Event -> New Step Request
  ---
*/
package ion.Core;

import ion.Core.Events.IResumed;
import ion.Core.Events.IStarted;
import ion.Core.Events.IStepped;
import ion.Meta.Element;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueue;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;

/**
 * An Action is an Element that represents an ongoing operation.
 * During this process it can be in three possible states: Running, Idle or Paused.
 * 
 * @author GAIPS
 * @param <TStartArguments>
 */
public class Action<TStartArguments> extends Element {

    /**
     * Represents the possible states of an Action.
     * When in the Running state, an Action raises a Step event for each Update step of the Simulation.
     * Both the Idle and Paused states indicate that an Action is not running but the Pause state usually
     * has some associated progression information.
     */
    public enum State {Running, Idle, Paused}
    
    /**
     * Direcly changes the value of the Action starting arguments without regarding the synchronization cycle.
     * Only change this value if you know what you are doing!!!
     */
    protected TStartArguments startArguments;
    
    /**
     * Direcly changes the value of the Action state without regarding the synchronization cycle.
     * Only change this value if you know what you are doing!!!
     */
    protected State state; 

    public Action() {
		this.state = State.Idle;

		this.getRequestHandlers().add(new ActionRequestHandler());
		this.getEventHandlers().add(new SteppedHandler());
        this.getEventHandlers().add(new StartedHandler());
        this.getEventHandlers().add(new ResumedHandler());
	}
    
    // <editor-fold defaultstate="collapsed" desc="Requests">
    protected final class StartRequest extends Request {}
    protected final class ResumeRequest extends  Request {}
    protected final class StopSuccessRequest extends  Request {}
    protected final class StopFailRequest extends  Request {}
    protected final class PauseRequest extends  Request {}
    protected final class StepRequest extends  Request {}
    
    protected final class StartArgumentsRequest extends Request {

        public final TStartArguments arguments;

        public StartArgumentsRequest(TStartArguments arguments) {
            this.arguments = arguments;
        }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Request Handlers">
    
    protected final class ActionRequestHandler extends RequestHandler{

        public ActionRequestHandler(){
            super(new TypeSet(StartRequest.class, StartArgumentsRequest.class,
                    StopSuccessRequest.class, StopFailRequest.class,
                    PauseRequest.class, ResumeRequest.class, StepRequest.class));
        }

        @Override
        public void invoke(IReadOnlyQueueSet<Request> requests) {
            try {
                handleRequests(requests);
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
    }
    
    /**
     * Calls the appropriate handler according to the state of the action.
     * Has the following policy:
     *  Idle State Request Priorities:
     *      Start > Other requests
     *  Running State Request Priorities:
     *      Stop Fail > Stop Sucess > Pause > Step > Other Requests
     *  Paused State Request Priorities:
     *      Stop Fail > Stop Success > Resume > Other Requests
     * 
     * @param requests the set of request queues to be processed
     */
    protected void handleRequests(IReadOnlyQueueSet<Request> requests) throws Exception {
        switch (this.state) {
            case Idle:
                executeIdleState(requests.get(Action.StartRequest.class), requests.get(Action.StartArgumentsRequest.class));
                break;
            case Running:
                executeRunningState(requests.get(Action.StopSuccessRequest.class), requests.get(Action.StopFailRequest.class), requests.get(Action.PauseRequest.class), requests.get(Action.StepRequest.class));
                break;
            case Paused:
                executePausedState(requests.get(Action.StopSuccessRequest.class), requests.get(Action.StopFailRequest.class), requests.get(Action.ResumeRequest.class));
                break;
            default:
                throw new Exception("Unknown State Type: " + this.state);
        }
    }
    
    private void executeIdleState(IReadOnlyQueue<Action.StartRequest> startRequests, IReadOnlyQueue<Action.StartArgumentsRequest> startArgumentsRequests) {
        for (Action<TStartArguments>.StartArgumentsRequest request : startArgumentsRequests) {
            this.executeStartArguments(request.arguments); //Choose the first start
            return;
        }

        if (startRequests.count() > 0) {
            this.startArguments = null;
            this.executeStart();
            return;
        }
    }
    
    void executeRunningState(IReadOnlyQueue<Action.StopSuccessRequest> stopSucceed,
            IReadOnlyQueue<Action.StopFailRequest> stopFail, IReadOnlyQueue<Action.PauseRequest> pause,
            IReadOnlyQueue<Action.StepRequest> stepRequests) {
        
        if (stopFail.count() > 0) {
            this.executeStopFail();
        } else if (stopSucceed.count() > 0) {
            this.executeStopSuccess();
        } else if (pause.count() > 0) {
            this.executePause();
        } else if (stepRequests.count() > 0) {
            this.executeStep();
        }
    }
    
    void executePausedState(IReadOnlyQueue<Action.StopSuccessRequest> stopSucceed,
            IReadOnlyQueue<Action.StopFailRequest> stopFail,
            IReadOnlyQueue<Action.ResumeRequest> resume) {
        
        if (stopFail.count() > 0) {
            this.executeStopFail();
        } else if (stopSucceed.count() > 0) {
            this.executeStopSuccess();
        } else if (resume.count() > 0) {
            this.executeResume();
        }
    }
    
    void executeStart() {
        this.raise(new Started<Action>(this, this.state));
        this.state = State.Running;
    }
    
    private void executeStopSuccess() {
        this.raise(new Succeeded<Action>(this, this.state));
        this.state = State.Idle;
    }
    
    private void executeStopFail() {
        this.raise(new Failed<Action>(this, this.state));
        this.state = State.Idle;
    }
    
    private void executePause() {
        this.raise(new Paused<Action>(this, this.state));
        this.state = State.Paused;
    }
    
    private void executeResume() {
        this.raise(new Resumed<Action>(this, this.state));
        this.state = State.Running;
    }

    private void executeStep() {
        this.raise(new Stepped<Action>(this));
    }
    
    private void executeStartArguments(TStartArguments arguments) {
        this.raise(new Started<Action>(this, this.state));
        this.startArguments = arguments;
        this.state = State.Running;
        this.schedule(new StepRequest());
    }
    
    //</editor-fold>
    
    protected void onStep(IStepped evt) {
		if (evt.getAction() == this) {
			this.schedule(new StepRequest());
		}
	}

	protected void onStart(IStarted evt) {
		if (evt.getAction() == this) {
			this.schedule(new StepRequest());
		}
	}

	protected void onResume(IResumed evt) {
		if (evt.getAction() == this) {
			this.schedule(new StepRequest());
		}
	}
	
	private class SteppedHandler extends EventHandler {
		
		public SteppedHandler() {
			super(IStepped.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			onStep((IStepped)evt);
		}
	}
	
	private class StartedHandler extends EventHandler {
		
		public StartedHandler() {
			super(IStarted.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			onStart((IStarted)evt);
		}
	}
	
	private class ResumedHandler extends EventHandler {
		
		public ResumedHandler() {
			super(IResumed.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			onResume((IResumed)evt);
		}
	}
    
    // <editor-fold defaultstate="collapsed" desc="Action Members">
    
    /**
	 * Starts the Action.
	 */
    public void start() {
        this.schedule(new StartRequest());
    }
    
    /**
     * Stops the Action.
     * 
     * @param success Indicates whether the Action stops successfully or not.
     */
    public void stop(boolean success) {
        if (success) {
            this.schedule(new StopSuccessRequest());
        } else {
            this.schedule(new StopFailRequest());
        }
    }

    /**
     * Pauses the Action.
     */
    public void pause() {
        this.schedule(new PauseRequest());
    }

    /**
     * Resumes the Action.
     */
    public void resume() {
        this.schedule(new ResumeRequest());
    }

    /**
     * Starts the Action with a specific starting arguments.
     * 
     * @param arguments the starting arguments
     */
    public void start(TStartArguments arguments) {
        this.schedule(new StartArgumentsRequest(arguments));
    }

    /**
     * Gets the Action current State.
     * 
     * @return the current State of the Action.
     */
    public State getCurrentState() {
        return this.state;
    }
    
    /**
     * Gets the Argument with which the action was started.
     * 
     * @return the starting arguments of the Action
     */
    public TStartArguments getStartArguments() {
        return startArguments;
    }
    
    //</editor-fold>
    
    @Override
    public void onDestroy() {
    }
}
