/** 
 * Intention.java - Represents an explicit intention to achieve a goal
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
 * Created: 14/01/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 14/01/2004 - File created
 * João Dias: 24/05/2006 - Added comments to each public method's header
 * João Dias: 24/05/2006 - Removed the Intention's type from the class (was not being used)
 * João Dias: 10/07/2006 - the class is now serializable 
 * João Dias: 17/07/2007 - Instead of storing two instances of emotions (Hope and Fear),
 * 						   the class now stores only the hashkeys of such emotions in order
 * 						   to make the class easily serializable
 */
package FAtiMA.DeliberativeComponent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.plans.Plan;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.OCCAffectDerivation.OCCAffectDerivationComponent;


/**
 * Represents an explicit intention to achieve an ActivePursuitGoal
 * @author João Dias
 */
public class Intention implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final int MAXPLANS = 150;
	
	private String _fearEmotionID;
	private String _hopeEmotionID;
	private ActivePursuitGoal _goal;
	private ArrayList<Plan> _planConstruction;
	private Intention _subIntention = null;
	private Intention _parentIntention = null;
	private boolean _strongCommitment;
	private AppraisalFrame _appraisalFrame;


	/**
	 * Creates a new Intention to a achieve a goal
	 * @param p - the initial empty plan to achieve the goal
	 * @param g - the goal that the intention tries to achieve
	 * @see Plan
	 * @see Goal
	 */
	public Intention(AgentModel am, ActivePursuitGoal g) {
		_goal = g;
		_planConstruction = new ArrayList<Plan>();
		_fearEmotionID = null;
		_hopeEmotionID = null;
		_strongCommitment = false;
		_appraisalFrame = new AppraisalFrame(g.GetActivationEvent());
		_appraisalFrame.SetAppraisalVariable(DeliberativeComponent.NAME, (short) 7, OCCAppraisalVariables.GOALSTATUS.name(), OCCAffectDerivationComponent.GOALUNCONFIRMED);
	}
	
	
	
	/**
	 * Adds a plan to the set of alternative plans that the agent has
	 * to achieve the intention
	 * @param p - the Plan to add
	 * @see Plan
	 */
	public void AddPlan(Plan p) {
		
		//TODO get a sorted list here
		_planConstruction.add(p);
		
		/*if(_planConstruction.size() >= MAXPLANS)
		{
		}*/
	}
	
	public void AddPlans(ArrayList<Plan> plans)
	{
		ListIterator<Plan> li;
		Plan p;
		li = plans.listIterator();
		while(li.hasNext())
		{
			p = (Plan) li.next();
			_planConstruction.add(p);
		}
	}
	
	public void AddSubIntention(Intention i)
	{
		_subIntention = i;
		i.setMainIntention(this);
	}
	
	/*public boolean containsIntention(String goalName)
	{
		
		if(this._goal instanceof Ritual){
			if (this._goal.getNameWithCharactersOrdered().equalsIgnoreCase(goalName)) return true;
			if(this._subIntention != null)
			{
				return this._subIntention.containsIntention(goalName);
			}
			return false;
		}else{
			if (this._goal.getName().toString().equalsIgnoreCase(goalName)) return true;
			if(this._subIntention != null)
			{
				return this._subIntention.containsIntention(goalName);
			}
			return false;			
		}
	}*/
	
	public void RemoveSubIntention()
	{
		_subIntention = null;
	}
	
	public Intention GetSubIntention()
	{
		if(_subIntention != null)
		{
			return _subIntention.GetSubIntention();
		}
		else return this;
	}
	
	public AppraisalFrame getAppraisalFrame()
	{
		return _appraisalFrame; 
	}
	
	public void setMainIntention(Intention i)
	{
		this._parentIntention = i;
	}
	
	public boolean isRootIntention()
	{
		return this._parentIntention == null;
	}
	
	public Intention getParentIntention()
	{
		return this._parentIntention;
	}

	
	/**
	 * Gets the Fear emotion associated with the intention.
	 * This fear is caused by the prospect of failing to achieve the goal  
	 * @return - the Fear emotion
	 */
	public ActiveEmotion GetFear(EmotionalState es) {
		if(_fearEmotionID == null) return null;
		return es.GetEmotion(_fearEmotionID);
	}


	/**
	 * Gets the goal that intention corresponds to
	 * @return the intention's goal
	 */
	public ActivePursuitGoal getGoal() {
		return _goal;
	}
	
	/**
	 * Gets the Home emotion associated with the intention.
	 * This hope is caused by the prospect of succeeding in achieving the goal  
	 * @return - the Hope emotion
	 */
	public ActiveEmotion GetHope(EmotionalState es) {
		if(_hopeEmotionID == null) return null;
		return es.GetEmotion(_hopeEmotionID);
	}

	/**
	 * Gets the best plan developed so far to achieve the intention
	 * @return the best plan
	 */
	public Plan GetBestPlan(AgentModel am) {
		ListIterator<Plan> li;
		Plan p;
		Plan bestPlan = null;
		float minH = 9999999;

		li = _planConstruction.listIterator();
		while (li.hasNext()) {
			p = (Plan) li.next();
			if (p.h(am) < minH) {
				bestPlan = p;
				minH = p.h(am);
			}
		}
		return bestPlan;
	}
	
	/**
	 * Gets the likelihood of the agent achieving the intention
	 * @return a float value representing the probability [0;1]
	 */
	public float GetProbability(AgentModel am) {
		
		DeliberativeComponent dp = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		
		float p;
		float p2;
		float bestProb = 0;
		float goalProb = dp.getProbabilityStrategy().getProbability(am, _goal);
		
		for(Plan plan : _planConstruction)
		{
			p = plan.getProbability(am);
			if(plan.getOpenPreconditions().size() == 0)
			{
				p2 = p;
			}
			else
			{
				p2 = Math.min(goalProb, p); 
			}
			
			if (p2 > bestProb) bestProb = p2; 
		}
		
		return bestProb;
	}
	

	/**
	 * Gets the number of alternative plans that the agent has
	 * to achieve the intention
	 * @return
	 */
	public int NumberOfAlternativePlans() {
		return _planConstruction.size();
	}
	
	public void ResetPlans()
	{
		_planConstruction.clear();
		_subIntention = null;
		_strongCommitment = true;
	}

	/**
	 * Removes the last plan from the list of alternative plans
	 * @return the removed Plan
	 */
	public Plan RemovePlan() {
		return (Plan) _planConstruction.remove(_planConstruction.size() - 1);
	}
	
	/**
	 * Removes the received plan from the list of alternative plans
	 * @param p - the plan to remove
	 */
	public void RemovePlan(Plan p) {
		_planConstruction.remove(p);
	}
	
	/**
	 * Sets the Fear emotion associated with the intention.
	 * This fear is caused by the prospect of failling to achieve the goal
	 * @param fear - the Fear emotion to associate with the intention  
	 */
	public void SetFear(ActiveEmotion fear) {
		if(fear != null) _fearEmotionID = fear.GetHashKey();
	}
	
	/**
	 * Sets the Hope emotion associated with the intention.
	 * This hope is caused by the prospect of succeeding in achieving the goal
	 * @param hope - the hope emotion to associate with the intention  
	 */
	public void SetHope(ActiveEmotion hope) {
		if(hope!= null) _hopeEmotionID = hope.GetHashKey();
	}
	
	public boolean IsStrongCommitment()
	{
		return _strongCommitment;
	}
	
	public void SetStrongCommitment(AgentModel am)
	{
		if(!_strongCommitment)
		{
			_strongCommitment = true;
		}
	}
	
	/**
	 * Converts the intention to a String
	 * @return the converted String
	 */
	public String toString() {
		return "Intention: " + _goal;
	}
	
	/**
	 * Updates all the plans for the intention according to the new 
	 * state of the world. Supports continuous planning.
	 */
	public void CheckLinks(AgentModel am) {
	    ListIterator<Plan> li;
	    li = _planConstruction.listIterator();
	    
	    while(li.hasNext()) {
	        ((Plan) li.next()).UpdatePlan(am);
	    }
	    
	    if(this._subIntention != null)
	    {
	    	this._subIntention.CheckLinks(am);
	    }
	}
	
	/**
	 * Updates the probability of achieving the intention
	 * This function should be called whenever the plans change
	 */
	public void UpdateProbabilities() {
	    ListIterator<Plan> li;
	    li = _planConstruction.listIterator();
	    
	    while(li.hasNext()) {
	        ((Plan) li.next()).UpdateProbabilities();
	    }
	    
	    if(this._subIntention != null)
	    {
	    	this._subIntention.UpdateProbabilities();
	    }
	}
	
	/**
	 * Registers and appraises the activation of a given intention
	 * @param intention - the intention that was activated
	 */
	public void ProcessIntentionActivation(AgentModel am) 
	{
		DeliberativeComponent dp = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
	    Event e = _goal.GetActivationEvent();
	    
	    float goalConduciveness = dp.getUtilityStrategy().getUtility(am, _goal);
	    float probability = dp.getProbabilityStrategy().getProbability(am, _goal);
	    
	    AgentLogger.GetInstance().logAndPrint("Adding a new Intention: " + _goal.getName().toString());
	  
	    am.getMemory().getEpisodicMemory().StoreAction(am.getMemory(), e);
	    
	    _appraisalFrame.SetAppraisalVariable(DeliberativeComponent.NAME, (short)7, OCCAppraisalVariables.SUCCESSPROBABILITY.name(), probability);
	    _appraisalFrame.SetAppraisalVariable(DeliberativeComponent.NAME, (short)7, OCCAppraisalVariables.FAILUREPROBABILITY.name(), 1-probability);
	    _appraisalFrame.SetAppraisalVariable(DeliberativeComponent.NAME, (short)7, OCCAppraisalVariables.GOALCONDUCIVENESS.name(), goalConduciveness);
	    _appraisalFrame.SetAppraisalVariable(DeliberativeComponent.NAME, (short)7, OCCAppraisalVariables.GOALSTATUS.name(), OCCAffectDerivationComponent.GOALUNCONFIRMED);
	    
	    am.updateEmotions(_appraisalFrame);
		
		//SetHope(hope);
		//SetFear(fear);	
	}
	
	/**
	 * Registers and appraises the failure of this intention
	 */
	public void ProcessIntentionFailure(AgentModel am) 
	{	
		DeliberativeComponent dp = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		//mental disengagement consists in lowering the goal's importance
		_goal.DecreaseImportanceOfFailure(am, 0.5f);
		
	    Event e = _goal.GetFailureEvent();
	    
	    am.getMemory().getEpisodicMemory().StoreAction(am.getMemory(), e);
	    
	    AppraisalFrame af = new AppraisalFrame(e);
	    af.SetAppraisalVariable(DeliberativeComponent.NAME,(short)8,OCCAppraisalVariables.GOALSTATUS.name(), OCCAffectDerivationComponent.GOALDISCONFIRMED);
	    af.SetAppraisalVariable(DeliberativeComponent.NAME, (short)8,OCCAppraisalVariables.GOALCONDUCIVENESS.name(), dp.getUtilityStrategy().getUtility(am, _goal));
	    am.updateEmotions(af);
	    
	    if(!isRootIntention())
	    {
	    	AgentLogger.GetInstance().logAndPrint("Removing Parent Intention!: " + this.getParentIntention());
	    	getParentIntention().ProcessIntentionFailure(am);
	    	//getParentIntention().CheckLinks();
	    }
	    
	     AgentLogger.GetInstance().logAndPrint("Goal FAILED - " + _goal.getName().toString());	    
	}
	
	public void ProcessIntentionCancel(AgentModel am)
	{
		Event e = _goal.GetCancelEvent();
		am.getMemory().getEpisodicMemory().StoreAction(am.getMemory(), e);
		if(!isRootIntention())
	    {
	    	getParentIntention().CheckLinks(am);
	    }
	}
	
	/**
	 * Registers and appraises the success of the intention
	 */
	public void ProcessIntentionSuccess(AgentModel am) 
	{
		DeliberativeComponent dp = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
	    if(!isRootIntention())
	    {
	    	getParentIntention().CheckLinks(am);
	    }
	    
	    Event e = _goal.GetSuccessEvent();
	    am.getMemory().getEpisodicMemory().StoreAction(am.getMemory(), e);
	    AppraisalFrame af = new AppraisalFrame(e);
	    af.SetAppraisalVariable(DeliberativeComponent.NAME,(short)8,OCCAppraisalVariables.GOALSTATUS.name(), OCCAffectDerivationComponent.GOALCONFIRMED);
	    af.SetAppraisalVariable(DeliberativeComponent.NAME, (short)8,OCCAppraisalVariables.GOALCONDUCIVENESS.name(), dp.getUtilityStrategy().getUtility(am, _goal));
	    am.updateEmotions(af);
	        		   		
	    System.out.println("Goal SUCCESS - " + getGoal().getName());
	}

}