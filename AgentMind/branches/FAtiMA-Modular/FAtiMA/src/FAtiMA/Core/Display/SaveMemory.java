package FAtiMA.Core.Display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import FAtiMA.Core.AgentCore;

public class SaveMemory implements ActionListener {

	AgentCore _ag;

	public SaveMemory(AgentCore a)
	{	
		_ag = a;
	}

	public void actionPerformed(ActionEvent arg0) {
		_ag.RequestMemorySave();
	}
}
