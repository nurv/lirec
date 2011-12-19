package cmion.TeamBuddy.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import cmion.architecture.CmionComponent;
import cmion.architecture.IArchitecture;

public class TeamBuddyControl extends CmionComponent {

	/** the gui for the teambuddy */
	TbGui tbGui;
	
	
	public TeamBuddyControl(IArchitecture architecture) {
		super(architecture);
		
		//Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TeamBuddy Control");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //Add contents to the window.
        tbGui = new TbGui();
        frame.add(tbGui);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
	
	/** frame to display events */
	private class TbGui extends JPanel implements ActionListener 
	{

		private static final long serialVersionUID = 1L;

		protected JButton btnGeneralise;
		
	    public TbGui() {
	        super(new BorderLayout());

	        // create buttons
	        
	        btnGeneralise = new JButton("Generalise");
	        btnGeneralise.addActionListener(this);
	        
	        //Add components to this panel.
	        add(btnGeneralise, BorderLayout.NORTH);
	    }

		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			if (arg0.getSource() == btnGeneralise)
			{
				//Simulation.instance.schedule(new RequestGeneralise());
			}
		}
	}

	@Override
	public void registerHandlers() {
		// TODO Auto-generated method stub
		
	}

}
