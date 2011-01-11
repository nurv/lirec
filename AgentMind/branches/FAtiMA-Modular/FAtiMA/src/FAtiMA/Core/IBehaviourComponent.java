package FAtiMA.Core;

public interface IBehaviourComponent extends IComponent{
	
	public ValuedAction actionSelection(AgentModel am);
	
	public void actionSelectedForExecution(ValuedAction va);

}
