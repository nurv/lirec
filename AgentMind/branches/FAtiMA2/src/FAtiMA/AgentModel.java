package FAtiMA;

import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.memory.Memory;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.reactiveLayer.ActionTendencies;

public interface AgentModel {
	
	public String getName();
	
	public EmotionalState getEmotionalState();
	
	public Memory getMemory();
	
	public MotivationalState getMotivationalState();
	
	public ActionTendencies getActionTendencies();
}
