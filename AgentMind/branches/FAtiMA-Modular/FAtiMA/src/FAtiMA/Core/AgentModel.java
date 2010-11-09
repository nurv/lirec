package FAtiMA.Core;

import java.util.Collection;
import java.util.HashMap;

import FAtiMA.Core.Display.AgentDisplay;
import FAtiMA.Core.deliberativeLayer.DeliberativeProcess;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.memory.ICompoundCue;
import FAtiMA.Core.memory.ISpreadActivate;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.reactiveLayer.ActionTendencies;
import FAtiMA.Core.reactiveLayer.EmotionalReactionTreeNode;
import FAtiMA.Core.reactiveLayer.ReactiveProcess;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.motivationalSystem.MotivationalState;

public interface AgentModel {
	
	public String getName();
	
	public EmotionalState getEmotionalState();
	
	public Memory getMemory(); 
	
	public DeliberativeProcess getDeliberativeLayer();
	
	public ReactiveProcess getReactiveLayer();
	
	public AgentModel getModelToTest(Symbol ToM);
	
	public void setModelStrategy(IGetModelStrategy strat);
	
}
