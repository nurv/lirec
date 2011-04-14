/** 
 * AgentDisplay.java - Graphical Swing Display used to show the internal state of the 
 * agent's mind: its emotional state, active goals and intentions, knowledge
 * about the world, etc...
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 18/10/2005 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 18/10/2005 - File created
 * Matthias Keysermann: 13/04/2011 - added panel for Activation-Based Selection
 */

package FAtiMA.Core.Display;

import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import FAtiMA.Core.AgentCore;


public class AgentDisplay {
    JFrame _frame;
    JTabbedPane _displayPane;
    
    AgentCore _ag;
    
    ArrayList<String> _componentTabsNames;
    
    public AgentDisplay(AgentCore ag) {
        
        _ag = ag;
        
        _componentTabsNames = new ArrayList<String>();
        
        _frame = new JFrame(ag.displayName());
        _frame.getContentPane().setLayout(new BoxLayout(_frame.getContentPane(),BoxLayout.Y_AXIS));
		_frame.setSize(850,650);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		_displayPane = new JTabbedPane();
		_frame.getContentPane().add(_displayPane);
		
		JPanel panel;
		
		panel = new EmotionalStatePanel();
		_displayPane.addTab("Emotional State",null,panel,"displays the character's emotional state");
		
		panel = new KnowledgeBasePanel();
		_displayPane.addTab("Knowledge Base",null,panel,"displays all information stored in the KB");
		
		panel = new EpisodicMemoryPanel();
		_displayPane.addTab("Episodic Memory", null, panel, "displays all the records in the character's episodic memory");
	
		panel = new ShortTermMemoryPanel();
		_displayPane.addTab("Short Term Memory", null, panel, "displays all the records in the character's short term memory");
		
		panel = new ABSelectionPanel();
		_displayPane.addTab("Activation-Based Selection", null, panel, "displays information related to Activation-Based Selection");
		
		JButton teste = new JButton("Save");
		teste.addActionListener(new TestAction(ag));
		teste.setText("Save");
		teste.setEnabled(true);
		_frame.getContentPane().add(teste);
		
		JButton sMemory = new JButton("Save Memory");
		sMemory.addActionListener(new SaveMemory(ag));
		sMemory.setText("Save Memory");
		sMemory.setEnabled(true);
		_frame.getContentPane().add(sMemory);
		_frame.setVisible(true);		
    }
    
    public void AddPanel(AgentDisplayPanel panel, String title, String description)
    {
    	_displayPane.addTab(title,null,panel, description);
    	_componentTabsNames.add(title);
    }

    public void clearAllComponentTabs()
    {
    	for (String title : _componentTabsNames)
    	{
    		int tabIndex = _displayPane.indexOfTab(title);
    		if (tabIndex!=-1)
    		{
    			_displayPane.removeTabAt(tabIndex);
    		}		
    	} 
    	 _componentTabsNames.clear();
    }
    
    public void update() {
        AgentDisplayPanel pnl = (AgentDisplayPanel) _displayPane.getSelectedComponent();
        if(pnl.Update(_ag)) _frame.setVisible(true);
    }
    
    public void dispose() {
    	_frame.dispose();
    }
}
