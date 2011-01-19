package FAtiMA.emotionalIntelligence;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.conditions.EmotionCondition;
import FAtiMA.Core.conditions.RecentEventCondition;
import FAtiMA.Core.deliberativeLayer.plan.Effect;
import FAtiMA.Core.deliberativeLayer.plan.Step;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.reactiveLayer.Action;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

public abstract class ActionTendencyOperatorFactory {
	
	public static Step CreateATOperator(AgentModel am, Action at)
	{
		Step s = null;
		BaseEmotion em = at.GetElicitingEmotion();
		EmotionCondition ec = new EmotionCondition(true,new Symbol("[AGENT]"),em.getType().getName());
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
