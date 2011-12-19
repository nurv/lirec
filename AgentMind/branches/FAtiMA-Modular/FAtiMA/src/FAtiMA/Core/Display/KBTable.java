/** 
 * KBTable.java - Swing Table used to display the knowledge facts stored in the KB
 *  
 * Copyright (C) 2011 GAIPS/INESC-ID 
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
 * Company: INESC-ID
 * Project: LIREC
 * Created: 07/07/11 
 * @author: Joao dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: 07/07/11 - File created
 **/

package FAtiMA.Core.Display;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

public class KBTable extends JTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DefaultTableModel _model;
	private ArrayList<Integer> _highlighted;
	
	public class MyTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MyTableModel() {
			super();
			
		}
		
		@Override
		public Class getColumnClass(int column) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}
	
	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component component = super.prepareRenderer(renderer, row, column);
		
		if(_highlighted.contains(new Integer(row)))
		{
			component.setBackground(Color.YELLOW);
		}
		else
		{
			component.setBackground(Color.WHITE);
		}
		
		return component;
	}
	
	public KBTable(DefaultTableModel model)
	{
		super(model);
		this._model = model;
		
		this._model.addColumn("Name");
        this._model.addColumn("Value");
        
        TableRowSorter<DefaultTableModel> tableRowSorter = new TableRowSorter<DefaultTableModel>(model);
		this.setRowSorter(tableRowSorter);
        
        this._highlighted = new ArrayList<Integer>();
	}
	
	public void AddKBFact(String name, Object value, boolean highlighted)
	{
		Object[] rowData = new Object[_model.getColumnCount()];
		
		rowData[0] = name;
		rowData[1] = String.valueOf(value);
        
        this._model.addRow(rowData);
        if(highlighted)
        {
        	this._highlighted.add(new Integer(this._model.getRowCount()-1));
        }
	}
	
	public void AddKBFact(String name, Object value)
	{
		AddKBFact(name, value, false);
	}
	
	public void SetRow(int row, String name, Object value)
	{
		this._model.setValueAt(name, row, 0);
		this._model.setValueAt(value, row, 1);
	}
	
	public void Clear()
	{	
		while(this._model.getRowCount() > 0)
		{
			this._model.removeRow(0);
		}
		
		this._highlighted.clear();
	}
}
