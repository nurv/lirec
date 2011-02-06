package FAtiMA.Core.componentTypes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.ValuedAction;

public interface IBehaviourComponent extends IComponent{
	
	public ValuedAction actionSelection(AgentModel am);
	
	public void actionSelectedForExecution(ValuedAction va);

}
