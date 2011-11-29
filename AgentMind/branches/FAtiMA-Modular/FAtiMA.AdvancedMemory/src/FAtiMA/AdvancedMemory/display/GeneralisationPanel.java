/** 
 * CompoundCuePanel.java - Display panel for the Generalisation mechanism
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
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;
import FAtiMA.AdvancedMemory.GER;
import FAtiMA.AdvancedMemory.Generalisation;

public class GeneralisationPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private AdvancedMemoryComponent advancedMemoryComponent;

	private Generalisation generalisation;

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

	private JCheckBox cbSubject;
	private JCheckBox cbAction;
	private JCheckBox cbTarget;
	private JCheckBox cbObject;
	private JCheckBox cbLocation;
	private JCheckBox cbIntention;
	private JCheckBox cbStatus;
	private JCheckBox cbEmotion;
	private JCheckBox cbSpeechActMeaning;
	private JCheckBox cbMultimediaPath;
	private JCheckBox cbPraiseworthiness;
	private JCheckBox cbDesirability;
	private JCheckBox cbTime;

	private JTextField tfMinimumCoverage;

	private TableModelGeneralisation tmResults;

	private JLabel lbStatus;

	public GeneralisationPanel(AdvancedMemoryComponent advancedMemoryComponent) {
		super();
		this.advancedMemoryComponent = advancedMemoryComponent;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel pnSettings = new JPanel();
		pnSettings.setLayout(new BoxLayout(pnSettings, BoxLayout.X_AXIS));
		pnSettings.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnSettings);

		JPanel pnFilter = new JPanel();
		pnFilter.setLayout(new GridLayout(7, 4));
		pnFilter.setBorder(BorderFactory.createTitledBorder("Filter"));
		pnSettings.add(pnFilter);

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

		JPanel pnAttributeSelection = new JPanel();
		pnAttributeSelection.setLayout(new GridLayout(7, 6));
		pnAttributeSelection.setBorder(BorderFactory.createTitledBorder("Attribute Selection"));
		pnSettings.add(pnAttributeSelection);

		cbSubject = new JCheckBox("Subject");
		pnAttributeSelection.add(cbSubject);

		cbAction = new JCheckBox("Action");
		pnAttributeSelection.add(cbAction);

		cbTarget = new JCheckBox("Target");
		pnAttributeSelection.add(cbTarget);

		cbObject = new JCheckBox("Object");
		pnAttributeSelection.add(cbObject);

		cbLocation = new JCheckBox("Location");
		pnAttributeSelection.add(cbLocation);

		cbIntention = new JCheckBox("Intention");
		pnAttributeSelection.add(cbIntention);

		cbStatus = new JCheckBox("Status");
		pnAttributeSelection.add(cbStatus);

		cbEmotion = new JCheckBox("Emotion");
		pnAttributeSelection.add(cbEmotion);

		cbSpeechActMeaning = new JCheckBox("Speech Act Meaning");
		pnAttributeSelection.add(cbSpeechActMeaning);

		cbMultimediaPath = new JCheckBox("Multimedia Path");
		pnAttributeSelection.add(cbMultimediaPath);

		cbPraiseworthiness = new JCheckBox("Praiseworthiness");
		pnAttributeSelection.add(cbPraiseworthiness);

		cbDesirability = new JCheckBox("Desirability");
		pnAttributeSelection.add(cbDesirability);

		cbTime = new JCheckBox("Time");
		pnAttributeSelection.add(cbTime);

		JPanel pnMechanism = new JPanel();
		pnMechanism.setLayout(new BoxLayout(pnMechanism, BoxLayout.Y_AXIS));
		pnSettings.add(pnMechanism);

		JPanel pnParameters = new JPanel();
		pnParameters.setLayout(new BoxLayout(pnParameters, BoxLayout.Y_AXIS));
		pnParameters.setBorder(BorderFactory.createTitledBorder("Parameters"));
		pnMechanism.add(pnParameters);

		JLabel lbMinimumCoverage = new JLabel("Mininum Coverage:");
		pnParameters.add(lbMinimumCoverage);

		tfMinimumCoverage = new JTextField("1");
		tfMinimumCoverage.setMinimumSize(new Dimension(80, 26));
		tfMinimumCoverage.setMaximumSize(new Dimension(80, 26));
		pnParameters.add(tfMinimumCoverage);

		JPanel pnActions = new JPanel();
		pnActions.setLayout(new BoxLayout(pnActions, BoxLayout.Y_AXIS));
		pnActions.setBorder(BorderFactory.createTitledBorder("Actions"));
		pnMechanism.add(pnActions);

		JButton btGeneralise = new JButton("Generalise");
		btGeneralise.addActionListener(new AlGeneralise());
		pnActions.add(btGeneralise);

		JButton btStoreResult = new JButton("Store Result");
		btStoreResult.addActionListener(new AlStoreResult());
		pnActions.add(btStoreResult);

		for (Component component : pnActions.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnParameters.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		for (Component component : pnSettings.getComponents())
			((JComponent) component).setAlignmentY(Component.TOP_ALIGNMENT);

		JPanel pnResults = new JPanel();
		pnResults.setLayout(new BoxLayout(pnResults, BoxLayout.Y_AXIS));
		pnResults.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnResults);

		tmResults = new TableModelGeneralisation();
		tmResults.addColumn("Attribute Values");
		tmResults.addColumn("Coverage");
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

	public Generalisation getGeneralisation() {
		return generalisation;
	}

	public void setGeneralisation(Generalisation generalisation) {
		this.generalisation = generalisation;
	}

	private class AlGeneralise implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			calculate();
		}
	}

	private void calculate() {

		// update status
		lbStatus.setText("Executing Generalisation mechanism...");

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
		// create list of attributes to use for generalisation
		ArrayList<String> attributeNames = new ArrayList<String>();
		if (cbSubject.isSelected())
			attributeNames.add("subject");
		if (cbAction.isSelected())
			attributeNames.add("action");
		if (cbTarget.isSelected())
			attributeNames.add("target");
		if (cbObject.isSelected())
			attributeNames.add("object");
		if (cbLocation.isSelected())
			attributeNames.add("location");
		if (cbIntention.isSelected())
			attributeNames.add("intention");
		if (cbStatus.isSelected())
			attributeNames.add("status");
		if (cbEmotion.isSelected())
			attributeNames.add("emotion");
		if (cbSpeechActMeaning.isSelected())
			attributeNames.add("speechActMeaning");
		if (cbMultimediaPath.isSelected())
			attributeNames.add("multimediaPath");
		if (cbPraiseworthiness.isSelected())
			attributeNames.add("praiseworthiness");
		if (cbDesirability.isSelected())
			attributeNames.add("desirability");
		if (cbTime.isSelected())
			attributeNames.add("time");

		// parse minimum coverage
		int minimumCoverage = 1;
		try {
			minimumCoverage = Integer.valueOf(tfMinimumCoverage.getText());
		} catch (Exception e) {
			lbStatus.setText("Error while parsing Minimum Coverage!");
			return;
		}

		// execute Generalisation mechanism
		Generalisation generalisation = new Generalisation();
		generalisation.generalise(advancedMemoryComponent.getMemory().getEpisodicMemory(), filterAttributesStr, attributeNames, minimumCoverage);
		this.generalisation = generalisation;

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
		ArrayList<String> filterAttributes = generalisation.getFilterAttributes();
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

		// set check boxes		
		ArrayList<String> attributeNames = generalisation.getAttributeNames();
		for (String attributeName : attributeNames) {
			if (attributeName.equals("subject")) {
				cbSubject.setSelected(true);
			} else if (attributeName.equals("action")) {
				cbAction.setSelected(true);
			} else if (attributeName.equals("target")) {
				cbTarget.setSelected(true);
			} else if (attributeName.equals("object")) {
				cbObject.setSelected(true);
			} else if (attributeName.equals("location")) {
				cbLocation.setSelected(true);
			} else if (attributeName.equals("intention")) {
				cbIntention.setSelected(true);
			} else if (attributeName.equals("status")) {
				cbStatus.setSelected(true);
			} else if (attributeName.equals("emotion")) {
				cbEmotion.setSelected(true);
			} else if (attributeName.equals("speechActMeaning")) {
				cbSpeechActMeaning.setSelected(true);
			} else if (attributeName.equals("multimediaPath")) {
				cbMultimediaPath.setSelected(true);
			} else if (attributeName.equals("praiseworthiness")) {
				cbPraiseworthiness.setSelected(true);
			} else if (attributeName.equals("desirability")) {
				cbDesirability.setSelected(true);
			} else if (attributeName.equals("time")) {
				cbTime.setSelected(true);
			}
		}

		// update minimum coverage
		tfMinimumCoverage.setText(String.valueOf(generalisation.getMinimumCoverage()));

		// clear table model
		int rowCount = tmResults.getRowCount();
		for (int i = 0; i < rowCount; i++) {
			tmResults.removeRow(0);
		}

		// update table model
		Object[] columnNames = new Object[attributeNames.size() + 1];
		for (int i = 0; i < attributeNames.size(); i++) {
			columnNames[i] = attributeNames.get(i);
		}
		columnNames[attributeNames.size()] = "Coverage";
		tmResults.setColumnIdentifiers(columnNames);
		for (GER ger : generalisation.getGers()) {
			Object[] data = new Object[attributeNames.size() + 1];
			for (int i = 0; i < attributeNames.size(); i++) {
				data[i] = ger.getAttributeItem(attributeNames.get(i)).getValue();
			}
			data[attributeNames.size()] = ger.getCoverage();
			tmResults.addRow(data);
		}

		// update status
		lbStatus.setText("Generalisation mechanism executed at " + generalisation.getTime().getRealTimeFormatted());

	}

	private class AlStoreResult implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (generalisation != null) {
				advancedMemoryComponent.getResults().add(generalisation);
				advancedMemoryComponent.getAdvancedMemoryPanel().getOverviewPanel().updateResultList();
				lbStatus.setText("Result stored!");
			} else {
				lbStatus.setText("Result is null and was not stored!");
			}
		}
	}

	private class TableModelGeneralisation extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		public TableModelGeneralisation() {
			super();
		}

		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			default:
				if (column == getColumnCount() - 1)
					return Integer.class;
				else
					return String.class;
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}

}
