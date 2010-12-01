package FAtiMA.advancedMemoryComponent;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



public class GERDisplay extends JPanel {
	
	private static final long serialVersionUID = 1L;
	JTextField _subject;
    JTextField _action;
    JTextField _intention;
    JTextField _target;
    JTextField _object;
 	JTextField _desirability;
	JTextField _praiseworthiness;
	JTextField _location;
	JTextField _time;
	JTextField _coverage;
    
    public GERDisplay(GER ger, ArrayList<String> gAttributes) {
    	super();
		this.setBorder(BorderFactory.createRaisedBevelBorder());
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.setMinimumSize(new Dimension(500,30));
        this.setMaximumSize(new Dimension(500,30));
        
        if (gAttributes.contains("subject"))
        {
	        _subject = new JTextField(ger.getSubject());
	        _subject.setMinimumSize(new Dimension(50,30));
	        _subject.setMaximumSize(new Dimension(50,30));
	        _subject.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_subject);
        }
        
        if (gAttributes.contains("action"))
        {
	        _action = new JTextField(ger.getAction());
	        _action.setMinimumSize(new Dimension(80,30));
	        _action.setMaximumSize(new Dimension(80,30));
	        _action.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_action);
        }   
        
        if (gAttributes.contains("intention"))
        {
	        _intention = new JTextField(ger.getIntention());
	        _intention.setMinimumSize(new Dimension(80,30));
	        _intention.setMaximumSize(new Dimension(80,30));
	        _intention.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_intention);
        }   
        
        if (gAttributes.contains("target"))
        {
	        _target = new JTextField(ger.getTarget());
	        _target.setMinimumSize(new Dimension(80,30));
	        _target.setMaximumSize(new Dimension(80,30));
	        _target.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_target);       
        }
        
        if (gAttributes.contains("object"))
        {
	        _object = new JTextField(ger.getObject());
	        _object.setMinimumSize(new Dimension(80,30));
	        _object.setMaximumSize(new Dimension(80,30));
	        _object.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_object);       
        }
        
        if (gAttributes.contains("desirability"))
        {
	        _desirability = new JTextField(ger.getDesirability());
	        _desirability.setMinimumSize(new Dimension(80,30));
	        _desirability.setMaximumSize(new Dimension(80,30));
	        _desirability.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_desirability);
        }
        
        if (gAttributes.contains("praiseworthiness"))
        {        
	        _praiseworthiness = new JTextField(ger.getPraiseworthiness());
	        _praiseworthiness.setMinimumSize(new Dimension(80,30));
	        _praiseworthiness.setMaximumSize(new Dimension(80,30));
	        _praiseworthiness.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_praiseworthiness);
        }
        
        if (gAttributes.contains("location"))
        {
	        _location = new JTextField(ger.getLocation());
	        _location.setMinimumSize(new Dimension(80,30));
	        _location.setMaximumSize(new Dimension(80,30));
	        _location.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_location);
        }
        
        if (gAttributes.contains("time"))
        {
	        _time = new JTextField(ger.getTime());
	        _time.setMinimumSize(new Dimension(80,30));
	        _time.setMaximumSize(new Dimension(80,30));
	        _time.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
	        this.add(_time);
        }
        
        _coverage = new JTextField(String.valueOf(ger.getCoverage()));
        _coverage.setMinimumSize(new Dimension(80,30));
        _coverage.setMaximumSize(new Dimension(80,30));
        _coverage.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_coverage); 

    }
    
    public JPanel getPanel() {
        return this;
    }
}
