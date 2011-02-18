package FAtiMA.ToM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import FAtiMA.Core.ActionLibrary;
import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IGetModelStrategy;
import FAtiMA.Core.componentTypes.IAffectDerivationComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IProcessEmotionComponent;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionDisposition;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.goals.GoalLibrary;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.RemoteAgent;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.ReactiveComponent.ReactiveComponent;

public class ModelOfOther implements AgentModel, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String _name;
	private EmotionalState _es;
	private Memory _mem;
	private HashMap<String,IComponent> _components;
	private ArrayList<IProcessEmotionComponent> _processEmotionComponents;
	private ArrayList<IAppraisalDerivationComponent> _appraisalComponents;
	private ArrayList<IAffectDerivationComponent> _affectDerivationComponents;
	private ReactiveComponent _reactiveComponent;
	
	public ModelOfOther(String name, AgentCore ag) 
	{
		_name = name;
		_es = new EmotionalState();
		_mem = new Memory();
		_components = new HashMap<String,IComponent>();
		_processEmotionComponents = new ArrayList<IProcessEmotionComponent>();
		_appraisalComponents = new ArrayList<IAppraisalDerivationComponent>();
		_affectDerivationComponents = new ArrayList<IAffectDerivationComponent>();
		
		for(EmotionDisposition ed : ag.getEmotionalState().getEmotionDispositions())
		{
			_es.AddEmotionDisposition(ed);
		}		
	}
	
	public boolean isSelf()
	{
		return false;
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
	
	public void update(long time)
	{
		_es.Decay();
		
		for(IComponent c : _components.values())
		{
			c.update(this,time);
		}
	}
	
	public void update(Event e)
	{
		_mem.getEpisodicMemory().StoreAction(_mem, e);
		_mem.getSemanticMemory().Tell(AgentCore.ACTION_CONTEXT, e.toName().toString());
		
		for(IAppraisalDerivationComponent c : _appraisalComponents)
		{
			c.update(this,e);
		}
		
		if(e.GetSubject().equals(Constants.SELF))
		{
			emotionReading(e);
		}
	}
	
	public void emotionReading(Event e)
	{
		
		BaseEmotion perceivedEmotion;
		ActiveEmotion predictedEmotion;
		AppraisalFrame af;
		//if the perceived action corresponds to an emotion expression of other, we 
		//should update its action tendencies accordingly
		perceivedEmotion = _reactiveComponent.getActionTendencies().RecognizeEmotion(this, e.toStepName());
		if(perceivedEmotion != null)
		{
			predictedEmotion = _es.GetEmotion(perceivedEmotion.GetHashKey());
			if(predictedEmotion == null)
			{
				//Agent model has to be null or the appraisal frame will generate emotions when we set the appraisal
				// variables
				af = new AppraisalFrame(perceivedEmotion.GetCause());
				
				for(IAffectDerivationComponent c : _affectDerivationComponents)
				{
					c.inverseAffectDerivation(this,perceivedEmotion,af);
				}
				
				//updating other's emotional state
				_es.AddEmotion(perceivedEmotion, this);
				
				for(IAppraisalDerivationComponent c : _appraisalComponents)
				{
					c.inverseAppraisal(this,af);
				}
			}
		}
	}
			
		
		
	
	public void appraisal(Event e, AppraisalFrame as) 
	{	
		for(IAppraisalDerivationComponent ac : _appraisalComponents)
		{
			ac.appraisal(this,e,as);
		}
	}
	
	
	public void addComponent(IComponent c)
	{
		if(c.name().equals(ReactiveComponent.NAME))
		{
			_reactiveComponent = (ReactiveComponent) c;
		}
		
		if(c instanceof IProcessEmotionComponent)
		{
			_processEmotionComponents.add((IProcessEmotionComponent)c);
		}
		
		if(c instanceof IAppraisalDerivationComponent)
		{
			_appraisalComponents.add((IAppraisalDerivationComponent) c);
		}
		if(c instanceof IAffectDerivationComponent)
		{
			_affectDerivationComponents.add((IAffectDerivationComponent) c);
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
	
	public GoalLibrary getGoalLibrary()
	{
		return null;
	}
	
	public ActionLibrary getActionLibrary()
	{
		return null;
	}

	@Override
	public AgentModel getModelToTest(Symbol ToM) {
		return null;
	}

	@Override
	public void setModelStrategy(IGetModelStrategy strat) {
	}

	@Override
	public void updateEmotions(AppraisalFrame af) {
		ArrayList<BaseEmotion> emotions;
		ActiveEmotion activeEmotion;
		
		if(af.hasChanged())
		{
			for(IAffectDerivationComponent ac : this._affectDerivationComponents)
			{	
				emotions = ac.affectDerivation(this, af);
				for(BaseEmotion em : emotions)
				{
					activeEmotion = _es.AddEmotion(em, this);
					if(activeEmotion != null)
					{
						for(IProcessEmotionComponent pec : this._processEmotionComponents)
						{
							pec.emotionActivation(this,activeEmotion);
						}
					}
				}
			}
		}	
	}

	@Override
	public EmotionalState simulateEmotionalState(Event ficticiousEvent,
			IComponent callingComponent) {
		return null;
	}

}
