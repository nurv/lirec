package FAtiMA.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import FAtiMA.Agent;


public class TestAction implements ActionListener {
	
	Agent _ag;

	public TestAction(Agent a)
	{	
		_ag = a;
	}

	public void actionPerformed(ActionEvent arg0) {
		_ag.SaveAgentState(_ag.name());
		_ag.SaveAM(_ag.name());
		_ag.SaveSTM(_ag.name());
	}
	
}
