package FAtiMA;

import java.util.Collection;
import java.util.HashMap;

import FAtiMA.Display.AgentDisplay;
import FAtiMA.deliberativeLayer.DeliberativeProcess;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.memory.ICompoundCue;
import FAtiMA.memory.ISpreadActivate;
import FAtiMA.memory.Memory;
import FAtiMA.motivationalSystem.MotivationalState;
import FAtiMA.reactiveLayer.ActionTendencies;
import FAtiMA.reactiveLayer.EmotionalReactionTreeNode;
import FAtiMA.reactiveLayer.ReactiveProcess;
import FAtiMA.sensorEffector.Event;
import FAtiMA.wellFormedNames.Symbol;

public interface AgentModel {
	
	public String getName();
	
	public EmotionalState getEmotionalState();
	
	public Memory getMemory(); 
	
	public DeliberativeProcess getDeliberativeLayer();
	
	public ReactiveProcess getReactiveLayer();
	
	public AgentModel getModelToTest(Symbol ToM);
	
}
