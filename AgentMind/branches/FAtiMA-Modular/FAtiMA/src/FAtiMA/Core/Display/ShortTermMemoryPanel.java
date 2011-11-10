/** 
 * ShortTermMemoryPanel.java - Tab panel for ShortTermMemory
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
 * Company: HWU
 * Project: LIREC
 * Created: 12/03/09 
 * @author: Meiyii Lim
 * Email to: myl@macs.hw.ac.uk
 * 
 * History: 
 * Meiyii : 12/03/09 - File created
 * **/

package FAtiMA.Core.Display;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.Core.memory.semanticMemory.SemanticMemory;

public class ShortTermMemoryPanel extends AgentDisplayPanel implements Runnable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private STMRecordDisplay _stmRecordDisplay;
    private JPanel _memoryRecords;
  
   
    private JPanel _workingFactsPanel;
    private KBTable _KBTable;
    
    private AgentModel _agentModelTemp;
 
    
    public ShortTermMemoryPanel() {
        
        super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
      	_memoryRecords = new JPanel();
      	_memoryRecords.setBorder(BorderFactory.createTitledBorder("Memory Records"));
      	_memoryRecords.setLayout(new BoxLayout(_memoryRecords,BoxLayout.Y_AXIS));
    	_memoryRecords.setMinimumSize(new Dimension(850,300));
      	_memoryRecords.setMaximumSize(new Dimension(850,300));
      	
      	_stmRecordDisplay = new STMRecordDisplay();
        _memoryRecords.add(_stmRecordDisplay.getSTMRecordPanel());
		
		JScrollPane eventsScrool = new JScrollPane(_memoryRecords);
		
		this.add(eventsScrool);
	     
		_workingFactsPanel = new JPanel();
		_workingFactsPanel.setBorder(BorderFactory.createTitledBorder("Working Memory"));
		_workingFactsPanel.setLayout(new BoxLayout(_workingFactsPanel,BoxLayout.Y_AXIS));
		_workingFactsPanel.setMaximumSize(new Dimension(400,600));
	
		
		_KBTable = new KBTable(new DefaultTableModel());
		JScrollPane KBScroll = new JScrollPane(_KBTable);
		
		_workingFactsPanel.add(KBScroll);
		
			
		this.add(_workingFactsPanel);
    }
    
    public boolean Update(AgentCore ag)
    {
    	return Update((AgentModel) ag);
    }
    
    public void run()
    {       
        SemanticMemory sm = _agentModelTemp.getMemory().getSemanticMemory();
        	
        
        
        _stmRecordDisplay.update(_agentModelTemp);
        
        ListIterator<KnowledgeSlot> li = sm.GetFactList().listIterator();
        
        KnowledgeSlot slot;     
        
        
        li = sm.GetFactList().listIterator();
        ArrayList<KnowledgeSlot> changeList = sm.GetChangeList();
       
        
        _KBTable.Clear();
        
        while (li.hasNext()) {
            slot = (KnowledgeSlot) li.next();
            if (changeList.contains(slot))
            {
            	_KBTable.AddKBFact(slot.getDisplayName(), slot.getValue(),true);
            }
            else
            {
            	_KBTable.AddKBFact(slot.getDisplayName(), slot.getValue(),false);
            } 
        }
        synchronized(sm)
        {
        	sm.ClearChangeList();
        }
    }
    
    
    public boolean Update(AgentModel am) {
    	
    	_agentModelTemp = am;

    	SwingUtilities.invokeLater(this);

    	return true;
    	
    }
}

