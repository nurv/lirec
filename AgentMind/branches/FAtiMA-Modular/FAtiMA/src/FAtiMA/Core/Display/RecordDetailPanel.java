package FAtiMA.Core.Display;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;

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
	
	//07/01/10
	JTextField _intention;
	JTextField _status;
	JTextField _desirability;
	JTextField _praiseworthiness;
	JTextField _speechActMeaning;
	JTextField _multimediaPath;
	JTextField _object;
	
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
        
        _intention = new JTextField(detail.getIntention());
        _intention.setMinimumSize(new Dimension(80,30));
        _intention.setMaximumSize(new Dimension(80,30));
        _intention.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_intention);
        
        _target = new JTextField(detail.getTarget());
        _target.setMinimumSize(new Dimension(80,30));
        _target.setMaximumSize(new Dimension(80,30));
        _target.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_target);
        
        _status = new JTextField(detail.getStatus());
        _status.setMinimumSize(new Dimension(80,30));
        _status.setMaximumSize(new Dimension(80,30));
        _status.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_status);
        
        _speechActMeaning = new JTextField(detail.getSpeechActMeaning());
        _speechActMeaning.setMinimumSize(new Dimension(80,30));
        _speechActMeaning.setMaximumSize(new Dimension(80,30));
        _speechActMeaning.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_speechActMeaning);
        
        _multimediaPath = new JTextField(detail.getMultimediaPath());
        _multimediaPath.setMinimumSize(new Dimension(80,30));
        _multimediaPath.setMaximumSize(new Dimension(80,30));
        _multimediaPath.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_multimediaPath);
        
        _object = new JTextField(detail.getObject());
        _object.setMinimumSize(new Dimension(80,30));
        _object.setMaximumSize(new Dimension(80,30));
        _object.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_object);
        
        _desirability = new JTextField(Float.toString(detail.getDesirability()));
        _desirability.setMinimumSize(new Dimension(80,30));
        _desirability.setMaximumSize(new Dimension(80,30));
        _desirability.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_desirability);
        
        _praiseworthiness = new JTextField(Float.toString(detail.getPraiseworthiness()));
        _praiseworthiness.setMinimumSize(new Dimension(80,30));
        _praiseworthiness.setMaximumSize(new Dimension(80,30));
        _praiseworthiness.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_praiseworthiness);
        
        /*_parameters = new JTextField(detail.getParameters().toString());
        _parameters.setMinimumSize(new Dimension(100,30));
        _parameters.setMaximumSize(new Dimension(100,30));
        _parameters.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_parameters);*/
	
        _feeling = new JTextField(detail.getEmotion().getType() + "-" 
        		+ detail.getEmotion().GetPotential());
        _feeling.setMinimumSize(new Dimension(110,30));
        _feeling.setMaximumSize(new Dimension(110,30));
        _feeling.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_feeling);
                
    	/*_evaluation = new JTextField(detail.getEvaluation().toString());
        _evaluation.setMinimumSize(new Dimension(150,30));
        _evaluation.setMaximumSize(new Dimension(150,30));
        _evaluation.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        this.add(_evaluation);*/
        
        // Matthias 27/04/11: formatted Calendar data
        //_time = new JTextField(String.valueOf(detail.getTime().getRealTime()));              
        _time = new JTextField(detail.getTime().getRealTimeFormatted());
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
