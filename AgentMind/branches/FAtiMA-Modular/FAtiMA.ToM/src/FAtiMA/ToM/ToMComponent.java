package FAtiMA.ToM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IAppraisalComponent;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.IGetModelStrategy;
import FAtiMA.Core.IModelOfOtherComponent;
import FAtiMA.Core.IProcessPerceptionsComponent;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.OCCAffectDerivation.OCCComponent;
import FAtiMA.Core.deliberativeLayer.IGetUtilityForOthers;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

public class ToMComponent implements Serializable, IAppraisalComponent, IProcessPerceptionsComponent, IGetModelStrategy, IGetUtilityForOthers {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "ToM";
	
	protected String _name;
	protected HashMap<String,ModelOfOther> _ToM;
	protected ArrayList<String> _nearbyAgents;
	
	
	public ToMComponent(String agentName)
	{
		this._name = agentName;
		this._nearbyAgents = new ArrayList<String>();
		this._ToM = new HashMap<String,ModelOfOther>();
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
	public void initialize(AgentModel am) {
		am.setModelStrategy(this);
		am.getDeliberativeLayer().setDetectThreatStrategy(new DetectThreatStrategy());
		am.getDeliberativeLayer().setUtilityForOthersStrategy(this);
	}
	
	private void initializeModelOfOther(AgentCore ag, String name)
	{
		if(!_ToM.containsKey(name))
		{
			ModelOfOther model = new ModelOfOther(name, ag);
			for(IComponent c : ag.getComponents())
			{
				if(c instanceof IModelOfOtherComponent)
				{
					IComponent componentOfOther = ((IModelOfOtherComponent)c).createModelOfOther();
					if(componentOfOther != null)
					{
						model.addComponent(componentOfOther);
					}
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
	public void update(AgentModel am,long time) {
		
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			m.update(time);
		}		
	}
	
	@Override
	public void update(AgentModel am, Event e)
	{
		Event e2 = e.RemovePerspective(_name);
		Event e3;
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			e3 = e2.ApplyPerspective(s);
			m.update(e3);
		}		
	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame as) {
		
		Event e2 = e.RemovePerspective(_name);
		Event e3;
		float desirability;
		
		AppraisalFrame otherAF;
		
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			e3 = e2.ApplyPerspective(s);
			otherAF = new AppraisalFrame(m,e3);
			m.appraisal(e3, otherAF);
			
			desirability = otherAF.getAppraisalVariable(OCCComponent.DESIRABILITY);
			if(desirability != 0)
			{
				as.SetAppraisalVariable(NAME, 
						(short)7,
						OCCComponent.DESFOROTHER+s,
						otherAF.getAppraisalVariable(OCCComponent.DESIRABILITY));
			} 
		}
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
		Name propertyName2 = AgentCore.removePerspective(propertyName, _name);
		
		if(ToM.equals(Constants.UNIVERSAL.toString()))
		{
			for(String other : _nearbyAgents)
			{
				ModelOfOther m = _ToM.get(other);
				m.getMemory().getSemanticMemory().Tell(AgentCore.applyPerspective(propertyName2,other), value);
			}
		}
		else if(!ToM.equals(_name))
		{
			ModelOfOther m = _ToM.get(ToM);
			if(m != null)
			{
				m.getMemory().getSemanticMemory().Tell(AgentCore.applyPerspective(propertyName2,ToM), value);
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
					initializeModelOfOther(ag, target);
					addNearbyAgent(target);
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
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new ToMPanel(this);
	}

	@Override
	public float getUtilityForOthers(AgentModel am, ActivePursuitGoal g) {
		
		float utility = 0;
		
		for(ModelOfOther m : _ToM.values())
		{
			utility+= m.getDeliberativeLayer().getUtilityStrategy().getUtility(m, g);
		}
		
		return utility;
	}

	@Override
	public void reappraisal(AgentModel am) {
	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
	}
}
