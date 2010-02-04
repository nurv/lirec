package FAtiMA.emotionalIntelligence;

import FAtiMA.AgentModel;
import FAtiMA.conditions.Condition;
import FAtiMA.conditions.EmotionCondition;
import FAtiMA.conditions.RecentEventCondition;
import FAtiMA.deliberativeLayer.plan.Effect;
import FAtiMA.deliberativeLayer.plan.Step;
import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.reactiveLayer.Action;
import FAtiMA.util.enumerables.ActionEvent;
import FAtiMA.util.enumerables.EventType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;

public abstract class ActionTendencyOperatorFactory {
	
	public static Step CreateATOperator(AgentModel am, Action at)
	{
		Step s = null;
		BaseEmotion em = at.GetElicitingEmotion();
		EmotionCondition ec = new EmotionCondition(true,em.GetType());
		ec.SetIntensity(em.GetPotential());
		
		s = new Step(new Symbol("[AGENT]"), at.getName(), 0.9f);
		
		for(Condition c : at.GetPreconditions())
		{
			s.AddPrecondition((Condition)c.clone());
		}
		
		s.AddPrecondition(ec);
		
		String eventName = "EVENT([AGENT]," + at.getName() + ")";
		RecentEventCondition ev = new RecentEventCondition(true,EventType.ACTION,ActionEvent.SUCCESS,Name.ParseName(eventName));
		
		s.AddEffect(new Effect(am, at.getName().toString(), 1.0f, ev));
		
		return s;
	}
}
