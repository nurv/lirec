/*
 * MotivatorState.java - Represents the character's motivational state
 */

package FAtiMA.motivationalSystem;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.AgentSimulationTime;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.DeliberativeComponent.IProbabilityStrategy;
import FAtiMA.DeliberativeComponent.IUtilityStrategy;
import FAtiMA.DeliberativeComponent.Intention;
import FAtiMA.DeliberativeComponent.strategies.IActionFailureStrategy;
import FAtiMA.DeliberativeComponent.strategies.IExpectedUtilityStrategy;
import FAtiMA.DeliberativeComponent.strategies.IGoalFailureStrategy;
import FAtiMA.DeliberativeComponent.strategies.IGoalSuccessStrategy;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;

/**
 * Implements the character's motivational state.
 * 
 * @author Meiyii Lim, Samuel Mascarenhas
 */

public class MotivationalComponent implements Serializable, Cloneable,
		IAppraisalDerivationComponent, IModelOfOtherComponent,
		IExpectedUtilityStrategy, IProbabilityStrategy, IUtilityStrategy,
		IGoalSuccessStrategy, IGoalFailureStrategy, IActionFailureStrategy {

	private static final float EFFECT_ON_DRIVES_WEIGHT = 0.5f;
	private static final long serialVersionUID = 1L;
	public static final String NAME = "MotivationalState";
	private static final float MAX_INTENSITY = 10;
	private static final float MIN_INTENSITY = 0;

	public static double determineQuadraticNeedVariation(float currentLevel,
			float deviation) {
		double result = 0;
		float finalLevel;
		double currentLevelStr;
		double finalLevelStr;

		finalLevel = currentLevel + deviation;
		finalLevel = Math.min(finalLevel, MAX_INTENSITY);
		finalLevel = Math.max(finalLevel, MIN_INTENSITY);

		currentLevelStr = Math.pow(MAX_INTENSITY - currentLevel, 2);
		finalLevelStr = Math.pow(MAX_INTENSITY - finalLevel, 2);

		result = -(finalLevelStr - currentLevelStr);
		return result;
	}

	protected Motivator[] _motivators;
	// protected Hashtable<String,Motivator[]> _otherAgentsMotivators;
	protected long _lastTime;
	protected int _goalTried;

	protected int _goalSucceeded;
	protected HashMap<String, Float> _appraisals;
	protected HashMap<String, ExpectedGoalEffectsOnDrives> _goalEffectsOnDrives;

	protected HashMap<String, ActionEffectsOnDrives> _actionEffectsOnDrives;

	private ArrayList<String> _parsingFiles;

	/**
	 * Creates an empty MotivationalState
	 */
	public MotivationalComponent(ArrayList<String> extraFiles) {
		_motivators = new Motivator[MotivatorType.numberOfTypes()];
		_goalTried = 0;
		_goalSucceeded = 0;
		_lastTime = AgentSimulationTime.GetInstance().Time();
		_appraisals = new HashMap<String, Float>();
		_goalEffectsOnDrives = new HashMap<String, ExpectedGoalEffectsOnDrives>();
		_actionEffectsOnDrives = new HashMap<String, ActionEffectsOnDrives>();

		_parsingFiles = new ArrayList<String>();
		_parsingFiles.add(ConfigurationManager.getGoalsFile());
		_parsingFiles.add(ConfigurationManager.getPersonalityFile());
		_parsingFiles.add(ConfigurationManager.getActionsFile());
		_parsingFiles.addAll(extraFiles);
	}

	private void LoadNeeds(AgentModel am) {
		AgentLogger.GetInstance().log("LOADING Needs: ");
		NeedsLoaderHandler needsLoader = new NeedsLoaderHandler(am, this);

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			for (String file : _parsingFiles) {
				parser.parse(new File(file), needsLoader);
			}

		} catch (Exception e) {
			throw new RuntimeException("Error on Loading Needs from XML Files:"
					+ e);
		}
	}

	private float newCompetence(boolean succeed) {
		float alpha = 0.25f;
		int value = 0;
		float newCompetence;
		if (succeed) {
			value = 10;
		}

		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];

		newCompetence = alpha * value + (1 - alpha)
				* competenceM.GetIntensity();

		if (newCompetence < 1) {
			newCompetence = 1;
		}

		return newCompetence;
	}

	public void addActionEffectsOnDrive(String action, String driveName,
			Symbol target, float value) {
		ActionEffectsOnDrives effects;
		if (!_actionEffectsOnDrives.containsKey(action)) {
			effects = new ActionEffectsOnDrives(action);
			_actionEffectsOnDrives.put(action, effects);
		} else {
			effects = _actionEffectsOnDrives.get(action);
		}

		effects.AddEffect(new EffectOnDrive(EffectType.ON_PERFORMANCE,
				driveName, target, value));
	}

	public void addExpectedGoalEffectOnDrive(String goal, short effectType,
			String driveName, Symbol target, float value) {
		ExpectedGoalEffectsOnDrives effects;
		if (!_goalEffectsOnDrives.containsKey(goal)) {
			effects = new ExpectedGoalEffectsOnDrives(goal);
			_goalEffectsOnDrives.put(goal, effects);
		} else {
			effects = _goalEffectsOnDrives.get(goal);
		}

		effects.AddEffect(new EffectOnDrive(effectType, driveName, target,
				value));
	}

	/**
	 * Adds a motivator to the MotivationalState
	 */
	public void AddMotivator(Motivator motivator) {
		_motivators[motivator.GetType()] = new Motivator(motivator);
	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame as) {
		Float desirability = _appraisals.get(e.toString());
		if (desirability != null) {
			as.SetAppraisalVariable(NAME, (short) 8,
					OCCAppraisalVariables.DESIRABILITY.name(),
					desirability.floatValue());
		}
		_appraisals.remove(e.toString());
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new NeedsPanel(this);
	}

	@Override
	public IComponent createModelOfOther() {
		MotivationalComponent ms = new MotivationalComponent(
				new ArrayList<String>());
		Motivator m2;

		for (Motivator m : _motivators) {
			m2 = new Motivator(m);
			m2.SetIntensity(5);
			ms.AddMotivator(m2);
		}

		ms._actionEffectsOnDrives = (HashMap<String, ActionEffectsOnDrives>) _actionEffectsOnDrives
				.clone();
		ms._goalEffectsOnDrives = (HashMap<String, ExpectedGoalEffectsOnDrives>) _goalEffectsOnDrives
				.clone();

		return ms;
	}

	public float getCompetence(AgentModel am, ActivePursuitGoal g) {
		float generalCompetence = GetIntensity(MotivatorType.COMPETENCE) / 10;
		Float probability = g.GetProbability(am);

		if (probability != null) {
			return (generalCompetence + probability.floatValue()) / 2;
		} else {
			// if there is no knowledge about the goal probability, the goal was
			// never executed before
			// however, the agent assumes that he will be successful in
			// achieving it
			return (generalCompetence + 1) / 2;
		}
	}

	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = { DeliberativeComponent.NAME };
		return dependencies;
	}

	public float getContributionToNeeds(AgentModel am, ActivePursuitGoal g) {
		float result = 0;
		float expectedContribution;
		float currentIntensity = 0;
		float auxMultiplier = 1;
		boolean test;

		try {
			result = 0;

			ExpectedGoalEffectsOnDrives effects = _goalEffectsOnDrives.get(g
					.getKey());
			if (effects == null)
				return 0;
			for (EffectOnDrive e : effects.getEffects()) {
				if (am.isSelf()) {
					test = e.getTarget().toString().equals(Constants.SELF);
				} else {
					Symbol target = (Symbol) e.getTarget().clone();
					target.MakeGround(g.getAppliedSubstitutions());
					test = target.toString().equals(am.getName());
				}

				if (test) {
					expectedContribution = e.getValue();
					currentIntensity = GetIntensity(MotivatorType.ParseType(e
							.getDriveName()));
					if (e.getType() == EffectType.ON_SELECT) {
						auxMultiplier = 1;
					} else if (e.getType() == EffectType.ON_IGNORE) {
						auxMultiplier = -1;
					}

					result += auxMultiplier
							* MotivationalComponent
									.determineQuadraticNeedVariation(
											currentIntensity,
											expectedContribution) * 0.1f;
				}
			}

			float currentCompetenceIntensity = GetIntensity(MotivatorType.COMPETENCE);
			float expectedCompetenceContribution = PredictCompetenceChange(true);
			result += MotivationalComponent.determineQuadraticNeedVariation(
					currentCompetenceIntensity, expectedCompetenceContribution) * 0.1f;

			float currentUncertaintyIntensity = GetIntensity(MotivatorType.CERTAINTY);
			// expected error assuming that the goal is successful
			float expectedError = 1 - g.getProbability(am);
			float currentError = g.getUncertainty(am);
			float expectedUncertaintyContribution = 10 * (currentError - expectedError);
			result += MotivationalComponent.determineQuadraticNeedVariation(
					currentUncertaintyIntensity,
					expectedUncertaintyContribution) * 0.1f;

		} catch (InvalidMotivatorTypeException e) {
			AgentLogger.GetInstance().log("EXCEPTION:" + e);
			e.printStackTrace();
		}

		return result;
	}

	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g) {
		DeliberativeComponent dc = (DeliberativeComponent) am
				.getComponent(DeliberativeComponent.NAME);
		float utility = dc.getUtilityStrategy().getUtility(am, g);
		float probability = dc.getProbabilityStrategy().getProbability(am, g);

		float EU = utility * probability * (1 + g.GetGoalUrgency());

		AgentLogger.GetInstance().intermittentLog(
				"Goal: " + g.getName() + " Utilitity: " + utility
						+ " Competence: " + probability + " Urgency: "
						+ g.GetGoalUrgency() + " Total: " + EU);
		return EU;
	}

	public float getExpectedUtility(AgentModel am, Intention i) {
		DeliberativeComponent dc = (DeliberativeComponent) am
				.getComponent(DeliberativeComponent.NAME);

		float utility = dc.getUtilityStrategy().getUtility(am, i.getGoal());
		float probability = dc.getProbabilityStrategy().getProbability(am, i);

		float EU = utility * probability * (1 + i.getGoal().GetGoalUrgency());

		AgentLogger.GetInstance().intermittentLog(
				"Intention: " + i.getGoal().getName() + " Utilitity: "
						+ utility + " Competence: " + probability
						+ " Urgency: " + i.getGoal().GetGoalUrgency()
						+ " Total: " + EU);
		return EU;
	}

	/**
	 * Gets the received motivator's intensity, i.e. the current level of the
	 * motivator
	 * 
	 * @return a float value corresponding to the motivator's intensity
	 */
	public float GetIntensity(short type) {
		return _motivators[type].GetIntensity();
	}

	public Motivator GetMotivator(short motivatorType) {
		return _motivators[motivatorType];
	}

	public Motivator[] getMotivators() {
		return _motivators;
	}

	/**
	 * Gets the motivator's urgency discretizing the need intensity into diffent
	 * categories (very urgent, urgent, not urgent, satisfied)
	 * 
	 * @return a multiplier corresponding to the motivator's urgency
	 */
	public float GetNeedUrgency(String agentName, short type) {
		return _motivators[type].GetNeedUrgency();
	}

	public float getProbability(AgentModel am, ActivePursuitGoal g) {
		return getCompetence(am, g);
	}

	public float getProbability(AgentModel am, Intention i) {
		return i.GetProbability(am);
	}

	public float getUtility(AgentModel am, ActivePursuitGoal g) {
		return getContributionToNeeds(am, g) * EFFECT_ON_DRIVES_WEIGHT;
	}

	/**
	 * Gets the received motivator's weight, i.e. how important is the motivator
	 * to the agent
	 * 
	 * @return a float value corresponding to the motivator's weight
	 */
	public float GetWeight(short type) {
		return _motivators[type].GetWeight();
	}

	@Override
	public void initialize(AgentModel am) {
		DeliberativeComponent dp = (DeliberativeComponent) am
				.getComponent(DeliberativeComponent.NAME);

		dp.setExpectedUtilityStrategy(this);
		dp.setProbabilityStrategy(this);
		dp.setUtilityStrategy(this);
		dp.addActionFailureStrategy(this);
		dp.addGoalFailureStrategy(this);
		dp.addGoalSuccessStrategy(this);
		LoadNeeds(am);
	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
		// TODO
	}

	@Override
	public String name() {
		return MotivationalComponent.NAME;
	}

	@Override
	public void perceiveActionFailure(AgentModel am, Step a) {
		// System.out.println("Calling UpdateCertainty (other's action: step completed)");
		UpdateCertainty(-a.getProbability(am));
	}

	@Override
	public void perceiveGoalFailure(AgentModel am, ActivePursuitGoal g) {
		// _numberOfGoalsTried++;
		UpdateCompetence(false);

		// observed error = |estimation of success - realsuccess|
		// given that the goal failed, the real success is none and the formula
		// resumes to
		// observed error = estimation of success - 0 (=) estimation of success
		float observedError = g.getProbability(am);
		float previousExpectedError = g.getUncertainty(am);

		float newExpectedError = ActivePursuitGoal.alfa * observedError
				+ (1 - ActivePursuitGoal.alfa) * previousExpectedError;
		float deltaError = newExpectedError - previousExpectedError;
		UpdateCertainty(-deltaError);
		g.setUncertainty(am, newExpectedError);
	}

	@Override
	public void perceiveGoalSuccess(AgentModel am, ActivePursuitGoal g) {

		UpdateCompetence(true);

		// observed error = |realsuccess - estimation of success|
		// given that the goal succeeded, the real success is 1 and the formula
		// resumes to
		// observed error = 1 - estimation of success
		float observedError = 1 - g.getProbability(am);
		float previousExpectedError = g.getUncertainty(am);

		float newExpectedError = ActivePursuitGoal.alfa * observedError
				+ (1 - ActivePursuitGoal.alfa) * previousExpectedError;
		float deltaError = newExpectedError - previousExpectedError;
		UpdateCertainty(-deltaError);
		g.setUncertainty(am, newExpectedError);
	}

	public float PredictCompetenceChange(boolean succeed) {
		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];
		return newCompetence(succeed) - competenceM.GetIntensity();
	}

	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	/**
	 * Converts the motivational state to XML
	 * 
	 * @return a XML String that contains all information in the motivational
	 *         state
	 */
	public String toXml() {
		String result;

		result = "<MotivationalState>";
		for (int i = 0; i < _motivators.length; i++) {
			result = result + _motivators[i].toXml();
		}

		result = result + "</MotivationalState>";
		return result;
	}

	@Override
	public void update(AgentModel am, Event e) {
		float result = UpdateMotivators(am, e);
		_appraisals.put(e.toString(), new Float(result));
	}

	@Override
	public void update(AgentModel am, long time) {
		_appraisals.clear();
		if (time >= _lastTime + 1000) {
			_lastTime = time;

			// decay self motivators
			for (int i = 0; i < _motivators.length; i++) {
				_motivators[i].DecayMotivator();
			}

		}
	}

	/**
	 * Update the agent's certainty value
	 * 
	 * @param expectation
	 *            - ranges from -1 to 1, -1 means complete violation of
	 *            expectation while 1 means complete fulfillment of expectation
	 *            Changed the factor from 10 to 3 (Meiyii)
	 */
	public void UpdateCertainty(float expectation) {
		// System.out.println("Certainty before update" +
		// _selfMotivators[MotivatorType.CERTAINTY].GetIntensity());
		_motivators[MotivatorType.CERTAINTY].UpdateIntensity(expectation * 3);
		// System.out.println("Certainty after update" +
		// _selfMotivators[MotivatorType.CERTAINTY].GetIntensity());
	}

	/**
	 * Calculates the agent's competence about a goal
	 * 
	 * @param succeed
	 *            - whether a goal has succeeded, true is success, and false is
	 *            failure
	 */
	public void UpdateCompetence(boolean succeed) {
		Motivator competenceM = _motivators[MotivatorType.COMPETENCE];
		// System.out.println("Competence before update" +
		// competenceM.GetIntensity());

		competenceM.SetIntensity(newCompetence(succeed));
		// System.out.println("Competence after update" +
		// competenceM.GetIntensity());
	}

	/**
	 * Updates the intensity of the motivators based on the event received
	 * 
	 * @throws InvalidMotivatorTypeException
	 */
	public float UpdateMotivators(AgentModel am, Event e) {
		ArrayList<Substitution> substitutions;
		Symbol target;
		float deviation = 0;
		double contributionToNeed = 0f;
		float contributionToSelfNeeds = 0f; // used for events performed by the
											// agent

		for (ActionEffectsOnDrives actionEffects : _actionEffectsOnDrives.values()) {
			Name actionName = actionEffects.getActionName();
			substitutions = Unifier.Unify(e.toStepName(), actionName);
			if (substitutions != null) {
				substitutions.add(new Substitution(new Symbol("[AGENT]"),new Symbol(e.GetSubject())));
				for (EffectOnDrive eff : actionEffects.getEffects()) {
					target = (Symbol) eff.getTarget().clone();
					target.MakeGround(substitutions);
					if (target.toString().equals(Constants.SELF)) {
						AgentLogger.GetInstance().log("Updating motivator " + eff.getDriveName());
						try {
							short driveType = MotivatorType.ParseType(eff.getDriveName());
							float oldLevel = _motivators[driveType].GetIntensity();
							deviation = _motivators[driveType].UpdateIntensity(eff.getValue());
							contributionToNeed = determineQuadraticNeedVariation(oldLevel, deviation) * 0.1f;
							contributionToSelfNeeds += contributionToNeed;
						} catch (InvalidMotivatorTypeException e1) {
							e1.printStackTrace();
						}
					}
				}

				return contributionToSelfNeeds; // finishes after finding the
												// action that unifies with the
												// event
			}
		}

		return 0; // no action was found that unified with the event
	}

	public ArrayList<SubstitutionSet> searchEventsWithAppraisal(AgentModel am,
			Symbol subjectVariable, Symbol actionVariable,
			Symbol targetVariable, Symbol paramVariable, float desirability) {
		class Box {
			float value;
			String target;

			Box(String target, float value) {
				this.target = target;
				this.value = value;
			}
		}

		HashMap<String, Box> test;

		ArrayList<SubstitutionSet> substitutions = new ArrayList<SubstitutionSet>();
		ArrayList<Symbol> actionListOfSymbols;
		SubstitutionSet currentSet;
		Symbol target;
		Symbol action;
		Symbol param1;
		String auxName;
		float contributionToNeed = 0f;

		if (am.isSelf()) {
			auxName = Constants.SELF;
		} else {
			auxName = am.getName();
		}

		for (ActionEffectsOnDrives actionEffects : _actionEffectsOnDrives
				.values()) {

			test = new HashMap<String, Box>();

			for (EffectOnDrive eff : actionEffects.getEffects()) {
				try {
					target = eff.getTarget();
					short driveType = MotivatorType.ParseType(eff
							.getDriveName());
					float oldValue = _motivators[driveType].GetIntensity();
					float newValue = Math.max(0,
							Math.min(10, oldValue + eff.getValue()));
					contributionToNeed = (float) determineQuadraticNeedVariation(
							oldValue, newValue - oldValue) * 0.1f;
					if (test.containsKey(target.toString())) {
						Box b = test.get(target.toString());
						b.value = b.value + contributionToNeed;
					} else {
						test.put(target.toString(), new Box(target.toString(),
								contributionToNeed));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			for (Box b : test.values()) {
				if ((desirability >= 0) ? b.value >= desirability
						: b.value <= desirability) {
					
					//this test is done temporarily because right now I'm only interested in
					//in interpersonal emotion regulation and I don't want to consider the 
					//actions where the target causes an emotion in itself
				    //TODO: this must be done properly latter
					if(!b.target.equals("[AGENT]"))
					{
						actionListOfSymbols = actionEffects.getActionName().GetLiteralList();
	
						action = actionListOfSymbols.get(0);
						currentSet = new SubstitutionSet();
						currentSet.AddSubstitution(new Substitution(actionVariable,
								action));
						if (b.target.equals("[AGENT]")) {
							currentSet.AddSubstitution(new Substitution(
									subjectVariable, new Symbol(auxName)));
						}
	
						if (actionListOfSymbols.size() > 1) {
							target = actionListOfSymbols.get(1);
							if (b.target.equals(target.toString())) {
								currentSet.AddSubstitution(new Substitution(
										targetVariable, new Symbol(auxName)));
							} else {
								currentSet.AddSubstitution(new Substitution(
										targetVariable, target));
							}
						}
						
						if(actionListOfSymbols.size() > 2)
						{
							param1 = actionListOfSymbols.get(2);
							currentSet.AddSubstitution(new Substitution(paramVariable, param1));
						}
						substitutions.add(currentSet);
					}
				}
			}
		}

		Collections.shuffle(substitutions);
		return substitutions; // finishes after finding the action that unifies
								// with the event
	}

}
