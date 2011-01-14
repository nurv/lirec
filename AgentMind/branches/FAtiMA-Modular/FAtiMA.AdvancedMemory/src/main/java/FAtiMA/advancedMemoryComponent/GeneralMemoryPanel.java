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
	private ArrayList<GER> _gers;
	
	private JPanel _aux;
	
	public GeneralMemoryPanel(AdvancedMemoryComponent gm)
	{
		super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        _generalMemory = gm;
        
        _gerDisplayList = new ArrayList<GERDisplay>();
        _gers = new ArrayList<GER>();
        
      	_gersPanel = new JPanel();
      	_gersPanel.setBorder(BorderFactory.createTitledBorder("GERs"));
      	_gersPanel.setLayout(new BoxLayout(_gersPanel,BoxLayout.Y_AXIS));
      	_gersPanel.setMinimumSize(new Dimension(500,300));
      	_gersPanel.setMaximumSize(new Dimension(500,300));      
        
      	JScrollPane gerScroll = new JScrollPane(_gersPanel);
		
		this.add(gerScroll);
	}

	public void attributesPanel()
	{	 
		_aux = new JPanel();
        _aux.setLayout(new BoxLayout(_aux,BoxLayout.X_AXIS));
        _aux.setMinimumSize(new Dimension(500,30));
        _aux.setMaximumSize(new Dimension(500,30));
		
        JLabel lbl;
        GER ger = new GER();
        
        if (_gers.size() > 0)
        {
        	ger = _gers.get(0);
        }
        
        if (ger.getSubject() != null && !ger.getSubject().equals(""))
        {
	        lbl = new JLabel("Subject"); // Who?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(50,30));
	        lbl.setMaximumSize(new Dimension(50,30));
	        _aux.add(lbl);
	        _gAttributes.add("subject");
        }
        
        if (ger.getAction() != null && !ger.getAction().equals(""))
        {
	        lbl = new JLabel("Action"); // What?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
	        _gAttributes.add("action");
        }
        
        if (ger.getIntention() != null && !ger.getIntention().equals(""))
        {
	        lbl = new JLabel("Intention"); // What?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
	        _gAttributes.add("intention");
        }
        
        if (ger.getTarget() != null && !ger.getTarget().equals(""))
        {
	        lbl = new JLabel("Target"); // Whom?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
	        _gAttributes.add("target");
        }
        
        if (ger.getObject() != null && !ger.getObject().equals(""))
        {
	        lbl = new JLabel("Object"); // What?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
	        _gAttributes.add("object");
        }
        
        if (ger.getDesirability() != null && !ger.getDesirability().equals(""))
        {
	        lbl = new JLabel("Desirability"); // Desirable?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
	        _gAttributes.add("desirability");
        }
        
        if (ger.getPraiseworthiness() != null && !ger.getPraiseworthiness().equals(""))
        {
	        lbl = new JLabel("Praiseworthiness"); // Praiseworthy?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
	        _gAttributes.add("praiseworthiness");
        }
        
        if (ger.getLocation() != null && !ger.getLocation().equals(""))
        {
	        lbl = new JLabel("Location"); // Where?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
	        _gAttributes.add("location");
        }
        
        if (ger.getTime() != null && !ger.getTime().equals(""))
        {
	        lbl = new JLabel("Time"); // When?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
	        _gAttributes.add("time");
        }
        
        if (!_gAttributes.isEmpty())
        {
	        lbl = new JLabel("Coverage"); // Frequency?
	        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        lbl.setMinimumSize(new Dimension(80,30));
	        lbl.setMaximumSize(new Dimension(80,30));
	        _aux.add(lbl);
        }
        
        _gersPanel.add(_aux); 
	}
	
	public boolean Update(AgentCore ag)
    {
    	return Update((AgentModel) ag);
    }
    
    public boolean Update(AgentModel am) 
    {
        _gers = _generalMemory.getGeneralisation().getAllGERs();
	    ListIterator<GER> li = _gers.listIterator();
       
        GER ger;
        GERDisplay gerDisplay;        
        int index;
        
        _gersPanel.removeAll();
        _gerDisplayList.clear();
		_gAttributes.clear();
		
		// create the attributes panel
        attributesPanel();
        
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
