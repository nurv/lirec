package FAtiMA.ToM;

import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

import FAtiMA.Agent;
import FAtiMA.AgentCore;
import FAtiMA.AgentModel;
import FAtiMA.Display.AgentDisplayPanel;

public class ToMPanel extends AgentDisplayPanel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane _agentsPanel;
	private int _currentAgents;
	private ToMComponent _ToM;
	
	public ToMPanel(ToMComponent tom)
	{
		 super();
	     this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	     _agentsPanel = new JTabbedPane();
	     _currentAgents = 0;
	     _ToM = tom;
	     
	     this.add(_agentsPanel);
	}
	

	@Override
	public boolean Update(AgentCore ag) {
		boolean update = false;
		
		HashMap<String, ModelOfOther> ToM = _ToM.getToM();
		if(ToM.size() != _currentAgents)
		{
			UpdateAgentsTab(ToM);
			update = true;
		}
		
		update = ((ModelOfOtherPanel)_agentsPanel.getSelectedComponent()).Update(ag) || update;
		
		return update;
	}
	
	public boolean Update(AgentModel am)
	{
		return false;
	}
	
	private void UpdateAgentsTab(HashMap<String, ModelOfOther> ToM)
	{
		_agentsPanel.removeAll();
		for(ModelOfOther m : ToM.values())
		{
			_agentsPanel.addTab(m.getName(), null, new ModelOfOtherPanel(m),"Theory of Mind for agent " + m.getName());
		}
		
		_currentAgents = ToM.size();
		
	}

}
