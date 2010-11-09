package FAtiMA.Core.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import FAtiMA.Core.AgentCore;

public class TestAction implements ActionListener {
	
	AgentCore _ag;

	public TestAction(AgentCore a)
	{	
		_ag = a;
	}

	public void actionPerformed(ActionEvent arg0) {
		_ag.RequestAgentSave();
	}
	
}
