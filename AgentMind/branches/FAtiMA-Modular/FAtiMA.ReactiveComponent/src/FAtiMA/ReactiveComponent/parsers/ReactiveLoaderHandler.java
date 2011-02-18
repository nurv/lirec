package FAtiMA.ReactiveComponent.parsers;

import java.util.Locale;

import org.xml.sax.Attributes;

import FAtiMA.Core.conditions.EmotionCondition;
import FAtiMA.Core.conditions.MoodCondition;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.conditions.PredicateCondition;
import FAtiMA.Core.conditions.PropertyCondition;
import FAtiMA.Core.conditions.RecentEventCondition;
import FAtiMA.Core.emotionalState.ElicitingEmotion;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.Reaction;
import FAtiMA.ReactiveComponent.ReactiveComponent;

public class ReactiveLoaderHandler extends ReflectXMLHandler{
	
	private ReactiveComponent _reactiveComponent;
	protected Action _action;
  	protected ElicitingEmotion _elicitingEmotion;
    private Reaction _eventReaction;
    private Substitution _self;
	
    public ReactiveLoaderHandler(){
    	//this is just because of compatibility issues.
    	this._self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF));
   
    }
    
	public ReactiveLoaderHandler(ReactiveComponent reactiveComponent)
	{
		this._reactiveComponent = reactiveComponent;
		this._self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF));
	}
	
	public void ActionTendency(Attributes attributes) {
    	_action = new Action(Name.ParseName(attributes.getValue("action")));
    }
    
    public void CauseEvent(Attributes attributes) {
    	
    	String subject = attributes.getValue("subject");
		String action = attributes.getValue("action");
		String target = attributes.getValue("target");
		String parameters = attributes.getValue("parameters");
		
    	Event event = Event.ParseEvent(subject, action, target, parameters);
    	_elicitingEmotion.SetCause(event);
    }

    protected void parseElicitingEmotion(String emotionName, Integer minIntensity){
    	
    	//Just to prevent typing errors
    	if(emotionName.equalsIgnoreCase("happy-for")){
    		emotionName = "happy_for";
    	}
    	if(emotionName.equalsIgnoreCase("fears-confirmed")){
    		emotionName = "fears_confirmed";
		}
   
    	emotionName = emotionName.toUpperCase(Locale.ENGLISH);
 
    	_elicitingEmotion = new ElicitingEmotion(emotionName,minIntensity.intValue());
    	_action.SetElicitingEmotion(_elicitingEmotion);
    }
    
    public void ElicitingEmotion(Attributes attributes) throws InvalidEmotionTypeException {
    	parseElicitingEmotion(attributes.getValue("type"),new Integer(attributes.getValue("minIntensity")));
    	_reactiveComponent.getActionTendencies().AddAction(_action);
    }

    public void EmotionalReaction(Attributes attributes) {
    	Float desirability=null;
    	Float desirabilityForOther=null;
    	Float praiseworthiness=null;
    	Symbol other = null;
    	String aux;
    	
    	aux = attributes.getValue("desirability");
    	if(aux!=null) desirability = new Float(aux);
    	
    	aux = attributes.getValue("desirabilityForOther");
    	if(aux!=null) desirabilityForOther = new Float(aux);
    	
    	aux = attributes.getValue("praiseworthiness");
    	if(aux!=null) praiseworthiness = new Float(aux);
    	
    	aux = attributes.getValue("other");
    	if(aux!=null) other = new Symbol(aux);
    	
    	_eventReaction = new Reaction(desirability, desirabilityForOther, praiseworthiness, other);
    }
    
    public void Event(Attributes attributes) 
    {
    	String subject = attributes.getValue("subject");
		String action = attributes.getValue("action");
		String target = attributes.getValue("target");
		String parameters = attributes.getValue("parameters");
			
    	Event event = Event.ParseEvent(subject, action, target, parameters);
    	//this is a trick just to save time
    	event = event.ApplyPerspective(Constants.SELF);
     
    	_eventReaction.setEvent(event);
    	_reactiveComponent.getEmotionalReactions().AddEmotionalReaction(_eventReaction);
    }

    
    
    public void Predicate(Attributes attributes) 
    {
    	PredicateCondition cond;
    	
    	cond = PredicateCondition.ParsePredicate(attributes);
    	cond.MakeGround(_self);
    	_action.AddPreCondition(cond); 
    }

    public void Property(Attributes attributes) 
    {
      PropertyCondition cond;
      
      cond = PropertyCondition.ParseProperty(attributes);
      cond.MakeGround(_self);
      _action.AddPreCondition(cond);
    }
    
    public void RecentEvent(Attributes attributes)
    {
    	RecentEventCondition event;
    	
    	event = new RecentEventCondition(PastEventCondition.ParseEvent(attributes));
    	event.MakeGround(_self);
    	_action.AddPreCondition(event);
    }
    
    public void PastEvent(Attributes attributes)
    {
    	PastEventCondition event;
    	
    	event = PastEventCondition.ParseEvent(attributes);
    	event.MakeGround(_self);
    	_action.AddPreCondition(event);
    }
    
    public void EmotionCondition(Attributes attributes)
    {
    	EmotionCondition ec;
    	try
    	{
    		ec = EmotionCondition.ParseEmotionCondition(attributes);
        	ec.MakeGround(_self);
        	_action.AddPreCondition(ec);
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
        	_action.AddPreCondition(mc);
    	}
    	catch(Exception e) 
    	{
    		e.printStackTrace();
    	}
    }

}
