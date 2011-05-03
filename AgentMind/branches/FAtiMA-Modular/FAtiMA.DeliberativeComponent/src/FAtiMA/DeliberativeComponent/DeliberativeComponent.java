/** 
 * DeliberativeProcess.java - Implements the Agent's Architecture deliberative processes
 * (deliberative appraisal + problem-focused coping/emotion-focused coping).
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 21/12/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 21/12/2004 - File created
 * João Dias: 10/07/2006 - the class is now serializable 
 * João Dias: 12/07/2006 - Replaced the deprecated Ground methods for the new ones.
 * João Dias: 15/07/2006 - Removed the KnowledgeBase from the Class's fields since the KB is now
 * 						   a singleton that can be used anywhere without previous references.
 * João Dias: 15/07/2006 - Removed the EmotionalState from the Class's fields since the 
 * 						   EmotionalState is now a singleton that can be used anywhere 
 * 					       without previous references.
 * João Dias: 17/07/2006 - The following methods were moved from Agent class to this class 
 * 							  - AddGoal
 * 							  - ChangeGoalImportance
 * 							  - RemoveGoal
 * 							  - RemoveAllGoals
 * 							  - GetGoals
 * 							  - getGoalLibrary
 * 							  - getEmotionalPlanner
 * João Dias: 17/07/2006 - Added the field GoalLibrary and changed the class constructor
 * 						   accordingly
 * João Dias: 20/07/2006 - small change in add goal method, now we have to create
 * 						   a protected condition from the conditions stored in an 
 * 						   InterestGoal
 * João Dias: 21/07/2006 - removed the list of goals received in the class constructor
 * João Dias: 24/07/2006 - removed the field _selectedActionValue and added a new one
 * 						   _selectedActionEmotion. This means that instead of storing the value
 * 						   associated with a selected action, we store the emotion associated with
 * 						   the action.
 * João Dias: 08/09/2006 - removed some unreachable code from method Appraisal
 * João Dias: 18/09/2006 - small changes due to changes in Question SpeechActs
 * João Dias: 20/09/2006 - Added the new functionality of InferenceOperators
 * 						 - Added new funcionality of WaitFor actions
 *  					 - Removed the method RemoveSelectedAction. The method
 * 						   GetSelectedMethod now additionally has the functionality 
 * 						   of the RemoveSelectedAction method
 * 						 - I was forgeting to update an effect's probability when
 * 						   an expirableActionMonitor expired. In this situation,  
 * 						   all the step effects probability must be updated. I solved this
 * 						   problem.
 * 						 - Added ForceUpdate method. Read description.
 * João Dias: 21/09/2006 - Small change in the way that I was updating a Step's probability
 * 						   of execution and the probability of its effects.
 * 						 - Now the deliberative proccess can deal with actions of other agents
 * 						   in the planning proccess. If such action (of another agent) is selected
 * 						   for execution, the agent waits (through an ExpirableActionMonitor), hoping
 * 						   that the corresponding agent will decide to execute the action. If the action
 * 						   is indeed executed, the agent will increase the step's probability. If after
 * 						   some time, the action is still not executed, the agent gives up waiting and
 * 						   lowers its probability of execution.
 * João Dias: 29/09/2006 - There was a problem when the agent tried to execute a plan's action where the
 * 						   agent is not specified. In these situations, if the agent is unspecified it 
 * 						   means that any agent can do the action, and so the agent decides to do it himself
 * 						   since he wants to achieve the goal. The problem was that we were forgetting to apply
 * 						   the substitution of the agent's name to the step, and so the step's effects were
 * 						   wrong. Now it works properly.
 * 					     - When the agent would choose for execution an action of other agent, we would create
 * 						   an ExpirableActionMonitor to wait for such action. However, in addition to expiring
 * 					       after a predetermined time, the action monitor would be stopped by any action done by
 * 						   any character. It was implemented in this way to make sure that the agent would be 
 * 						   able to do something else if the environment would change. However, this has the problem
 * 						   of always lowering the step's probability even if the agent that is supposed to perform the action
 * 						   does not act. Now, we only stop the monitor if it expires or if the agent that we expect to
 * 						   act does something. In that case, if it was what we were expecting, the step's probability is 
 * 						   increased. If not, it is lowered. 
 * João Dias: 02/10/2006 - Added attribute selectedPlan, this attribute contains the plan last developed or selected
 * 					       for execution by the deliberative layer
 * João Dias: 03/10/2006 - the deliberative layer no longer needs to add the effects of internal operators (when they are 
 * 						   selected for execution) to the KnowledgeBase. It's up to the KB to detect when it should use
 * 						   the operators, and add the new knowledge resulting from the inference
 * João Dias: 04/10/2006 - Before trying to execute an action that does not have agency defined, the agent has to check if he
 * 						   can do it, by adding the self substitution and verifying if the resulting plan is valid. Only in
 * 						   this situation he can try to execute the action.
 * João Dias: 20/10/2006 - The probability of a step's effects was being wrongly updated in some situations where the action
 * 						   corresponded to an action by other agent. This bug is solved.
 * João Dias: 06/12/2006 - If a complete plan does not have a valid next action to execute (ex: the next
 *						   action to execute contains unboundvariables), it means that the plan cannot be executed, 
 *						   and therefore the plan is removed from the intention structure
 * João Dias: 07/12/2006 - The RemoveAllGoals method additionaly removes current goals, intentions and plans from the
 * 						   planner
 * João Dias: 07/12/2006 - the methods that used or accessed the agent's goals are now synchronized. This was necessary
 * 						   because in some particular situations simultaneous accesses to the goal list structure was
 * 						   being carried out, causing the agents to crash.
 * João Dias: 22/12/2006 - Appraisal Method: when monitoring an action of another agent, we must test the received event
 * 						   by trying to unify it with the expected action, instead of just comparing the names. This is
 * 						   because now an action of another agent may contain unbound variables
 * João Dias: 22/02/2006 - Refactoring: the inference procedure was removed from the Appraisal Method. The inference is now
 * 						   performed at the agent level. Additionaly, the appraisal method now allways checks goal activation.
 * 						   From now on, its the agent's responsability to decide whether to call the appraisal method or not.
 * 						 - Because of the above refactorization, the method ForceUpdate was now unnecessary and was removed. 	  
 * 			
 */

package FAtiMA.DeliberativeComponent;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAdvancedPerceptionsComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IBehaviourComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.componentTypes.IProcessExternalRequestComponent;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.goals.InterestGoal;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.IPlanningOperator;
import FAtiMA.Core.plans.Plan;
import FAtiMA.Core.plans.ProtectedCondition;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;
import FAtiMA.DeliberativeComponent.display.GoalsPanel;
import FAtiMA.DeliberativeComponent.parsers.DeliberativeLoaderHandler;
import FAtiMA.DeliberativeComponent.strategies.DefaultStrategy;
import FAtiMA.DeliberativeComponent.strategies.IActionFailureStrategy;
import FAtiMA.DeliberativeComponent.strategies.IActionSuccessStrategy;
import FAtiMA.DeliberativeComponent.strategies.IExpectedUtilityStrategy;
import FAtiMA.DeliberativeComponent.strategies.IGetUtilityForOthers;
import FAtiMA.DeliberativeComponent.strategies.IGoalFailureStrategy;
import FAtiMA.DeliberativeComponent.strategies.IGoalSuccessStrategy;

/**
 * Implements the Agent's Architecture deliberative processes (deliberative
 * appraisal + problem-focused coping/emotion-focused coping).
 * 
 * @author João Dias
 */
public class DeliberativeComponent implements Serializable, IComponent,
		IBehaviourComponent, IModelOfOtherComponent,
		IAppraisalDerivationComponent, IAdvancedPerceptionsComponent,
		IProcessExternalRequestComponent, IOptionsStrategy,
		IExpectedUtilityStrategy {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final long waitingTime = 2000;
	private static final float MINIMUMUTILITY = 0.5f;
	private static final float SELECTIONTHRESHOLD = 1.2f;
	public static final String NAME = "Deliberative";

	private static final String CHANGE_IMPORTANCE_SUCCESS = "CIS";
	private static final String CHANGE_IMPORTANCE_FAILURE = "CIF";
	private static final String ADD_GOALS = "ADDGOALS";
	private static final String REMOVE_GOAL = "REMOVEGOAL";
	private static final String REMOVE_ALL_GOALS = "REMOVEALLGOALS";

	private ArrayList<Goal> _goals;

	private EmotionalPlanner _planner;
	private ActionMonitor _actionMonitor;
	private Plan _selectedPlan;
	private Step _selectedAction;
	private ActiveEmotion _selectedActionEmotion;
	private ArrayList<ActivePursuitGoal> _options;
	private HashMap<String, Intention> _intentions;
	private ArrayList<ProtectedCondition> _protectionConstraints;
	private Intention _currentIntention;

	// strategies
	private IExpectedUtilityStrategy _EUStrategy;
	private IUtilityStrategy _UStrategy;
	private IGetUtilityForOthers _UOthersStrategy;
	private IProbabilityStrategy _PStrategy;
	private ArrayList<IOptionsStrategy> _optionStrategies;
	private ArrayList<IGoalSuccessStrategy> _goalSuccessStrategies;
	private ArrayList<IGoalFailureStrategy> _goalFailureStrategies;
	private ArrayList<IActionSuccessStrategy> _actionSuccessStrategies;
	private ArrayList<IActionFailureStrategy> _actionFailureStrategies;

	/**
	 * Creates a new DeliberativeProcess
	 * 
	 * @param goalLibrary
	 *            - the GoalLibrary with all domain's goals
	 * @param planner
	 *            - the EmotionalPlanner that will be used by the deliberative
	 *            layer
	 */
	public DeliberativeComponent() {
		_goals = new ArrayList<Goal>();

		_planner = new EmotionalPlanner();
		_actionMonitor = null;
		_selectedAction = null;
		_selectedPlan = null;
		_options = new ArrayList<ActivePursuitGoal>();

		_intentions = new HashMap<String, Intention>();
		_protectionConstraints = new ArrayList<ProtectedCondition>();
		_currentIntention = null;
		_EUStrategy = this;
		_UStrategy = new DefaultStrategy();
		_UOthersStrategy = new DefaultStrategy();
		_PStrategy = new DefaultStrategy();
		_optionStrategies = new ArrayList<IOptionsStrategy>();
		_optionStrategies.add(this);

		_goalFailureStrategies = new ArrayList<IGoalFailureStrategy>();
		_goalSuccessStrategies = new ArrayList<IGoalSuccessStrategy>();
		_actionFailureStrategies = new ArrayList<IActionFailureStrategy>();
		_actionSuccessStrategies = new ArrayList<IActionSuccessStrategy>();
	}

	private void AddGoalsRequest(AgentModel am, String perc) {
		String goalDescription;
		String goalName;
		float importance;
		float importance2;
		StringTokenizer st2;
		StringTokenizer st = new StringTokenizer(perc, " ");

		while (st.hasMoreTokens()) {
			goalDescription = st.nextToken();
			st2 = new StringTokenizer(goalDescription, "|");
			goalName = st2.nextToken();
			importance = new Float(st2.nextToken()).floatValue();
			importance2 = new Float(st2.nextToken()).floatValue();
			try {
				addGoal(am, goalName, importance, importance2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void cancelAction(AgentModel am) {
		/*if (_actionMonitor != null) {
			if (_actionMonitor.getStep().getAgent().toString()
					.equals(Constants.SELF)) {
				am.getRemoteAgent().cancelAction();
			}
			_actionMonitor = null;
		}

		_selectedAction = null;
		*/
	}

	/**
	 * Gets the action selected in the coping cycle, if any.
	 * 
	 * @return the action selected for execution, or null if no such action
	 *         exists
	 */
	private ValuedAction getSelectedAction() {

		Event e;

		if (_selectedAction == null) {
			return null;
		}

		/*
		 * Prepares the action selected for execution, by adding a monitor to it
		 * (that detects when the action finishes). Finally it removes the
		 * action from the selected state so that it is not selected again.
		 */

		if (!_selectedAction.getAgent().isGrounded()) {
			// in this situation the agent that is going to perform the action
			// is not defined. Since the agent needs this action to be performed
			// it will check if he can do it himself by applying the [SELF]
			// substitution to the plan and testing if the resulting plan is
			// valid

			Substitution sub = new Substitution(_selectedAction.getAgent(),
					new Symbol(Constants.SELF));

			Plan clonedPlan = (Plan) _selectedPlan.clone();
			clonedPlan.AddBindingConstraint(sub);
			if (clonedPlan.isValid()) {
				// this means that the agent can indeed perform the action

				// TODO I should clone the step before grounding it,
				// however I can only do it if the UpdatePlan method in class
				// Plan starts to work properly with unbound preconditions
				// _selectedAction = (Step) _selectedAction.clone();
				// I know that this sucks, but for the moment I have to do it
				// like this
				_selectedPlan.AddBindingConstraint(sub);
				_selectedPlan.CheckProtectedConstraints();
				_selectedPlan.CheckCausalConflicts();
			} else {
				// the agent cannot perform the action, we must inform
				// the step that it cannot be executed by the agent, so
				// that the plan's probability is correctly updated
				_selectedAction.SetSelfExecutable(false);
			}
		} else if (!_selectedAction.getAgent().toString()
				.equals(Constants.SELF)) {
			// we have to wait for another agent to act
			AgentLogger.GetInstance().logAndPrint(
					"Waiting for agent "
							+ _selectedAction.getAgent().toString() + " to do:"
							+ _selectedAction.getName().toString());

			e = new Event(_selectedAction.getAgent().toString(), null, null);
			_actionMonitor = new ExpirableActionMonitor(waitingTime,
					_selectedAction, e);
			_selectedAction = null;
			_selectedActionEmotion = null;
			return null;
		}

		return new ValuedAction(DeliberativeComponent.NAME,
				_selectedAction.getName(), _selectedActionEmotion);
	}

	@Override
	public void actionFailedPerception(Event e) {
		if (e.GetSubject().equals(Constants.SELF)) {
			if (_actionMonitor != null) {
				if (_actionMonitor.matchEvent(e)) {
					_actionMonitor = null;
				}
			}
		}
	}

	public void actionSelectedForExecution(ValuedAction selectedAction) {
		String action;
		String target = null;
		Event e;

		if (_selectedAction == null) {
			return;
		}

		ListIterator<Symbol> li = _selectedAction.getName().GetLiteralList()
				.listIterator();

		action = li.next().toString();

		if (li.hasNext()) {
			target = li.next().toString();
		}

		e = new Event(Constants.SELF, action, target);
		_actionMonitor = new ActionMonitor(_selectedAction, e);

		while (li.hasNext()) {
			e.AddParameter(new Parameter("parameter", li.next().toString()));
		}

		_selectedActionEmotion = null;
		_selectedAction = null;
	}

	/**
	 * Deliberative Coping process. Gets the most relevant intention, thinks
	 * about it for one reasoning cycle (planning) and if possible selects an
	 * action for execution.
	 */
	@Override
	public ValuedAction actionSelection(AgentModel am) {

		Intention i = null;
		ActiveEmotion fear;
		ActiveEmotion hope;

		IPlanningOperator copingAction;

		_selectedActionEmotion = null;
		_selectedAction = null;
		_selectedPlan = null;

		_options.clear();
		for (IOptionsStrategy strategy : _optionStrategies) {
			_options.addAll(strategy.options(am));
		}

		// deliberation;
		ActivePursuitGoal g = filter(am, this._options);

		if (g != null) {
			addIntention(am, g);
		}

		// means-end reasoning
		_currentIntention = filter2ndLevel(am);
		if (_currentIntention != null) {
			i = _currentIntention.GetSubIntention();

			// TODO adding and removing intentions from memory.
			// ok, this needs some explaining. If you play close attention to
			// the following
			// code u'll see that if an intention has no available plans (i.e.
			// failed), I will not
			// remove the intention unless it has been strongly committed. This
			// is intended.
			// If not strongly committed the intention needs to stay in the list
			// of current intentions.
			// This is because if we would remove it, the agent would
			// immediately select the intention
			// again not knowing that he would fail to create a plan for it. So
			// he needs to remember that
			// the intention is not feasible. What we should do latter is to
			// remove the intention from memory
			// after some significant time has passed, or if a maximum number of
			// stored intentions is reached

			if (i.getGoal().CheckSuccess(am)) {
				removeIntention(i);
				for (IGoalSuccessStrategy s : _goalSuccessStrategies) {
					s.perceiveGoalSuccess(am, i.getGoal());
				}
				i.ProcessIntentionSuccess(am);
				return null;
			} else if (i.getGoal().CheckFailure(am)) {
				removeIntention(i);
				for (IGoalFailureStrategy s : _goalFailureStrategies) {
					s.perceiveGoalFailure(am, i.getGoal());
				}
				i.ProcessIntentionFailure(am);
				cancelAction(am);
				return null;
			} else if (i.NumberOfAlternativePlans() == 0) {
				for (IGoalFailureStrategy s : _goalFailureStrategies) {
					s.perceiveGoalFailure(am, i.getGoal());
				}
				i.ProcessIntentionFailure(am);
				_currentIntention = null;
				// we need to maintain the goal failure in memory (remembering
				// there is no way to achieve the goal)
				// if there is no strong commitment
				if (i.IsStrongCommitment()) {
					removeIntention(i);
				}
				cancelAction(am);
				return null;
			}
			/*
			 * else if(!i.IsStrongCommitment() &&
			 * !i.getGoal().checkPreconditions(am)) { //this is done only if the
			 * agent hasn't tried to do anything yet, he cancels the goal out
			 * //if the preconditions are not yet established
			 * removeIntention(i); }
			 */
			else {
				_selectedPlan = _planner.ThinkAbout(am, this, i);
			}

			if (_selectedPlan == null && i.IsStrongCommitment()
					&& !i.getGoal().checkPreconditions(am)) {
				i.ProcessIntentionCancel(am);
				removeIntention(i);
				return null;
			}

			// A plan does not have open preconditions nor any steps. This means
			// that the plan was succesfully
			// achieved
			if (_selectedPlan != null
					&& _selectedPlan.getOpenPreconditions().size() == 0
					&& _selectedPlan.getSteps().size() == 0) {
				removeIntention(i);
				for (IGoalSuccessStrategy s : _goalSuccessStrategies) {
					s.perceiveGoalSuccess(am, i.getGoal());
				}
				i.ProcessIntentionSuccess(am);
				return null;
			}

			if (_actionMonitor == null && _selectedPlan != null) {
				//AgentLogger.GetInstance().logAndPrint(
				//		"Plan Finished: " + _selectedPlan.toString());
				copingAction = _selectedPlan.UnexecutedAction(am);

				if (copingAction != null) {
					if (!i.IsStrongCommitment()) {
						i.SetStrongCommitment(am);
						AgentLogger.GetInstance().log(
								"Plan Commited: " + _selectedPlan.toString());
					}
					String actionName = copingAction.getName()
							.GetFirstLiteral().toString();

					if (copingAction instanceof ActivePursuitGoal) {
						addSubIntention(am, _currentIntention,
								(ActivePursuitGoal) copingAction);
					} else if (!actionName.startsWith("Inference")
							&& !actionName.endsWith("Appraisal")
							&& !actionName.startsWith("SA")) {
						fear = i.GetFear(am.getEmotionalState());
						hope = i.GetHope(am.getEmotionalState());
						if (hope != null) {
							if (fear != null) {
								if (hope.GetIntensity() >= fear.GetIntensity()) {
									_selectedActionEmotion = hope;
								} else {
									_selectedActionEmotion = fear;
								}
							} else {
								_selectedActionEmotion = hope;
							}
						} else {
							_selectedActionEmotion = fear;
						}

						_selectedAction = (Step) copingAction;
						AgentLogger.GetInstance().log(
								"Selecting Action: "
										+ _selectedAction.toString()
										+ "from plan:"
										+ _selectedPlan.toString());
					} else if (actionName.startsWith("SA")) {
						Effect eff;

						for (ListIterator<Effect> li = copingAction
								.getEffects().listIterator(); li.hasNext();) {
							eff = (Effect) li.next();
							if (eff.isGrounded())
								am.getMemory()
										.getSemanticMemory()
										.Tell(eff.GetEffect().getName(),
												eff.GetEffect().GetValue()
														.toString());
						}
						this.checkLinks(am);
					} else {
						// this should never be selected
						// System.out.println("InferenceOperator selected for execution");
					}
				} else {
					// If a complete plan does not have a valid next action to
					// execute (ex: the next
					// action to execute by self contains unboundvariables),
					// it means that the plan cannot be executed, and the plan
					// must be removed
					i.RemovePlan(_selectedPlan);
					AgentLogger.GetInstance().logAndPrint(
							"Plan with invalid next action removed!");
				}
			}
		}

		return getSelectedAction();
	}

	public void addActionFailureStrategy(IActionFailureStrategy strat) {
		_actionFailureStrategies.add(strat);
	}

	public void addActionSuccessStrategy(IActionSuccessStrategy strat) {
		_actionSuccessStrategies.add(strat);
	}

	/**
	 * Adds a goal to the agent's Goal List
	 * 
	 * @param goalName
	 *            - the name of the Goal
	 * @param importanceOfSuccess
	 *            - the goal's importance of success
	 * @param importanceOfFailure
	 *            - the goal's importance of failure
	 * @throws UnknownGoalException
	 *             - thrown if the goal is not specified in the GoalLibrary
	 *             file. You can only add goals defined in the GoalLibrary.
	 */
	public void addGoal(AgentModel am, String goalName)
			throws UnknownGoalException {
		Goal g = am.getGoalLibrary().GetGoal(Name.ParseName(goalName));
		if (g != null) {
			g.SetImportanceOfSuccess(am, 1);
			g.SetImportanceOfFailure(am, 1);
			addGoal(g);
		} else {
			throw new UnknownGoalException(goalName);
		}
	}

	/**
	 * Adds a goal to the agent's Goal List
	 * 
	 * @param goalName
	 *            - the name of the Goal
	 * @param importanceOfSuccess
	 *            - the goal's importance of success
	 * @param importanceOfFailure
	 *            - the goal's importance of failure
	 * @throws UnknownGoalException
	 *             - thrown if the goal is not specified in the GoalLibrary
	 *             file. You can only add goals defined in the GoalLibrary.
	 */
	public void addGoal(AgentModel am, String goalName,
			float importanceOfSuccess, float importanceOfFailure)
			throws UnknownGoalException {

		Goal g = am.getGoalLibrary().GetGoal(Name.ParseName(goalName));
		if (g != null) {
			g.SetImportanceOfSuccess(am, importanceOfSuccess);
			g.SetImportanceOfFailure(am, importanceOfFailure);
			addGoal(g);
		} else {
			throw new UnknownGoalException(goalName);
		}
	}

	/**
	 * Adds a goal to the agent's Goal List
	 * 
	 * @param goal
	 *            - the goal to add
	 */
	public void addGoal(Goal goal) {
		InterestGoal iGoal;
		ArrayList<Condition> protectionConstraints;
		ListIterator<Condition> li;

		synchronized (this) {
			if (!_goals.contains(goal)) {
				_goals.add(goal);
				if (goal instanceof InterestGoal) {
					iGoal = (InterestGoal) goal;
					protectionConstraints = iGoal.getProtectionConstraints();
					if (protectionConstraints != null) {
						li = protectionConstraints.listIterator();
						while (li.hasNext()) {
							addProtectionConstraint(new ProtectedCondition(
									iGoal, (Condition) li.next()));
						}
					}
				}
			}
		}
	}

	public void addGoalFailureStrategy(IGoalFailureStrategy strat) {
		_goalFailureStrategies.add(strat);
	}

	public void addGoalSuccessStrategy(IGoalSuccessStrategy strat) {
		_goalSuccessStrategies.add(strat);
	}

	/**
	 * Creates and Adds an intention to the set of intentions that the planner
	 * is currently trying to achieve (however the planner only picks one of
	 * them at each reasoning cycle)
	 * 
	 * @param goal
	 *            - the goal that we want to add
	 */
	public void addIntention(AgentModel am, ActivePursuitGoal goal) {
		ArrayList<Plan> plans;
		Plan newPlan;
		Intention intention;
		String goalName = goal.getNameWithCharactersOrdered();

		synchronized (this) {
			AgentLogger.GetInstance().logAndPrint(
					"Adding 1st level intention: " + goal.getName());
			intention = new Intention(am, goal);

			plans = goal.getPlans(am);
			if (plans == null) {
				newPlan = new Plan(_protectionConstraints,
						goal.GetSuccessConditions());
				intention.AddPlan(newPlan);
			} else {
				intention.AddPlans(plans);
			}

			_intentions.put(goalName, intention);
			intention.ProcessIntentionActivation(am);
		}
	}

	public void addOptionsStrategy(IOptionsStrategy strategy) {
		_optionStrategies.add(strategy);
	}

	/**
	 * Adds a ProtectionConstraint to the DeliberativeLayer. The planner will
	 * detect when there are threats to these ProtectionConstraints and deal
	 * with them with emotion-focused coping strategies.
	 * 
	 * @param cond
	 *            - the ProtectedCondition to add
	 * @see ProtectedCondition
	 */
	public void addProtectionConstraint(ProtectedCondition cond) {
		_protectionConstraints.add(cond);
	}

	public void addSubIntention(AgentModel am, Intention mainIntention,
			ActivePursuitGoal goal) {
		ArrayList<Plan> plans;
		Plan newPlan;
		Intention subIntention;

		subIntention = new Intention(am, goal);
		plans = goal.getPlans(am);
		if (plans == null) {
			newPlan = new Plan(_protectionConstraints,
					goal.GetSuccessConditions());
			subIntention.AddPlan(newPlan);
		} else {
			subIntention.AddPlans(plans);
		}

		mainIntention.AddSubIntention(subIntention);
	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame as) {
	}

	/**
	 * Changes a Goal's Importance
	 * 
	 * @param goalName
	 *            - the name of the goal to change
	 * @param importance
	 *            - the new value for the importance
	 * @param importanceType
	 *            - the type of importance: the String "CIS" changes the
	 *            importance of success the String "CIF" changes the importance
	 *            of failure
	 */
	public void changeGoalImportance(AgentModel am, String goalName,
			float importance, String importanceType) {
		ListIterator<Goal> li;

		synchronized (this) {
			li = _goals.listIterator();
			Goal g;

			while (li.hasNext()) {
				g = (Goal) li.next();
				if (goalName.equals(g.getName().toString())) {
					if (importanceType.equals("CIS")) {
						g.SetImportanceOfSuccess(am, importance);
					} else {
						g.SetImportanceOfFailure(am, importance);
					}
					break;
				}
			}
		}
	}

	/**
	 * Updates all the plans that the deliberative layer is currently working
	 * with, i.e., it updates all plans of all current active intentions
	 */
	public void checkLinks(AgentModel am) {
		Iterator<Intention> it;

		synchronized (this) {
			it = _intentions.values().iterator();
			while (it.hasNext()) {
				((Intention) it.next()).CheckLinks(am);
			}
		}
	}

	public boolean containsIntention(ActivePursuitGoal goal) {
		String goalName = goal.getNameWithCharactersOrdered();

		return _intentions.containsKey(goalName);

		/*
		 * while(it.hasNext()) { i = (Intention) it.next(); if
		 * (i.containsIntention(goalName)) return true; }
		 * 
		 * return false;
		 */
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new GoalsPanel(this);
	}

	@Override
	public IComponent createModelOfOther() {
		return new DeliberativeComponent();
	}

	@Override
	public void entityRemovedPerception(String entity) {
	}

	public ActivePursuitGoal filter(AgentModel am,
			ArrayList<ActivePursuitGoal> options) {
		ActivePursuitGoal g;
		ActivePursuitGoal maxGoal = null;
		float maxUtility;
		// expected utility of achieving a goal
		float EU;

		maxUtility = -200;

		ListIterator<ActivePursuitGoal> li = options.listIterator();
		while (li.hasNext()) {
			g = li.next();
			if (!containsIntention(g)) {
				EU = _EUStrategy.getExpectedUtility(am, g);

				if (EU > maxUtility) {
					maxUtility = EU;
					maxGoal = g;
				}
			}
		}

		if (maxGoal != null) {
			if (maxUtility >= MINIMUMUTILITY) {
				if (_currentIntention == null
						|| maxUtility > _EUStrategy.getExpectedUtility(am,
								_currentIntention) * SELECTIONTHRESHOLD) {
					return maxGoal;
				}
			}
		}

		return null;
	}

	/**
	 * Filters the most relevant intention from the set of possible
	 * intentions/goals. Corresponds to Focusing on a given goal
	 * 
	 * @return - the most relevant intention (the one with highest expected
	 *         utility)
	 */
	public Intention filter2ndLevel(AgentModel am) {
		Iterator<Intention> it;
		Intention intention;
		float highestUtility;
		Intention maxIntention = null;
		float EU;

		if (_currentIntention != null) {
			highestUtility = _EUStrategy.getExpectedUtility(am,
					_currentIntention);

			maxIntention = _currentIntention;
			// TODO selection threshold here!
		} else {
			maxIntention = null;
			highestUtility = -200;
		}

		synchronized (this) {
			it = _intentions.values().iterator();

			while (it.hasNext()) {

				intention = (Intention) it.next();

				if (intention.getGoal().CheckSuccess(am)) {
					removeIntention(intention);
					for (IGoalSuccessStrategy s : _goalSuccessStrategies) {
						s.perceiveGoalSuccess(am, intention.getGoal());
					}
					intention.ProcessIntentionSuccess(am);
					return null;
				} else if (intention.getGoal().CheckFailure(am)) {
					removeIntention(intention);
					for (IGoalFailureStrategy s : _goalFailureStrategies) {
						s.perceiveGoalFailure(am, intention.getGoal());
					}
					intention.ProcessIntentionFailure(am);
					cancelAction(am);
					return null;
				}

				if (intention != _currentIntention) {
					EU = _EUStrategy.getExpectedUtility(am, intention);

					if (EU > highestUtility && EU > MINIMUMUTILITY) {
						highestUtility = EU;
						maxIntention = intention;
					}
				}
			}
		}

		if (this._currentIntention != maxIntention) {
			AgentLogger.GetInstance().logAndPrint(
					"Switching 2nd level intention from "
							+ this._currentIntention + " to " + maxIntention);
		}

		this._currentIntention = maxIntention;

		return maxIntention;
	}

	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {};
		return dependencies;
	}

	/**
	 * Gets the agent's emotional planner used in the deliberative reasoning
	 * process
	 * 
	 * @return the agent's EmotionalPlanner
	 */
	public EmotionalPlanner getEmotionalPlanner() {
		return _planner;
	}

	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g) {
		return _UStrategy.getUtility(am, g) * _PStrategy.getProbability(am, g);
	}

	public float getExpectedUtility(AgentModel am, Intention i) {
		return _UStrategy.getUtility(am, i.getGoal())
				* _PStrategy.getProbability(am, i);
	}

	public IExpectedUtilityStrategy getExpectedUtilityStrategy() {
		return _EUStrategy;
	}

	/**
	 * Gets the agent's goals
	 * 
	 * @return a list with the agent's goals
	 */
	public ArrayList<Goal> getGoals() {
		return _goals;
	}

	/**
	 * Gets a set of IntentionKeys
	 * 
	 * @return a set with the keys used to store all intentions
	 */
	public Set<String> getIntentionKeysSet() {
		synchronized (this) {
			return _intentions.keySet();
		}
	}

	/**
	 * Gets a iterator that allows you to iterate over the set of active
	 * Intentions
	 * 
	 * @return
	 */
	public Iterator<Intention> getIntentionsIterator() {
		return _intentions.values().iterator();
	}

	public IProbabilityStrategy getProbabilityStrategy() {
		return _PStrategy;
	}

	public IGetUtilityForOthers getUtilityForOthersStrategy() {
		return _UOthersStrategy;
	}

	public IUtilityStrategy getUtilityStrategy() {
		return _UStrategy;
	}

	@Override
	public void initialize(AgentModel ag) {

		AgentLogger.GetInstance().log(
				"Adding Goals in the DeliberativeComponent:");
		DeliberativeLoaderHandler deliberativeLoader = new DeliberativeLoaderHandler(
				ag, this);

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			parser.parse(new File(ConfigurationManager.getPersonalityFile()),
					deliberativeLoader);

		} catch (Exception e) {
			throw new RuntimeException(
					"Error on loading goals from the agent XML Files:" + e);
		}
	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
	}

	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
	}

	@Override
	public String name() {
		return DeliberativeComponent.NAME;
	}

	public ArrayList<ActivePursuitGoal> options(AgentModel am) {
		Goal g;
		ActivePursuitGoal aGoal;
		ListIterator<Goal> li;
		ListIterator<SubstitutionSet> li2;
		ActivePursuitGoal desire;
		SubstitutionSet subSet;
		ArrayList<SubstitutionSet> substitutionSets;
		ArrayList<ActivePursuitGoal> options;

		options = new ArrayList<ActivePursuitGoal>();

		// TODO optimize the goal activation verification
		synchronized (this) {
			li = _goals.listIterator();
			while (li.hasNext()) {
				g = (Goal) li.next();
				if (g instanceof ActivePursuitGoal) {
					aGoal = (ActivePursuitGoal) g;

					substitutionSets = Condition.CheckActivation(am,
							aGoal.GetPreconditions());
					if (substitutionSets != null) {
						li2 = substitutionSets.listIterator();
						while (li2.hasNext()) {

							subSet = (SubstitutionSet) li2.next();

							desire = (ActivePursuitGoal) aGoal.clone();
							desire.MakeGround(subSet.GetSubstitutions());

							// In addition to testing the preconditions, we only
							// add a goal
							// as a desire if it's success and failure
							// conditions are not satisfied

							if (!desire.CheckSuccess(am)
									&& !desire.CheckFailure(am)) {

								options.add(desire);
							}
						}
					}
				}
			}
		}

		return options;
	}

	@Override
	public void processExternalRequest(AgentModel am, String type,
			String perception) {

		StringTokenizer st;

		if (type.equals(CHANGE_IMPORTANCE_SUCCESS)
				|| type.equals(CHANGE_IMPORTANCE_FAILURE)) {
			st = new StringTokenizer(perception, " ");
			String goalName = st.nextToken();
			float importance = new Float(st.nextToken()).floatValue();
			changeGoalImportance(am, goalName, importance, type);
		} else if (type.equals(ADD_GOALS)) {
			AddGoalsRequest(am, perception);
		} else if (type.equals(REMOVE_GOAL)) {
			removeGoal(perception);
		} else if (type.equals(REMOVE_ALL_GOALS)) {
			removeAllGoals();
		}
	}

	@Override
	public void propertyChangedPerception(String ToM, Name propertyName,
			String value) {
	}

	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
	}

	/**
	 * Removes all the agent's goals
	 * 
	 */
	public void removeAllGoals() {

		synchronized (this) {
			_goals.clear();
			_options.clear();
			_intentions.clear();
		}
	}

	/**
	 * Removes a given goal from the agent's goal list
	 * 
	 * @param goalName
	 *            - the name of the goal to remove
	 */
	public void removeGoal(String goalName) {
		Goal g;

		synchronized (this) {
			for (int i = 0; i < _goals.size(); i++) {
				g = (Goal) _goals.get(i);
				if (goalName.equals(g.getName().toString())) {
					_goals.remove(i);
					break;
				}
			}
		}
	}

	public void removeIntention(Intention i) {
		if (i.isRootIntention()) {
			synchronized (this) {
				_intentions.remove(i.getGoal().getNameWithCharactersOrdered()
						.toString());
			}
			_currentIntention = null;
		} else {
			// TODO remove or change this
			this.removeIntention(i.getParentIntention());
			// i.getParentIntention().RemoveSubIntention();
		}
	}

	/**
	 * Resets the deliberative layer. Clears the events to be appraised, the
	 * current intentions and actions.
	 */
	@Override
	public void reset() {
		// TODO incomplete
		_options.clear();
		_intentions.clear();
		_actionMonitor = null;
		_selectedAction = null;
		_selectedActionEmotion = null;
	}

	public void setExpectedUtilityStrategy(IExpectedUtilityStrategy strategy) {
		_EUStrategy = strategy;
	}

	public void setProbabilityStrategy(IProbabilityStrategy strategy) {
		_PStrategy = strategy;
	}

	public void setUtilityForOthersStrategy(IGetUtilityForOthers strat) {
		_UOthersStrategy = strat;
	}

	public void setUtilityStrategy(IUtilityStrategy strategy) {
		_UStrategy = strategy;
	}

	/**
	 * Determines an answer to a SpeechAct according to the agent's goals and
	 * plans
	 * 
	 * @return the best answer to give according to its influence on the agent's
	 *         goals and plans
	 */
	/*
	 * public ValuedAction AnswerToSpeechAct(SpeechAct speechAct) { Step
	 * positiveAnswer; Step negativeAnswer; Name positiveSpeech; Name
	 * negativeSpeech; float positiveAnswerIntensity; float
	 * negativeAnswerIntensity; ArrayList bindings; Name action; Name
	 * goalFailure; float actionValue; Goal g;
	 * 
	 * positiveSpeech = Name.ParseName("Reply(" + speechAct.getSender() + "," +
	 * speechAct.getMeaning() + ",positiveanswer)"); negativeSpeech =
	 * Name.ParseName("Reply(" + speechAct.getSender() + "," +
	 * speechAct.getMeaning() + ",negativeanswer)");
	 * 
	 * //check if the speech act refers to any goal synchronized (this) {
	 * ListIterator li = _goals.listIterator(); while(li.hasNext()) { g = (Goal)
	 * li.next();
	 * if(g.GetName().GetFirstLiteral().toString().equals(speechAct.getMeaning
	 * ())) { //in this case, the user is suggesting that the agent should try
	 * to achieve this goal //if the agent tried previously to achieve it and
	 * the goal failed, it will reply no way goalFailure =
	 * Name.ParseName(g.GenerateGoalStatus(Goal.GOALFAILURE)); bindings =
	 * KnowledgeBase.GetInstance().GetPossibleBindings(goalFailure); if
	 * (bindings != null) { return new ValuedAction(negativeSpeech,10); } else {
	 * //if the goal didn't failed before, the agent will accept the user
	 * sugestion by increasing //the goal's importance
	 * g.IncreaseImportanceOfFailure(4); g.IncreaseImportanceOfSuccess(4);
	 * return new ValuedAction(positiveSpeech,10); } } } }
	 * 
	 * 
	 * positiveAnswer = _planner.GetStep(positiveSpeech); if(positiveAnswer !=
	 * null) { positiveAnswerIntensity =
	 * _planner.AppraiseAnswer(positiveAnswer); } else positiveAnswerIntensity =
	 * 0; negativeAnswer = _planner.GetStep(negativeSpeech); if(negativeAnswer
	 * != null) { negativeAnswerIntensity =
	 * _planner.AppraiseAnswer(negativeAnswer); } else negativeAnswerIntensity =
	 * 0;
	 * 
	 * if(positiveAnswerIntensity >= negativeAnswerIntensity) {
	 * if(positiveAnswer != null) { action = positiveAnswer.getName();
	 * actionValue = positiveAnswerIntensity - negativeAnswerIntensity; } else
	 * return null; } else { if(negativeAnswer != null) { action =
	 * negativeAnswer.getName(); actionValue = negativeAnswerIntensity -
	 * positiveAnswerIntensity; } else return null; }
	 * 
	 * return new ValuedAction(action,actionValue); }
	 */

	@Override
	public void update(AgentModel am, Event event) {

		checkLinks(am);

		if (_actionMonitor != null && _actionMonitor.matchEvent(event)) {
			if (_actionMonitor.getStep().getAgent().isGrounded()
					&& !_actionMonitor.getStep().getAgent().toString()
							.equals(Constants.SELF)) {
				// the agent was waiting for an action of other agent to be
				// complete
				// since the step of another agent may contain unbound
				// variables,
				// we cannot just compare the names, we need to try to unify
				// them
				if (Unifier.Unify(event.toStepName(), _actionMonitor.getStep()
						.getName()) != null) {
					_actionMonitor.getStep().IncreaseProbability(am);
					// System.out.println("Calling updateEffectsProbability (other's action: step completed)");
					_actionMonitor.getStep().updateEffectsProbability(am);
				} else {
					for (IActionFailureStrategy s : _actionFailureStrategies) {
						s.perceiveActionFailure(am, _actionMonitor.getStep());
					}

					_actionMonitor.getStep().DecreaseProbability(am);
				}
			} else {
				for (IActionSuccessStrategy s : _actionSuccessStrategies) {
					s.perceiveActionSuccess(am, _actionMonitor.getStep());
				}
				// System.out.println("Calling updateEffectsProbability (self: step completed)");
				_actionMonitor.getStep().updateEffectsProbability(am);
			}

			updateProbabilities();
			_actionMonitor = null;
		}
	}

	@Override
	public void update(AgentModel am, long time) {
		ArrayList<IPlanningOperator> canceledActions;

		if (_selectedPlan != null) {
			canceledActions = _selectedPlan.UpdatePlan(am);

			if (_actionMonitor != null) {
				for (IPlanningOperator op : canceledActions) {
					if (_actionMonitor.getStep().getName().equals(op.getName())) {
						cancelAction(am);
						checkLinks(am);
						return;
					}
				}

			}
		}

		if (_actionMonitor != null) {
			if (_actionMonitor.expired()) {
				AgentLogger.GetInstance().logAndPrint(
						"Action monitor expired: " + _actionMonitor.toString());
				// If the action expired we must check the plan links
				// (continuous planning)
				// just to make sure
				checkLinks(am);

				for (IActionFailureStrategy s : _actionFailureStrategies) {
					s.perceiveActionFailure(am, _actionMonitor.getStep());
				}
				_actionMonitor.getStep().DecreaseProbability(am);

				// ResetIntention(am);

				updateProbabilities();

				_actionMonitor = null;
			}
		}
	}

	public void ResetIntention(AgentModel am) {
		ArrayList<Plan> plans;
		Plan newPlan;
		ActivePursuitGoal g;

		if (_currentIntention != null) {
			_currentIntention.ResetPlans();
			_selectedPlan = null;
			g = _currentIntention.getGoal();

			plans = g.getPlans(am);
			if (plans == null) {
				newPlan = new Plan(_protectionConstraints,
						g.GetSuccessConditions());
				_currentIntention.AddPlan(newPlan);
			} else {
				_currentIntention.AddPlans(plans);
			}
		}
	}

	/**
	 * Forces the recalculation of all plan's probability
	 */
	public void updateProbabilities() {

		Iterator<Intention> it;

		it = _intentions.values().iterator();
		while (it.hasNext()) {
			((Intention) it.next()).UpdateProbabilities();
		}
	}
}
