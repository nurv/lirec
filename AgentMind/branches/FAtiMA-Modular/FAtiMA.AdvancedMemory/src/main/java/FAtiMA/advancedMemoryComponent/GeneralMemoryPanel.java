package FAtiMA.advancedMemoryComponent;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;

public class GeneralMemoryPanel extends AgentDisplayPanel {

	private static final long serialVersionUID = 1L;
	
	private JPanel _gersPanel;
	private ArrayList<GERDisplay> _gerDisplayList;
	private AdvancedMemoryComponent _generalMemory;
	private ArrayList<String> _gAttributes = new ArrayList<String>();
	
	private JPanel _aux;
	
	public GeneralMemoryPanel(AdvancedMemoryComponent gm)
	{
		super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        _generalMemory = gm;
        
        _gerDisplayList = new ArrayList<GERDisplay>();
        
      	_gersPanel = new JPanel();
      	_gersPanel.setBorder(BorderFactory.createTitledBorder("GERs"));
      	_gersPanel.setLayout(new BoxLayout(_gersPanel,BoxLayout.Y_AXIS));
      	_gersPanel.setMinimumSize(new Dimension(500,300));
      	_gersPanel.setMaximumSize(new Dimension(500,300));      
        
      	JScrollPane gerScroll = new JScrollPane(_gersPanel);
		
		this.add(gerScroll);
	}

	public void PanelAttributes(ArrayList<String> gAttributes)
	{	 
		_gersPanel.removeAll();
		_gerDisplayList.clear();
		
		_aux = new JPanel();
        _aux.setLayout(new BoxLayout(_aux,BoxLayout.X_AXIS));
        _aux.setMinimumSize(new Dimension(500,30));
        _aux.setMaximumSize(new Dimension(500,30));
		
        JLabel lbl;
        _gAttributes = gAttributes;
        
        if (_gAttributes.contains("subject"))
        {
	        lbl = new JLabel("Subject"); // Who?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(50,30));
	        lbl.setMaximumSize(new Dimension(50,30));
	        _aux.add(lbl);
        }
        
        if (_gAttributes.contains("action"))
        {
	        lbl = new JLabel("Action"); // What?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        if (_gAttributes.contains("intention"))
        {
	        lbl = new JLabel("Intention"); // What?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        if (_gAttributes.contains("target"))
        {
	        lbl = new JLabel("Target"); // Whom?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        if (_gAttributes.contains("object"))
        {
	        lbl = new JLabel("Object"); // What?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        if (_gAttributes.contains("desirability"))
        {
	        lbl = new JLabel("Desirability"); // Desirable?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        if (_gAttributes.contains("praiseworthiness"))
        {
	        lbl = new JLabel("Praiseworthiness"); // Praiseworthy?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        if (_gAttributes.contains("location"))
        {
	        lbl = new JLabel("Location"); // Where?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        if (_gAttributes.contains("time"))
        {
	        lbl = new JLabel("Time"); // When?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        lbl = new JLabel("Coverage"); // Frequency?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        _aux.add(lbl);
        
        _gersPanel.add(_aux); 
	}
	
	public boolean Update(AgentCore ag)
    {
    	return Update((AgentModel) ag);
    }
    
    public boolean Update(AgentModel am) 
    {
	    ListIterator<GER> li = _generalMemory.getGeneralisation().getAllGERs().listIterator();
       
        GER ger;
        GERDisplay gerDisplay;
        
        int index;
        
        while (li.hasNext()) {
            index = li.nextIndex();
            ger = (GER) li.next();
            if(index >= _gerDisplayList.size()) {
	            gerDisplay = new GERDisplay(ger, _gAttributes);
	            _gerDisplayList.add(gerDisplay);
	            _gersPanel.add(gerDisplay.getPanel());
            }
        }
            
    	return true;
    }
	    
}
