package FAtiMA.Core;

import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.deliberativeLayer.goals.GoalLibrary;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.reactiveLayer.ReactiveProcess;
import FAtiMA.Core.sensorEffector.RemoteAgent;
import FAtiMA.Core.wellFormedNames.Symbol;

public interface AgentModel {
	
	public ActionLibrary getActionLibrary();
	
	public IComponent getComponent(String name);
	
	public EmotionalState getEmotionalState(); 
	
	public GoalLibrary getGoalLibrary();
	
	public Memory getMemory();
	
	//public DeliberativeProcess getDeliberativeLayer();
	
	public AgentModel getModelToTest(Symbol ToM);
	
	public String getName();
	
	public ReactiveProcess getReactiveLayer();
	
	public RemoteAgent getRemoteAgent();
	
	public boolean isSelf();
	
	public void setModelStrategy(IGetModelStrategy strat);
	
	public void updateEmotions(AppraisalFrame af);
	
}
