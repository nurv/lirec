package FAtiMA.ToM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import FAtiMA.AgentCore;
import FAtiMA.AgentModel;
import FAtiMA.IComponent;
import FAtiMA.Display.AgentDisplay;
import FAtiMA.deliberativeLayer.DeliberativeProcess;
import FAtiMA.emotionalState.EmotionDisposition;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.memory.ICompoundCue;
import FAtiMA.memory.ISpreadActivate;
import FAtiMA.memory.Memory;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.motivationalSystem.Motivator;
import FAtiMA.reactiveLayer.ActionTendencies;
import FAtiMA.reactiveLayer.EmotionalReactionTreeNode;
import FAtiMA.reactiveLayer.ReactiveProcess;
import FAtiMA.sensorEffector.Event;
import FAtiMA.wellFormedNames.Symbol;

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
