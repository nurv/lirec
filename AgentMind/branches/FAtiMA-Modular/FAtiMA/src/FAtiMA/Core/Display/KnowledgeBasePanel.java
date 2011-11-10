/** 
 * KnowledgeBasePanel.java - Graphical Swing Panel that shows all the facts stored in the
 * KnowledgeBase
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
 * Created: 26/10/2005 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 26/10/2005 - File created
 */

package FAtiMA.Core.Display;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;



public class KnowledgeBasePanel extends AgentDisplayPanel {

    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
    private KBTable _table;
    
    public KnowledgeBasePanel() {
        super();
        this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setMaximumSize(new Dimension(400,600));
        //panel.setMinimumSize(new Dimension(400,700));
               
        _table = new KBTable(new DefaultTableModel());
        		
		JScrollPane KBScroll = new JScrollPane(_table);
		
		panel.add(KBScroll);
		
		this.add(panel);
		
    }
    
    public boolean Update(AgentCore ag)
    {
    	return Update((AgentModel)ag);
    }
   
    public boolean Update(AgentModel am) {
              
        ListIterator<KnowledgeSlot> li = am.getMemory().getSemanticMemory().GetKnowledgeBaseFacts().listIterator(); 
        
        KnowledgeSlot slot;
        int index;
        
        boolean newFactAdded = false;
        
        while (li.hasNext()) {
            index = li.nextIndex();
            slot = (KnowledgeSlot) li.next();
            if(index >= _table.getRowCount())
            {
            //if(index >=  _knowledgeFactList.size()) {
                newFactAdded = true;
                _table.AddKBFact(slot.getDisplayName(),slot.getValue());
            }
            else {
                _table.SetRow(index, slot.getDisplayName(), slot.getValue());
            }
        }
        
        return newFactAdded;
    }

}
