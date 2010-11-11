package FAtiMA.ToM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.IGetModelStrategy;
import FAtiMA.Core.deliberativeLayer.DeliberativeProcess;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.Appraisal;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionDisposition;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.reactiveLayer.ReactiveProcess;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.RemoteAgent;
import FAtiMA.Core.wellFormedNames.Symbol;

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
	private DeliberativeProcess _deliberativeProcess;
	
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
	
	public RemoteAgent getRemoteAgent()
	{
		return null;
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
	
	public void decay(long time)
	{
		_es.Decay();
		
		for(IComponent c : _components.values())
		{
			c.decay(time);
		}
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
	
	public void appraisal(Event e, AppraisalStructure as) 
	{
		ArrayList<BaseEmotion> emotions;
		ActiveEmotion activeEmotion;
		
		for(IComponent c : this._components.values())
		{
			c.appraisal(e,as,this);
		}

		emotions = Appraisal.GenerateEmotions(this, e, as);

		for(BaseEmotion em : emotions)
		{
			activeEmotion = _es.AddEmotion(em, this);
			if(activeEmotion != null)
			{
				for(IComponent c : this._components.values())
				{
					c.emotionActivation(e,activeEmotion,this);
				}
			}
		}
	}
	
	
	public void addComponent(IComponent c)
	{
		c.initialize(this);
		if(c.name().equals(ReactiveProcess.NAME))
		{
			_reactiveProcess = (ReactiveProcess) c;
		}
		if(c.name().equals(DeliberativeProcess.NAME))
		{
			_deliberativeProcess = (DeliberativeProcess) c;
			return;
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
		return _deliberativeProcess;
	}

	@Override
	public ReactiveProcess getReactiveLayer() {
		
		return _reactiveProcess;
	}

	@Override
	public AgentModel getModelToTest(Symbol ToM) {
		return null;
	}

	@Override
	public void setModelStrategy(IGetModelStrategy strat) {
	}

}
