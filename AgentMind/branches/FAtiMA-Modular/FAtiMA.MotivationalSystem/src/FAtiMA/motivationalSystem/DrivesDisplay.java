package FAtiMA.motivationalSystem;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import FAtiMA.Core.util.Constants;

public class DrivesDisplay {
	JPanel _panel;

	JProgressBar _energyBar;
	JProgressBar _integrityBar;
	JProgressBar _affiliationBar;
	JProgressBar _certaintyBar;
	JProgressBar _competenceBar;

	//TODO: The way that the constructor distinguishes from the agent's needs panel
	//of the other agents needs panel is getting agentName == null; 
	public DrivesDisplay(String agentName) {

		super();

		boolean isSelf = (agentName.equalsIgnoreCase(Constants.SELF));

		_panel = new JPanel();
		
		_panel.setBorder(BorderFactory.createTitledBorder(agentName));
	    
		
		_panel.setLayout(new BoxLayout(_panel,BoxLayout.Y_AXIS));
	
		_energyBar = new JProgressBar(0,100);
		_integrityBar = new JProgressBar(0,100);
		_affiliationBar = new JProgressBar(0,100);
		_certaintyBar = new JProgressBar(0,100);
		_competenceBar = new JProgressBar(0,100);
		
		

		_panel.add(InitializePanel(_energyBar, "Energy"));
		_panel.add(InitializePanel(_integrityBar, "Integrity"));
		_panel.add(InitializePanel(_affiliationBar, "Affiliation"));
		if(isSelf){
			_panel.add(InitializePanel(_certaintyBar, "Certainty"));
			_panel.add(InitializePanel(_competenceBar, "Competence"));
		}
	

	}
	private JPanel InitializePanel(JProgressBar driveBar, String driveName)
	{
		JPanel drivePanel = new JPanel();
		drivePanel.setBorder(BorderFactory.createTitledBorder(driveName));
		drivePanel.setMaximumSize(new Dimension(300,60));

		driveBar.setValue(0);
		driveBar.setStringPainted(true);
		driveBar.setForeground(new Color(255,0,0));
		drivePanel.add(driveBar);

		return drivePanel;
	}

	
	 
    public boolean Update(MotivationalComponent ms) {
        Float aux;
        
        aux = new Float(ms.GetIntensity(MotivatorType.ENERGY));
        _energyBar.setString(aux.toString());
        _energyBar.setValue(Math.round(aux.floatValue()*10));
        
        aux = new Float(ms.GetIntensity(MotivatorType.INTEGRITY));
        _integrityBar.setString(aux.toString());
        _integrityBar.setValue(Math.round(aux.floatValue()*10));
        
        aux = new Float(ms.GetIntensity(MotivatorType.AFFILIATION));
        _affiliationBar.setString(aux.toString());
        _affiliationBar.setValue(Math.round(aux.floatValue()*10));
        
        aux = new Float(ms.GetIntensity(MotivatorType.CERTAINTY));
        _certaintyBar.setString(aux.toString());
        _certaintyBar.setValue(Math.round(aux.floatValue()*10));
        
        aux = new Float(ms.GetIntensity(MotivatorType.COMPETENCE));
        _competenceBar.setString(aux.toString());
        _competenceBar.setValue(Math.round(aux.floatValue()*10));
        
        return true;
    }
    
	public JPanel getDrivesPanel() {
		return _panel;
	}
}
