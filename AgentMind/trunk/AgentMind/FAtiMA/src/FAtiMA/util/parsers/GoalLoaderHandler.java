/** 
 * GoalLoaderHandler.java - Parses Interest and ActivePursuit Goals
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
 * Created: 17/01/2004 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 17/01/2004 - File created
 * Jo�o Dias: 12/07/2006 - Removed the reference to the KB from the class.
 * 						   It is no longer needed.
 * Jo�o Dias: 12/07/2006 - Replaced the deprecated Ground methods for the new ones
 * Jo�o Dias: 31/08/2006 - Added parsing for RecentEvents as conditions
 * 						 - Added parsing for PastEvents as conditions
 * Jo�o Dias: 06/09/2006 - Changed the name of Goal's success conditions from
 * 						   SucessCondition to SuccessCondition (it had a typo). However
 * 						   I've kept the old method so that files from FearNot! V1.0 can
 * 						   be parsed properly.
 * Jo�o Dias: 28/09/2006 - Added parsing for EmotionConditions
 * Jo�o Dias: 10/02/2007 - Added parsing for MoodConditions
 */

package FAtiMA.util.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.conditions.EmotionCondition;
import FAtiMA.conditions.LikeCondition;
import FAtiMA.conditions.MoodCondition;
import FAtiMA.conditions.NewEventCondition;
import FAtiMA.conditions.PastEventCondition;
import FAtiMA.conditions.PredicateCondition;
import FAtiMA.conditions.PropertyCondition;
import FAtiMA.conditions.RecentEventCondition;
import FAtiMA.conditions.RitualCondition;
import FAtiMA.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.deliberativeLayer.goals.Goal;
import FAtiMA.deliberativeLayer.goals.InterestGoal;
import FAtiMA.exceptions.InvalidEmotionTypeException;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;


public class GoalLoaderHandler extends ReflectXMLHandler {
    protected String _conditionType;
    private Goal _currentGoal;
    private ArrayList<Goal> _goals;
    private Substitution _self;

    public GoalLoaderHandler() {
      _goals = new ArrayList<Goal>();
      _self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF));
    }
    
    public void ActivePursuitGoal(Attributes attributes) {
    	Name description;
    	description = Name.ParseName(attributes.getValue("name"));
    	_currentGoal = new ActivePursuitGoal(description);
    	_goals.add(_currentGoal);
    }

    public ArrayList<Goal> GetGoals() {
      return _goals;
    }
    
    public void InterestGoal(Attributes attributes) {
    	Name description;
    	description = Name.ParseName(attributes.getValue("name"));
    	_currentGoal = new InterestGoal(description);
    	_goals.add(_currentGoal);
    }


    public void PreConditions(Attributes attributes) {
      _conditionType = "PreConditions";
    }
    
    public void RitualCondition(Attributes attributes)
	{
		RitualCondition cond;

		cond = RitualCondition.ParseRitualCondition(attributes);
		cond.MakeGround(this._self);

		cond.setRepeat(Boolean.parseBoolean(attributes.getValue("repeat")));
		
		_currentGoal.AddCondition(_conditionType, cond);	
	}
    
    public void Predicate(Attributes attributes) {
    	PredicateCondition cond;
    	
    	cond = PredicateCondition.ParsePredicate(attributes);
    	cond.MakeGround(this._self);
		_currentGoal.AddCondition(_conditionType, cond); 
    }

    public void Property(Attributes attributes) {
      PropertyCondition cond;
      
      cond = PropertyCondition.ParseProperty(attributes);
      cond.MakeGround(this._self);
      _currentGoal.AddCondition(_conditionType, cond);
    }
    
    public void RecentEvent(Attributes attributes)
    {
    	RecentEventCondition cond;
    	
    	cond = new RecentEventCondition(PastEventCondition.ParseEvent(attributes));
    	cond.MakeGround(this._self);
    	_currentGoal.AddCondition(_conditionType, cond);
    }
    
    public void NewEvent(Attributes attributes)
    {
    	NewEventCondition cond;
    	
    	cond = new NewEventCondition(PastEventCondition.ParseEvent(attributes));
    	cond.MakeGround(this._self);
    	_currentGoal.AddCondition(_conditionType, cond);
    }
    
    
    public void PastEvent(Attributes attributes)
    {
    	PastEventCondition event;
    	
    	event = PastEventCondition.ParseEvent(attributes);
    	event.MakeGround(this._self);
    	
    	//PastEvent can only be added as preconditions to Goals 
    	if(_conditionType.equals("PreConditions"))
    	{
    		_currentGoal.AddCondition(_conditionType,event);
    	}
    }
    
    public void EmotionCondition(Attributes attributes)
    {
    	EmotionCondition ec;
    	try
    	{
    		ec = EmotionCondition.ParseEmotionCondition(attributes);
        	ec.MakeGround(this._self);
        	_currentGoal.AddCondition(_conditionType, ec);
    	}
    	catch (InvalidEmotionTypeException e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public void MoodCondition(Attributes attributes)
    {
    	MoodCondition mc;
    	
    	try
    	{
    		mc = MoodCondition.ParseMoodCondition(attributes);
        	mc.MakeGround(_self);
        	_currentGoal.AddCondition(_conditionType, mc);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
    
    public void LikeRelation(Attributes attributes)
    {
    	LikeCondition lc;
    	
    	try
    	{
    		lc = LikeCondition.ParseSocialCondition(attributes);
    			
        	lc.MakeGround(_self);
        	_currentGoal.AddCondition(_conditionType, lc);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }

    public void SucessConditions(Attributes attributes)
    {
    	SuccessConditions(attributes);
    }
    
    public void SuccessConditions(Attributes attributes) {
      _conditionType = "SuccessConditions";
    }
    
    public void FailureConditions(Attributes attributes) {
      _conditionType = "FailureConditions";
    }
    
    
	public void Motivator(Attributes attributes)
	{
    	AgentLogger.GetInstance().logAndPrint("ERROR! The use of the keyword 'Motivator' on a goal's expected effects has been deprecated.");
    	AgentLogger.GetInstance().logAndPrint("Instead use 'OnSelect' or 'OnIgnore' accordingly.");
		System.exit(-1);
	}
    
    public void OnSelect(Attributes attributes)
	{
		String driveName = attributes.getValue("drive");
		String value = attributes.getValue("value");
		String target = attributes.getValue("target");

		if(driveName != null && _currentGoal != null){
			((ActivePursuitGoal)_currentGoal).SetExpectedEffectOnDrive("OnSelect", driveName, target, Float.parseFloat(value));					
		}
	}

	public void OnIgnore(Attributes attributes)
	{
		String driveName = attributes.getValue("drive");
		String value = attributes.getValue("value");
		String target = attributes.getValue("target");
		
		if(driveName != null && _currentGoal != null){
			((ActivePursuitGoal)_currentGoal).SetExpectedEffectOnDrive("OnIgnore", driveName, target, Float.parseFloat(value));					
		}
	}   
}