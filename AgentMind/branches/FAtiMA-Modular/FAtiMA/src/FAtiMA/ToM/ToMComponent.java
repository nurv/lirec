package FAtiMA.ToM;

import java.util.ArrayList;
import java.util.HashMap;

import com.sun.org.apache.bcel.internal.generic.FADD;

import FAtiMA.AgentCore;
import FAtiMA.AgentModel;
import FAtiMA.IComponent;
import FAtiMA.IGetModelStrategy;
import FAtiMA.Display.AgentDisplayPanel;
import FAtiMA.emotionalState.ActiveEmotion;
import FAtiMA.emotionalState.AppraisalStructure;
import FAtiMA.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.sensorEffector.Event;
import FAtiMA.util.Constants;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Symbol;

public class ToMComponent implements IComponent, IGetModelStrategy {
	
	public static final String NAME = "ToM";
	
	protected String _name;
	protected HashMap<String,ModelOfOther> _ToM;
	protected ArrayList<String> _nearbyAgents;
	
	public ToMComponent(String name)
	{
		this._name = name;
		this._nearbyAgents = new ArrayList<String>();
	}
	
	private void addNearbyAgent(String agent)
	{
		if(!this._nearbyAgents.contains(agent))
		{
			this._nearbyAgents.add(agent);
		}
	}
	
	private boolean isPerson(AgentCore ag, String agent)
	{
		Name isPerson = Name.ParseName(agent + "(isPerson)");
		return ag.getMemory().getSemanticMemory().AskPredicate(isPerson);
	}
	
	public HashMap<String,ModelOfOther> getToM()
	{
		return this._ToM;
	}

	@Override
	public String name() {
		return ToMComponent.NAME;
	}

	@Override
	public void initialize(AgentCore ag) {
		ag.setModelStrategy(this);
		ag.getDeliberativeLayer().setDetectThreatStrategy(new DetectThreatStrategy());
	}
	
	private void initializeModelOfOther(AgentCore ag, String name)
	{
		if(!_ToM.containsKey(name))
		{
			ModelOfOther model = new ModelOfOther(name, ag);
			for(IComponent c : ag.getComponents())
			{
				IComponent componentOfOther = c.createModelOfOther();
				if(componentOfOther != null)
				{
					model.addComponent(componentOfOther);
				}
			}
			_ToM.put(name, model);
		}
	}
	
	public void RemoveNearByAgent(String entity)
	{
		this._nearbyAgents.remove(entity);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decay(long time) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(AgentModel am) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update(Event e, AgentModel am)
	{
	}

	@Override
	public void appraisal(Event e, AppraisalStructure as, AgentModel am) {
	}

	@Override
	public void emotionActivation(Event e, ActiveEmotion em, AgentModel am) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void coping(AgentModel am) {
		// TODO Auto-generated method stub
		
	}
	
	public AgentModel execute(Symbol ToM)
	{
		if(ToM.isGrounded() && !ToM.equals(Constants.UNIVERSAL) && !_ToM.toString().equals(Constants.SELF))
		{
			if(_ToM.containsKey(ToM.toString()))
			{
				return _ToM.get(ToM.toString());
			}
		}
		return null;
	}

	@Override
	public void propertyChangedPerception(String ToM, Name propertyName, String value) 
	{
		if(ToM.equals(Constants.UNIVERSAL.toString()))
		{
			for(String other : _nearbyAgents)
			{
				ModelOfOther m = _ToM.get(other);
				m.getMemory().getSemanticMemory().Tell(AgentCore.applyPerspective(propertyName,other), value);
			}
		}
		else if(!ToM.equals(_name))
		{
			ModelOfOther m = _ToM.get(ToM);
			if(m != null)
			{
				m.getMemory().getSemanticMemory().Tell(AgentCore.applyPerspective(propertyName,ToM), value);
			}
		}
		
	}

	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
		KnowledgeSlot knownInfo;
		KnowledgeSlot property;
		Name propertyName;
		
		if(subject.equals(Constants.SELF))
		{
			if(!target.equals(Constants.SELF))
			{
				if(isPerson(ag, target))
				{
					addNearbyAgent(target);
					initializeModelOfOther(ag, target);
				}
			}
			return;
		}
		
		for(String other : _nearbyAgents)
		{
			if(other.equals(subject))
			{
				ModelOfOther m = _ToM.get(other);
				knownInfo = ag.getMemory().getSemanticMemory().GetObjectDetails(target);
				if(knownInfo!= null)
				{
					for(String s : knownInfo.getKeys())
					{	
						property = knownInfo.get(s);
						propertyName = Name.ParseName(target + "(" + property.getName() + ")");
						m.getMemory().getSemanticMemory().Tell(AgentCore.applyPerspective(propertyName,other), property.getValue());
					}
				}		
			}	
		}
	}

	@Override
	public void entityRemovedPerception(String entity) {
		_nearbyAgents.remove(entity);
	}

	@Override
	public IComponent createModelOfOther() {
		return null;
	}

	@Override
	public AgentDisplayPanel createComponentDisplayPanel(AgentModel am) {
		return new ToMPanel(this);
	}
}
