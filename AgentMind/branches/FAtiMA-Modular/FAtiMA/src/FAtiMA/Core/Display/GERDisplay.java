package FAtiMA.Core.Display;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import FAtiMA.Core.memory.generalMemory.GER;


public class GERDisplay extends JPanel {
	
	private static final long serialVersionUID = 1L;
	JTextField _subject;
    JTextField _action;
    JTextField _target;
 	JTextField _desirability;
	JTextField _praiseworthiness;
	JTextField _time;
	JTextField _coverage;
    
    public GERDisplay(GER ger) {
    	super();
		this.setBorder(BorderFactory.createRaisedBevelBorder());
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.setMinimumSize(new Dimension(500,30));
        this.setMaximumSize(new Dimension(500,30));
        
        _subject = new JTextField(ger.getSubject());
        _subject.setMinimumSize(new Dimension(50,30));
        _subject.setMaximumSize(new Dimension(50,30));
        _subject.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_subject);
        
        _action = new JTextField(ger.getAction());
        _action.setMinimumSize(new Dimension(80,30));
        _action.setMaximumSize(new Dimension(80,30));
        _action.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_action);
        
        _target = new JTextField(ger.getTarget());
        _target.setMinimumSize(new Dimension(80,30));
        _target.setMaximumSize(new Dimension(80,30));
        _target.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_target);       
        
        _desirability = new JTextField(ger.getDesirability());
        _desirability.setMinimumSize(new Dimension(80,30));
        _desirability.setMaximumSize(new Dimension(80,30));
        _desirability.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_desirability);
        
        _praiseworthiness = new JTextField(ger.getPraiseworthiness());
        _praiseworthiness.setMinimumSize(new Dimension(80,30));
        _praiseworthiness.setMaximumSize(new Dimension(80,30));
        _praiseworthiness.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_praiseworthiness);
        
        _time = new JTextField(ger.getTime());
        _time.setMinimumSize(new Dimension(80,30));
        _time.setMaximumSize(new Dimension(80,30));
        _time.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_time);   
        
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
