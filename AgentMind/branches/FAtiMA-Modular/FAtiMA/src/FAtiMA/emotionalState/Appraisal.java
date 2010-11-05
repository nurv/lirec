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
import FAtiMA.util.Constants;
import FAtiMA.util.enumerables.EmotionType;

import FAtiMA.wellFormedNames.Name;

public abstract class Appraisal {
	
	public static ArrayList<BaseEmotion> GenerateSelfEmotions(AgentModel am, Event event, AppraisalStructure vector)
	{
		ArrayList<BaseEmotion> emotions = new ArrayList<BaseEmotion>();
		float desirability;
		float praiseworthiness;
		float like;
		
		desirability = vector.getAppraisalVariable(AppraisalStructure.DESIRABILITY);
		praiseworthiness = vector.getAppraisalVariable(AppraisalStructure.PRAISEWORTHINESS);
		like = vector.getAppraisalVariable(AppraisalStructure.LIKE);
		
		
		if(like!=0)
		{
			emotions.add(OCCAppraiseAttribution(event, like));
		}
		
		//WellBeingEmotions: Joy, Distress
		if (desirability != 0) {
			emotions.add(OCCAppraiseWellBeing(event, desirability));
		}
		
		if (praiseworthiness != 0) {
			emotions.add(OCCAppraisePraiseworthiness(am.getName(), event, praiseworthiness));
		}
		
		return emotions;
	}
	
	public static AppraisalStructure InverseOCCAppraisal(BaseEmotion em, EmotionalState es)
	{
		//ignoring mood for now
		EmotionDisposition disposition = es._emotionDispositions[em.GetType()];
		
		int threshold = disposition.GetThreshold();
		float potentialValue = em.GetPotential() + threshold; 
		
		AppraisalStructure vector = new AppraisalStructure();
		
		if(em.GetType() == EmotionType.LOVE)
		{
			vector.setAppraisalVariable(AppraisalStructure.LIKE, potentialValue * 1.43f);  
		}
		else if(em.GetType() == EmotionType.HATE)
		{
			vector.setAppraisalVariable(AppraisalStructure.LIKE, -potentialValue * 1.43f);
		}
		else if(em.GetType() == EmotionType.JOY)
		{
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY, potentialValue);
		}
		else if(em.GetType() == EmotionType.DISTRESS)
		{
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY, -potentialValue);
		}
		else if(em.GetType() == EmotionType.PRIDE || em.GetType() == EmotionType.ADMIRATION)
		{
			vector.setAppraisalVariable(AppraisalStructure.PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.SHAME || em.GetType() == EmotionType.REPROACH)
		{
			vector.setAppraisalVariable(AppraisalStructure.PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.GLOATING)
		{
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY, potentialValue);
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, -potentialValue);
		}
		else if(em.GetType() == EmotionType.HAPPYFOR)
		{
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY, potentialValue);
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, potentialValue);
		}
		else if(em.GetType() == EmotionType.PITTY)
		{
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY, -potentialValue);
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, -potentialValue);
		}
		else if(em.GetType() == EmotionType.RESENTMENT)
		{
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY, -potentialValue);
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER, potentialValue);
		}
		else if(em.GetType() == EmotionType.GRATIFICATION || em.GetType() == EmotionType.GRATITUDE)
		{
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY, potentialValue);
			vector.setAppraisalVariable(AppraisalStructure.PRAISEWORTHINESS, potentialValue);
		}
		else if(em.GetType() == EmotionType.REGRET || em.GetType() == EmotionType.ANGER)
		{
			vector.setAppraisalVariable(AppraisalStructure.DESIRABILITY, -potentialValue);
			vector.setAppraisalVariable(AppraisalStructure.PRAISEWORTHINESS, -potentialValue);
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
	
	public static BaseEmotion GenerateEmotionForOther(AgentModel am, Event event, AppraisalStructure v, String other)
	{
		float desirabilityForOther = v.getAppraisalVariable(AppraisalStructure.DESIRABILITY_FOR_OTHER);
		float desirability = v.getAppraisalVariable(AppraisalStructure.DESIRABILITY);
		float targetBias = 0;
		float subjectBias = 0;
		float bias;
		float newDesirability = 0;
	
		
		String subject = event.GetSubject();
		
		if(desirabilityForOther == 0) return null;
		
		targetBias = LikeRelation.getRelation(Constants.SELF,other).getValue(am.getMemory()) * desirabilityForOther/10;
		bias = targetBias;
		if(!subject.equals(Constants.SELF))
		{
				subjectBias = LikeRelation.getRelation(Constants.SELF, subject).getValue(am.getMemory());
				bias = (bias + subjectBias)/2;
		}
		
		newDesirability = (desirability + bias)/2;

		
		return OCCAppraiseFortuneOfOthers(event, newDesirability, desirabilityForOther, other);
					
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
