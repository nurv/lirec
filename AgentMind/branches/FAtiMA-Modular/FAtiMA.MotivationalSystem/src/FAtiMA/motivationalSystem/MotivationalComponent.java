/*
 * MotivatorState.java - Represents the character's motivational state
 */

package FAtiMA.motivationalSystem;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.AgentSimulationTime;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAdvancedPerceptionsComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.componentTypes.IProcessExternalRequestComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.RatedObject;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
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
		IGoalSuccessStrategy, IGoalFailureStrategy, IActionFailureStrategy, 
		IProcessExternalRequestComponent, IAdvancedPerceptionsComponent {

	private static final float EFFECT_ON_DRIVES_WEIGHT = 0.5f;
	private static final long serialVersionUID = 1L;
	public static final String NAME = "MotivationalState";


	protected HashMap<String, Motivator> _motivators;
	// protected Hashtable<String,Motivator[]> _otherAgentsMotivators;
	protected long _lastTime;
	protected int _goalTried;

	protected int _goalSucceeded;
	protected HashMap<String, Float> _appraisals;
	protected HashMap<String, ExpectedGoalEffectsOnDrives> _goalEffectsOnDrives;

	protected HashMap<String, ActionEffectsOnDrives> _actionEffectsOnDrives;
	
	private float _contributionToNeeds;

	private ArrayList<String> _parsingFiles;
	private NeedsLoaderHandler _parser;

	/**
	 * Creates an empty MotivationalState
	 */
	public MotivationalComponent(ArrayList<String> extraFiles) {
		_motivators = new HashMap<String,Motivator>();
		_goalTried = 0;
		_goalSucceeded = 0;
		_lastTime = AgentSimulationTime.GetInstance().Time();
		_appraisals = new HashMap<String, Float>();
		_goalEffectsOnDrives = new HashMap<String, ExpectedGoalEffectsOnDrives>();
		_actionEffectsOnDrives = new HashMap<String, ActionEffectsOnDrives>();
		
		_contributionToNeeds = 0;

		_parsingFiles = new ArrayList<String>();
		/*_parsingFiles.add(ConfigurationManager.getGoalsFile());
		_parsingFiles.add(ConfigurationManager.getPersonalityFile());
		_parsingFiles.add(ConfigurationManager.getActionsFile());*/
		_parsingFiles.addAll(extraFiles);
	}

	/*private void LoadNeeds(AgentModel am) {
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
	}*/

	private float newCompetence(boolean succeed) {
		float alpha = 0.25f;
		int value = 0;
		float newCompetence;
		if (succeed) {
			value = 10;
		}

		Motivator competenceM = _motivators.get(MotivatorType.COMPETENCE);

		newCompetence = alpha * value + (1 - alpha)
				* competenceM.GetIntensity();

		if (newCompetence < 1) {
			newCompetence = 1;
		}

		return newCompetence;
	}

	public void addActionEffectsOnDrive(String action, String driveName,
			Symbol target, Symbol value) {
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
			String driveName, Symbol target, Symbol value) {
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
		_motivators.put(motivator.GetName(), motivator);
	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame as) {
		Float desirability = new Float(0);
		
		
		desirability = _appraisals.get(e.toString());
		_appraisals.remove(e.toString());	
		
		if(e.GetSubject().equals(Constants.SELF) || (e.GetTarget() != null && e.GetTarget().equals(Constants.SELF)))
		{
			if(desirability != null)
			{
				desirability += _contributionToNeeds;
				_contributionToNeeds = 0;
			}
		}
		
		
		if (desirability != null) {
			as.SetAppraisalVariable(NAME, (short) 8,
					OCCAppraisalVariables.DESIRABILITY.name(),
					desirability.floatValue());
		}
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

		for (Motivator m : _motivators.values()) {
			m2 = (Motivator) m.clone();
			m2.SetIntensity(m.GetInitialIntensity());
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
		float auxMultiplier = 1;
		boolean test;
		Motivator m;
		EffectOnDrive groundedEffect;
		Symbol value;

		
		result = 0;

		ExpectedGoalEffectsOnDrives effects = _goalEffectsOnDrives.get(g.getKey());
		if (effects == null)
			return 0;
		for (EffectOnDrive e : effects.getEffects()) {
			groundedEffect = (EffectOnDrive) e.clone();
			groundedEffect.MakeGround(g.getAppliedSubstitutions());
			
			if (am.isSelf()) {
				test = groundedEffect.getTarget().toString().equals(Constants.SELF);
			} else {
				test = groundedEffect.getTarget().toString().equals(am.getName());
			}

			if (test) {
				value = groundedEffect.getValue();
				if(value.isGrounded())
				{
					expectedContribution = Float.parseFloat(value.toString());
					if(_motivators.containsKey(groundedEffect.getDriveName()))
					{
						m = _motivators.get(groundedEffect.getDriveName());
						
						if (groundedEffect.getType() == EffectType.ON_SELECT) 
						{
							auxMultiplier = 1;
						} else if (groundedEffect.getType() == EffectType.ON_IGNORE) 
						{
							auxMultiplier = -1;
						}
						
						result += auxMultiplier * m.evaluateNeedVariation(expectedContribution);
					}
				}
			}
		}
		
		m = _motivators.get(MotivatorType.COMPETENCE);
		float expectedCompetenceContribution = PredictCompetenceChange(true);
		result += m.evaluateNeedVariation(expectedCompetenceContribution);
		
		m = _motivators.get(MotivatorType.CERTAINTY);
		// expected error assuming that the goal is successful
		float expectedError = 1 - g.getProbability(am);
		float currentError = g.getUncertainty(am);
		float expectedUncertaintyContribution = 10 * (currentError - expectedError);
		result += m.evaluateNeedVariation(expectedUncertaintyContribution);

		

		return result;
	}

	public float getExpectedUtility(AgentModel am, ActivePursuitGoal g) {
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
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
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);

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
	public float GetIntensity(String type) {
		if(_motivators.containsKey(type))
		{
			return _motivators.get(type).GetIntensity();
		}
		return 0;
	}

	public Motivator GetMotivator(String motivatorType) {
		return _motivators.get(motivatorType);
	}

	public HashMap<String,Motivator> getMotivators() {
		return _motivators;
	}

	/**
	 * Gets the motivator's urgency discretizing the need intensity into diffent
	 * categories (very urgent, urgent, not urgent, satisfied)
	 * 
	 * @return a multiplier corresponding to the motivator's urgency
	 */
	/*public float GetNeedUrgency(String agentName, String type) {
		
		if(_motivators.containsKey(type))
		{
			return _motivators.get(type).GetNeedUrgency();
		}
		return 0;
		
	}*/

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
	public float GetWeight(String type) {
		if(_motivators.containsKey(type))
		{
			return _motivators.get(type).GetWeight();
		}
		return 0;
	}

	@Override
	public void initialize(AgentModel am) {
		DeliberativeComponent dp = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);

		dp.setExpectedUtilityStrategy(this);
		dp.setProbabilityStrategy(this);
		dp.setUtilityStrategy(this);
		dp.addActionFailureStrategy(this);
		dp.addGoalFailureStrategy(this);
		dp.addGoalSuccessStrategy(this);
		//LoadNeeds(am);
		this._parser = new NeedsLoaderHandler(am, this);
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

		float newExpectedError = ActivePursuitGoal.alfa * observedError + (1 - ActivePursuitGoal.alfa) * previousExpectedError;
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

		float newExpectedError = ActivePursuitGoal.alfa * observedError + (1 - ActivePursuitGoal.alfa) * previousExpectedError;
		float deltaError = newExpectedError - previousExpectedError;
		UpdateCertainty(-deltaError);
		g.setUncertainty(am, newExpectedError);
	}

	public float PredictCompetenceChange(boolean succeed) {
		Motivator competenceM = _motivators.get(MotivatorType.COMPETENCE);
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
		for(Motivator m : _motivators.values())
		{
			result = result + m.toXml();
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
			for(Motivator m : _motivators.values())
			{
				m.DecayMotivator();
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
		_motivators.get(MotivatorType.CERTAINTY).UpdateIntensity(expectation * 3);
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
		Motivator competenceM = _motivators.get(MotivatorType.COMPETENCE);
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
		EffectOnDrive groundedEffect;
		Float value;
		Motivator motivator;
		ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
		Symbol target;
		double contributionToNeed = 0f;
		float contributionToSelfNeeds = 0f; // used for events performed by the
											// agent

		for (ActionEffectsOnDrives actionEffects : _actionEffectsOnDrives.values()) {
			Name actionName = actionEffects.getActionName();
			if (Unifier.PartialUnify(e.toStepName(), actionName, substitutions)) {
				substitutions.add(new Substitution(new Symbol("[AGENT]"),new Symbol(e.GetSubject())));
				for (EffectOnDrive eff : actionEffects.getEffects()) {
					groundedEffect = (EffectOnDrive) eff.clone();
					groundedEffect.MakeGround(substitutions);
					target = groundedEffect.getTarget();
					
					if (target.toString().equals(Constants.SELF)) {
						AgentLogger.GetInstance().log("Updating motivator " + groundedEffect.getDriveName());
						
						if(_motivators.containsKey(groundedEffect.getDriveName()))
						{
							motivator = _motivators.get(groundedEffect.getDriveName());
							if(motivator.hasInternalUpdate())
							{
								value = Float.parseFloat(groundedEffect.getValue().toString());
								contributionToNeed = motivator.evaluateNeedVariation(value);
								contributionToSelfNeeds += contributionToNeed;
								motivator.UpdateIntensity(value);
							}
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

	public ArrayList<RatedObject<SubstitutionSet>> searchEventsWithAppraisal(AgentModel am,
			Symbol appraisingAgent, Symbol subjectVariable, Symbol actionVariable,
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
		
		ArrayList<Substitution> subs;
		ArrayList<RatedObject<SubstitutionSet>> ratedSubstitutions = new ArrayList<RatedObject<SubstitutionSet>>();
		ArrayList<Symbol> actionListOfSymbols;
		EffectOnDrive groundedEffect;
		SubstitutionSet currentSet;
		Symbol actionTarget;
		Symbol effectTarget;
		Symbol action;
		Symbol param1;
		Symbol sValue;
		float fValue;
		float ratio;
		Name actionName;
		Motivator motivator;
		float contributionToNeed = 0f;
		float score;

		for (ActionEffectsOnDrives actionEffects : _actionEffectsOnDrives.values()) {

			test = new HashMap<String, Box>();

			for (EffectOnDrive eff : actionEffects.getEffects()) {
				
				effectTarget = eff.getTarget();
				if(_motivators.containsKey(eff.getDriveName()))
				{
					motivator = _motivators.get(eff.getDriveName());
					sValue = eff.getValue();
					if(sValue.isGrounded())
					{
						fValue = Float.parseFloat(sValue.toString());
						contributionToNeed = (float) motivator.evaluateNeedVariation(fValue);
						if (test.containsKey(effectTarget.toString())) {
							Box b = test.get(effectTarget.toString());
							b.value = b.value + contributionToNeed;
						} else {
							test.put(effectTarget.toString(), new Box(effectTarget.toString(),contributionToNeed));
						}
					}
				}				
			}

			for (Box b : test.values()) {
				ratio = b.value/desirability;
				if ((desirability >= 0) ? ratio > 0.3 : ratio > 0.3) {
					
					//AgentLogger.GetInstance().log("found a desired motivational effect torwards -" + b.target);
					//AgentLogger.GetInstance().log("appraising agent: "  + appraisingAgent);
					actionName = (Name) actionEffects.getActionName().clone();
					
					subs = Unifier.Unify(new Symbol(b.target), appraisingAgent);
					if(subs != null)
					{
						currentSet = new SubstitutionSet();
						//unfortunately I don't have the performer agent in the action's name, so I have to do
						//this test manually
						if(b.target.equals("[AGENT]"))
						{
							currentSet.AddSubstitution(new Substitution(subjectVariable,appraisingAgent));
						}
						
						actionName.MakeGround(subs);
						//AgentLogger.GetInstance().log("action after grounding: " + actionName);
						actionListOfSymbols = actionName.GetLiteralList();
						
						action = actionListOfSymbols.get(0);
						
						currentSet.AddSubstitution(new Substitution(actionVariable,action));
							
		
						if (actionListOfSymbols.size() > 1) {
							actionTarget = actionListOfSymbols.get(1);
							currentSet.AddSubstitution(new Substitution(targetVariable, actionTarget));
						}
							
						if(actionListOfSymbols.size() > 2)
						{
							param1 = actionListOfSymbols.get(2);
							currentSet.AddSubstitution(new Substitution(paramVariable, param1));
						}
						
						score = Math.abs(desirability-b.value);
						//AgentLogger.GetInstance().log("Score: " + score);
						RatedObject<SubstitutionSet> ratedSet = new RatedObject<SubstitutionSet>(currentSet, score);
						ratedSubstitutions.add(ratedSet);
					}
				}
			}
		}

		return ratedSubstitutions; // finishes after finding the action that unifies
								// with the event
	}

	@Override
	public void processExternalRequest(AgentModel am, String msgType, String perception) {
		
		Motivator motivator;
		String motivatorName;
		
		AgentLogger.GetInstance().log("Processing external request: " + msgType + " " + perception);
		
		if(msgType.equals("DRIVECHANGED"))
		{
			StringTokenizer st = new StringTokenizer(perception);
			String agent = st.nextToken();
			if(agent.equals(am.getName()))
			{
				motivatorName = st.nextToken();
				
				if(_motivators.containsKey(motivatorName))
				{
					motivator = _motivators.get(motivatorName);
					
					float deviation = Float.parseFloat(st.nextToken());
					double contributionToNeed = motivator.evaluateNeedVariation(deviation);
					_contributionToNeeds += contributionToNeed;
					motivator.UpdateIntensity(deviation);
				}	
			}
		}
	}

	@Override
	public void propertyChangedPerception(AgentModel am, String ToM, Name propertyName, String value) {
		
		Motivator motivator;
		
		if(ToM.equals(Constants.UNIVERSAL.toString()))
		{
			String agent = propertyName.GetFirstLiteral().toString();
			if(agent.equals(Constants.SELF))
			{
				String property = propertyName.GetLiteralList().get(1).toString();
				
				if(_motivators.containsKey(property))
				{
					motivator = _motivators.get(property);
					float oldLevel = motivator.GetIntensity();
					
					//quick fix for culture-variant decimal point separator
					String newValue = value.replace(",", ".");
					float intensity = Float.parseFloat(newValue);
					float deviation = intensity - oldLevel;
					double contributionToNeed = motivator.evaluateNeedVariation(deviation);
					_contributionToNeeds += contributionToNeed;
					motivator.SetIntensity(intensity);		
				}
			}
		}
	}

	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
	}

	@Override
	public void entityRemovedPerception(AgentModel am, String entity) {
		
	}

	@Override
	public void actionFailedPerception(AgentModel am, Event e) {
		
	}

	@Override
	public ReflectXMLHandler getActionsParser(AgentModel am) {
		return this._parser;
	}

	@Override
	public ReflectXMLHandler getGoalsParser(AgentModel am) {
		return this._parser;
	}

	@Override
	public ReflectXMLHandler getPersonalityParser(AgentModel am) {
		return this._parser;
	}

	@Override
	public void parseAdditionalFiles(AgentModel am) 
	{
		AgentLogger.GetInstance().log("LOADING Needs: ");

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();

			for (String file : _parsingFiles) {
				parser.parse(new File(file), this._parser);
			}

		} catch (Exception e) {
			throw new RuntimeException("Error on Loading Needs from XML Files:" + e);
		}	
	}

}
