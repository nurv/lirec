package FAtiMA.ToM;

import java.util.ArrayList;
import java.util.HashMap;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IComponent;
import FAtiMA.Core.IGetModelStrategy;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.deliberativeLayer.IGetUtilityForOthers;
import FAtiMA.Core.deliberativeLayer.goals.ActivePursuitGoal;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalStructure;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

public class ToMComponent implements IComponent, IGetModelStrategy, IGetUtilityForOthers {
	
	public static final String NAME = "ToM";
	
	protected String _name;
	protected HashMap<String,ModelOfOther> _ToM;
	protected ArrayList<String> _nearbyAgents;
	protected HashMap<Event,AppraisalStructure> _appraisalsOfOthers;
	
	
	public ToMComponent(String name)
	{
		this._name = name;
		this._nearbyAgents = new ArrayList<String>();
		this._appraisalsOfOthers = new HashMap<Event,AppraisalStructure>();
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
		
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			m.decay(time);
		}
	}

	@Override
	public void update(AgentModel am) {
		_appraisalsOfOthers.clear();
		
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			m.update();
		}		
	}
	
	@Override
	public void update(Event e, AgentModel am)
	{
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			m.update(e);
		}	
	}

	@Override
	public void appraisal(Event e, AppraisalStructure as, AgentModel am) {
		
		//one time appraisal for each event, so if the event was already appraised this cycle, just return
		if(_appraisalsOfOthers.containsKey(e))
		{
			return;
		}
		
		AppraisalStructure otherAS;
		
		for(String s : _nearbyAgents)
		{
			otherAS = new AppraisalStructure();
			ModelOfOther m = _ToM.get(s);
			m.appraisal(e, otherAS);
			
			as.SetAppraisalOfOther(s, otherAS);
		}
	}

	@Override
	public void emotionActivation(Event e, ActiveEmotion em, AgentModel am) {

	}

	@Override
	public void coping(AgentModel am) {
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
	public void processExternalRequest(String requestMsg) {		
	}
}
