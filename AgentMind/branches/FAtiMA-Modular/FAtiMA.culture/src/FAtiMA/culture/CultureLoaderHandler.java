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

package FAtiMA.culture;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.EmotionCondition;
import FAtiMA.Core.conditions.MoodCondition;
import FAtiMA.Core.conditions.NewEventCondition;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.conditions.PredicateCondition;
import FAtiMA.Core.conditions.PropertyCondition;
import FAtiMA.Core.conditions.RecentEventCondition;
import FAtiMA.Core.deliberativeLayer.DeliberativeProcess;
import FAtiMA.Core.deliberativeLayer.goals.Goal;
import FAtiMA.Core.exceptions.ContextParsingException;
import FAtiMA.Core.exceptions.DuplicateSymbolTranslatorEntry;
import FAtiMA.Core.exceptions.InvalidDimensionTypeException;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.reactiveLayer.Reaction;
import FAtiMA.Core.reactiveLayer.ReactiveProcess;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.enumerables.CulturalDimensionType;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;


public class CultureLoaderHandler extends ReflectXMLHandler {

	private ArrayList<Ritual> _rituals;
	private Ritual _ritual = null;
	private String _conditionType;
	private Substitution _self;
	private AgentModel _am;
	private CulturalDimensionsComponent _culturalComponent;
	private String _currentGoalKey;
	
	ReactiveProcess _reactiveLayer;
	DeliberativeProcess _deliberativeLayer;
	

	private Context _contextBeingParsed;

	public CultureLoaderHandler(AgentModel aM, CulturalDimensionsComponent cDM) {
		_rituals = new ArrayList<Ritual>();
		_self = new Substitution(new Symbol("[SELF]"), new Symbol(FAtiMA.Core.util.Constants.SELF));
		_reactiveLayer = aM.getReactiveLayer(); 
		_deliberativeLayer = aM.getDeliberativeLayer();
		_am = aM;
		_culturalComponent = cDM;
		
		_currentGoalKey = null;
	}


	public ArrayList<Ritual> GetRituals(AgentModel am)
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

		_culturalComponent.setDimensionValue(dimensionType,value);
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
		Float desirability = new Float(0);
		Float desirabilityForOther = new Float(0);
		Symbol other = null;
		Float praiseworthiness = new Float(attributes.getValue("value"));
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
		_currentGoalKey = null;
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
		_currentGoalKey = attributes.getValue("name");
	}

	public void Motivator(Attributes attributes)
	{
		AgentLogger.GetInstance().logAndPrint("ERROR! The use of the keyword 'Motivator' on a goal's expected effects has been deprecated.");
		AgentLogger.GetInstance().logAndPrint("Instead use 'OnSelect' or 'OnIgnore' accordingly.");
		AgentLogger.GetInstance().close();
		System.exit(-1);
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
		}else if (_currentGoalKey != null){
			Goal g = _deliberativeLayer.getGoalLibrary().GetGoal(Name.ParseName(_currentGoalKey));
			g.AddCondition(_conditionType, cond);
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

	public void Context( Attributes attributes ){
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
