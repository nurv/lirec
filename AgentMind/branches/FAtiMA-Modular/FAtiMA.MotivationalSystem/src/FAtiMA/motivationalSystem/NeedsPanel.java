package FAtiMA.motivationalSystem;

import java.awt.Dimension;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.util.Constants;

public class NeedsPanel extends AgentDisplayPanel {
	private static final long serialVersionUID = 1L;
	
	private Hashtable<String,DrivesDisplay> _drivesDisplays;
	 
	private JPanel _needs;
	private MotivationalComponent _motivationalState; 

	public NeedsPanel(MotivationalComponent ms) {
		
		super();
		
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
      
        _drivesDisplays = new Hashtable<String, DrivesDisplay>();
		
        _needs = new JPanel();
        _needs.setBorder(BorderFactory.createTitledBorder("Needs"));
        _needs.setLayout(new BoxLayout(_needs,BoxLayout.Y_AXIS));
		
        _needs.setMaximumSize(new Dimension(350,400));
		_needs.setMinimumSize(new Dimension(350,400));
		
		JScrollPane goalsScrool = new JScrollPane(_needs);
		
		this.add(goalsScrool);
		
		DrivesDisplay aux = new DrivesDisplay(Constants.SELF);
		_needs.add(aux.getDrivesPanel());
		_drivesDisplays.put(Constants.SELF,aux);
		
		_motivationalState = ms;
		
		
	}
	
	public boolean Update(AgentCore ag)
	{
		return Update((AgentModel) ag);
	}
	
	
	public boolean Update(AgentModel ag) {

		//CheckForOtherAgents(ag);
		
		for(DrivesDisplay dd : _drivesDisplays.values())
		{
			dd.Update(_motivationalState);
		}
	
		return false;
	}
}
