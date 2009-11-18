package FAtiMA;

import java.util.Collection;
import java.util.HashMap;

import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.memory.Memory;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.reactiveLayer.ActionTendencies;
import FAtiMA.reactiveLayer.EmotionalReactionTreeNode;
import FAtiMA.sensorEffector.Event;

public interface AgentModel {
	
	public String getName();
	
	public EmotionalState getEmotionalState();
	
	public Memory getMemory();
	
	public MotivationalState getMotivationalState();
	
	public ActionTendencies getActionTendencies();
	
	public EmotionalReactionTreeNode getEmotionalReactions();
	
	public HashMap<String,ModelOfOther> getToM();
	
	public Collection<String> getNearByAgents();
	
	public Collection<Event> getEvents();
	
	public void clearEvents();
}
