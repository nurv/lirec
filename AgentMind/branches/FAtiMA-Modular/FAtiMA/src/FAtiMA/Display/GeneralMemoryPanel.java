package FAtiMA.Display;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.AgentCore;
import FAtiMA.AgentModel;
import FAtiMA.memory.generalMemory.GER;

public class GeneralMemoryPanel extends AgentDisplayPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel _gersPanel;
	private ArrayList<GERDisplay> _gerDisplayList;
	
	public GeneralMemoryPanel()
	{
		super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        _gerDisplayList = new ArrayList<GERDisplay>();
        
      	_gersPanel = new JPanel();
      	_gersPanel.setBorder(BorderFactory.createTitledBorder("GERs"));
      	_gersPanel.setLayout(new BoxLayout(_gersPanel,BoxLayout.Y_AXIS));
      	_gersPanel.setMinimumSize(new Dimension(500,300));
      	_gersPanel.setMaximumSize(new Dimension(500,300));      
        
      	JScrollPane gerScroll = new JScrollPane(_gersPanel);
		
		this.add(gerScroll);
		
        JPanel aux = new JPanel();
        aux.setLayout(new BoxLayout(aux,BoxLayout.X_AXIS));
        aux.setMinimumSize(new Dimension(500,30));
        aux.setMaximumSize(new Dimension(500,30));
                
        JLabel lbl = new JLabel("Subject"); // Who?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(50,30));
        lbl.setMaximumSize(new Dimension(50,30));
        aux.add(lbl);
        
        lbl = new JLabel("Action"); // What?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Target"); // Whom?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Desirability"); // Desirable?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Praiseworthiness"); // Praiseworthy?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Time"); // When?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Coverage"); // Frequency?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        _gersPanel.add(aux); 
	}

	 public boolean Update(AgentCore ag)
    {
    	return Update((AgentModel) ag);
    }
    
    public boolean Update(AgentModel am) 
    {
	    ListIterator<GER> li = am.getMemory().getGeneralMemory().getAllGERs().listIterator();
       
        GER ger;
        GERDisplay gerDisplay;
        int index;
       
        while (li.hasNext()) {
            index = li.nextIndex();
            ger = (GER) li.next();
            if(index >= _gerDisplayList.size()) {
                gerDisplay = new GERDisplay(ger);
                _gerDisplayList.add(gerDisplay);
                _gersPanel.add(gerDisplay.getPanel());
            }
            else {
               gerDisplay = (GERDisplay) _gerDisplayList.get(index);
	        }
	    }    	
    	return true;
    }
	    
}
