package FAtiMA.ReactiveComponent.display;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.ActionTendencies;
import FAtiMA.ReactiveComponent.ReactiveComponent;

public class ActionTendenciesPanel extends AgentDisplayPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel _actionsPanel;
	private int _numberOfAT;
	private ReactiveComponent _reactiveComponent;

	public ActionTendenciesPanel(ReactiveComponent reactiveComponent)
	{
		super();
		
		
		_numberOfAT = 0;
		_reactiveComponent = reactiveComponent;
		
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		_actionsPanel = new JPanel();
		_actionsPanel.setLayout(new BoxLayout(_actionsPanel,BoxLayout.Y_AXIS));
		
		
		JScrollPane actionsScroll = new JScrollPane(_actionsPanel);
		actionsScroll.setBorder(BorderFactory.createTitledBorder("ActionTendencies"));
		
		this.add(actionsScroll);
	}

	@Override
	public boolean Update(AgentCore ag) {
		return Update((AgentModel) ag);
	}

	@Override
	public boolean Update(AgentModel am) {
		ActionTendencies at = _reactiveComponent.getActionTendencies();
		if(at.getActions().size() != _numberOfAT)
		{
			_actionsPanel.removeAll();
			for(Action a : at.getActions())
			{
				_actionsPanel.add(new ActionDisplay(a));
			}
			
			_numberOfAT = at.getActions().size();
			return true;
		}
		
		return false;
	}

}
