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

import ion.Meta.Event;
import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import cmion.architecture.IArchitecture;
import cmion.architecture.CmionComponent;
import cmion.architecture.CmionEvent;


/** this class is a simple example of a monitor. This one lists all ION events within the 
 * 	simulation */
public class IonEventMonitor extends CmionComponent {

	/** the gui for the monitor */
	MonitorWindow window;
	
	/** create a new ion event monitor */
	public IonEventMonitor(IArchitecture architecture) 
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
    public void addEvent(Event evt)
    {
    	final String evtText = evt.toString();
		//Schedule a job for the event dispatch thread:
        //update list
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                window.displayNewLineOfText(evtText);
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
	    	addEvent((Event) evt);
	    }
	}
	
    
	/** frame to display events */
	private class MonitorWindow extends JPanel 
	{

		private static final long serialVersionUID = 1L;

	    protected JTextArea textArea;

	    public MonitorWindow() {
	        super(new BorderLayout());

	        textArea = new JTextArea(5, 20);
	        textArea.setEditable(false);
	        JScrollPane scrollPane = new JScrollPane(textArea);

	        //Add Components to this panel.
	        add(scrollPane, BorderLayout.CENTER);
	    }

	    public void displayNewLineOfText(String text) 
	    {
	        textArea.append(text + "\n");
	        textArea.setCaretPosition(textArea.getDocument().getLength());
	    }

	}
}
