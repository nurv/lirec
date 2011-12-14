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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import FAtiMA.AdvancedMemory.CompoundCue;
import FAtiMA.AdvancedMemory.ontology.TreeOntology;
import FAtiMA.AdvancedMemory.ontology.NounOntology;
import FAtiMA.AdvancedMemory.ontology.TimeOntology;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;

public class CompoundCuePanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int TARGET_ID_DEFAULT = 0;

	private static final int TARGET_DEPTH_MAX_DEFAULT = 1;
	private static final int OBJECT_DEPTH_MAX_DEFAULT = 1;
	private static final int LOCATION_DEPTH_MAX_DEFAULT = 2;

	private AdvancedMemoryPanel advancedMemoryPanel;

	private CompoundCue compoundCue;

	private JCheckBox cbFilterSubject;
	private JTextField tfFilterSubject;
	private JCheckBox cbFilterAction;
	private JTextField tfFilterAction;
	private JCheckBox cbFilterTarget;
	private JTextField tfFilterTarget;
	private JCheckBox cbFilterObject;
	private JTextField tfFilterObject;
	private JCheckBox cbFilterLocation;
	private JTextField tfFilterLocation;
	private JCheckBox cbFilterIntention;
	private JTextField tfFilterIntention;
	private JCheckBox cbFilterStatus;
	private JTextField tfFilterStatus;
	private JCheckBox cbFilterEmotion;
	private JTextField tfFilterEmotion;
	private JCheckBox cbFilterSpeechActMeaning;
	private JTextField tfFilterSpeechActMeaning;
	private JCheckBox cbFilterMultimediaPath;
	private JTextField tfFilterMultimediaPath;
	private JCheckBox cbFilterPraiseworthiness;
	private JTextField tfFilterPraiseworthiness;
	private JCheckBox cbFilterDesirability;
	private JTextField tfFilterDesirability;
	private JCheckBox cbFilterTime;
	private JTextField tfFilterTime;

	private JCheckBox cbTimeOntology;
	private JComboBox cbTimeAbstractionMode;
	private JCheckBox cbTargetOntology;
	private JTextField tfTargetDepthMax;
	private JCheckBox cbObjectOntology;
	private JTextField tfObjectDepthMax;
	private JCheckBox cbLocationOntology;
	private JTextField tfLocationDepthMax;

	private JTextField tfTargetID;

	private TableModelCompoundCue tmResults;

	private JLabel lbStatus;

	public CompoundCuePanel(AdvancedMemoryPanel advancedMemoryPanel) {
		super();
		this.advancedMemoryPanel = advancedMemoryPanel;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel pnFilter = new JPanel();
		pnFilter.setLayout(new GridLayout(5, 6));
		pnFilter.setBorder(BorderFactory.createTitledBorder("Filter"));
		this.add(pnFilter);

		cbFilterSubject = new JCheckBox("Subject");
		pnFilter.add(cbFilterSubject);
		tfFilterSubject = new JTextField();
		pnFilter.add(tfFilterSubject);

		cbFilterAction = new JCheckBox("Action");
		pnFilter.add(cbFilterAction);
		tfFilterAction = new JTextField();
		pnFilter.add(tfFilterAction);

		cbFilterTarget = new JCheckBox("Target");
		pnFilter.add(cbFilterTarget);
		tfFilterTarget = new JTextField();
		pnFilter.add(tfFilterTarget);

		cbFilterObject = new JCheckBox("Object");
		pnFilter.add(cbFilterObject);
		tfFilterObject = new JTextField();
		pnFilter.add(tfFilterObject);

		cbFilterLocation = new JCheckBox("Location");
		pnFilter.add(cbFilterLocation);
		tfFilterLocation = new JTextField();
		pnFilter.add(tfFilterLocation);

		cbFilterIntention = new JCheckBox("Intention");
		pnFilter.add(cbFilterIntention);
		tfFilterIntention = new JTextField();
		pnFilter.add(tfFilterIntention);

		cbFilterStatus = new JCheckBox("Status");
		pnFilter.add(cbFilterStatus);
		tfFilterStatus = new JTextField();
		pnFilter.add(tfFilterStatus);

		cbFilterEmotion = new JCheckBox("Emotion");
		pnFilter.add(cbFilterEmotion);
		tfFilterEmotion = new JTextField();
		pnFilter.add(tfFilterEmotion);

		cbFilterSpeechActMeaning = new JCheckBox("Speech Act Meaning");
		pnFilter.add(cbFilterSpeechActMeaning);
		tfFilterSpeechActMeaning = new JTextField();
		pnFilter.add(tfFilterSpeechActMeaning);

		cbFilterMultimediaPath = new JCheckBox("Multimedia Path");
		pnFilter.add(cbFilterMultimediaPath);
		tfFilterMultimediaPath = new JTextField();
		pnFilter.add(tfFilterMultimediaPath);

		cbFilterPraiseworthiness = new JCheckBox("Praiseworthiness");
		pnFilter.add(cbFilterPraiseworthiness);
		tfFilterPraiseworthiness = new JTextField();
		pnFilter.add(tfFilterPraiseworthiness);

		cbFilterDesirability = new JCheckBox("Desirability");
		pnFilter.add(cbFilterDesirability);
		tfFilterDesirability = new JTextField();
		pnFilter.add(tfFilterDesirability);

		cbFilterTime = new JCheckBox("Time");
		pnFilter.add(cbFilterTime);
		tfFilterTime = new JTextField();
		pnFilter.add(tfFilterTime);

		JPanel pnSettings = new JPanel();
		pnSettings.setLayout(new BoxLayout(pnSettings, BoxLayout.X_AXIS));
		pnSettings.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnSettings);

		JPanel pnOntology = new JPanel();
		pnOntology.setLayout(new BoxLayout(pnOntology, BoxLayout.X_AXIS));
		//pnOntology.setLayout(new GridLayout(2, 2));
		pnOntology.setBorder(BorderFactory.createTitledBorder("Ontology"));
		pnSettings.add(pnOntology);

		JPanel pnTimeOntology = new JPanel();
		pnTimeOntology.setLayout(new BoxLayout(pnTimeOntology, BoxLayout.Y_AXIS));
		pnTimeOntology.setBorder(BorderFactory.createEtchedBorder());
		pnOntology.add(pnTimeOntology);

		cbTimeOntology = new JCheckBox("Time Ontology");
		pnTimeOntology.add(cbTimeOntology);

		JLabel lbTimeAbstractionMode = new JLabel("Abstraction Mode:");
		pnTimeOntology.add(lbTimeAbstractionMode);

		cbTimeAbstractionMode = new JComboBox();
		cbTimeAbstractionMode.setMinimumSize(new Dimension(150, 26));
		cbTimeAbstractionMode.setMaximumSize(new Dimension(150, 26));
		cbTimeAbstractionMode.addItem("Part Of Day");
		cbTimeAbstractionMode.addItem("Day Of Week");
		cbTimeAbstractionMode.addItem("Year-Month-Day");
		pnTimeOntology.add(cbTimeAbstractionMode);

		JPanel pnTargetOntology = new JPanel();
		pnTargetOntology.setLayout(new BoxLayout(pnTargetOntology, BoxLayout.Y_AXIS));
		pnTargetOntology.setBorder(BorderFactory.createEtchedBorder());
		pnOntology.add(pnTargetOntology);

		cbTargetOntology = new JCheckBox("Target Ontology");
		pnTargetOntology.add(cbTargetOntology);

		JLabel lbTargetDepthMax = new JLabel("Maximum Depth:");
		pnTargetOntology.add(lbTargetDepthMax);

		tfTargetDepthMax = new JTextField(String.valueOf(TARGET_DEPTH_MAX_DEFAULT));
		tfTargetDepthMax.setMinimumSize(new Dimension(40, 20));
		tfTargetDepthMax.setMaximumSize(new Dimension(40, 20));
		pnTargetOntology.add(tfTargetDepthMax);

		JPanel pnObjectOntology = new JPanel();
		pnObjectOntology.setLayout(new BoxLayout(pnObjectOntology, BoxLayout.Y_AXIS));
		pnObjectOntology.setBorder(BorderFactory.createEtchedBorder());
		pnOntology.add(pnObjectOntology);

		cbObjectOntology = new JCheckBox("Object Ontology");
		pnObjectOntology.add(cbObjectOntology);

		JLabel lbObjectDepthMax = new JLabel("Maximum Depth:");
		pnObjectOntology.add(lbObjectDepthMax);

		tfObjectDepthMax = new JTextField(String.valueOf(OBJECT_DEPTH_MAX_DEFAULT));
		tfObjectDepthMax.setMinimumSize(new Dimension(40, 20));
		tfObjectDepthMax.setMaximumSize(new Dimension(40, 20));
		pnObjectOntology.add(tfObjectDepthMax);

		JPanel pnLocationOntology = new JPanel();
		pnLocationOntology.setLayout(new BoxLayout(pnLocationOntology, BoxLayout.Y_AXIS));
		pnLocationOntology.setBorder(BorderFactory.createEtchedBorder());
		pnOntology.add(pnLocationOntology);

		cbLocationOntology = new JCheckBox("Location Ontology");
		pnLocationOntology.add(cbLocationOntology);

		JLabel lbLocationDepthMax = new JLabel("Maximum Depth:");
		pnLocationOntology.add(lbLocationDepthMax);

		tfLocationDepthMax = new JTextField(String.valueOf(LOCATION_DEPTH_MAX_DEFAULT));
		tfLocationDepthMax.setMinimumSize(new Dimension(40, 20));
		tfLocationDepthMax.setMaximumSize(new Dimension(40, 20));
		pnLocationOntology.add(tfLocationDepthMax);

		JPanel pnMechanism = new JPanel();
		pnMechanism.setLayout(new BoxLayout(pnMechanism, BoxLayout.X_AXIS));
		pnSettings.add(pnMechanism);

		JPanel pnParameters = new JPanel();
		pnParameters.setLayout(new BoxLayout(pnParameters, BoxLayout.Y_AXIS));
		pnParameters.setBorder(BorderFactory.createTitledBorder("Parameters"));
		pnMechanism.add(pnParameters);

		JLabel lbTargetID = new JLabel("Target ID:");
		pnParameters.add(lbTargetID);

		tfTargetID = new JTextField(String.valueOf(TARGET_ID_DEFAULT));
		tfTargetID.setMinimumSize(new Dimension(80, 26));
		tfTargetID.setMaximumSize(new Dimension(80, 26));
		pnParameters.add(tfTargetID);

		JPanel pnActions = new JPanel();
		pnActions.setLayout(new BoxLayout(pnActions, BoxLayout.Y_AXIS));
		pnActions.setBorder(BorderFactory.createTitledBorder("Actions"));
		pnMechanism.add(pnActions);

		JButton btCalculate = new JButton("Calculate Evaluation Values");
		btCalculate.addActionListener(new AlCalculate());
		pnActions.add(btCalculate);

		JButton btStoreResult = new JButton("Store Result");
		btStoreResult.addActionListener(new AlStoreResult());
		pnActions.add(btStoreResult);

		for (Component component : pnSettings.getComponents())
			((JComponent) component).setAlignmentY(Component.TOP_ALIGNMENT);

		for (Component component : pnOntology.getComponents())
			((JComponent) component).setAlignmentY(Component.TOP_ALIGNMENT);

		for (Component component : pnTimeOntology.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnTargetOntology.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnObjectOntology.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnLocationOntology.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnMechanism.getComponents())
			((JComponent) component).setAlignmentY(Component.TOP_ALIGNMENT);

		for (Component component : pnParameters.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnActions.getComponents())
			((JComponent) component).setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel pnResults = new JPanel();
		pnResults.setLayout(new BoxLayout(pnResults, BoxLayout.Y_AXIS));
		pnResults.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnResults);

		tmResults = new TableModelCompoundCue();
		tmResults.addColumn("Action Detail ID");
		tmResults.addColumn("Evaluation Value");
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

		// build filter attribute string
		String filterAttributesStr = "";
		if (cbFilterSubject.isSelected())
			filterAttributesStr += "*subject " + tfFilterSubject.getText().trim();
		if (cbFilterAction.isSelected())
			filterAttributesStr += "*action " + tfFilterAction.getText().trim();
		if (cbFilterTarget.isSelected())
			filterAttributesStr += "*target " + tfFilterTarget.getText().trim();
		if (cbFilterObject.isSelected())
			filterAttributesStr += "*object " + tfFilterObject.getText().trim();
		if (cbFilterLocation.isSelected())
			filterAttributesStr += "*location " + tfFilterLocation.getText().trim();
		if (cbFilterIntention.isSelected())
			filterAttributesStr += "*intention " + tfFilterIntention.getText().trim();
		if (cbFilterStatus.isSelected())
			filterAttributesStr += "*status " + tfFilterStatus.getText().trim();
		if (cbFilterEmotion.isSelected())
			filterAttributesStr += "*emotion " + tfFilterEmotion.getText().trim();
		if (cbFilterSpeechActMeaning.isSelected())
			filterAttributesStr += "*speechActMeaning " + tfFilterSpeechActMeaning.getText().trim();
		if (cbFilterMultimediaPath.isSelected())
			filterAttributesStr += "*multimediaPath " + tfFilterMultimediaPath.getText().trim();
		if (cbFilterPraiseworthiness.isSelected())
			filterAttributesStr += "*praiseworthiness " + tfFilterPraiseworthiness.getText().trim();
		if (cbFilterDesirability.isSelected())
			filterAttributesStr += "*desirability " + tfFilterDesirability.getText().trim();
		if (cbFilterTime.isSelected())
			filterAttributesStr += "*time " + tfFilterTime.getText().trim();

		// parse ontology usage

		// time ontology
		TimeOntology timeOntology = null;
		if (cbTimeOntology.isSelected()) {
			timeOntology = new TimeOntology();
			// combo box indices must correspond to abstraction mode constants here
			short abstractionMode = (short) cbTimeAbstractionMode.getSelectedIndex();
			timeOntology.setAbstractionMode(abstractionMode);
		}

		// target ontology
		NounOntology targetOntology = null;
		if (cbTargetOntology.isSelected()) {
			targetOntology = new NounOntology();
			int depthMax = Integer.valueOf(tfTargetDepthMax.getText());
			targetOntology.setDepthMax(depthMax);
		}

		// object ontology
		NounOntology objectOntology = null;
		if (cbObjectOntology.isSelected()) {
			objectOntology = new NounOntology();
			int depthMax = Integer.valueOf(tfObjectDepthMax.getText());
			objectOntology.setDepthMax(depthMax);
		}

		// location ontology
		TreeOntology locationOntology = null;
		if (cbLocationOntology.isSelected()) {
			locationOntology = new TreeOntology(advancedMemoryPanel.getAdvancedMemoryComponent().getLocationOntologyFile());
			int depthMax = Integer.valueOf(tfLocationDepthMax.getText());
			locationOntology.setDepthMax(depthMax);
		}

		// parse target ID
		Integer targetID = null;
		try {
			targetID = Integer.valueOf(tfTargetID.getText());
		} catch (Exception e) {
			lbStatus.setText("Error while parsing Target ID!");
			return;
		}

		// obtain target action detail
		ActionDetail actionDetailTarget = null;
		for (MemoryEpisode memoryEpisode : advancedMemoryPanel.getAdvancedMemoryComponent().getMemory().getEpisodicMemory().getAM().GetAllEpisodes()) {
			for (ActionDetail actionDetail : memoryEpisode.getDetails()) {
				if (actionDetail.getID() == targetID) {
					actionDetailTarget = actionDetail;
				}
			}
		}
		for (ActionDetail actionDetail : advancedMemoryPanel.getAdvancedMemoryComponent().getMemory().getEpisodicMemory().getSTEM().getDetails()) {
			if (actionDetail.getID() == targetID) {
				actionDetailTarget = actionDetail;
			}
		}

		// check if target action detail exists
		if (actionDetailTarget == null) {
			lbStatus.setText("Action Detail with Target ID does not exist!");
			return;
		}

		// execute compound cue mechanism
		CompoundCue compoundCue = new CompoundCue();
		compoundCue.execute(advancedMemoryPanel.getAdvancedMemoryComponent().getMemory().getEpisodicMemory(), filterAttributesStr, actionDetailTarget, timeOntology, targetOntology, objectOntology,
				locationOntology);
		this.compoundCue = compoundCue;

		// update panel
		updatePanel();

	}

	public void updatePanel() {

		// clear check boxes
		cbFilterSubject.setSelected(false);
		cbFilterAction.setSelected(false);
		cbFilterTarget.setSelected(false);
		cbFilterObject.setSelected(false);
		cbFilterLocation.setSelected(false);
		cbFilterIntention.setSelected(false);
		cbFilterStatus.setSelected(false);
		cbFilterEmotion.setSelected(false);
		cbFilterSpeechActMeaning.setSelected(false);
		cbFilterMultimediaPath.setSelected(false);
		cbFilterPraiseworthiness.setSelected(false);
		cbFilterDesirability.setSelected(false);
		cbFilterTime.setSelected(false);

		// clear text fields
		tfFilterSubject.setText("");
		tfFilterAction.setText("");
		tfFilterTarget.setText("");
		tfFilterObject.setText("");
		tfFilterLocation.setText("");
		tfFilterIntention.setText("");
		tfFilterStatus.setText("");
		tfFilterEmotion.setText("");
		tfFilterSpeechActMeaning.setText("");
		tfFilterMultimediaPath.setText("");
		tfFilterPraiseworthiness.setText("");
		tfFilterDesirability.setText("");
		tfFilterTime.setText("");

		// set check boxes		
		ArrayList<String> filterAttributes = compoundCue.getFilterAttributes();
		for (String filterAttribute : filterAttributes) {
			String[] filterAttributeSplitted = filterAttribute.split(" ");
			String name = filterAttributeSplitted[0];
			String value = "";
			// check if a value was given
			if (filterAttributeSplitted.length == 2) {
				value = filterAttributeSplitted[1];
			}

			if (name.equals("subject")) {
				cbFilterSubject.setSelected(true);
				tfFilterSubject.setText(value);
			} else if (name.equals("action")) {
				cbFilterAction.setSelected(true);
				tfFilterAction.setText(value);
			} else if (name.equals("target")) {
				cbFilterTarget.setSelected(true);
				tfFilterTarget.setText(value);
			} else if (name.equals("object")) {
				cbFilterObject.setSelected(true);
				tfFilterObject.setText(value);
			} else if (name.equals("location")) {
				cbFilterLocation.setSelected(true);
				tfFilterLocation.setText(value);
			} else if (name.equals("intention")) {
				cbFilterIntention.setSelected(true);
				tfFilterIntention.setText(value);
			} else if (name.equals("status")) {
				cbFilterStatus.setSelected(true);
				tfFilterStatus.setText(value);
			} else if (name.equals("emotion")) {
				cbFilterEmotion.setSelected(true);
				tfFilterEmotion.setText(value);
			} else if (name.equals("speechActMeaning")) {
				cbFilterSpeechActMeaning.setSelected(true);
				tfFilterSpeechActMeaning.setText(value);
			} else if (name.equals("multimediaPath")) {
				cbFilterMultimediaPath.setSelected(true);
				tfFilterMultimediaPath.setText(value);
			} else if (name.equals("praiseworthiness")) {
				cbFilterPraiseworthiness.setSelected(true);
				tfFilterPraiseworthiness.setText(value);
			} else if (name.equals("desirability")) {
				cbFilterDesirability.setSelected(true);
				tfFilterDesirability.setText(value);
			} else if (name.equals("time")) {
				cbFilterTime.setSelected(true);
				tfFilterTime.setText(value);
			}

		}

		// set ontology usage

		// time ontology
		TimeOntology timeOntology = compoundCue.getTimeOntology();
		if (timeOntology == null) {
			cbTimeOntology.setSelected(false);
			cbTimeAbstractionMode.setSelectedIndex(0);
		} else {
			cbTimeOntology.setSelected(true);
			// combo box indices must correspond to abstraction mode constants here			
			cbTimeAbstractionMode.setSelectedIndex(timeOntology.getAbstractionMode());
		}

		// target ontology
		NounOntology targetOntology = compoundCue.getTargetOntology();
		if (targetOntology == null) {
			cbTargetOntology.setSelected(false);
			tfTargetDepthMax.setText(String.valueOf(TARGET_DEPTH_MAX_DEFAULT));
		} else {
			cbTargetOntology.setSelected(true);
			tfTargetDepthMax.setText(String.valueOf(targetOntology.getDepthMax()));
		}

		// object ontology
		NounOntology objectOntology = compoundCue.getObjectOntology();
		if (objectOntology == null) {
			cbObjectOntology.setSelected(false);
			tfObjectDepthMax.setText(String.valueOf(OBJECT_DEPTH_MAX_DEFAULT));
		} else {
			cbObjectOntology.setSelected(true);
			tfObjectDepthMax.setText(String.valueOf(objectOntology.getDepthMax()));
		}

		// location ontology
		TreeOntology locationOntology = compoundCue.getLocationOntology();
		if (locationOntology == null) {
			cbLocationOntology.setSelected(false);
			tfLocationDepthMax.setText(String.valueOf(LOCATION_DEPTH_MAX_DEFAULT));
		} else {
			cbLocationOntology.setSelected(true);
			tfLocationDepthMax.setText(String.valueOf(locationOntology.getDepthMax()));
		}

		// update target id
		tfTargetID.setText(String.valueOf(compoundCue.getTargetID()));

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
				advancedMemoryPanel.getAdvancedMemoryComponent().getResults().add(compoundCue);
				advancedMemoryPanel.getOverviewPanel().updateResultList();
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
