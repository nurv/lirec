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

package FAtiMA.Display;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Agent;
import FAtiMA.memory.KnowledgeSlot;
import FAtiMA.memory.shortTermMemory.WorkingMemory;
import FAtiMA.memory.shortTermMemory.STMemoryRecord;
import FAtiMA.memory.shortTermMemory.ShortTermMemory;
import FAtiMA.knowledgeBase.KnowledgeBase;

public class ShortTermMemoryPanel extends AgentDisplayPanel {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private STMRecordDisplay _stmRecordDisplay;
    private JPanel _memoryRecords;
    
    //private JPanel _knowledgePanel;
    private static int _knowledgeSize = 0;
    private ArrayList _knowledgeFactList;
    private JPanel _knowledgeFactsPanel;
    
    private ArrayList _workingFactList;
    private JPanel _workingFactsPanel;
    
    public ShortTermMemoryPanel() {
        
        super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
      	_memoryRecords = new JPanel();
      	_memoryRecords.setBorder(BorderFactory.createTitledBorder("Memory Records"));
      	_memoryRecords.setLayout(new BoxLayout(_memoryRecords,BoxLayout.Y_AXIS));
    	_memoryRecords.setMinimumSize(new Dimension(850,300));
      	_memoryRecords.setMaximumSize(new Dimension(850,300));      
		
		JScrollPane eventsScrool = new JScrollPane(_memoryRecords);
		
		this.add(eventsScrool);
		
		_knowledgeFactList = new ArrayList();
	       
	    _knowledgeFactsPanel = new JPanel();
	    _knowledgeFactsPanel.setBorder(BorderFactory.createTitledBorder("Long Term Memory"));
	    _knowledgeFactsPanel.setLayout(new BoxLayout(_knowledgeFactsPanel,BoxLayout.X_AXIS));
		_knowledgeFactsPanel.setMaximumSize(new Dimension(850,200));
		_knowledgeFactsPanel.setMinimumSize(new Dimension(850,200));
		JScrollPane knowledgeFactScroll = new JScrollPane(_knowledgeFactsPanel);
		knowledgeFactScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);			
		this.add(knowledgeFactScroll);
		
		_workingFactList = new ArrayList();
	        
		_workingFactsPanel = new JPanel();
		_workingFactsPanel.setBorder(BorderFactory.createTitledBorder("Working Memory"));
		_workingFactsPanel.setLayout(new BoxLayout(_workingFactsPanel,BoxLayout.Y_AXIS));
		_workingFactsPanel.setMaximumSize(new Dimension(850,300));
		_workingFactsPanel.setMinimumSize(new Dimension(850,300));
	    
		JScrollPane workingFactScroll = new JScrollPane(_workingFactsPanel);
		workingFactScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);	
		this.add(workingFactScroll);
		
		/*_knowledgePanel = new JPanel();
		_knowledgePanel.setBorder(BorderFactory.createEmptyBorder());
		_knowledgePanel.setLayout(new BoxLayout(_knowledgePanel,BoxLayout.X_AXIS));		
		_knowledgePanel.setMaximumSize(new Dimension(750,300));
		_knowledgePanel.setMinimumSize(new Dimension(750,300));
		_knowledgePanel.add(_knowledgeFactsPanel);
		_knowledgePanel.add(_workingFactsPanel);*/
    }
    
    
    public boolean Update(Agent ag) {
    	
        _memoryRecords.removeAll();
        	
        synchronized(ShortTermMemory.GetInstance().GetSyncRoot()){
        	STMemoryRecord records = ShortTermMemory.GetInstance().GetAllRecords();
        	_stmRecordDisplay = new STMRecordDisplay(records);
            _memoryRecords.add(_stmRecordDisplay.getSTMRecordPanel());   	
        }     
        
        ListIterator li = KnowledgeBase.GetInstance().GetFactList();
        
        KnowledgeSlot slot;
        KnowledgeFactDisplay kDisplay;
        int index;         
        
        if (KnowledgeBase.GetInstance().Count() >= _knowledgeSize)
        {
        	 _knowledgeFactsPanel.removeAll();
        	 _knowledgeSize = KnowledgeBase.GetInstance().Count();
        }
        
        while (li.hasNext()) {
            index = li.nextIndex();
            slot = (KnowledgeSlot) li.next();
            if(index >= _knowledgeFactList.size()) {
                kDisplay = new KnowledgeFactDisplay(slot.getName(),slot.getValue().toString());
                _knowledgeFactList.add(kDisplay);
                _knowledgeFactsPanel.add(kDisplay.GetPanel());
            }
        }
        
        li = WorkingMemory.GetInstance().GetFactList();
        _workingFactList.clear(); 
        _workingFactsPanel.removeAll();
        
        while (li.hasNext()) {
            index = li.nextIndex();
            slot = (KnowledgeSlot) li.next();
            kDisplay = new KnowledgeFactDisplay(slot.getName(),slot.getValue().toString());
            _workingFactList.add(kDisplay);
            _workingFactsPanel.add(kDisplay.GetPanel());
        }
  
        return true;
    }
}

