package FAtiMA.Display;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import FAtiMA.memory.ActionDetail;
import FAtiMA.util.enumerables.EmotionType;

public class RecordDetailPanel extends JPanel {

	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	JTextField _id;
	
	JTextField _subject;
    JTextField _action;
    JTextField _target;
    JTextField _parameters;
    
    JTextField _feeling;
    JTextField _evaluation;
    JTextField _time;
	JTextField _location;
	
	public RecordDetailPanel(ActionDetail detail)
	{
		super();
		this.setBorder(BorderFactory.createRaisedBevelBorder());
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.setMinimumSize(new Dimension(800,30));
        this.setMaximumSize(new Dimension(800,30));
        
        _id = new JTextField(new Integer(detail.getID()).toString());
        _id.setMinimumSize(new Dimension(30,30));
        _id.setMaximumSize(new Dimension(30,30));
        _id.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_id);
        
        _subject = new JTextField(detail.getSubject());
        _subject.setMinimumSize(new Dimension(50,30));
        _subject.setMaximumSize(new Dimension(50,30));
        _subject.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_subject);
        
        _action = new JTextField(detail.getAction());
        _action.setMinimumSize(new Dimension(80,30));
        _action.setMaximumSize(new Dimension(80,30));
        _action.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_action);
        
        _target = new JTextField(detail.getTarget());
        _target.setMinimumSize(new Dimension(80,30));
        _target.setMaximumSize(new Dimension(80,30));
        _target.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_target);
        
        _parameters = new JTextField(detail.getParameters().toString());
        _parameters.setMinimumSize(new Dimension(100,30));
        _parameters.setMaximumSize(new Dimension(100,30));
        _parameters.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_parameters);
	
        _feeling = new JTextField(EmotionType.GetName(detail.getEmotion().GetType()) + "-" 
        		+ detail.getEmotion().GetPotential());
        _feeling.setMinimumSize(new Dimension(110,30));
        _feeling.setMaximumSize(new Dimension(110,30));
        _feeling.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_feeling);
                
    	_evaluation = new JTextField(detail.getEvaluation().toString());
        _evaluation.setMinimumSize(new Dimension(150,30));
        _evaluation.setMaximumSize(new Dimension(150,30));
        _evaluation.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_evaluation);
        
        _time = new JTextField(String.valueOf(detail.getTime().getNarrativeTime()));
        _time.setMinimumSize(new Dimension(100,30));
        _time.setMaximumSize(new Dimension(100,30));
        _time.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_time);
        
        _location = new JTextField(detail.getLocation().toString());
        _location.setMinimumSize(new Dimension(100,30));
        _location.setMaximumSize(new Dimension(100,30));
        _location.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add( _location);        
	}

}
