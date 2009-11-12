package FAtiMA;

import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.memory.Memory;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.reactiveLayer.ActionTendencies;
import FAtiMA.reactiveLayer.EmotionalReactionTreeNode;

public class ModelOfOther implements AgentModel {
	
	private String _name;
	private EmotionalState _es;
	private Memory _mem;
	private MotivationalState _ms;
	private EmotionalReactionTreeNode _emotionalReactions;
	private ActionTendencies _actionTendencies;
	
	public ModelOfOther(String name, EmotionalReactionTreeNode reactions, ActionTendencies at)
	{
		_name = name;
		_es = new EmotionalState();
		_mem = new Memory();
		_ms = new MotivationalState();
		_emotionalReactions = (EmotionalReactionTreeNode) reactions.clone();
		_actionTendencies = (ActionTendencies) at.clone();
		_actionTendencies.ClearFilters();
		
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

}