/** 
 * SpreadingActivationPanel.java - Display panel for the Spreading Activation mechanism
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;
import FAtiMA.AdvancedMemory.SpreadingActivation;

public class SpreadingActivationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private AdvancedMemoryComponent advancedMemoryComponent;

	private SpreadingActivation spreadingActivation;

	private JCheckBox cbSubject;
	private JTextField tfSubject;
	private JCheckBox cbAction;
	private JTextField tfAction;
	private JCheckBox cbTarget;
	private JTextField tfTarget;
	private JCheckBox cbObject;
	private JTextField tfObject;
	private JCheckBox cbLocation;
	private JTextField tfLocation;
	private JCheckBox cbIntention;
	private JTextField tfIntention;
	private JCheckBox cbStatus;
	private JTextField tfStatus;
	private JCheckBox cbEmotion;
	private JTextField tfEmotion;
	private JCheckBox cbSpeechActMeaning;
	private JTextField tfSpeechActMeaning;
	private JCheckBox cbMultimediaPath;
	private JTextField tfMultimediaPath;
	private JCheckBox cbPraiseworthiness;
	private JTextField tfPraiseworthiness;
	private JCheckBox cbDesirability;
	private JTextField tfDesirability;
	private JCheckBox cbTime;
	private JTextField tfTime;

	private JComboBox cbTargetAttribute;

	private TableModelSpreadingActivation tmResults;

	private JLabel lbStatus;

	public SpreadingActivationPanel(AdvancedMemoryComponent advancedMemoryComponent) {
		super();
		this.advancedMemoryComponent = advancedMemoryComponent;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel pnActions = new JPanel();
		pnActions.setLayout(new BoxLayout(pnActions, BoxLayout.X_AXIS));
		pnActions.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnActions);

		JPanel pnKnownAttributes = new JPanel();
		pnKnownAttributes.setLayout(new GridLayout(7, 4));
		pnKnownAttributes.setBorder(BorderFactory.createTitledBorder("Known Attributes"));
		pnActions.add(pnKnownAttributes);

		cbSubject = new JCheckBox("Subject");
		pnKnownAttributes.add(cbSubject);
		tfSubject = new JTextField();
		pnKnownAttributes.add(tfSubject);

		cbAction = new JCheckBox("Action");
		pnKnownAttributes.add(cbAction);
		tfAction = new JTextField();
		pnKnownAttributes.add(tfAction);

		cbTarget = new JCheckBox("Target");
		pnKnownAttributes.add(cbTarget);
		tfTarget = new JTextField();
		pnKnownAttributes.add(tfTarget);

		cbObject = new JCheckBox("Object");
		pnKnownAttributes.add(cbObject);
		tfObject = new JTextField();
		pnKnownAttributes.add(tfObject);

		cbLocation = new JCheckBox("Location");
		pnKnownAttributes.add(cbLocation);
		tfLocation = new JTextField();
		pnKnownAttributes.add(tfLocation);

		cbIntention = new JCheckBox("Intention");
		pnKnownAttributes.add(cbIntention);
		tfIntention = new JTextField();
		pnKnownAttributes.add(tfIntention);

		cbStatus = new JCheckBox("Status");
		pnKnownAttributes.add(cbStatus);
		tfStatus = new JTextField();
		pnKnownAttributes.add(tfStatus);

		cbEmotion = new JCheckBox("Emotion");
		pnKnownAttributes.add(cbEmotion);
		tfEmotion = new JTextField();
		pnKnownAttributes.add(tfEmotion);

		cbSpeechActMeaning = new JCheckBox("Speech Act Meaning");
		pnKnownAttributes.add(cbSpeechActMeaning);
		tfSpeechActMeaning = new JTextField();
		pnKnownAttributes.add(tfSpeechActMeaning);

		cbMultimediaPath = new JCheckBox("Multimedia Path");
		pnKnownAttributes.add(cbMultimediaPath);
		tfMultimediaPath = new JTextField();
		pnKnownAttributes.add(tfMultimediaPath);

		cbPraiseworthiness = new JCheckBox("Praiseworthiness");
		pnKnownAttributes.add(cbPraiseworthiness);
		tfPraiseworthiness = new JTextField();
		pnKnownAttributes.add(tfPraiseworthiness);

		cbDesirability = new JCheckBox("Desirability");
		pnKnownAttributes.add(cbDesirability);
		tfDesirability = new JTextField();
		pnKnownAttributes.add(tfDesirability);

		cbTime = new JCheckBox("Time");
		pnKnownAttributes.add(cbTime);
		tfTime = new JTextField();
		pnKnownAttributes.add(tfTime);

		JPanel pnTargetAttribute = new JPanel();
		pnTargetAttribute.setLayout(new BoxLayout(pnTargetAttribute, BoxLayout.Y_AXIS));
		pnTargetAttribute.setBorder(BorderFactory.createTitledBorder("Target Attribute"));
		pnActions.add(pnTargetAttribute);

		cbTargetAttribute = new JComboBox();
		cbTargetAttribute.setMinimumSize(new Dimension(200, 26));
		cbTargetAttribute.setMaximumSize(new Dimension(200, 26));
		cbTargetAttribute.addItem("Subject");
		cbTargetAttribute.addItem("Action");
		cbTargetAttribute.addItem("Target");
		cbTargetAttribute.addItem("Object");
		cbTargetAttribute.addItem("Location");
		cbTargetAttribute.addItem("Intention");
		cbTargetAttribute.addItem("Status");
		cbTargetAttribute.addItem("Emotion");
		cbTargetAttribute.addItem("Speech Act Meaning");
		cbTargetAttribute.addItem("Multimedia Path");
		cbTargetAttribute.addItem("Praiseworthiness");
		cbTargetAttribute.addItem("Desirability");
		cbTargetAttribute.addItem("Time");
		pnTargetAttribute.add(cbTargetAttribute);

		JButton btCalculate = new JButton("Calculate Frequencies");
		btCalculate.addActionListener(new AlCalculate());
		pnTargetAttribute.add(btCalculate);

		JButton btStoreResult = new JButton("Store Result");
		btStoreResult.addActionListener(new AlStoreResult());
		pnTargetAttribute.add(btStoreResult);

		for (Component component : pnTargetAttribute.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnActions.getComponents())
			((JComponent) component).setAlignmentY(Component.TOP_ALIGNMENT);

		JPanel pnResults = new JPanel();
		pnResults.setLayout(new BoxLayout(pnResults, BoxLayout.Y_AXIS));
		pnResults.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnResults);

		tmResults = new TableModelSpreadingActivation();
		tmResults.addColumn("Value");
		tmResults.addColumn("Frequency");
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

	public SpreadingActivation getSpreadingActivation() {
		return spreadingActivation;
	}

	public void setSpreadingActivation(SpreadingActivation spreadingActivation) {
		this.spreadingActivation = spreadingActivation;
	}

	private class AlCalculate implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			calculate();
		}
	}

	private void calculate() {

		// update status
		lbStatus.setText("Executing Spreading Activation mechanism...");

		// build known attribute string
		String knownAttributesStr = "";
		if (cbSubject.isSelected())
			knownAttributesStr += "*subject " + tfSubject.getText().trim();
		if (cbAction.isSelected())
			knownAttributesStr += "*action " + tfAction.getText().trim();
		if (cbTarget.isSelected())
			knownAttributesStr += "*target " + tfTarget.getText().trim();
		if (cbObject.isSelected())
			knownAttributesStr += "*object " + tfObject.getText().trim();
		if (cbLocation.isSelected())
			knownAttributesStr += "*location " + tfLocation.getText().trim();
		if (cbIntention.isSelected())
			knownAttributesStr += "*intention " + tfIntention.getText().trim();
		if (cbStatus.isSelected())
			knownAttributesStr += "*status " + tfStatus.getText().trim();
		if (cbEmotion.isSelected())
			knownAttributesStr += "*emotion " + tfEmotion.getText().trim();
		if (cbSpeechActMeaning.isSelected())
			knownAttributesStr += "*speechActMeaning " + tfSpeechActMeaning.getText().trim();
		if (cbMultimediaPath.isSelected())
			knownAttributesStr += "*multimediaPath " + tfMultimediaPath.getText().trim();
		if (cbPraiseworthiness.isSelected())
			knownAttributesStr += "*praiseworthiness " + tfPraiseworthiness.getText().trim();
		if (cbDesirability.isSelected())
			knownAttributesStr += "*desirability " + tfDesirability.getText().trim();
		if (cbTime.isSelected())
			knownAttributesStr += "*time " + tfTime.getText().trim();

		// parse target attribute
		String targetAttributeStr = String.valueOf(cbTargetAttribute.getSelectedItem());
		// remove spaces
		String targetAttributeName = targetAttributeStr.replaceAll(" ", "");
		// decapitalise
		targetAttributeName = targetAttributeName.substring(0, 1).toLowerCase() + targetAttributeName.substring(1);

		// execute spreading activation
		SpreadingActivation spreadingActivation = new SpreadingActivation();
		spreadingActivation.spreadActivation(advancedMemoryComponent.getMemory().getEpisodicMemory(), knownAttributesStr, targetAttributeName);
		this.spreadingActivation = spreadingActivation;

		// update panel
		updatePanel();
	}

	public void updatePanel() {

		// clear check boxes
		cbSubject.setSelected(false);
		cbAction.setSelected(false);
		cbTarget.setSelected(false);
		cbObject.setSelected(false);
		cbLocation.setSelected(false);
		cbIntention.setSelected(false);
		cbStatus.setSelected(false);
		cbEmotion.setSelected(false);
		cbSpeechActMeaning.setSelected(false);
		cbMultimediaPath.setSelected(false);
		cbPraiseworthiness.setSelected(false);
		cbDesirability.setSelected(false);
		cbTime.setSelected(false);

		// clear text fields
		tfSubject.setText("");
		tfAction.setText("");
		tfTarget.setText("");
		tfObject.setText("");
		tfLocation.setText("");
		tfIntention.setText("");
		tfStatus.setText("");
		tfEmotion.setText("");
		tfSpeechActMeaning.setText("");
		tfMultimediaPath.setText("");
		tfPraiseworthiness.setText("");
		tfDesirability.setText("");
		tfTime.setText("");

		// set check boxes		
		ArrayList<String> knownAttributes = spreadingActivation.getKnownAttributes();
		for (String attribute : knownAttributes) {
			String[] attributeSplitted = attribute.split(" ");
			String attributeName = attributeSplitted[0];
			String attributeValue = "";
			// check if a value was given
			if (attributeSplitted.length == 2) {
				attributeValue = attributeSplitted[1];
			}

			if (attributeName.equals("subject")) {
				cbSubject.setSelected(true);
				tfSubject.setText(attributeValue);
			} else if (attributeName.equals("action")) {
				cbAction.setSelected(true);
				tfAction.setText(attributeValue);
			} else if (attributeName.equals("target")) {
				cbTarget.setSelected(true);
				tfTarget.setText(attributeValue);
			} else if (attributeName.equals("object")) {
				cbObject.setSelected(true);
				tfObject.setText(attributeValue);
			} else if (attributeName.equals("location")) {
				cbLocation.setSelected(true);
				tfLocation.setText(attributeValue);
			} else if (attributeName.equals("intention")) {
				cbIntention.setSelected(true);
				tfIntention.setText(attributeValue);
			} else if (attributeName.equals("status")) {
				cbStatus.setSelected(true);
				tfStatus.setText(attributeValue);
			} else if (attributeName.equals("emotion")) {
				cbEmotion.setSelected(true);
				tfEmotion.setText(attributeValue);
			} else if (attributeName.equals("speechActMeaning")) {
				cbSpeechActMeaning.setSelected(true);
				tfSpeechActMeaning.setText(attributeValue);
			} else if (attributeName.equals("multimediaPath")) {
				cbMultimediaPath.setSelected(true);
				tfMultimediaPath.setText(attributeValue);
			} else if (attributeName.equals("praiseworthiness")) {
				cbPraiseworthiness.setSelected(true);
				tfPraiseworthiness.setText(attributeValue);
			} else if (attributeName.equals("desirability")) {
				cbDesirability.setSelected(true);
				tfDesirability.setText(attributeValue);
			} else if (attributeName.equals("time")) {
				cbTime.setSelected(true);
				tfTime.setText(attributeValue);
			}

		}

		String targetAttributeName = spreadingActivation.getTargetAttributeName();
		// add spaces
		String targetAttributeStr = targetAttributeName.replaceAll("[A-Z]", " $0");
		// capitalise
		targetAttributeStr = targetAttributeStr.substring(0, 1).toUpperCase() + targetAttributeStr.substring(1);
		// set selected target attribute
		cbTargetAttribute.setSelectedItem(targetAttributeStr);

		// clear table model
		int rowCount = tmResults.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tmResults.removeRow(0);
		}

		// update table model
		for (String value : spreadingActivation.getFrequencies().keySet()) {
			Object[] data = new Object[2];
			data[0] = value;
			data[1] = spreadingActivation.getFrequencies().get(value);
			tmResults.addRow(data);
		}

		// update status
		lbStatus.setText("Spreading Activation mechanism executed at " + spreadingActivation.getTime().getRealTimeFormatted());

	}

	private class AlStoreResult implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (spreadingActivation != null) {
				advancedMemoryComponent.getResults().add(spreadingActivation);
				advancedMemoryComponent.getAdvancedMemoryPanel().getOverviewPanel().updateResultList();
				lbStatus.setText("Result stored!");
			} else {
				lbStatus.setText("Result is null and was not stored!");
			}
		}
	}

	private class TableModelSpreadingActivation extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public TableModelSpreadingActivation() {
			super();
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 1:
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

}
