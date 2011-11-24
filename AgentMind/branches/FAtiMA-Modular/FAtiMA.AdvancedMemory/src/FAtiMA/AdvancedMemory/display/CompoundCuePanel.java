/** 
 * CompoundCuePanel.java - Display panel for the Compound Cue mechanism
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
 * Created: 21/11/11
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 21/11/11 - File created
 * 
 * **/

package FAtiMA.AdvancedMemory.display;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;
import FAtiMA.AdvancedMemory.CompoundCue;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;

public class CompoundCuePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private AdvancedMemoryComponent advancedMemoryComponent;

	private CompoundCue compoundCue;

	private JTextField tfTargetID;

	private TableModelCompoundCue tmResults;

	private JLabel lbStatus;

	public CompoundCuePanel(AdvancedMemoryComponent advancedMemoryComponent) {
		super();
		this.advancedMemoryComponent = advancedMemoryComponent;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel pnActions = new JPanel();
		pnActions.setLayout(new BoxLayout(pnActions, BoxLayout.X_AXIS));
		pnActions.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnActions);

		JLabel lbTargetID = new JLabel("Target ID:");
		pnActions.add(lbTargetID);
		tfTargetID = new JTextField("0");
		tfTargetID.setMinimumSize(new Dimension(80, 26));
		tfTargetID.setMaximumSize(new Dimension(80, 26));
		pnActions.add(tfTargetID);

		JButton btCalculate = new JButton("Calculate Evaluation Values");
		btCalculate.addActionListener(new AlCalculate());
		pnActions.add(btCalculate);

		JButton btStoreResult = new JButton("Store Result");
		btStoreResult.addActionListener(new AlStoreResult());
		pnActions.add(btStoreResult);

		JPanel pnResults = new JPanel();
		pnResults.setLayout(new BoxLayout(pnResults, BoxLayout.Y_AXIS));
		pnResults.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnResults);

		tmResults = new TableModelCompoundCue();
		tmResults.addColumn("Action Detail ID");
		tmResults.addColumn("Similarity Score");
		JTable tResults = new JTable(tmResults);
		tResults.setAutoCreateRowSorter(true);
		JScrollPane spResults = new JScrollPane(tResults);
		pnResults.add(spResults);

		JPanel pnStatus = new JPanel();
		pnStatus.setLayout(new BoxLayout(pnStatus, BoxLayout.X_AXIS));
		this.add(pnStatus);

		lbStatus = new JLabel(" ");
		pnStatus.add(lbStatus);

	}

	public CompoundCue getCompoundCue() {
		return compoundCue;
	}

	public void setCompoundCue(CompoundCue compoundCue) {
		this.compoundCue = compoundCue;
	}

	private class AlCalculate implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			calculate();
		}
	}

	private void calculate() {

		// update status
		lbStatus.setText("Executing Compound Cue mechanism...");

		Integer actionDetailTargetID = null;
		try {
			actionDetailTargetID = Integer.valueOf(tfTargetID.getText());
		} catch (Exception e) {
			lbStatus.setText("Error while parsing Target ID!");
			return;
		}

		// obtain target action detail
		ActionDetail actionDetailTarget = null;
		for (MemoryEpisode memoryEpisode : advancedMemoryComponent.getMemory().getEpisodicMemory().getAM().GetAllEpisodes()) {
			for (ActionDetail actionDetail : memoryEpisode.getDetails()) {
				if (actionDetail.getID() == actionDetailTargetID.intValue()) {
					actionDetailTarget = actionDetail;
				}
			}
		}
		for (ActionDetail actionDetail : advancedMemoryComponent.getMemory().getEpisodicMemory().getSTEM().getDetails()) {
			if (actionDetail.getID() == actionDetailTargetID.intValue()) {
				actionDetailTarget = actionDetail;
			}
		}

		// check if target action detail exists
		if (actionDetailTarget == null) {
			lbStatus.setText("Action Detail with target ID does not exist!");
		} else {

			// execute compound cue mechanism
			CompoundCue compoundCue = new CompoundCue();
			compoundCue.execute(advancedMemoryComponent.getMemory().getEpisodicMemory(), actionDetailTarget);
			this.compoundCue = compoundCue;

			// update panel
			updatePanel();
		}

	}

	public void updatePanel() {

		// update target id
		tfTargetID.setText(String.valueOf(compoundCue.getActionDetailTargetID()));

		// clear table model
		int rowCount = tmResults.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tmResults.removeRow(0);
		}

		// update table model			
		for (Integer id : compoundCue.getEvaluationValues().keySet()) {
			Object[] data = new Object[2];
			data[0] = id;
			data[1] = compoundCue.getEvaluationValues().get(id);
			tmResults.addRow(data);
		}

		// update status
		lbStatus.setText("Compound Cue mechanism executed at " + compoundCue.getTime().getRealTimeFormatted());

	}

	private class AlStoreResult implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (compoundCue != null) {
				advancedMemoryComponent.getResults().add(compoundCue);
				advancedMemoryComponent.getAdvancedMemoryPanel().getOverviewPanel().updateResultList();
				lbStatus.setText("Result stored!");
			} else {
				lbStatus.setText("Result is null and was not stored!");
			}
		}
	}

	private class TableModelCompoundCue extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public TableModelCompoundCue() {
			super();
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return Integer.class;
			case 1:
				return Double.class;
			default:
				return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}

}
