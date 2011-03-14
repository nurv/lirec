package cmion.addOns.manipulators;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import cmion.architecture.CmionComponent;
import cmion.architecture.IArchitecture;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;

public class RemoteActionSimulator extends CmionComponent 
{

	/** the gui for the simulator */
	SimulatorWindow window;

	/** create a new simulator */
	public RemoteActionSimulator(IArchitecture architecture) 
	{
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
	    JFrame frame = new JFrame("Remote Action Simulator");
	    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

	    //Add contents to the window.
	    window = new SimulatorWindow();
	    frame.add(window);

	    //Display the window.
	    frame.pack();
	    frame.setVisible(true);
	}

	public void newRA(final MindAction ma)
	{
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
	        public void run() 
	        {
	        	window.addRA(ma);
	        }
	    });		
	}	


	@Override
	public void registerHandlers() 
	{
		// create a new handler
		HandleRemoteActionEvent handler = new HandleRemoteActionEvent();
		// listen to any remote action events (any source)
		Simulation.instance.getEventHandlers().add(handler);
	}

	/** internal event handler class for listening to any event */
	private class HandleRemoteActionEvent extends EventHandler {

	    public HandleRemoteActionEvent() {
	        super(EventRemoteAction.class);
	    }

	    @Override
	    public void invoke(IEvent evt) 
	    {
	    	if (evt instanceof EventRemoteAction)
	    	{
	    		EventRemoteAction evt1 = (EventRemoteAction) evt;
	    		newRA(evt1.getRemoteAction());
	    	}
	    }
	}


	/** frame to display events */
	private class SimulatorWindow extends JPanel implements ActionListener, ListSelectionListener 
	{
		
		private static final long serialVersionUID = 1L;
		
		protected LinkedList<MindAction> remoteActions;
		
	    protected JList list;
	    protected JPanel toolBar;
	    
	    protected JTextField txtSubject;
	    protected JTextField txtActionName;
	    protected JTextField txtParameters;
	    protected JButton btnPerform;
	    
	    public SimulatorWindow() {
	        super(new BorderLayout());

	        remoteActions = new LinkedList<MindAction>();
	        list = new JList();

	        //Listen for when the selection changes.
	        list.addListSelectionListener(this);

	        toolBar = new JPanel();
	        JScrollPane scrollPane = new JScrollPane(list);

	        // create toolbar
	        JLabel lblSubject = new JLabel("subject: ");
	        Font f =lblSubject.getFont();
	        lblSubject.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
	        toolBar.add(lblSubject);

	        txtSubject = new JTextField();
	        txtSubject.setPreferredSize(new Dimension(70,25));
	        toolBar.add(txtSubject);

	        JLabel lblAction = new JLabel("  action name: ");
	        f =lblAction.getFont();
	        lblAction.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
	        toolBar.add(lblAction);

	        txtActionName = new JTextField();
	        txtActionName.setPreferredSize(new Dimension(70,25));
	        toolBar.add(txtActionName);
	        
	        JLabel lblParameters = new JLabel("  parameters (space separated): ");
	        f =lblParameters.getFont();
	        lblParameters.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
	        toolBar.add(lblParameters);
	        	        
	        txtParameters = new JTextField();
	        txtParameters.setPreferredSize(new Dimension(70,25));        
	        toolBar.add(txtParameters);

	        btnPerform = new JButton("perform");
	        btnPerform.addActionListener(this);
	        toolBar.add(btnPerform);
	        
	        //Add components to this panel.
	        add(toolBar, BorderLayout.NORTH);
	        add(scrollPane, BorderLayout.CENTER);
	    }

	    public void addRA(MindAction ma)
	    {
	    	remoteActions.addLast(ma);
	    	if (remoteActions.size() > 100) remoteActions.removeFirst();
	    	list.setListData(remoteActions.toArray());
	    }
	    
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			if (arg0.getSource() == this.btnPerform)
			{
				String subjectName = this.txtSubject.getText().trim();
				String actionName = this.txtActionName.getText().trim();
				String parametersStr = this.txtParameters.getText().trim();
				
				if (subjectName.equals(""))
				{
					JOptionPane.showMessageDialog(this, "Cannot perform action with no subject");
					return;
				}

				if (actionName.equals(""))
				{
					JOptionPane.showMessageDialog(this, "Cannot perform action, no action name specified");
					return;
				}

				StringTokenizer st = new StringTokenizer(parametersStr);
				ArrayList<String> parameters = new ArrayList<String>();
				while (st.hasMoreTokens()) parameters.add(st.nextToken());
				
				MindAction ma = new MindAction(subjectName,actionName,parameters);
				
				RemoteActionSimulator.this.raise(new EventRemoteAction(ma));
			}

		}

		@Override
		public void valueChanged(ListSelectionEvent arg0) 
		{
			if (list.getSelectedValue()==null) return;
			if (!(list.getSelectedValue() instanceof MindAction)) return;
			MindAction ma = (MindAction) list.getSelectedValue();
			txtSubject.setText(ma.getSubject());
			txtActionName.setText(ma.getName());
			String pStr = "";
			for (String parameter : ma.getParameters()) 
				pStr = pStr + parameter + " ";
			txtParameters.setText(pStr.trim());
		}

	}
	}

