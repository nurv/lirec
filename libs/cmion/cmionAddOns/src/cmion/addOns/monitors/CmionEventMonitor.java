/*	
    CMION
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  ---  
*/


package cmion.addOns.monitors;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import cmion.addOns.samgar.SamgarConnector;
import cmion.architecture.IArchitecture;
import cmion.architecture.CmionComponent;
import cmion.architecture.CmionEvent;
import cmion.level2.Competency;
import cmion.level2.CompetencyExecution;
import cmion.level3.CompetencyManager;
import cmion.storage.BlackBoard;
import cmion.storage.WorldModel;


/** this class is a simple example of a monitor. This one lists all 
 *  CMION events within the simulation */
public class CmionEventMonitor extends CmionComponent {

	/** the gui for the monitor */
	MonitorWindow window;
	
	/** create a new ion event monitor */
	public CmionEventMonitor(IArchitecture architecture) 
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
        JFrame frame = new JFrame("Ion Event Monitor");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Add contents to the window.
        window = new MonitorWindow();
        frame.add(window);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /** add another event to the monitor's event listing */
    public void addEvent(final CmionEvent evt)
    {
    	final String evtText = evt.toString();
		//Schedule a job for the event dispatch thread:
        //update list
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                window.addEvent(evtText,evt);
            }
        });
    }
    
    
	@Override
	public void registerHandlers() 
	{
		Simulation.instance.getEventHandlers().add(new HandleAnyCmionEvent());
	}
	
	/** internal event handler class for listening to any event */
	private class HandleAnyCmionEvent extends EventHandler {

	    public HandleAnyCmionEvent() {
	        super(CmionEvent.class);
	    }

	    @Override
	    public void invoke(IEvent evt) 
	    {
	    	addEvent((CmionEvent) evt);
	    }
	}
	
    
	/** frame to display events */
	private class MonitorWindow extends JPanel implements ActionListener 
	{

		private static final long serialVersionUID = 1L;

		private ArrayList<String> eventTexts;
		private ArrayList<CmionEvent> events;
		
	    protected JTextArea textArea;
	    protected JPanel toolBar;
	    
	    // buttons to toggle the display of certain types of events
	    protected JToggleButton btnCompetencyManager;
	    protected JToggleButton btnExecution;
	    protected JToggleButton btnWorldModel;
	    protected JToggleButton btnBlackBoard;
	    protected JToggleButton btnCompetencies;
	    protected JToggleButton btnSamgar;
	    protected JToggleButton btnOther;

	    public MonitorWindow() {
	        super(new BorderLayout());

	        eventTexts = new ArrayList<String>();
	        events = new ArrayList<CmionEvent>();
	            
	        textArea = new JTextArea(5, 20);
	        toolBar = new JPanel();
	        textArea.setEditable(false);
	        JScrollPane scrollPane = new JScrollPane(textArea);

	        // create buttons
	        btnCompetencyManager = new JToggleButton("CM");
	        btnCompetencyManager.setToolTipText("Filter Competency Manager events");
	        btnCompetencyManager.addActionListener(this);

	        btnExecution = new JToggleButton("CE");
	        btnExecution.setToolTipText("Filter Competency Execution events");
	        btnExecution.addActionListener(this);
	        
	        btnWorldModel = new JToggleButton("WM");
	        btnWorldModel.setToolTipText("Filter World Model events");
	        btnWorldModel.addActionListener(this);
	        
	        btnBlackBoard = new JToggleButton("BB");
	        btnBlackBoard.setToolTipText("Filter Black Board events");
	        btnBlackBoard.addActionListener(this);
	        
	        btnCompetencies = new JToggleButton("Comp");
	        btnCompetencies.setToolTipText("Filter Competencies events");
	        btnCompetencies.addActionListener(this);

	        btnSamgar = new JToggleButton("Samgar");
	        btnSamgar.setToolTipText("Filter Samgar Connector events");
	        btnSamgar.addActionListener(this);
	        
	        btnOther = new JToggleButton("Other");
	        btnOther.setToolTipText("Filter any other events");
	        btnOther.addActionListener(this);
	        
	        JLabel lblFilters = new JLabel("filters: ");
	      
	        toolBar.add(lblFilters);
	        toolBar.add(btnCompetencyManager);
	        toolBar.add(btnExecution);
	        toolBar.add(btnWorldModel);
	        toolBar.add(btnBlackBoard);
	        toolBar.add(btnCompetencies);
	        toolBar.add(btnSamgar);
	        toolBar.add(btnOther);
	        
	        //Add components to this panel.
	        add(toolBar, BorderLayout.NORTH);
	        add(scrollPane, BorderLayout.CENTER);
	    }

	    public synchronized void addEvent(String text, CmionEvent evt) 
	    {
			eventTexts.add(text);
			events.add(evt);
	    	if (!isFiltered(evt))
	    	{
	    		textArea.append(text + "\n");
	    	}
	    }

	    /** determines wether a certain event should be filtered or displayed 
	     *  by looking at the state of the GUI filter buttons */
	    private synchronized boolean isFiltered(CmionEvent evt)
	    {
			if (evt.getOriginator()==null) 
				return btnOther.isSelected();		
			else if (evt.getOriginator() instanceof CompetencyManager)
				return btnCompetencyManager.isSelected();
			else if (evt.getOriginator() instanceof CompetencyExecution)
				return btnExecution.isSelected();
			else if (evt.getOriginator() instanceof WorldModel)
				return btnWorldModel.isSelected();	    	
			else if (evt.getOriginator() instanceof BlackBoard)
				return btnBlackBoard.isSelected();	  
			else if (evt.getOriginator() instanceof Competency)
				return btnCompetencies.isSelected();
			else if (evt.getOriginator() instanceof SamgarConnector)
				return btnSamgar.isSelected();
			else return btnOther.isSelected();
	    }
	    
	    private synchronized void updateList()
	    {
	    	textArea.setText("");
	    	
	    	for (int i=0; i<events.size(); i++)
	    		if (!isFiltered(events.get(i)))
	    			textArea.append(eventTexts.get(i) + "\n");
	    	
	    	textArea.setCaretPosition(textArea.getDocument().getLength());
	    }
	    
		@Override
		public void actionPerformed(ActionEvent arg0) 
		{
			updateList();
		}

	}
}
