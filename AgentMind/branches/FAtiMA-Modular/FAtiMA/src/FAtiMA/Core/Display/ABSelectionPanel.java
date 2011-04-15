/** 
 * ABSelectionPanel.java - Tab panel for Activation-Based Selection
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
 * Created: 13/04/11 
 * @author: Matthias Keysermann
 * Email to: muk7@hw.ac.uk
 * 
 * History: 
 * Matthias Keysermann: 13/04/11 - File created
 * **/

package FAtiMA.Core.Display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;
import FAtiMA.Core.memory.episodicMemory.Time;

import javax.swing.*;
import javax.swing.table.*;

public class ABSelectionPanel extends AgentDisplayPanel {

	private static final long serialVersionUID = 1L;

	private EpisodicMemory episodicMemory;
	private JSpinner spDecayValue;
	private JCheckBox cbConstantUpdate;
	private JLabel lbCalculationStatus;
	private JRadioButton rbSelectionCount;
	private JTextField spSelectionCount;
	private JRadioButton rbSelectionRatio;
	private JSpinner spSelectionRatio;
	private JRadioButton rbSelectionThreshold;
	private JTextField tfSelectionThreshold;
	private JCheckBox cbSimulateSelection;
	private JLabel lbSelectionStatus;
	private MyTable table;
	private MyTableModel tableModel;
	private ArrayList<Integer> selectedIDs;
	private double activationValueMin;
	private double activationValueMax;

	private class MyTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MyTableModel() {
			super();
		}

		@Override
		public Class getColumnClass(int column) {
			switch (column) {
			case 0:
				return Integer.class;
			case 9:
				return Long.class;
			case 11:
				return Double.class;
			case 12:
				return Integer.class;
			default:
				return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}

	private class MyTable extends JTable {

		private static final long serialVersionUID = 1L;

		public MyTable(TableModel tableModel) {
			super(tableModel);
		}

		@Override
		public Component prepareRenderer(TableCellRenderer renderer, int row,
				int column) {
			Component component = super.prepareRenderer(renderer, row, column);
			int rowID = (Integer) table.getValueAt(row, 0);
			if (selectedIDs.contains(rowID)) {
				component.setBackground(Color.YELLOW);
			} else {
				double activationValue = (Double) table.getValueAt(row, 11);
				double range = activationValueMax - activationValueMin;
				int grey = (int) Math
						.round((activationValue - activationValueMin) / range
								* 155) + 100;
				if (grey < 0)
					grey = 0;
				if (grey > 255)
					grey = 255;
				component.setBackground(new Color(grey, grey, grey));
				// component.setBackground(Color.WHITE);
			}
			return component;
		}
	}

	private class AlABCalculation implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			calculateValues();
			updateTable();
		}
	}

	private class AlABSelection implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			performSelection();
		}
	}

	public ABSelectionPanel() {
		super();
		selectedIDs = new ArrayList<Integer>();
		createGUI();
	}

	private void createGUI() {

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel pnActions = new JPanel();
		pnActions.setBorder(BorderFactory.createEtchedBorder());
		pnActions.setLayout(new BoxLayout(pnActions, BoxLayout.X_AXIS));
		this.add(pnActions);

		JPanel pnCalculation = new JPanel();
		pnCalculation.setBorder(BorderFactory.createEtchedBorder());
		pnCalculation.setLayout(new BoxLayout(pnCalculation, BoxLayout.Y_AXIS));
		pnCalculation.setMinimumSize(new Dimension(400, 100));
		pnCalculation.setMaximumSize(new Dimension(400, 100));
		pnActions.add(pnCalculation);

		JPanel pnSelection = new JPanel();
		pnSelection.setBorder(BorderFactory.createEtchedBorder());
		pnSelection.setLayout(new BoxLayout(pnSelection, BoxLayout.X_AXIS));
		pnSelection.setMinimumSize(new Dimension(400, 100));
		pnSelection.setMaximumSize(new Dimension(400, 100));
		pnActions.add(pnSelection);

		JPanel pnDecayValue = new JPanel();
		pnDecayValue.setLayout(new BoxLayout(pnDecayValue, BoxLayout.X_AXIS));
		pnCalculation.add(pnDecayValue);

		JLabel lbDecayValue = new JLabel("Decay Value");
		pnDecayValue.add(lbDecayValue);

		spDecayValue = new JSpinner();
		spDecayValue.setModel(new SpinnerNumberModel(0.8, 0.1, 9.9, 0.1));
		spDecayValue.setMinimumSize(new Dimension(40, 20));
		spDecayValue.setMaximumSize(new Dimension(40, 20));
		pnDecayValue.add(spDecayValue);

		JPanel pnCalculationButton = new JPanel();
		pnCalculationButton.setLayout(new BoxLayout(pnCalculationButton,
				BoxLayout.X_AXIS));
		pnCalculation.add(pnCalculationButton);

		JButton btABCalculation = new JButton("Calculate Activation Values");
		btABCalculation.addActionListener(new AlABCalculation());
		pnCalculationButton.add(btABCalculation);

		cbConstantUpdate = new JCheckBox("Constant Update");
		cbConstantUpdate.setSelected(true);
		pnCalculationButton.add(cbConstantUpdate);

		JPanel pnCalculationStatus = new JPanel();
		pnCalculationStatus.setLayout(new BoxLayout(pnCalculationStatus,
				BoxLayout.X_AXIS));
		pnCalculation.add(pnCalculationStatus);

		lbCalculationStatus = new JLabel("Calculated at narrative time -");
		pnCalculationStatus.add(lbCalculationStatus);

		JPanel pnSelectionMethod = new JPanel();
		pnSelectionMethod.setLayout(new BoxLayout(pnSelectionMethod,
				BoxLayout.Y_AXIS));
		pnSelection.add(pnSelectionMethod);

		rbSelectionCount = new JRadioButton("Count");
		pnSelectionMethod.add(rbSelectionCount);
		rbSelectionRatio = new JRadioButton("Ratio");
		pnSelectionMethod.add(rbSelectionRatio);
		rbSelectionThreshold = new JRadioButton("Threshold");
		pnSelectionMethod.add(rbSelectionThreshold);

		ButtonGroup bgSelectionMethod = new ButtonGroup();
		bgSelectionMethod.add(rbSelectionCount);
		bgSelectionMethod.add(rbSelectionRatio);
		bgSelectionMethod.add(rbSelectionThreshold);
		rbSelectionCount.setSelected(true);

		JPanel pnSelectionParams = new JPanel();
		pnSelectionParams.setLayout(new BoxLayout(pnSelectionParams,
				BoxLayout.Y_AXIS));
		pnSelection.add(pnSelectionParams);

		spSelectionCount = new JTextField();
		spSelectionCount.setText("10");
		spSelectionCount.setMinimumSize(new Dimension(60, 20));
		spSelectionCount.setMaximumSize(new Dimension(60, 20));
		pnSelectionParams.add(spSelectionCount);

		spSelectionRatio = new JSpinner();
		spSelectionRatio.setModel(new SpinnerNumberModel(0.2, 0.0, 1.0, 0.1));
		spSelectionRatio.setMinimumSize(new Dimension(60, 20));
		spSelectionRatio.setMaximumSize(new Dimension(60, 20));
		pnSelectionParams.add(spSelectionRatio);

		tfSelectionThreshold = new JTextField();
		tfSelectionThreshold.setText("-5.0");
		tfSelectionThreshold.setMinimumSize(new Dimension(60, 20));
		tfSelectionThreshold.setMaximumSize(new Dimension(60, 20));
		pnSelectionParams.add(tfSelectionThreshold);

		JPanel pnSelectionButton = new JPanel();
		pnSelectionButton.setLayout(new BoxLayout(pnSelectionButton,
				BoxLayout.Y_AXIS));
		pnSelection.add(pnSelectionButton);

		cbSimulateSelection = new JCheckBox("Simulate Selection");
		cbSimulateSelection.setSelected(true);
		cbSimulateSelection.setEnabled(false);
		pnSelectionButton.add(cbSimulateSelection);

		JButton btABSelection = new JButton("Activation-Based Selection");
		btABSelection.addActionListener(new AlABSelection());
		pnSelectionButton.add(btABSelection);

		lbSelectionStatus = new JLabel("Selected at narrative time -");
		pnSelectionButton.add(lbSelectionStatus);

		JPanel pnDetails = new JPanel();
		pnDetails.setBorder(BorderFactory.createTitledBorder("Details"));
		pnDetails.setLayout(new BoxLayout(pnDetails, BoxLayout.Y_AXIS));
		this.add(pnDetails);

		tableModel = new MyTableModel();

		tableModel.addColumn("ID");
		tableModel.addColumn("Subject");
		tableModel.addColumn("Action");
		tableModel.addColumn("Intention");
		tableModel.addColumn("Target");
		tableModel.addColumn("Status");
		tableModel.addColumn("Meaning");
		tableModel.addColumn("Object");
		tableModel.addColumn("Feeling");
		tableModel.addColumn("Time");
		tableModel.addColumn("Location");
		tableModel.addColumn("Activation");
		tableModel.addColumn("Retrievals");

		table = new MyTable(tableModel);
		table.setAutoCreateRowSorter(true);

		JScrollPane scrollPane = new JScrollPane(table);
		pnDetails.add(scrollPane);

	}

	private void updateTable() {

		ArrayList<ActionDetail> actionDetails = new ArrayList<ActionDetail>();
		// Autobiographic Memory
		for (MemoryEpisode episode : episodicMemory.getAM().GetAllEpisodes()) {
			actionDetails.addAll(episode.getDetails());
		}
		// Short-Term Memory
		actionDetails.addAll(episodicMemory.getSTEM().getDetails());

		// set initial minimum and maximum for cell colouring
		if (actionDetails.size() > 0) {
			activationValueMin = actionDetails.get(0).getActivationValue()
					.getValue();
		} else {
			activationValueMin = 0;
		}
		activationValueMax = activationValueMin;

		// clear table model
		int rowCount = tableModel.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tableModel.removeRow(0);
		}

		// add action details to table model
		for (ActionDetail actionDetail : actionDetails) {
			Object[] rowData = new Object[tableModel.getColumnCount()];
			int j = 0;

			rowData[j++] = actionDetail.getID();
			rowData[j++] = actionDetail.getSubject();
			rowData[j++] = actionDetail.getAction();
			rowData[j++] = actionDetail.getIntention();
			rowData[j++] = actionDetail.getTarget();
			rowData[j++] = actionDetail.getStatus();
			rowData[j++] = actionDetail.getSpeechActMeaning();
			rowData[j++] = actionDetail.getObject();
			rowData[j++] = actionDetail.getEmotion().getType() + "-"
					+ actionDetail.getEmotion().GetPotential();
			rowData[j++] = actionDetail.getTime().getNarrativeTime();
			rowData[j++] = actionDetail.getLocation();
			rowData[j++] = actionDetail.getActivationValue().getValue();
			rowData[j++] = actionDetail.getActivationValue().getNumRetrievals();

			tableModel.addRow(rowData);

			// save minimum and maximum for cell colouring
			double activationValue = actionDetail.getActivationValue()
					.getValue();
			if (activationValue < activationValueMin)
				activationValueMin = activationValue;
			if (activationValue > activationValueMax)
				activationValueMax = activationValue;

		}

	}

	private void calculateValues() {

		Time timeCalculated = new Time();
		double decayValue = Double.valueOf(spDecayValue.getValue().toString());
		episodicMemory.calculateActivationValues(timeCalculated, decayValue);

		lbCalculationStatus.setText("Calculated at narrative time "
				+ timeCalculated.getNarrativeTime());

	}

	private void performSelection() {

		Time timeSelected = new Time();
		ArrayList<ActionDetail> selected = new ArrayList<ActionDetail>();

		if (rbSelectionCount.isSelected()) {
			int countMax = Integer.valueOf(Integer.valueOf(spSelectionCount
					.getText()));
			selected = episodicMemory.activationBasedSelectionByCount(countMax);

		} else if (rbSelectionRatio.isSelected()) {
			double amount = Double.valueOf(spSelectionRatio.getValue()
					.toString());
			selected = episodicMemory.activationBasedSelectionByAmount(amount);

		} else if (rbSelectionThreshold.isSelected()) {
			double threshold = Double.valueOf(tfSelectionThreshold.getText());
			selected = episodicMemory
					.activationBasedSelectionByThreshold(threshold);
		}

		lbSelectionStatus.setText("Selected at narrative time "
				+ timeSelected.getNarrativeTime());

		ArrayList<Integer> selectedIDsTemp = new ArrayList<Integer>();
		for (ActionDetail actionDetail : selected) {
			selectedIDsTemp.add(new Integer(actionDetail.getID()));
		}
		selectedIDs = selectedIDsTemp;

		table.repaint();
	}

	public boolean Update(AgentCore ag) {

		episodicMemory = ag.getMemory().getEpisodicMemory();

		if (cbConstantUpdate.isSelected()) {
			calculateValues();
		}
		updateTable();

		return true;

	}

	public boolean Update(AgentModel am) {

		episodicMemory = am.getMemory().getEpisodicMemory();

		if (cbConstantUpdate.isSelected()) {
			calculateValues();
		}
		updateTable();

		return true;

	}

}
