/** 
 * STMRecordDisplay.java - Tab panel for ShortTermMemory Records
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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;

public class STMRecordDisplay {

	private JPanel _panel;
    private JPanel _details;
    private MemoryTable _table;

    
    public STMRecordDisplay() {

    	_panel = new JPanel();
        _panel.setBorder(BorderFactory.createEtchedBorder());
        _panel.setLayout(new BoxLayout(_panel,BoxLayout.Y_AXIS));
        _panel.setMaximumSize(new Dimension(800,300));
        _panel.setMinimumSize(new Dimension(800,300));
        
        //DETAILS
        _details = new JPanel();
        _details.setBorder(BorderFactory.createTitledBorder("Details"));
        _details.setLayout(new BoxLayout(_details,BoxLayout.Y_AXIS));
        
        _table = new MemoryTable(new DefaultTableModel());

        JScrollPane scrollPane = new JScrollPane(_table);
		_details.add(scrollPane);

		
		//ListIterator<ActionDetail> li = am.getMemory().getEpisodicMemory().getDetails().listIterator();
	
		//ArrayList<FAtiMA.Core.memory.episodicMemory.ActionDetail> newRecords = am.getMemory().getEpisodicMemory().GetNewRecords();
		
		/*while(li.hasNext())
		{
			ActionDetail actionDetail = (ActionDetail) li.next();
			//RecordDetailPanel recordDetailPanel = new RecordDetailPanel(actionDetail);
			
			//if (newRecords.contains(actionDetail))
			//	recordDetailPanel.setBackground(new Color(255,255,0));
			
			_table.AddMemoryDetail(actionDetail);
				
		}*/
		/*synchronized(am.getMemory().getEpisodicMemory())
		{
			am.getMemory().getEpisodicMemory().ClearNewRecords();
		}*/
        
		_panel.add(_details);
		
    }
    
    public JPanel getSTMRecordPanel()
    {
    	return this._panel;
    }
    
    public void update(AgentModel am)
    {
    	_table.ClearRows();
    	
    	for(ActionDetail detail : am.getMemory().getEpisodicMemory().getDetails())
    	{
    		_table.AddMemoryDetail(detail);
    	}
    }
    
}
