package FAtiMA.Display;

import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

import FAtiMA.Agent;
import FAtiMA.AgentModel;
import FAtiMA.ModelOfOther;

public class ToMPanel extends AgentDisplayPanel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTabbedPane _agentsPanel;
	private int _currentAgents;
	
	public ToMPanel()
	{
		 super();
	     this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	     _agentsPanel = new JTabbedPane();
	     _currentAgents = 0;
	     
	     this.add(_agentsPanel);
	}
	

	@Override
	public boolean Update(Agent ag) {
		boolean update = false;
		
		HashMap<String, ModelOfOther> ToM = ag.getToM();
		if(ToM.size() != _currentAgents)
		{
			UpdateAgentsTab(ToM);
			update = true;
		}
		
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
		
	}

}
