package FAtiMA.emotionalIntelligence;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.EmotionCondition;
import FAtiMA.Core.conditions.NewEventCondition;
import FAtiMA.Core.emotionalState.EmotionDisposition;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.OCCAffectDerivation.OCCAffectDerivationComponent;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.OCCAffectDerivation.OCCEmotionType;


public abstract class OCCAppraisalRules {
	
	//private final Step _joyOperator;
	//private final Step _distressOperator;
	//private ArrayList<Step> _appraisalOperators;
	
	public static ArrayList<Step> GenerateOCCAppraisalRules(AgentModel am)
	{	
		Step joyOperator;
		Step distressOperator;
		ArrayList<Step> appraisalOperators;	
		EmotionDisposition disp;
		int threshold;
		Effect aux;
		EmotionCondition c;
		AppraisalCondition appraisal;
		NewEventCondition event;
		ArrayList<Symbol> params;
		
		appraisalOperators = new ArrayList<Step>();
		
		joyOperator = new Step(new Symbol("[AGENT]"),Name.ParseName("JoyAppraisal()"),1.0f);
		c = new EmotionCondition(true, new Symbol("[AGENT]"), OCCEmotionType.JOY.name());
		c.SetIntensity(new Symbol("[X]"));
		aux = new Effect(am, "JoyEmotion", 1.0f,c);
		joyOperator.AddEffect(aux);
		
		disp = am.getEmotionalState().getEmotionDisposition(OCCEmotionType.JOY.name());
		threshold = disp.getThreshold();
		
		params = new ArrayList<Symbol>();
		params.add(new Symbol("[p1]"));
		//params.add(new Symbol("[p2]"));
		appraisal = new AppraisalCondition(new Symbol("[AGENT]"),
				OCCAppraisalVariables.DESIRABILITY.name(), new Symbol("[X]"),
				threshold, 
				(short)0,
				new Symbol("[s]"),
				new Symbol("[a]"),
				new Symbol("[t]"), params);
		joyOperator.AddPrecondition(appraisal);
		
		Name ev = Name.ParseName("EVENT([s],[a],[t],[p1])");
		
		event = new NewEventCondition(true, EventType.ACTION,ActionEvent.SUCCESS,ev);
		joyOperator.AddPrecondition(event);
		
		appraisalOperators.add(joyOperator);
		
		
		//distress
		distressOperator = new Step(new Symbol("[AGENT]"),Name.ParseName("DistressAppraisal()"),1.0f);
		c = new EmotionCondition(true, new Symbol("[AGENT]"), OCCEmotionType.DISTRESS.name());
		c.SetIntensity(new Symbol("[X]"));
		aux = new Effect(am, "DistressEmotion", 1.0f,c);
		distressOperator.AddEffect(aux);
		
		disp = am.getEmotionalState().getEmotionDisposition(OCCEmotionType.DISTRESS.name());
		threshold = disp.getThreshold();
		
		params = new ArrayList<Symbol>();
		params.add(new Symbol("[p1]"));
		//params.add(new Symbol("[p2]"));
		appraisal = new AppraisalCondition(new Symbol("[AGENT]"),
				OCCAppraisalVariables.DESIRABILITY.name(), new Symbol("[X]"),
				threshold,
				(short)1,
				new Symbol("[s]"),
				new Symbol("[a]"),
				new Symbol("[t]"), params);
		
		distressOperator.AddPrecondition(appraisal);
		
		ev = Name.ParseName("EVENT([s],[a],[t],[p1])");
		
		event = new NewEventCondition(true, EventType.ACTION,ActionEvent.SUCCESS,ev);
		
		distressOperator.AddPrecondition(event);
		
		appraisalOperators.add(distressOperator);
		
		return appraisalOperators;
		
	}

	
	/*public Step getJoyOperator()
	{
		return _joyOperator;
	}*/
	
	/*public Step getDistressOperator()
	{
		return _distressOperator;
	}
	
	public ArrayList<Step> getAppraisalOperators()
	{
		return _appraisalOperators;	
	}*/
	
	
	
}
