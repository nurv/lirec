/** 
 * MemoryTable.java - Swing Table used to store the memory fields retrieved from Episodic Memory
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

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


import FAtiMA.Core.memory.episodicMemory.ActionDetail;

public class MemoryTable extends JTable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private DefaultTableModel _model;
	
	public class MyTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MyTableModel() {
			super();
			
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}
	
	public MemoryTable(DefaultTableModel model)
	{
		super(model);
		this._model = model;
		
		this._model.addColumn("ID");
        this._model.addColumn("Subject");
        this._model.addColumn("Action");
        this._model.addColumn("Intention");
        this._model.addColumn("Target");
        this._model.addColumn("Status");
        this._model.addColumn("Meaning");
        this._model.addColumn("Path");
        this._model.addColumn("Object");
        this._model.addColumn("Desirability");
        this._model.addColumn("Praiseworthiness");
        this._model.addColumn("Feeling");
        this._model.addColumn("Time");
        this._model.addColumn("Location");
		
	}
	
	public void AddMemoryDetail(ActionDetail detail)
	{
		Object[] rowData = new Object[_model.getColumnCount()];
		
		rowData[0] = detail.getID();
		rowData[1] = detail.getSubject();
		rowData[2] = detail.getAction();
		rowData[3] = detail.getIntention();
        rowData[4] = detail.getTarget();
        rowData[5] = detail.getStatus();
        rowData[6] = detail.getSpeechActMeaning();
        rowData[7] = detail.getMultimediaPath();
        rowData[8] = detail.getObject();
        rowData[9] = detail.getDesirability();
        rowData[10] = detail.getPraiseworthiness();
        rowData[11] = detail.getEmotion().getType() + "-" + detail.getEmotion().GetPotential();
        rowData[12] = detail.getTime(); 
        rowData[13] = detail.getLocation();
        
        this._model.addRow(rowData);
	}
	
	public void ClearRows()
	{
		while(this._model.getRowCount() > 0)
		{
			this._model.removeRow(0);
		}
	}
}
