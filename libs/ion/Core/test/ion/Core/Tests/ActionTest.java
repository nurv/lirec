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
  05/05/2010      Pedro Cuba <pedro.cuba@tagus.ist.utl.pt>
  Fixed some problems with ambiguous use of assert methods.
  ---  
*/
package ion.Core.Tests;

import ion.Core.Action;
import ion.Core.Events.IFailed;
import ion.Core.Events.IPaused;
import ion.Core.Events.IResumed;
import ion.Core.Events.IStarted;
import ion.Core.Events.IStateChanged;
import ion.Core.Events.IStepped;
import ion.Core.Events.ISucceeded;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author GAIPS
 */
public class ActionTest {

    public ActionTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    //<editor-fold defaultstate="collapsed" desc="Idle State Tests">
    
    @Test
    public void idleStartTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    @Test
    public void idleStopTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.stop(true);
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        action.stop(false);
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }

    @Test
    public void idlePauseTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.pause();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void idleResumeTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void idleStartArgumentsTest() {
        Simulation simulation = Simulation.instance;
        Action<Integer> action = new Action<Integer>();
        final Integer expected = 3 + 1;

        simulation.getElements().add(action);
        simulation.update();

        action.start(expected);
        assertNull(action.getStartArguments());
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getStartArguments(), expected);
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getStartArguments(), expected);
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    @Test
    public void idleStartTestWithGenericArgument() {
        Simulation simulation = Simulation.instance;
        Action<Integer> action = new Action<Integer>();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
        assertNull(action.getStartArguments());

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
        assertNull(action.getStartArguments());
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Running State Tests">
    
    @Test
    public void runningStartTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);

    }
    
    @Test
    public void runningStopTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        action.stop(true);
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        /// Test with Stop(false)
        Action action2 = new Action();

        simulation.getElements().add(action2);
        simulation.update();

        action2.start();
        simulation.update();

        action2.stop(false);
        assertEquals(action2.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action2.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void runningPauseTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        action.pause();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Paused);
    }
    
    @Test
    public void runningResumeTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Paused State Tests">
    
    @Test
    public void pausedStartTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();
        action.pause();
        simulation.update();

        action.start();
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Paused);
    }
    
    @Test
    public void pausedStopTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();
        action.pause();
        simulation.update();

        action.stop(true);
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        /// Test with Stop(false)
        Action action2 = new Action();

        simulation.getElements().add(action2);
        simulation.update();

        action2.start();
        simulation.update();
        action2.pause();
        simulation.update();

        action2.stop(false);
        assertEquals(action2.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action2.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void pausedPauseTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();
        action.pause();
        simulation.update();

        action.pause();
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Paused);
    }
    
    @Test
    public void pausedResumeTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();
        action.pause();
        simulation.update();

        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Constructor Tests">
    
    @Test
    public void actionConstructorTest() {
        Action action = new Action();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Multiple Requests Policy Test">
    
    //<editor-fold defaultstate="collapsed" desc="Idle State Multiple Tests">
    
    @Test
    public void idleMultipleStartsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Idle);
        action.start();
        action.start();

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);

    }
    
    @Test
    public void idleStartStopPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Idle);
        action.start();
        action.start();
        action.stop(true);
        action.stop(true);
        action.stop(false);
        action.stop(false);
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    @Test
    public void idleStopPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Idle);
        action.stop(true);
        action.stop(true);
        action.stop(false);
        action.stop(false);
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void idleMultipleResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Idle);
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void idleMultipleStartArgumentsTest() {
        Simulation simulation = Simulation.instance;
        Action<Integer> action = new Action<Integer>();
        final Integer expectedStartArguments = 2 + 5;
        simulation.getElements().add(action);
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Idle);
        action.start(expectedStartArguments);
        action.start(expectedStartArguments + 3);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
        assertEquals(action.getStartArguments(), expectedStartArguments);
    }
    
    @Test
    public void idleStartArgumentsStopPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action<Integer> action = new Action<Integer>();
        final Integer expectedStartArguments = 3 + 5;
        simulation.getElements().add(action);
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Idle);
        action.start(expectedStartArguments);
        action.start(expectedStartArguments + 3);
        action.start();
        action.start();
        action.stop(true);
        action.stop(true);
        action.stop(false);
        action.stop(false);
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Idle);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
        assertEquals(action.getStartArguments(), expectedStartArguments);
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Running State Multiple Tests">
    
    @Test
    public void runningStartStopPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Running);
        action.start();
        action.start();
        action.stop(true);
        action.stop(true);
        action.stop(false);
        action.stop(false);
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void runningStartStopSucceedPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Running);
        action.start();
        action.start();
        action.stop(true);
        action.stop(true);
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void runningStartPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Running);
        action.start();
        action.start();
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Paused);
    }
    
    @Test
    public void runningStartResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Running);
        action.start();
        action.start();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    @Test
    public void runningResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Running);
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    @Test
    public void runningStartRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Running);
        action.start();
        action.start();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Pause State Multiple">
    
    @Test
    public void pauseStartStopPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        action.pause();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Paused);
        action.start();
        action.start();
        action.stop(true);
        action.stop(true);
        action.stop(false);
        action.stop(false);
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void pauseStartStopSucceededPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        action.pause();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Paused);
        action.start();
        action.start();
        action.stop(true);
        action.stop(true);
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Idle);
    }
    
    @Test
    public void pauseStartPauseResumeRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        action.pause();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Paused);
        action.start();
        action.start();
        action.pause();
        action.pause();
        action.resume();
        action.resume();
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Running);
    }
    
    @Test
    public void pauseStartPauseRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        action.pause();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Paused);
        action.start();
        action.start();
        action.pause();
        action.pause();
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Paused);
    }
    
    @Test
    public void pauseStartRequestsTest() {
        Simulation simulation = Simulation.instance;
        Action action = new Action();
        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();

        action.pause();
        simulation.update();

        assertEquals(action.getCurrentState(), Action.State.Paused);
        action.start();
        action.start();
        assertEquals(action.getCurrentState(), Action.State.Paused);

        simulation.update();
        assertEquals(action.getCurrentState(), Action.State.Paused);
    }
    
    //</editor-fold>
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Event Tests">
    
    @Test
    public void startedEventTest() {
        Simulation simulation = Simulation.instance;
        MyAction action = new MyAction();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        assertFalse(action.startedEventRaised);

        simulation.update();
        assertTrue(action.startedEventRaised);
    }
    
    @Test
    public void steppedEventTest() {
        Simulation simulation = Simulation.instance;
        MyAction action = new MyAction();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        assertFalse(action.steppedEventRaised);

        simulation.update();
        assertFalse(action.steppedEventRaised); //Stepped isn't raised in the first start cycle

        simulation.update();
        assertTrue(action.steppedEventRaised);

        action.setRaisedEventChecksToFalse();
        assertFalse(action.steppedEventRaised);

        simulation.update();
        assertTrue(action.steppedEventRaised);

        action.setRaisedEventChecksToFalse();
        action.stop(true);

        simulation.update();
        assertEquals(Action.State.Idle, action.getCurrentState());
        assertFalse(action.steppedEventRaised); //Stepped isn't raised on the stop cycle
    }
    
    @Test
    public void stopSuccessEventTest() {
        Simulation simulation = Simulation.instance;
        MyAction action = new MyAction();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();
        assertEquals(Action.State.Running, action.getCurrentState());

        action.stop(true);
        assertFalse(action.stopedSucessEventRaised);

        simulation.update();
        assertTrue(action.stopedSucessEventRaised);
    }
    
    @Test
    public void stopFailEventTest() {
        Simulation simulation = Simulation.instance;
        MyAction action = new MyAction();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();
        assertEquals(Action.State.Running, action.getCurrentState());

        action.stop(false);
        assertFalse(action.stopedFailEventRaised);

        simulation.update();
        assertTrue(action.stopedFailEventRaised);
    }
    
    @Test
    public void pausedEventTest() {
        Simulation simulation = Simulation.instance;
        MyAction action = new MyAction();

        simulation.getElements().add(action);
        simulation.update();

        action.start();
        simulation.update();
        assertEquals(Action.State.Running, action.getCurrentState());

        action.pause();
        assertFalse(action.pausedEventRaised);

        simulation.update();
        assertTrue(action.pausedEventRaised);
    }
    
    @Test
    public void resumeEventTest() {
        Simulation simulation = Simulation.instance;
        MyAction action = new MyAction();
        simulation.getElements().add(action);

        simulation.update();
        action.start();

        simulation.update();
        assertEquals(Action.State.Running, action.getCurrentState());
        action.pause();

        simulation.update();
        assertEquals(Action.State.Paused, action.getCurrentState());
        action.resume();
        assertFalse(action.resumeEventRaised);

        simulation.update();
        assertTrue(action.resumeEventRaised);
    }
    
    @Test
    public void stateChangedEventTest() {
        Simulation simulation = Simulation.instance;
        MyAction action = new MyAction();
        simulation.getElements().add(action);

        simulation.update();
        action.start();
        assertEquals(Action.State.Idle, action.getCurrentState());

        simulation.update();
        assertEquals(Action.State.Running, action.getCurrentState());
        assertTrue(action.stateChangedEventRaised);
        action.setRaisedEventChecksToFalse();
        action.pause();

        simulation.update();
        assertEquals(Action.State.Paused, action.getCurrentState());
        assertTrue(action.stateChangedEventRaised);
        action.setRaisedEventChecksToFalse();
        action.resume();

        simulation.update();
        assertEquals(Action.State.Running, action.getCurrentState());
        assertTrue(action.stateChangedEventRaised);
        action.setRaisedEventChecksToFalse();
        action.stop(true);

        simulation.update();
        assertEquals(Action.State.Idle, action.getCurrentState());
        assertTrue(action.stateChangedEventRaised);
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Auxiliary Classes">
    
    private class MyAction extends Action {

        public boolean startedEventRaised;
        public boolean stopedSucessEventRaised;
        public boolean stopedFailEventRaised;
        public boolean pausedEventRaised;
        public boolean resumeEventRaised;
        public boolean steppedEventRaised;
        public boolean stateChangedEventRaised;
        
        public MyAction() {
            this.getEventHandlers().add(new OnStarted());
            this.getEventHandlers().add(new OnStoppedSucess());
            this.getEventHandlers().add(new OnStoppedFail());
            this.getEventHandlers().add(new OnPaused());
            this.getEventHandlers().add(new OnResumed());
            this.getEventHandlers().add(new OnStepped());
            this.getEventHandlers().add(new OnStateChanged());
        }

        private class OnStarted extends EventHandler {

            public OnStarted() {
                super(IStarted.class);
            }

            @Override
            public void invoke(IEvent evt) {
                startedEventRaised = true;
            }
        }

        private class OnStoppedSucess extends EventHandler {

            public OnStoppedSucess() {
                super(ISucceeded.class);
            }

            @Override
            public void invoke(IEvent evt) {
                stopedSucessEventRaised = true;
            }
        }

        private class OnStoppedFail extends EventHandler {

            public OnStoppedFail() {
                super(IFailed.class);
            }

            @Override
            public void invoke(IEvent evt) {
                stopedFailEventRaised = true;
            }
        }

        private class OnPaused extends EventHandler {

            public OnPaused() {
                super(IPaused.class);
            }

            @Override
            public void invoke(IEvent evt) {
                pausedEventRaised = true;
            }
        }

        private class OnResumed extends EventHandler {

            public OnResumed() {
                super(IResumed.class);
            }

            @Override
            public void invoke(IEvent evt) {
                resumeEventRaised = true;
            }
        }

        private class OnStepped extends EventHandler {

            public OnStepped() {
                super(IStepped.class);
            }

            @Override
            public void invoke(IEvent evt) {
                steppedEventRaised = true;
            }
        }

        private class OnStateChanged extends EventHandler {

            public OnStateChanged() {
                super(IStateChanged.class);
            }

            @Override
            public void invoke(IEvent evt) {
                stateChangedEventRaised = true;
            }
        }

        public void setRaisedEventChecksToFalse() {
            startedEventRaised = stopedSucessEventRaised = stopedFailEventRaised = pausedEventRaised = resumeEventRaised = steppedEventRaised =
                    stateChangedEventRaised = false;
        }
    }
    
    //</editor-fold>
}