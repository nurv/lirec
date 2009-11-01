/** 
 * Appraisal.java - Static class that contains the appraisal methods 
 *  
 * Copyright (C) 2009 GAIPS/INESC-ID 
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
 * Created: 2009 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 */

package FAtiMA.emotionalState;

import java.util.ArrayList;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.goals.Goal;
import FAtiMA.sensorEffector.Event;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.util.enumerables.EmotionType;

import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;

public abstract class Appraisal {
	
	public static ArrayList<BaseEmotion> GenerateEmotions(AgentModel am, Event event, AppraisalVector vector, Symbol other)
	{
		ArrayList<BaseEmotion> emotions = new ArrayList<BaseEmotion>();
		float desirability;
		float desirabilityForOther;
		float praiseworthiness;
		float like;
		
		desirability = vector.getAppraisalVariable(AppraisalVector.DESIRABILITY);
		desirabilityForOther = vector.getAppraisalVariable(AppraisalVector.DESIRABILITY_FOR_OTHER);
		praiseworthiness = vector.getAppraisalVariable(AppraisalVector.PRAISEWORTHINESS);
		like = vector.getAppraisalVariable(AppraisalVector.LIKE);
		
		
		if(like!=0)
		{
			emotions.add(OCCAppraiseAttribution(event, like));
		}
		
		//WellBeingEmotions: Joy, Distress
		if (desirability != 0) {
			emotions.add(OCCAppraiseWellBeing(event, desirability));
				
			//FortuneOfOtherEmotions: HappyFor, Gloating, Resentment, Pitty
			if(desirabilityForOther != 0) {
				emotions.addAll(OCCAppraiseFortuneForAll(am, event, desirability, desirabilityForOther, other));
			}
		}
		
		if (praiseworthiness != 0) {
			emotions.add(OCCAppraisePraiseworthiness(am.getName(), event, praiseworthiness));
		}
		
		return emotions;
	}
	
	public static AppraisalVector InverseOCCAppraisal(BaseEmotion em, EmotionalState es)
	{
		//ignoring mood for now
		EmotionDisposition disposition = es._emotionDispositions[em.GetType()];
		
		int threshold = disposition.GetThreshold();
		float potentialValue = em.GetPotential() + threshold; 
		
		AppraisalVector vector = new AppraisalVector();
		
		if(em.GetType() == EmotionType.LOVE)
		{
			vector.setAppraisalVariable(AppraisalVector.LIKE, potentialValue * 1.43f);  
		}
		else if(em.GetType() == EmotionType.HATE)
		{
			vector.setAppraisalVariable(AppraisalVector.LIKE, -potentialValue * 1.43f);
		}
		else if(em.GetType() == EmotionType.JOY)
		{
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY, potentialValue);
		}
		else if(em.GetType() == EmotionType.DISTRESS)
		{
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY, -potentialValue);
		}
		else if(em.GetType() == EmotionType.PRIDE || em.GetType() == EmotionType.ADMIRATION)
		{
			vector.setAppraisalVariable(AppraisalVector.PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.SHAME || em.GetType() == EmotionType.REPROACH)
		{
			vector.setAppraisalVariable(AppraisalVector.PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.GLOATING)
		{
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY, potentialValue);
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY_FOR_OTHER, -potentialValue);
		}
		else if(em.GetType() == EmotionType.HAPPYFOR)
		{
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY, potentialValue);
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY_FOR_OTHER, potentialValue);
		}
		else if(em.GetType() == EmotionType.PITTY)
		{
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY, -potentialValue);
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY_FOR_OTHER, -potentialValue);
		}
		else if(em.GetType() == EmotionType.RESENTMENT)
		{
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY, -potentialValue);
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY_FOR_OTHER, potentialValue);
		}
		else if(em.GetType() == EmotionType.GRATIFICATION || em.GetType() == EmotionType.GRATITUDE)
		{
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY, potentialValue);
			vector.setAppraisalVariable(AppraisalVector.PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.REGRET || em.GetType() == EmotionType.ANGER)
		{
			vector.setAppraisalVariable(AppraisalVector.DESIRABILITY, -potentialValue);
			vector.setAppraisalVariable(AppraisalVector.PRAISEWORTHINESS, -potentialValue);
		}
		return vector;
		
	}
	
	private static BaseEmotion OCCAppraiseAttribution(Event event, float like)
	{
		BaseEmotion em;
		
		if(like >= 0) {
			em = new BaseEmotion(EmotionType.LOVE, like*0.7f, event, Name.ParseName(event.GetTarget()));
		}
		else {
			em = new BaseEmotion(EmotionType.HATE, -like*0.7f, event, Name.ParseName(event.GetTarget()));
		}
		
		return em;
	}
	
	private static BaseEmotion OCCAppraiseWellBeing(Event event, float desirability) {
		BaseEmotion em;
		
		if(desirability >= 0) {
			em = new BaseEmotion(EmotionType.JOY, desirability, event, null);
		}
		else {
			em = new BaseEmotion(EmotionType.DISTRESS, -desirability, event, null);
		}
		return em;
	}
	
	private static BaseEmotion OCCAppraisePraiseworthiness(String appraisingAgent, Event event, float praiseworthiness) {
		BaseEmotion em;
		
		if(praiseworthiness >= 0) {
			if(event.GetSubject().equals(appraisingAgent)) {
				em = new BaseEmotion(EmotionType.PRIDE, praiseworthiness, event, Name.ParseName("SELF"));
			}
			else {
				em = new BaseEmotion(EmotionType.ADMIRATION, praiseworthiness, event, Name.ParseName(event.GetSubject()));
			}
		}
		else {
			if(event.GetSubject().equals(appraisingAgent)) {
				em = new BaseEmotion(EmotionType.SHAME, -praiseworthiness, event, Name.ParseName("SELF"));
			}
			else {
				em = new BaseEmotion(EmotionType.REPROACH, -praiseworthiness, event, Name.ParseName(event.GetSubject()));
			}
		}
		
		return em;
	}
	
	private static ArrayList<BaseEmotion> OCCAppraiseFortuneForAll(AgentModel am, Event event, float desirability, float desirabilityForOther, Symbol other)
	{
		float targetBias = 0;
		float subjectBias = 0;
		float bias;
		float newDesirability = 0;
		ArrayList<BaseEmotion> emotions = new ArrayList<BaseEmotion>();
		
		String appraisingAgent = am.getName();
		String target = event.GetTarget();
		String subject = event.GetSubject();
		
		if(other != null && other.isGrounded())
		{
			target = other.toString();
		}		

				
		if(target != null && !target.equals(appraisingAgent))
		{	
			targetBias = LikeRelation.getRelation(appraisingAgent,event.GetTarget()).getValue(am.getMemory()) * desirabilityForOther/10;
			bias = targetBias;
			if(!subject.equals(appraisingAgent))
			{
				subjectBias = LikeRelation.getRelation(appraisingAgent, event.GetSubject()).getValue(am.getMemory());
				bias = (bias + subjectBias)/2;
			}
		}
		else 
		{
			subjectBias = LikeRelation.getRelation(appraisingAgent,event.GetSubject()).getValue(am.getMemory()) * desirabilityForOther/10;
			bias = subjectBias;
		}

		newDesirability = Math.round((desirability + bias)/2);

		if(target != null && !target.equals(appraisingAgent))
		{
			emotions.add(OCCAppraiseFortuneOfOthers(event, newDesirability, desirabilityForOther, target));
			
			if(!subject.equals(appraisingAgent))
			{
				emotions.add(OCCAppraiseFortuneOfOthers(event, newDesirability, 10, subject));
			}
		}
		else
		{
			emotions.add(OCCAppraiseFortuneOfOthers(event, newDesirability, desirabilityForOther, subject));
		}
		
		return emotions;
	}
	
	/**
	 * Appraises a Goal's Failure according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that failed
	 */
	public static BaseEmotion AppraiseGoalFailure(AgentModel am, ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, Goal g) {
		return AppraiseGoalEnd(EmotionType.DISAPPOINTMENT,EmotionType.FEARSCONFIRMED,hopeEmotion,fearEmotion,g.GetImportanceOfFailure(am),false, g);
	}
	
	/**
	 * Appraises a Goal's likelihood of failure
	 * @param g - the goal
	 * @param probability - the probability of the goal to fail
	 * @return - the emotion created
	 */
	public static BaseEmotion AppraiseGoalFailureProbability(AgentModel am, Goal g, float probability) {
		
		float potential;
		potential = probability * g.GetImportanceOfFailure(am);
		
		BaseEmotion em = new  BaseEmotion(EmotionType.FEAR, potential, g.GetActivationEvent(), null);
		
		return em;
		//return UpdateProspectEmotion(em);
	}

	/**
	 * Appraises a Goal's success according to the emotions that the agent is experiencing
	 * @param hopeEmotion - the emotion of Hope for achieving the goal that the character feels
	 * @param fearEmotion - the emotion of Fear for not achieving the goal that the character feels
	 * @param g - the Goal that succeeded
	 * @return - the emotion created
	 */
	public static BaseEmotion AppraiseGoalSuccess(AgentModel am, ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, Goal g) {
		return AppraiseGoalEnd(EmotionType.SATISFACTION,EmotionType.RELIEF,hopeEmotion,fearEmotion,g.GetImportanceOfSuccess(am),true, g);
	}

	/**
	 * Appraises a Goal's likelihood of succeeding
	 * @param g - the goal
	 * @param probability - the probability of the goal to succeed
	 * @return - the BaseEmotion created
	 */
	public static BaseEmotion AppraiseGoalSuccessProbability(AgentModel am, Goal g, float probability) {
	
		float potential;
		potential = probability * g.GetImportanceOfSuccess(am);
		
		BaseEmotion em = new BaseEmotion(EmotionType.HOPE, potential, g.GetActivationEvent(), null);
	
		return em;
		//return UpdateProspectEmotion(em);
	}
	
	private static BaseEmotion OCCAppraiseFortuneOfOthers(Event event, float desirability, float desirabilityForOther, String target) {
		BaseEmotion em;
		float potential;
		
		potential = (Math.abs(desirabilityForOther) + Math.abs(desirability)) / 2.0f;
		
		if(desirability >= 0) {
			if(desirabilityForOther >= 0) {
				em = new BaseEmotion(EmotionType.HAPPYFOR, potential, event, Name.ParseName(target));	
			}
			else {
				em = new BaseEmotion(EmotionType.GLOATING, potential, event, Name.ParseName(target));
			}
		}
		else {
			if(desirabilityForOther >= 0) {
				em = new BaseEmotion(EmotionType.RESENTMENT, potential, event, Name.ParseName(target));
			}
			else {
				em = new BaseEmotion(EmotionType.PITTY, potential, event, Name.ParseName(target));
			}
		}
		
		return em;
	}
	
	private static BaseEmotion AppraiseGoalEnd(short hopefullOutcome, short fearfullOutcome, 
			ActiveEmotion hopeEmotion, ActiveEmotion fearEmotion, float goalImportance, boolean succeded, Goal g) {
	
		short finalEmotion;
		float potential = 0;
		
		if(hopeEmotion != null) {
			if(fearEmotion != null && fearEmotion._intensity > hopeEmotion._intensity) {
				potential = fearEmotion.GetPotential();
				finalEmotion = fearfullOutcome;
			}
			else {
				potential = hopeEmotion.GetPotential();
				finalEmotion = hopefullOutcome;
			}
		}
		else if(fearEmotion != null) {
			potential = fearEmotion.GetPotential();
			finalEmotion = fearfullOutcome;
		}
		else return null;
		
		//RemoveEmotion(fearEmotion);
		//RemoveEmotion(hopeEmotion);
		
		//Change, the goal importance now affects 66% of the final potential value for the emotion
		potential = (potential +  2* goalImportance) / 3;
		Event e;
		if(succeded) 
		{
			e = g.GetSuccessEvent();
		}
		else
		{
			e = g.GetFailureEvent();
		}
		
		return new BaseEmotion(finalEmotion, potential, e, null);
		
		//this.AddEmotion(new BaseEmotion(finalEmotion, potential, e, null));
	}


}
