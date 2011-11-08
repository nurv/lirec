package FAtiMA.motivationalSystem;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class DrivesDisplay {
	JPanel _panel;

	
	protected HashMap<String,JProgressBar> _driveBars;
	
	public DrivesDisplay(String agentName, MotivationalComponent mc) {
		super();
		
		_driveBars = new HashMap<String,JProgressBar>();
		
		JProgressBar driveBar;

		_panel = new JPanel();
		
		_panel.setBorder(BorderFactory.createTitledBorder(agentName));
	    
		
		_panel.setLayout(new BoxLayout(_panel,BoxLayout.Y_AXIS));
		
		for(Motivator m : mc.getMotivators().values())
		{
			driveBar = new JProgressBar();
			_panel.add(InitializePanel(driveBar,m.GetName()));
			_driveBars.put(m.GetName(), driveBar);
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
    	JProgressBar driveBar;
    	
    	for(Motivator m : ms.getMotivators().values())
    	{
    		aux = new Float(m.GetIntensity());
    		if(_driveBars.containsKey(m.GetName()))
    		{
    			driveBar = _driveBars.get(m.GetName());
    		}
    		else
    		{
    			driveBar = new JProgressBar();
    			_panel.add(InitializePanel(driveBar,m.GetName()));
    			_driveBars.put(m.GetName(), driveBar);
    		}
    		
    		driveBar.setString(aux.toString());
    		driveBar.setValue(Math.round(aux.floatValue()*10));
    	}
    	        
        return true;
    }
    
	public JPanel getDrivesPanel() {
		return _panel;
	}
}
