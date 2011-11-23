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
 * Matthias Keysermann: 02/05/11 - table uses copies of corresponding objects
 * Matthias Keysermann: 02/05/11 - remove and recreate row sorter in order to minimise java.awt exceptions
 * Matthias Keysermann: 03/05/11 - added functionality to show/hide STEM events/AM events
 * Matthias Keysermann: 23/08/11 - table is no longer updated constantly (to avoid slowing down the program)
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

	private static final String NULL_STRING = "";

	// Show events from the Short-Term Episodic Memory?
	private static final boolean SHOW_STEM = false;
	// Show events from the Autobiographic Memory?
	private static final boolean SHOW_AM = true;
	// It is suggested to set these variables according to
	// AB_FORGETTING_STEM and AB_FORGETTING_AM (in EpisodicMemory.java)
	// colour cells according to absolute min and max values or relative to current?
	private static final boolean COLOURING_BY_ABSOLUTE_VALUE = true;
	private static final double ACTIVATION_VALUE_MIN = -15;
	private static final double ACTIVATION_VALUE_MAX = -5;
	// show narrative time or real time?
	private static final boolean DISPLAY_NARRATIVE_TIME = false;

	private EpisodicMemory episodicMemory;
	private JSpinner spDecayValue;
	private JCheckBox cbConstantUpdate;
	private JLabel lbCalculationStatus;
	private JRadioButton rbCount;
	private JTextField spCount;
	private JRadioButton rbRatio;
	private JSpinner spRatio;
	private JRadioButton rbThreshold;
	private JTextField tfThreshold;
	private JCheckBox cbSimulate;
	private JLabel lbExecutionStatus;
	private MyTable table;
	private MyTableModel tableModel;
	private TableRowSorter<MyTableModel> tableRowSorter;
	private ArrayList<Integer> selectedIDs;
	private ArrayList<Integer> forgetIDs;
	private double activationValueMin;
	private double activationValueMax;

	private class MyTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public MyTableModel() {
			super();
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return Integer.class;
			case 9:
				if (DISPLAY_NARRATIVE_TIME)
					return Long.class;
				else
					return String.class;
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
		public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
			Component component = super.prepareRenderer(renderer, row, column);
			int rowID = (Integer) table.getValueAt(row, 0);

			// colour by activation value
			double activationValue = (Double) table.getValueAt(row, 11);
			if (COLOURING_BY_ABSOLUTE_VALUE) {
				// colour independent of current min and max
				activationValueMin = ACTIVATION_VALUE_MIN;
				activationValueMax = ACTIVATION_VALUE_MAX;
				if (activationValue < activationValueMin) {
					activationValue = activationValueMin;
				} else if (activationValue > activationValueMax) {
					activationValue = activationValueMax;
				}
			}
			double range = activationValueMax - activationValueMin;
			int grey = (int) Math.round((activationValue - activationValueMin) / range * 155) + 100;
			if (grey < 0)
				grey = 0;
			if (grey > 255)
				grey = 255;
			component.setBackground(new Color(grey, grey, grey));

			// colour selected id's in yellow
			if (selectedIDs.contains(rowID)) {
				component.setBackground(Color.GREEN);
			}

			// colour forget id's in red
			if (forgetIDs.contains(rowID)) {
				component.setBackground(Color.RED);
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

	private class AlABForgetting implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			performForgetting();
		}
	}

	public ABSelectionPanel() {
		super();
		selectedIDs = new ArrayList<Integer>();
		forgetIDs = new ArrayList<Integer>();
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
		pnCalculationButton.setLayout(new BoxLayout(pnCalculationButton, BoxLayout.X_AXIS));
		pnCalculation.add(pnCalculationButton);

		JButton btABCalculation = new JButton("Update Table");
		btABCalculation.addActionListener(new AlABCalculation());
		pnCalculationButton.add(btABCalculation);

		cbConstantUpdate = new JCheckBox("Constant Update");
		cbConstantUpdate.setSelected(false);
		pnCalculationButton.add(cbConstantUpdate);

		JPanel pnCalculationStatus = new JPanel();
		pnCalculationStatus.setLayout(new BoxLayout(pnCalculationStatus, BoxLayout.X_AXIS));
		pnCalculation.add(pnCalculationStatus);

		lbCalculationStatus = new JLabel("-");
		pnCalculationStatus.add(lbCalculationStatus);

		JPanel pnSelectionMethod = new JPanel();
		pnSelectionMethod.setLayout(new BoxLayout(pnSelectionMethod, BoxLayout.Y_AXIS));
		pnSelection.add(pnSelectionMethod);

		rbCount = new JRadioButton("Count");
		pnSelectionMethod.add(rbCount);
		rbRatio = new JRadioButton("Ratio");
		pnSelectionMethod.add(rbRatio);
		rbThreshold = new JRadioButton("Threshold");
		pnSelectionMethod.add(rbThreshold);

		ButtonGroup bgSelectionMethod = new ButtonGroup();
		bgSelectionMethod.add(rbCount);
		bgSelectionMethod.add(rbRatio);
		bgSelectionMethod.add(rbThreshold);
		rbCount.setSelected(true);

		JPanel pnSelectionParams = new JPanel();
		pnSelectionParams.setLayout(new BoxLayout(pnSelectionParams, BoxLayout.Y_AXIS));
		pnSelection.add(pnSelectionParams);

		spCount = new JTextField();
		spCount.setText("10");
		spCount.setMinimumSize(new Dimension(60, 20));
		spCount.setMaximumSize(new Dimension(60, 20));
		pnSelectionParams.add(spCount);

		spRatio = new JSpinner();
		spRatio.setModel(new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1));
		spRatio.setMinimumSize(new Dimension(60, 20));
		spRatio.setMaximumSize(new Dimension(60, 20));
		pnSelectionParams.add(spRatio);

		tfThreshold = new JTextField();
		tfThreshold.setText("-5.0");
		tfThreshold.setMinimumSize(new Dimension(60, 20));
		tfThreshold.setMaximumSize(new Dimension(60, 20));
		pnSelectionParams.add(tfThreshold);

		JPanel pnExecution = new JPanel();
		pnExecution.setLayout(new BoxLayout(pnExecution, BoxLayout.Y_AXIS));
		pnSelection.add(pnExecution);

		JPanel pnSimulate = new JPanel();
		pnSimulate.setLayout(new BoxLayout(pnSimulate, BoxLayout.X_AXIS));
		pnExecution.add(pnSimulate);

		cbSimulate = new JCheckBox("Simulate (display only)");
		cbSimulate.setSelected(true);
		pnSimulate.add(cbSimulate);

		JPanel pnExecutionButtons = new JPanel();
		pnExecutionButtons.setLayout(new BoxLayout(pnExecutionButtons, BoxLayout.X_AXIS));
		pnExecution.add(pnExecutionButtons);

		JButton btABSelection = new JButton("Selection");
		btABSelection.addActionListener(new AlABSelection());
		pnExecutionButtons.add(btABSelection);

		JButton btABForgetting = new JButton("Forgetting");
		btABForgetting.addActionListener(new AlABForgetting());
		pnExecutionButtons.add(btABForgetting);

		JPanel pnExecutionStatus = new JPanel();
		pnExecutionStatus.setLayout(new BoxLayout(pnExecutionStatus, BoxLayout.X_AXIS));
		pnExecution.add(pnExecutionStatus);

		lbExecutionStatus = new JLabel("-");
		pnExecutionStatus.add(lbExecutionStatus);

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
		tableRowSorter = new TableRowSorter<MyTableModel>(tableModel);
		table.setRowSorter(tableRowSorter);

		JScrollPane scrollPane = new JScrollPane(table);
		pnDetails.add(scrollPane);

	}

	private void updateTable() {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				// hold details to be shown in one list for iterating over
				ArrayList<ActionDetail> actionDetails = new ArrayList<ActionDetail>();

				if (SHOW_AM) {
					// Autobiographic Memory
					for (MemoryEpisode episode : episodicMemory.getAM().GetAllEpisodes()) {
						actionDetails.addAll(episode.getDetails());
					}
				}

				if (SHOW_STEM) {
					// Short-Term Memory
					actionDetails.addAll(episodicMemory.getSTEM().getDetails());
				}

				// set initial minimum and maximum for cell colouring
				if (actionDetails.size() > 0) {
					activationValueMin = actionDetails.get(0).getActivationValue().getValue();
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

					// work with copies of values in table

					rowData[j++] = new Integer(actionDetail.getID());

					if (actionDetail.getSubject() != null)
						rowData[j++] = new String(actionDetail.getSubject());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getAction() != null)
						rowData[j++] = new String(actionDetail.getAction());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getIntention() != null)
						rowData[j++] = new String(actionDetail.getIntention());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getTarget() != null)
						rowData[j++] = new String(actionDetail.getTarget());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getStatus() != null)
						rowData[j++] = new String(actionDetail.getStatus());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getSpeechActMeaning() != null)
						rowData[j++] = new String(actionDetail.getSpeechActMeaning());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getObject() != null)
						rowData[j++] = new String(actionDetail.getObject());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getEmotion() != null)
						rowData[j++] = new String(actionDetail.getEmotion().getType() + "-" + actionDetail.getEmotion().GetPotential());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getTime() != null)
						if (DISPLAY_NARRATIVE_TIME)
							rowData[j++] = new Long(actionDetail.getTime().getNarrativeTime());
						else
							rowData[j++] = new String(actionDetail.getTime().getRealTimeFormatted());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getLocation() != null)
						rowData[j++] = new String(actionDetail.getLocation());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getActivationValue() != null)
						rowData[j++] = new Double(actionDetail.getActivationValue().getValue());
					else
						rowData[j++] = NULL_STRING;

					if (actionDetail.getActivationValue() != null)
						rowData[j++] = new Integer(actionDetail.getActivationValue().getNumRetrievals());
					else
						rowData[j++] = NULL_STRING;

					// add row data
					tableModel.addRow(rowData);

					// save minimum and maximum for cell colouring
					double activationValue = actionDetail.getActivationValue().getValue();
					if (activationValue < activationValueMin)
						activationValueMin = activationValue;
					if (activationValue > activationValueMax)
						activationValueMax = activationValue;

				}

			}
		});

	}

	private void calculateValues() {

		Time timeCalculated = new Time();
		double decayValue = Double.valueOf(spDecayValue.getValue().toString());
		episodicMemory.calculateActivationValues(timeCalculated, decayValue);

		if (DISPLAY_NARRATIVE_TIME)
			lbCalculationStatus.setText("Calculated at narrative time " + timeCalculated.getNarrativeTime());
		else
			lbCalculationStatus.setText("Calculated at " + timeCalculated.getRealTimeFormatted());

	}

	private void performSelection() {

		Time timeSelected = new Time();
		ArrayList<ActionDetail> selected = new ArrayList<ActionDetail>();

		if (rbCount.isSelected()) {
			int countMax = Integer.valueOf(Integer.valueOf(spCount.getText()));
			selected = episodicMemory.activationBasedSelectionByCount(countMax);
		} else if (rbRatio.isSelected()) {
			double amount = Double.valueOf(spRatio.getValue().toString());
			selected = episodicMemory.activationBasedSelectionByAmount(amount);

		} else if (rbThreshold.isSelected()) {
			double threshold = Double.valueOf(tfThreshold.getText());
			selected = episodicMemory.activationBasedSelectionByThreshold(threshold);
		}

		if (DISPLAY_NARRATIVE_TIME)
			lbExecutionStatus.setText("Selected at narrative time " + timeSelected.getNarrativeTime());
		else
			lbExecutionStatus.setText("Selected at " + timeSelected.getRealTimeFormatted());

		ArrayList<Integer> selectedIDsTemp = new ArrayList<Integer>();
		for (ActionDetail actionDetail : selected) {
			selectedIDsTemp.add(new Integer(actionDetail.getID()));
		}
		forgetIDs.clear();
		selectedIDs = selectedIDsTemp;

		if (!cbSimulate.isSelected()) {
			// perform selection
			episodicMemory.applyActivationBasedSelection(selected);
			// clear selection
			selectedIDs.clear();
		}

		table.repaint();

	}

	private void performForgetting() {

		Time timeForgotten = new Time();
		ArrayList<ActionDetail> forget = new ArrayList<ActionDetail>();

		if (rbCount.isSelected()) {
			int countMax = Integer.valueOf(Integer.valueOf(spCount.getText()));
			forget = episodicMemory.activationBasedForgettingByCount(countMax);
		} else if (rbRatio.isSelected()) {
			double amount = Double.valueOf(spRatio.getValue().toString());
			forget = episodicMemory.activationBasedForgettingByAmount(amount);
		} else if (rbThreshold.isSelected()) {
			double threshold = Double.valueOf(tfThreshold.getText());
			forget = episodicMemory.activationBasedForgettingByThreshold(threshold);
		}

		if (DISPLAY_NARRATIVE_TIME)
			lbExecutionStatus.setText("Forgotten at narrative time " + timeForgotten.getNarrativeTime());
		else
			lbExecutionStatus.setText("Forgotten at " + timeForgotten.getRealTimeFormatted());

		ArrayList<Integer> forgetIDsTemp = new ArrayList<Integer>();
		for (ActionDetail actionDetail : forget) {
			forgetIDsTemp.add(new Integer(actionDetail.getID()));
		}
		selectedIDs.clear();
		forgetIDs = forgetIDsTemp;

		if (!cbSimulate.isSelected()) {
			// perform forgetting
			episodicMemory.applyActivationBasedForgetting(forget);
			// clear selection
			forgetIDs.clear();
		}

		table.repaint();

	}

	public boolean Update(AgentCore ag) {

		episodicMemory = ag.getMemory().getEpisodicMemory();

		if (cbConstantUpdate.isSelected()) {
			calculateValues();
			updateTable();
		}
		return true;
	}

	public boolean Update(AgentModel am) {

		episodicMemory = am.getMemory().getEpisodicMemory();

		if (cbConstantUpdate.isSelected()) {
			calculateValues();
			updateTable();
		}
		return true;
	}

}
