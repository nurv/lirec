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

import java.awt.Color;
import java.awt.Dimension;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;

public class STMRecordDisplay {

	private JPanel _panel;
    private JPanel _details;

    
    public STMRecordDisplay(AgentModel am) {

    	_panel = new JPanel();
        _panel.setBorder(BorderFactory.createEtchedBorder());
        _panel.setLayout(new BoxLayout(_panel,BoxLayout.Y_AXIS));
        _panel.setMaximumSize(new Dimension(800,300));
        _panel.setMinimumSize(new Dimension(800,300));
        
        //DETAILS
        _details = new JPanel();
        _details.setBorder(BorderFactory.createTitledBorder("Details"));
        _details.setLayout(new BoxLayout(_details,BoxLayout.Y_AXIS));
        
        JPanel aux = new JPanel();
        aux.setLayout(new BoxLayout(aux,BoxLayout.X_AXIS));
        aux.setMinimumSize(new Dimension(800,30));
        aux.setMaximumSize(new Dimension(800,30));
        
        JLabel lbl = new JLabel("ID");
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(30,30));
        lbl.setMaximumSize(new Dimension(30,30));
        aux.add(lbl);
        
        lbl = new JLabel("Subject"); // Who?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(50,30));
        lbl.setMaximumSize(new Dimension(50,30));
        aux.add(lbl);
        
        lbl = new JLabel("Action"); // What?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Intention"); // Goal?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Target"); // Whom?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Status"); // Activation, Success, Failure?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Meaning"); // Which speechAct?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Path"); // Multimedia directory
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Object"); // object/third person
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Desirability"); // Desirable?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        lbl = new JLabel("Praiseworthiness"); // Praiseworthy?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(80,30));
        lbl.setMaximumSize(new Dimension(80,30));
        aux.add(lbl);
        
        /*lbl = new JLabel("Parameters"); // How?
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(100,30));
        lbl.setMaximumSize(new Dimension(100,30));
        aux.add(lbl);*/
        
        lbl = new JLabel("Feeling");
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(100,30));
        lbl.setMaximumSize(new Dimension(100,30));
        aux.add(lbl);
        
        /*lbl = new JLabel("Evaluation");
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(150,30));
        lbl.setMaximumSize(new Dimension(150,30));
        aux.add(lbl);*/
        
        lbl = new JLabel("Time");
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(100,30));
        lbl.setMaximumSize(new Dimension(100,30));
        aux.add(lbl);
        
        lbl = new JLabel("Location");
        lbl.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl.setMinimumSize(new Dimension(100,30));
        lbl.setMaximumSize(new Dimension(100,30));
        aux.add(lbl);
        
        _details.add(aux);
        
        JPanel prop = new JPanel();
		prop.setLayout(new BoxLayout(prop,BoxLayout.Y_AXIS));
		prop.setMaximumSize(new Dimension(650,300));
		prop.setMinimumSize(new Dimension(650,300));
		
		JScrollPane propertiesScroll = new JScrollPane(prop);
		propertiesScroll.setAutoscrolls(true);
		
		ListIterator<ActionDetail> li = am.getMemory().getEpisodicMemory().getDetails().listIterator();
	
		ArrayList<FAtiMA.Core.memory.episodicMemory.ActionDetail> newRecords = am.getMemory().getEpisodicMemory().GetNewRecords();
		
		while(li.hasNext())
		{
			ActionDetail actionDetail = (ActionDetail) li.next();
			RecordDetailPanel recordDetailPanel = new RecordDetailPanel(actionDetail);
			
			if (newRecords.contains(actionDetail))
				recordDetailPanel.setBackground(new Color(255,255,0));
				
			prop.add(recordDetailPanel);
		}
		synchronized(am.getMemory().getEpisodicMemory())
		{
			am.getMemory().getEpisodicMemory().ClearNewRecords();
		}
	
		_details.add(propertiesScroll);
        
		_panel.add(_details);
		
    }
    
    public JPanel getSTMRecordPanel()
    {
    	return this._panel;
    }
    
}
