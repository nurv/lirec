package FAtiMA.ToM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IGetModelStrategy;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAdvancedPerceptionsComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.Core.plans.Plan;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.DeliberativeComponent.strategies.IGetUtilityForOthers;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.ReactiveComponent.ReactiveComponent;

public class ToMComponent implements Serializable, IAppraisalDerivationComponent, IAdvancedPerceptionsComponent, IGetModelStrategy, IGetUtilityForOthers {
	
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
	
	private boolean isPerson(AgentCore ag, String agent)
	{
		Name isPerson = Name.ParseName(agent + "(isPerson)");
		return ag.getMemory().getSemanticMemory().AskPredicate(isPerson);
	}

	@Override
	public void actionFailedPerception(Event e) {
		// TODO Auto-generated method stub	
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
			otherAF = new AppraisalFrame(e3);
			m.appraisal(e3, otherAF);
			m.updateEmotions(otherAF);
			
			desirability = otherAF.getAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name());
			if(desirability != 0)
			{
				as.SetAppraisalVariable(NAME, 
						(short)7,
						OCCAppraisalVariables.DESFOROTHER.name()+s,
						otherAF.getAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name()));
			} 
		}
	}
	
	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new ToMPanel(this);
	}
	
	@Override
	public void entityRemovedPerception(String entity) {
		_nearbyAgents.remove(entity);
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
	public String[] getComponentDependencies() {
		String[] dependencies = {ReactiveComponent.NAME,DeliberativeComponent.NAME};
		return dependencies;
	}
	
	public HashMap<String,ModelOfOther> getToM()
	{
		return this._ToM;
	}

	@Override
	public float getUtilityForOthers(AgentModel am, ActivePursuitGoal g) {
		DeliberativeComponent dp = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		
		float utility = 0;
		
		for(ModelOfOther m : _ToM.values())
		{
			utility+= dp.getUtilityStrategy().getUtility(m, g);
		}
		
		return utility;
	}
	
	@Override
	public void initialize(AgentModel am) {
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		am.setModelStrategy(this);
		Plan.setDetectThreatStrategy(new DetectThreatStrategy());
		dc.setUtilityForOthersStrategy(this);
	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
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
	public String name() {
		return ToMComponent.NAME;
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
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
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
	public void update(AgentModel am,long time) {
		
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			m.update(time);
		}		
	}
}
