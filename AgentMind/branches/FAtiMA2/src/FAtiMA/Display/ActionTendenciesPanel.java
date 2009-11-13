package FAtiMA.Display;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Agent;
import FAtiMA.AgentModel;
import FAtiMA.reactiveLayer.ActionTendencies;

public class ActionTendenciesPanel extends AgentDisplayPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel _actionsPanel;
	private int _numberOfAT;

	public ActionTendenciesPanel()
	{
		super();
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		_numberOfAT = 0;
		
		_actionsPanel = new JPanel();
		_actionsPanel.setLayout(new BoxLayout(_actionsPanel,BoxLayout.Y_AXIS));
		
		JScrollPane actionsScroll = new JScrollPane(_actionsPanel);
		actionsScroll.setBorder(BorderFactory.createTitledBorder("ActionTendencies"));
		
		this.add(actionsScroll);
	}

	@Override
	public boolean Update(Agent ag) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean Update(AgentModel am) {
		ActionTendencies at = am.getActionTendencies();
		//if(at.)
		
		return false;
	}

}
