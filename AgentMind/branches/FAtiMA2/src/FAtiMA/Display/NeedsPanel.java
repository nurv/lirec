package FAtiMA.Display;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Agent;
import FAtiMA.AgentModel;
import FAtiMA.motivationalSystem.MotivationalState;

public class NeedsPanel extends AgentDisplayPanel {
	private static final long serialVersionUID = 1L;
	
	private Hashtable _drivesDisplays;
	 
	private JPanel _needs;
	private int _previousKnownAgents;

	public NeedsPanel(String agentName, String selfName) {
		
		super();
		_previousKnownAgents = 0;
		
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
      
        _drivesDisplays = new Hashtable();
		
        _needs = new JPanel();
        _needs.setBorder(BorderFactory.createTitledBorder("Needs"));
        _needs.setLayout(new BoxLayout(_needs,BoxLayout.Y_AXIS));
		
        _needs.setMaximumSize(new Dimension(350,400));
		_needs.setMinimumSize(new Dimension(350,400));
		
		JScrollPane goalsScrool = new JScrollPane(_needs);
		
		this.add(goalsScrool);
		
		DrivesDisplay aux = new DrivesDisplay(agentName, selfName);
		_needs.add(aux.getDrivesPanel());
		_drivesDisplays.put(agentName,aux);
		
		
	}
	
	
	public boolean Update(Agent ag) {

		CheckForOtherAgents(ag);
	
		Collection displays  = _drivesDisplays.values();
		
		Iterator it = displays.iterator();
		
		while(it.hasNext()){
			DrivesDisplay dd = (DrivesDisplay)it.next();
			
			dd.Update(ag);
		}
		return false;
	}
	 
	
	private void CheckForOtherAgents(AgentModel am){
		int numOfKnownAgents = am.getMotivationalState().getOtherAgentsMotivators().size();
		
		if(numOfKnownAgents > _previousKnownAgents){
			_previousKnownAgents = numOfKnownAgents;
			
			Collection otherAgentsNames  = am.getMotivationalState().getOtherAgentsMotivators().keySet();
		
			Iterator it = otherAgentsNames.iterator();
			
			while(it.hasNext()){
				String agentName = (String)it.next();
				
				if(_drivesDisplays.get(agentName) == null){
					DrivesDisplay aux = new DrivesDisplay(agentName, am.getName());
					_needs.add(aux.getDrivesPanel());
					_drivesDisplays.put(agentName,aux);
				}
			}	
		}
	}
	
	
}
