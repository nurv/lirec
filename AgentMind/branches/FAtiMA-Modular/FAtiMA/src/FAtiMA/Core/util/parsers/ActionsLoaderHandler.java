/** 
 * StripsOperatorsLoaderHandler.java - Parses the planner operators/steps
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
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 17/01/2004 - File created
 * João Dias: 12/07/2006 - Replaced the deprecated Ground methods for the new ones
 * João Dias: 15/07/2006 - Removed the KnowledgeBase from the Class fields since the KB is now
 * 						   a singleton that can be used anywhere without previous references.
 * João Dias: 31/08/2006 - Added parsing for RecentEvents as conditions
 * João Dias: 21/09/2006 - An operator is now created with the "[Agent]" variable instead
 * 						   of the SELF's name. This way we can deal with actions performed
 * 						   by other agents in planning.
 * João Dias: 22/09/2006 - Added parsing for the Step's optional parameter probability.
 * João Dias: 25/09/2006 - Changed the special variable [Agent] to [AGENT]
 * João Dias: 03/10/2006 - Inference operators are additionally added to the KnowledgeBase
 * João Dias: 27/01/2007 - Changed the default probability of an action to be executed by another agent
 * 						   from 40% which was too high to 10%
 * João Dias: 10/02/2007 - Added parsing for MoodConditions  
 */

package FAtiMA.Core.util.parsers;

import java.util.ArrayList;
import java.util.ListIterator;


import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.EmotionCondition;
import FAtiMA.Core.conditions.MoodCondition;
import FAtiMA.Core.conditions.NewEventCondition;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.conditions.PredicateCondition;
import FAtiMA.Core.conditions.PropertyCondition;
import FAtiMA.Core.conditions.RecentEventCondition;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * @author João Dias
 *
 */
public class ActionsLoaderHandler extends ReflectXMLHandler {
	private Step _currentOperator;
	private ArrayList<Step> _operators; 
	private boolean _precondition;
	private float _probability;
	//private Substitution _self;
	private AgentModel _am;
	
	public ActionsLoaderHandler(AgentModel am) {
		_operators = new ArrayList<Step>();
		_precondition = true;
		_am = am;
		//_self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF));
	}
	
	
	public void Action(Attributes attributes) {
		Name action;
		String event;
		String firstName;
		String aux;
		float probability;
		action = Name.ParseName(attributes.getValue("name"));
		aux = attributes.getValue("probability");
		if(aux != null)
		{
			probability = Float.parseFloat(aux);
		}
		else
		{
			//default probability of an action being executed by another
			//agent
			probability = 0.1f;
		}
		_currentOperator = new Step(new Symbol("[AGENT]"),action,probability);
		_operators.add(_currentOperator);
		
		//action.MakeGround(this._self);
		if(action.toString().startsWith("Inference"))
		{
			//inference operator, we must add it to the KnowledgeBase
			if(_am!= null)
			{
				_am.getMemory().getSemanticMemory().AddInferenceOperator(_currentOperator);
			}
		}
		else
		{
			//adds the event effect
			ListIterator<Symbol> li = action.GetLiteralList().listIterator();
			firstName = li.next().toString();
			event = "EVENT([AGENT]," + firstName;
			while(li.hasNext()) {
			    event = event + "," + li.next();
			}
			event = event + ")";
			RecentEventCondition eventCondition = new RecentEventCondition(true,EventType.ACTION,ActionEvent.SUCCESS,Name.ParseName(event));
			_currentOperator.AddEffect(new Effect(_am, firstName,1.0f,eventCondition));
		}
	}
	
	public void Effect(Attributes attributes) {
		_probability = Float.parseFloat(attributes.getValue("probability"));
	}

	public void Effects(Attributes attributes) {
		  _precondition = false;
	}

	public void EmotionCondition(Attributes attributes)
	{
		EmotionCondition ec;
		try
		{
			ec = EmotionCondition.ParseEmotionCondition(attributes);
			//ec.MakeGround(_self);
			if(_precondition) 
			  	_currentOperator.AddPrecondition(ec);
			else {
			  	String operatorName = _currentOperator.getName().GetFirstLiteral().toString();
			  	_currentOperator.AddEffect(new Effect(_am, operatorName,_probability, ec));	
			}
		}
		catch(InvalidEmotionTypeException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @return
	 */
	public ArrayList<Step> getOperators() {
		return _operators;
	}
    
	public void MoodCondition(Attributes attributes)
    {
    	MoodCondition mc;
    	
    	try
    	{
    		mc = MoodCondition.ParseMoodCondition(attributes);
        	//mc.MakeGround(_self);
        	if(_precondition) 
    		  	_currentOperator.AddPrecondition(mc);
    		else {
    		  	String operatorName = _currentOperator.getName().GetFirstLiteral().toString();
    		  	_currentOperator.AddEffect(new Effect(_am, operatorName,_probability, mc));	
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    }
	
	public void NewEvent(Attributes attributes)
	{
		RecentEventCondition event;
		event = new NewEventCondition(PastEventCondition.ParseEvent(attributes));
		//event.MakeGround(_self);
		
		if(_precondition) 
		  	_currentOperator.AddPrecondition(event);
		else {
		  String operatorName = _currentOperator.getName().GetFirstLiteral().toString();
		  _currentOperator.AddEffect(new Effect(_am, operatorName,_probability, event));	
		}
	}
	
	public void PreConditions(Attributes attributes) {
		  _precondition = true;
	}
	
	public void Predicate(Attributes attributes) {
		PredicateCondition p;
		
		p = PredicateCondition.ParsePredicate(attributes);
	
		//p.MakeGround(this._self);
		
		if(_precondition) 
			_currentOperator.AddPrecondition(p);
		else {
			String operatorName = _currentOperator.getName().GetFirstLiteral().toString();
			_currentOperator.AddEffect(new Effect(_am, operatorName,_probability, p));
		}
	}
	
	public void Property(Attributes attributes) {
	  PropertyCondition p;
	  
	  p = PropertyCondition.ParseProperty(attributes);

	  if(_precondition) 
	  	_currentOperator.AddPrecondition(p);
	  else {
	  	String operatorName = _currentOperator.getName().GetFirstLiteral().toString();
	  	_currentOperator.AddEffect(new Effect(_am, operatorName,_probability, p));	
	  }
	}
	
	
	
	public void RecentEvent(Attributes attributes)
	{
		RecentEventCondition event;
		event = new RecentEventCondition(PastEventCondition.ParseEvent(attributes));
		//event.MakeGround(_self);
		
		if(_precondition) 
		  	_currentOperator.AddPrecondition(event);
		else {
		  String operatorName = _currentOperator.getName().GetFirstLiteral().toString();
		  _currentOperator.AddEffect(new Effect(_am, operatorName,_probability, event));	
		}
	}
}