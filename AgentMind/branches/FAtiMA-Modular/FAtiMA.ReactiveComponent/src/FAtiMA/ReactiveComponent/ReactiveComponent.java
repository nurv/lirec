/** 
 * ReactiveProcess.java - Implements FearNot's Agent Reactive Process (appraisal and coping)
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
 * João Dias: 23/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable 
 * João Dias: 15/07/2006 - Removed the EmotionalState from the Class fields since the 
 * 						   EmotionalState is now a singleton that can be used anywhere 
 * 					       without previous references.
 * João Dias: 17/07/2006 - The following methods were moved from class Agent to this class
 * 							  - AddEmotionalReaction
 * 							  - GetEmotionalReactions
 * 							  - GetActionTendencies
 * João Dias: 21/07/2006 - The class constructor now only receives the agent's name
 * João Dias: 05/09/2006 - Changed the way in which Attribution Emotions are generated. From
 * 						   now on, is no longer necessary to specify attribution reactions for
 * 						   a character. They are automatically generated when a "look-at" action
 * 						   is perceived, and the like appraisal variable is retrieved from semantic
 * 						   memory (the KnowledgeBase)
 * 						 - Changed the way in which FortuneOfOthers Emotions are determined, now 
 * 						   the interpersonal relationShip (like/dislike) between characters affects the desirability
 * 						   of the event and thus the final emotions being generated.
 * João Dias: 06/09/2006 - Another change in the way that FortuneOfOthers Emotions are determined,
 * 						   they now are generated for the Actor that performs the actions, and the
 * 						   object or character that is the target of the action. We take into account
 * 						   the like relations between every character to determine the event's desirability.
 * João Dias: 18/09/2006 - Small change in the generation of FortuneOfOther emotions. Now they consider the 
 * 						   the new other variable. If this variable is defined the FortuneOfOther emotion
 * 						   created will be directed to the character specified in the variable. If the variable
 * 						   is not defined (null) the emotion is proccessed as before.
 * João Dias: 20/09/2006 - Removed the method RemoveSelectedAction. The method
 * 						   GetSelectedMethod now additionally has the functionality 
 * 						   of the RemoveSelectedAction method
 * João Dias: 03/02/2007 - the Reset command now removes the selectAction if any
 * João Dias: 04/08/2007 - The intensity of attribution emotions (Love/Hate) were halved
 */

package FAtiMA.ReactiveComponent;


import java.io.File;
import java.io.Serializable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IBehaviourComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.ReactiveComponent.display.ActionTendenciesPanel;
import FAtiMA.ReactiveComponent.parsers.ReactiveLoaderHandler;


/**
 * Implements FearNot's Agent Reactive Layer (appraisal and coping processes)
 * @author João Dias
 */
public class ReactiveComponent implements Serializable, IComponent, IBehaviourComponent, IModelOfOtherComponent, IAppraisalDerivationComponent {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final long IGNOREDURATION = 30000;
	public static final String NAME = "Reactive";
	
	private ActionTendencies _actionTendencies;
	private EmotionalReactionTreeNode _emotionalReactions;
	
	/**
	 * Creates a new ReactiveProcess
	 */
	public ReactiveComponent() {
		this._actionTendencies = new ActionTendencies();
		this._emotionalReactions = new EmotionalReactionTreeNode(EmotionalReactionTreeNode.subjectNode);
	}
	
	@Override
	public void actionSelectedForExecution(ValuedAction action)
	{		
		/*
		 * Temporarily removes the action selected for execution. This means 
		 * that when a action is executed it should not be selected again for a while,
		 * or else we will have a character reacting in the same way several times
		 */
		_actionTendencies.IgnoreActionForDuration(action,IGNOREDURATION);
	}
	
	/**
	 * Reactive Coping. Consists in selecting the most relevant action (reaction)
	 * according to the emotional state.
	 */
	@Override
	public ValuedAction actionSelection(AgentModel am) {
		return _actionTendencies.SelectAction(am);
	}
	
	/**
	 * Adds an emotional Reaction to the agent's emotional reactions
	 * @param emotionalReaction - the Reaction to add
	 */
	public void AddEmotionalReaction(Reaction emotionalReaction) {
	    _emotionalReactions.AddEmotionalReaction(emotionalReaction);
	}
	
	
	/**
	 * Reactive appraisal. Appraises received events according to the emotional
	 * reaction rules
	 */
	@Override
	public void appraisal(AgentModel ag, Event event, AppraisalFrame af) {
		Reaction selfEvaluation;
			
		
		selfEvaluation = Evaluate(ag, event);
		
		if(selfEvaluation != null)
		{
			if(selfEvaluation._desirability != null)
			{
				af.SetAppraisalVariable(NAME, OCCAppraisalVariables.DESIRABILITY.name(), selfEvaluation._desirability.intValue());
			}
			if(selfEvaluation._desirabilityForOther != null && selfEvaluation._other != null)
			{
				af.SetAppraisalVariable(NAME,  OCCAppraisalVariables.DESFOROTHER.name() + selfEvaluation._other, selfEvaluation._desirabilityForOther.intValue());
			}
			if(selfEvaluation._praiseworthiness != null)
			{
				af.SetAppraisalVariable(NAME, OCCAppraisalVariables.PRAISEWORTHINESS.name(), selfEvaluation._praiseworthiness);
			}
			if(selfEvaluation._like != null)
			{
				af.SetAppraisalVariable(NAME, OCCAppraisalVariables.LIKE.name(), selfEvaluation._like.intValue());
			}
		}
	}
	
	
	
	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new ActionTendenciesPanel(this);
	}
	
	@Override
	public IComponent createModelOfOther() {
		ReactiveComponent reactive = new ReactiveComponent();
		reactive._actionTendencies = (ActionTendencies) this._actionTendencies.clone();
		reactive._emotionalReactions = (EmotionalReactionTreeNode) this._emotionalReactions.clone();
	
		reactive._actionTendencies.ClearFilters();
		
		return reactive;
	}
	
	/**
	 * Determines an answer to a SpeechAct according to the agent's emotional reactions
	 * @return the best answer to give according to emotional reactions
	 */
	/*
	public ValuedAction AnswerToSpeechAct(SpeechAct speechAct) {
		String answer;
		ActiveEmotion em;
		
		em = EmotionalState.GetInstance().GetStrongestEmotion(speechAct.toEvent());
		if(em != null) {
			if(em.GetValence() == EmotionValence.POSITIVE) {
				answer = "Reply(" + speechAct.getSender() + "," + speechAct.getMeaning() + ",positiveanswer)";
			}
			else {
				answer = "Reply(" + speechAct.getSender() + "," + speechAct.getMeaning() + ",negativeanswer)";
			}
			return new ValuedAction(Name.ParseName(answer),em);
		}
		return null;
	}*/
	
	public void EnforceCopingStrategy(String coping)
	{
		_actionTendencies.ReinforceActionTendency(coping);
	}
	
	public Reaction Evaluate(AgentModel am, Event event)
	{
		Reaction emotionalReaction;
		
		emotionalReaction = _emotionalReactions.MatchEvent(event);
		if(emotionalReaction != null)
		{
			emotionalReaction = (Reaction) emotionalReaction.clone();
			emotionalReaction.MakeGround(event.GenerateBindings());
		}
		
		return emotionalReaction;
	}
	
	/**
	 * Gets the agent's action tendencies
	 * @return the agent's ActionTendencies
	 */
	public ActionTendencies getActionTendencies() {
		return _actionTendencies;
	}
	
	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {};
		return dependencies;
	}

	/**
	 * Gets the agent's emotional reactions
	 * @return the root EmotionalReactionTreeNode that stores 
	 * 		   the emotional reaction rules
	 */
	public EmotionalReactionTreeNode getEmotionalReactions() {
		return _emotionalReactions;
	}

	@Override
	public void initialize(AgentModel am) {
		
		AgentLogger.GetInstance().log("Adding Reactive rules in the ReactiveComponent:");
		ReactiveLoaderHandler reactiveLoader = new ReactiveLoaderHandler(this);

		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			
			parser.parse(new File(ConfigurationManager.getPersonalityFile()), reactiveLoader);


		} catch (Exception e) {
			throw new RuntimeException(
					"Error reading the agent XML Files:" + e);
		}
	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
		Reaction r;
		float desirability = af.getAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name());
		float praiseworthiness = af.getAppraisalVariable(OCCAppraisalVariables.PRAISEWORTHINESS.name());
		
		if(desirability != 0 || praiseworthiness != 0)
		{
			
			r = new Reaction(af.getEvent());
			r.setDesirability(desirability);
			r.setPraiseworthiness(praiseworthiness);
			AddEmotionalReaction(r);
		}
	}
	
	@Override
	public String name() {
		return ReactiveComponent.NAME;
	}

	
	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
	}

	/**
	 * Resets the reactive layer, clearing all received events that
	 * were not appraised yet
	 */
	public void reset() {
	}

	/**
	 * prepares the reactive layer for a shutdown
	 */
	public void shutDown() {
	}

	@Override
	public void update(AgentModel am, Event e)
	{
	}
	
	@Override
	public void update(AgentModel am,long time) {
	}
}