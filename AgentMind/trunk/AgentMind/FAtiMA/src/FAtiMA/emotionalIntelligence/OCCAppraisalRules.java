package FAtiMA.emotionalIntelligence;

import java.util.ArrayList;

import FAtiMA.AgentModel;
import FAtiMA.conditions.AppraisalCondition;
import FAtiMA.conditions.EmotionCondition;
import FAtiMA.conditions.NewEventCondition;

import FAtiMA.deliberativeLayer.plan.Effect;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.util.enumerables.EmotionType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;

public class OCCAppraisalRules {
	
	private final Step _joyOperator;
	private final Step _distressOperator;
	private ArrayList<Step> _appraisalOperators;
	
	public OCCAppraisalRules(AgentModel am)
	{	
		Effect aux;
		EmotionCondition c;
		AppraisalCondition appraisal;
		NewEventCondition event;
		ArrayList<Symbol> params;
		
		_appraisalOperators = new ArrayList<Step>();
		
		_joyOperator = new Step(new Symbol("[AGENT]"),Name.ParseName("JoyAppraisal()"),1.0f);
		c = new EmotionCondition(true, EmotionType.JOY);
		c.SetIntensity(new Symbol("[X]"));
		aux = new Effect(am, "JoyEmotion", 1.0f,c);
		_joyOperator.AddEffect(aux);
		
		
		params = new ArrayList<Symbol>();
		params.add(new Symbol("[p1]"));
		params.add(new Symbol("[p2]"));
		appraisal = new AppraisalCondition(new Symbol("[AGENT]"),
				"desirability", new Symbol("[X]"),(short)0,
				new Symbol("[s]"),
				new Symbol("[a]"),
				new Symbol("[t]"), params);
		_joyOperator.AddPrecondition(appraisal);
		
		event = new NewEventCondition(appraisal);
		_joyOperator.AddPrecondition(event);
		
		_appraisalOperators.add(_joyOperator);
		
		
		//distress
		_distressOperator = new Step(new Symbol("[AGENT]"),Name.ParseName("DistressAppraisal()"),1.0f);
		c = new EmotionCondition(true, EmotionType.DISTRESS);
		c.SetIntensity(new Symbol("[X]"));
		aux = new Effect(am, "DistressEmotion", 1.0f,c);
		_distressOperator.AddEffect(aux);
		
		
		params = new ArrayList<Symbol>();
		params.add(new Symbol("[p1]"));
		params.add(new Symbol("[p2]"));
		appraisal = new AppraisalCondition(new Symbol("[AGENT]"),
				"desirability", new Symbol("[X]"), (short)0,
				new Symbol("[s]"),
				new Symbol("[a]"),
				new Symbol("[t]"), params);
		
		event = new NewEventCondition(appraisal);
		
		_distressOperator.AddPrecondition(event);
		
		_appraisalOperators.add(_distressOperator);
		
	}
	
	public Step getJoyOperator()
	{
		return _joyOperator;
	}
	
	public Step getDistressOperator()
	{
		return _distressOperator;
	}
	
	public ArrayList<Step> getAppraisalOperators()
	{
		return _appraisalOperators;	
	}
	
	
	
}
