package FAtiMA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import FAtiMA.emotionalState.EmotionDisposition;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.memory.ICompoundCue;
import FAtiMA.memory.ISpreadActivate;
import FAtiMA.memory.Memory;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.motivationalSystem.Motivator;
import FAtiMA.reactiveLayer.ActionTendencies;
import FAtiMA.reactiveLayer.EmotionalReactionTreeNode;
import FAtiMA.sensorEffector.Event;

public class ModelOfOther implements AgentModel {
	
	private ArrayList<Event> _perceivedEvents;
	private String _name;
	private EmotionalState _es;
	private Memory _mem;
	private MotivationalState _ms;
	private EmotionalReactionTreeNode _emotionalReactions;
	private ActionTendencies _actionTendencies;
	
	public ModelOfOther(String name, AgentModel am)
	{
		_name = name;
		_es = new EmotionalState();
		_mem = new Memory();
		_ms = new MotivationalState();
		
		for(Motivator m : am.getMotivationalState().getMotivators())
		{
			Motivator m2 = new Motivator(m);
			m2.SetIntensity(5);
			_ms.AddMotivator(m2);
		}
		
		for(EmotionDisposition ed : am.getEmotionalState().getEmotionDispositions())
		{
			_es.AddEmotionDisposition(ed);
		}
		
		
		_perceivedEvents = new ArrayList<Event>();
		_emotionalReactions = (EmotionalReactionTreeNode) am.getEmotionalReactions().clone();
		_actionTendencies = (ActionTendencies) am.getActionTendencies().clone();
		_actionTendencies.ClearFilters();
		
	}
	
	public void AddEvent(Event e)
	{
		_perceivedEvents.add(e);
	}
	
	public Collection<Event> getEvents()
	{
		return _perceivedEvents;
	}
	
	public void clearEvents()
	{
		_perceivedEvents.clear();
	}

	public EmotionalState getEmotionalState() {
		return _es;
	}

	public Memory getMemory() {
		return _mem;
	}

	public MotivationalState getMotivationalState() {
		return _ms;
	}

	@Override
	public String getName() {
		return _name;
	}
	
	public EmotionalReactionTreeNode getEmotionalReactions()
	{
		return _emotionalReactions;
	}
	
	public ActionTendencies getActionTendencies()
	{
		return _actionTendencies;
	}

	@Override
	public Collection<String> getNearByAgents() {
		return null;
	}

	@Override
	public HashMap<String, ModelOfOther> getToM() {
		return null;
	}

	@Override
	public ISpreadActivate getSpreadActivate() {
		return null;
	}

	@Override
	public ICompoundCue getCompoundCue() {
		return null;
	}

}
