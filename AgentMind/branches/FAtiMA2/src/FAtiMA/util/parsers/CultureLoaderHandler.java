/** 
 * CultureLoaderHandler.java - Parses the agent's culture and rituals
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
 * Created: 12/03/2008 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 12/03/2008 - File created  
 */

package FAtiMA.util.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.AgentModel;
import FAtiMA.conditions.Context;
import FAtiMA.conditions.EmotionCondition;
import FAtiMA.conditions.MoodCondition;
import FAtiMA.conditions.NewEventCondition;
import FAtiMA.conditions.PastEventCondition;
import FAtiMA.conditions.PlaceCondition;
import FAtiMA.conditions.PredicateCondition;
import FAtiMA.conditions.PropertyCondition;
import FAtiMA.conditions.RecentEventCondition;
import FAtiMA.conditions.RitualCondition;
import FAtiMA.conditions.SocialCondition;
import FAtiMA.conditions.TimeCondition;
import FAtiMA.culture.CulturalDimensions;
import FAtiMA.culture.Ritual;
import FAtiMA.culture.SymbolTranslator;
import FAtiMA.deliberativeLayer.DeliberativeProcess;
import FAtiMA.exceptions.ContextParsingException;
import FAtiMA.exceptions.DuplicateSymbolTranslatorEntry;
import FAtiMA.exceptions.InvalidDimensionTypeException;
import FAtiMA.exceptions.InvalidEmotionTypeException;
import FAtiMA.exceptions.UnknownGoalException;
import FAtiMA.reactiveLayer.Reaction;
import FAtiMA.reactiveLayer.ReactiveProcess;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.enumerables.CulturalDimensionType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;


public class CultureLoaderHandler extends ReflectXMLHandler {

	private ArrayList _rituals;
	private ArrayList _culturalGoals;
	private Ritual _ritual = null;
	private String _conditionType;
	private Substitution _self;
	private AgentModel _am;
	
	ReactiveProcess _reactiveLayer;
	DeliberativeProcess _deliberativeLayer;
	

	private Context _contextBeingParsed;

	public CultureLoaderHandler(AgentModel am, ReactiveProcess reactiveLayer, DeliberativeProcess deliberativeLayer) {
		_rituals = new ArrayList();
		_culturalGoals = new ArrayList();
		_self = new Substitution(new Symbol("[SELF]"), new Symbol("SELF"));
		_reactiveLayer = reactiveLayer;
		_deliberativeLayer = deliberativeLayer;
		_am = am;
		
	}


	public ArrayList GetRituals(AgentModel am)
	{
		for(int i=0;i < _rituals.size();i++)
		{
			((Ritual)_rituals.get(i)).BuildPlan(am);
		}

		return _rituals;
	}


	public void CulturalDimension(Attributes attributes){
		short dimensionType = -1;
		String name;
		int value;

		name = attributes.getValue("name");
		value = Integer.parseInt(attributes.getValue("value"));

		try {
			dimensionType = CulturalDimensionType.ParseType(name);
		} catch (InvalidDimensionTypeException e) {
			e.printStackTrace();
		}

		CulturalDimensions.GetInstance().setDimensionValue(dimensionType,value);
	}

	/**
	 * @author Samuel
	 * This method parses the praiseworthiness of specific cultural actions
	 * It creates an Emotional Reaction Rule to all events that refer to that specific action
	 * regardless of who did the action or who/what was the target of the action
	 */
	public void Action(Attributes attributes) {	

		//Create the event
		String subject = "*";
		String target = "*";
		String action = attributes.getValue("name");
		String parameters = attributes.getValue("parameters");
		Event event = Event.ParseEvent(subject, action, target, parameters);

		//Create the reaction
		Integer desirability = new Integer(0);
		Integer desirabilityForOther = new Integer(0);
		Symbol other = null;
		Integer praiseworthiness = new Integer(attributes.getValue("value"));
		Reaction _eventReaction = new Reaction(desirability, desirabilityForOther, praiseworthiness, other);
		_eventReaction.setEvent(event);

		//Add the emotional reaction to the reactive layer
		_reactiveLayer.getEmotionalReactions().AddEmotionalReaction(_eventReaction);
	}


	public void Ritual(Attributes attributes) {
		Name description;
		description = new Symbol(attributes.getValue("name"));
		_ritual = new Ritual(description);
		_rituals.add(_ritual);
		_conditionType = "PreConditions";
	}

	public void Symbol(Attributes attributes)  {
		try{
			SymbolTranslator.GetInstance().addEntry(attributes.getValue("name"),
					attributes.getValue("meaning"));	
		}catch(DuplicateSymbolTranslatorEntry e){
			System.err.println("Exception: " + e.getClass() + " Msg: " + e.getMessage());
			System.err.println("Correct the XML culture file!");
			System.exit(1);
		}
	}
	
	public void Goal(Attributes attributes) throws UnknownGoalException 
    {
      String goalName = attributes.getValue("name");	
      _deliberativeLayer.AddGoal(_am, goalName); 
    }

	public void PreConditions(Attributes attributes)
	{
		_conditionType = "PreConditions";
	}

	public void SucessConditions(Attributes attributes)
	{
		_conditionType ="SuccessConditions";
	}

	
	public void ActivePursuitGoal(Attributes attributes)
	{
		AgentLogger.GetInstance().logAndPrint("ERROR! Culture file has an 'ActivePursuitGoal' defined in it.");
		System.exit(-1);
	}

	public void Motivator(Attributes attributes)
	{
		AgentLogger.GetInstance().logAndPrint("ERROR! The use of the keyword 'Motivator' on a goal's expected effects has been deprecated.");
		AgentLogger.GetInstance().logAndPrint("Instead use 'OnSelect' or 'OnIgnore' accordingly.");
		AgentLogger.GetInstance().close();
		System.exit(-1);
	}
	
	
	public void OnSelect(Attributes attributes)
	{
		String driveName = attributes.getValue("drive");
		String value = attributes.getValue("value");
		String target = attributes.getValue("target");

		if(driveName != null && _ritual != null){
			_ritual.SetExpectedEffectOnDrive("OnSelect", driveName, target, Float.parseFloat(value));					
		}
	}

	public void OnIgnore(Attributes attributes)
	{
		String driveName = attributes.getValue("drive");
		String value = attributes.getValue("value");
		String target = attributes.getValue("target");

		if(driveName != null && _ritual != null){
			_ritual.SetExpectedEffectOnDrive("OnIgnore", driveName, target, Float.parseFloat(value));					
		}
	}

	public void Role(Attributes attributes) {
		Symbol role;
		role = new Symbol(attributes.getValue("name"));
		_ritual.AddRole(role);
	}

	public void Step(Attributes attributes)
	{
		Name stepName;
		Name role;

		stepName = Name.ParseName(attributes.getValue("name"));
		role = new Symbol(attributes.getValue("role"));
		_ritual.AddStep(stepName, role);
	}

	public void Link(Attributes attributes)
	{
		int before;
		int after;

		before = Integer.parseInt(attributes.getValue("before"));
		after = Integer.parseInt(attributes.getValue("after"));

		_ritual.AddLink(before, after);

	}

	public void Predicate(Attributes attributes) {
		PredicateCondition cond;

		cond = PredicateCondition.ParsePredicate(attributes);
		cond.MakeGround(this._self);

		if(_ritual != null)
		{
			_ritual.AddCondition(_conditionType, cond);
		}
	}

	public void Property(Attributes attributes) {
		PropertyCondition cond;

		cond = PropertyCondition.ParseProperty(attributes);
		cond.MakeGround(this._self);

		if(_ritual != null)
		{	
			_ritual.AddCondition(_conditionType, cond);
		}
	}

	public void RitualCondition(Attributes attributes)
	{
		RitualCondition cond;

		cond = RitualCondition.ParseRitualCondition(attributes);

		cond.setRepeat(Boolean.parseBoolean(attributes.getValue("repeat")));
		if(_ritual != null)
		{	
			_ritual.AddCondition(_conditionType, cond);
		}
	}


	public void NewEvent(Attributes attributes)
	{
		NewEventCondition cond;

		cond = new NewEventCondition(PastEventCondition.ParseEvent(attributes));
		cond.MakeGround(this._self);


		if(_ritual != null)
		{	
			_ritual.AddCondition(_conditionType, cond);
		}
	}

	public void RecentEvent(Attributes attributes)
	{
		RecentEventCondition cond;

		cond = new RecentEventCondition(PastEventCondition.ParseEvent(attributes));
		cond.MakeGround(this._self);

		if( _ritual != null)
		{	
			_ritual.AddCondition(_conditionType, cond);
		}
	}

	public void PastEvent(Attributes attributes)
	{
		PastEventCondition event;

		event = PastEventCondition.ParseEvent(attributes);
		event.MakeGround(this._self);

		if(_ritual != null)
		{	
			_ritual.AddCondition(_conditionType, event);
		}
	}

	public void EmotionCondition(Attributes attributes)
	{
		EmotionCondition ec;
		try
		{
			ec = EmotionCondition.ParseEmotionCondition(attributes);
			ec.MakeGround(this._self);
			if(_ritual != null)
			{	
				_ritual.AddCondition(_conditionType, ec);
			}
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

			if(_ritual != null)
			{	
				_ritual.AddCondition(_conditionType, mc);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	// To read context
	public void Context( Attributes attributes ){
		//if( attributes != null ) TODO check if they exist? should be null...
		_contextBeingParsed = new Context();
		if(_ritual != null)
		{
			_ritual.AddCondition(_conditionType, _contextBeingParsed);
		}
	}

	public void Time( Attributes attributes ) throws ContextParsingException{
		if( _contextBeingParsed == null )
			throw new ContextParsingException("Trying to parse a TimeCondition outside of a Context");

		_contextBeingParsed.SetTimeCondition( TimeCondition.Parse(attributes) );
	}

	public void Place(Attributes attributes ) throws ContextParsingException{
		if( _contextBeingParsed == null )
			throw new ContextParsingException("Trying to parse a PlaceCondition outside of a Context");

		_contextBeingParsed.SetPlaceCondition( PlaceCondition.Parse(attributes) );
	}

	public void Social(Attributes attributes ) throws ContextParsingException{
		if( _contextBeingParsed == null )
			throw new ContextParsingException("Trying to parse a SocialCondition outside of a Context");

		_contextBeingParsed.AddSocialCondition( SocialCondition.Parse(attributes) );
	}
}
