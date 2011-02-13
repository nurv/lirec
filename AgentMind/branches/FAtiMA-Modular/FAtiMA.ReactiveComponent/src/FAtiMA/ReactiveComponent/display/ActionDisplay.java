package FAtiMA.ReactiveComponent.display ;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import FAtiMA.ReactiveComponent.Action;


public class ActionDisplay extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ActionDisplay(Action a)
	{
		super();
		this.setBorder(BorderFactory.createTitledBorder(a.getName().toString()));
        this.setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        this.setMaximumSize(new Dimension(350,300));
        this.setMinimumSize(new Dimension(350,300));
        
        Dimension d1 = new Dimension(70,25);
        
        JLabel em1 = new JLabel("Emotion: ");
        em1.setMaximumSize(d1);
        em1.setMinimumSize(d1);
        
        this.add(em1);
        
        JLabel em2 = new JLabel(a.GetElicitingEmotion().getType());
        em2.setMaximumSize(d1);
        em2.setMinimumSize(d1);
        
        this.add(em2);
        
        JLabel em3 = new JLabel("Intensity: " + a.GetElicitingEmotion().GetPotential());
        em3.setMaximumSize(d1);
        em3.setMinimumSize(d1);
        
        this.add(em3);
        
		
	}

}
