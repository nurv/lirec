package FAtiMA.Core.componentTypes;

import FAtiMA.Core.AgentModel;


public interface IProcessExternalRequestComponent extends IComponent{
	
	public void processExternalRequest(AgentModel am, String msgType, String perception);

}
