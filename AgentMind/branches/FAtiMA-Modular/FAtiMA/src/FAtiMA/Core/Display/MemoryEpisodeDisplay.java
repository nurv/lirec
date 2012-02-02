/** 
 * MemoryEpisodeDisplay.java - 
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
 * Created: 20/Jul/2006 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 20/Jul/2006 - File created
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
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;



public class MemoryEpisodeDisplay {

	JPanel _panel;
	JTextArea _abstract;
    JTextArea _time;
    JTextArea _people;
    JTextArea _location;
    JTextArea _objects;
    
    MemoryTable _table;
    
    public MemoryEpisodeDisplay(MemoryEpisode episode) {
    			
    	_panel = new JPanel();
        _panel.setBorder(BorderFactory.createEtchedBorder());
        _panel.setLayout(new BoxLayout(_panel,BoxLayout.Y_AXIS));
        _panel.setMaximumSize(new Dimension(750,250));
        _panel.setMinimumSize(new Dimension(750,250));
        
        Dimension d1 = new Dimension(100,20);
        Dimension d2 = new Dimension(100,100);
        Dimension d3 = new Dimension(115,80);

        JPanel pnl = new JPanel();
        pnl.setLayout(new BoxLayout(pnl,BoxLayout.X_AXIS));
        pnl.setMaximumSize(new Dimension(750,100));
         
        //TIME
        JPanel aux = new JPanel();
        aux.setLayout(new BoxLayout(aux,BoxLayout.Y_AXIS));
        aux.setMaximumSize(d2);
        aux.setMinimumSize(d2);
        aux.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        JLabel lbl = new JLabel("Time");
        lbl.setMaximumSize(d1);
        lbl.setMinimumSize(d1);
        aux.add(lbl);
        _time = new JTextArea(episode.getTime().toString());
        _time.setLineWrap(true);
        _time.setMaximumSize(d3);
        _time.setMinimumSize(d3);
        aux.add(_time);
        pnl.add(aux);
        
        //PEOPLE 
        aux = new JPanel();
        aux.setLayout(new BoxLayout(aux,BoxLayout.Y_AXIS));
        aux.setMaximumSize(d2); 
        aux.setMinimumSize(d2);
        aux.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl = new JLabel("People");
        lbl.setMaximumSize(d1);
        lbl.setMinimumSize(d1);
        aux.add(lbl);
        _people = new JTextArea(episode.getPeople().toString());
        _people.setLineWrap(true);
        _people.setMaximumSize(d3);
        _people.setMinimumSize(d3);
        aux.add(_people);
        pnl.add(aux);
        
        //LOCATION
        aux = new JPanel();
        aux.setLayout(new BoxLayout(aux,BoxLayout.Y_AXIS));
        aux.setMaximumSize(d2); 
        aux.setMinimumSize(d2);
        aux.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl = new JLabel("Location");
        lbl.setMaximumSize(d1);
        lbl.setMinimumSize(d1);
        aux.add(lbl);
        _location = new JTextArea(episode.getLocation().toString());
        _location.setLineWrap(true);
        _location.setMaximumSize(d3);
        _location.setMinimumSize(d3);
        aux.add(_location);
        pnl.add(aux);
        
        //OBJECTS
        aux = new JPanel();
        aux.setLayout(new BoxLayout(aux,BoxLayout.Y_AXIS));
        aux.setMaximumSize(d2); 
        aux.setMinimumSize(d2);
        aux.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
        lbl = new JLabel("Objects");
        lbl.setMaximumSize(d1);
        lbl.setMinimumSize(d1);
        aux.add(lbl);
        _objects = new JTextArea(episode.getObjects().toString());
        _objects.setLineWrap(true);
        _objects.setMaximumSize(d3);
        _objects.setMinimumSize(d3);
        aux.add(_objects);
        pnl.add(aux);
        
        _panel.add(pnl);
        
        _table = new MemoryTable(new DefaultTableModel());
        JScrollPane scrollPane = new JScrollPane(_table);
		_panel.add(scrollPane);
        
		
        for(ActionDetail ad : episode.getDetails())
        {
        	_table.AddMemoryDetail(ad);
        }
    }
    
    public JPanel getMemoryEpisodePanel()
    {
    	return this._panel;
    }
    
    public int countMemoryDetails()
    {
    	return this._table.getRowCount();
    }
}
