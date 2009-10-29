package FAtiMA;

import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.memory.Memory;
import FAtiMA.memory.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.motivationalSystem.MotivationalState;

public interface AgentModel {
	
	public String getName();
	
	public EmotionalState getEmotionalState();
	
	public Memory getMemory();
	
	public MotivationalState getMotivationalState();
}
