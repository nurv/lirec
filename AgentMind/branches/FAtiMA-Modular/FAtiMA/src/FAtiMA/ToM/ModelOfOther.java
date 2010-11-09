package FAtiMA.ToM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.Display.AgentDisplay;
import FAtiMA.Core.deliberativeLayer.DeliberativeProcess;
import FAtiMA.Core.emotionalState.EmotionDisposition;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.memory.ICompoundCue;
import FAtiMA.Core.memory.ISpreadActivate;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.reactiveLayer.ActionTendencies;
import FAtiMA.Core.reactiveLayer.EmotionalReactionTreeNode;
import FAtiMA.Core.reactiveLayer.ReactiveProcess;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.motivationalSystem.Motivator;

public class ModelOfOther implements AgentModel, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _name;
	private EmotionalState _es;
	private Memory _mem;
	private HashMap<String,IComponent> _components;
	private ReactiveProcess _reactiveProcess;
	
	public ModelOfOther(String name, AgentCore ag) 
	{
		_name = name;
		_es = new EmotionalState();
		_mem = new Memory();
		_components = new HashMap<String,IComponent>();
		
		for(EmotionDisposition ed : ag.getEmotionalState().getEmotionDispositions())
		{
			_es.AddEmotionDisposition(ed);
		}		
	}

	public EmotionalState getEmotionalState() {
		return _es;
	}

	public Memory getMemory() {
		return _mem;
	}

	@Override
	public String getName() {
		return _name;
	}
	
	public void update()
	{
		for(IComponent c : _components.values())
		{
			c.update(this);
		}
	}
	
	public void update(Event e)
	{
		for(IComponent c : _components.values())
		{
			c.update(e,this);
		}
	}
	
	public void addComponent(IComponent c)
	{
		if(c.name().equals(ReactiveProcess.NAME))
		{
			_reactiveProcess = (ReactiveProcess) c;
		}
		_components.put(c.name(),c);
	}
	
	public IComponent getComponent(String name)
	{
		return _components.get(name);
	}
	
	public Collection<IComponent> getComponents()
	{
		return _components.values();
	}

	@Override
	public DeliberativeProcess getDeliberativeLayer() {
		return null;
	}

	@Override
	public ReactiveProcess getReactiveLayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AgentModel getModelToTest(Symbol ToM) {
		return null;
	}

}
