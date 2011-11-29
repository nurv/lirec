/** 
 * ResultPanel.java - Display panel for the result of an Advanced Memory mechanism
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

import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;
import FAtiMA.AdvancedMemory.CompoundCue;
import FAtiMA.AdvancedMemory.Generalisation;
import FAtiMA.AdvancedMemory.SpreadingActivation;

public class ResultPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private AdvancedMemoryComponent advancedMemoryComponent;
	private Object result;

	public ResultPanel(AdvancedMemoryComponent advancedMemoryComponent, Object result) {
		super();
		this.advancedMemoryComponent = advancedMemoryComponent;
		this.result = result;

		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(BorderFactory.createEtchedBorder());

		JLabel lbMechanism = new JLabel();
		lbMechanism.setMinimumSize(new Dimension(140, 26));
		lbMechanism.setMaximumSize(new Dimension(140, 26));
		add(lbMechanism);

		JLabel lbTime = new JLabel();
		lbTime.setMinimumSize(new Dimension(110, 26));
		lbTime.setMaximumSize(new Dimension(110, 26));
		add(lbTime);

		JLabel lbParameters = new JLabel();
		lbParameters.setMinimumSize(new Dimension(430, 26));
		lbParameters.setMaximumSize(new Dimension(430, 26));
		add(lbParameters);

		if (result instanceof CompoundCue) {
			CompoundCue compoundCue = (CompoundCue) result;
			lbMechanism.setText(" " + CompoundCue.NAME);
			lbTime.setText(compoundCue.getTime().getRealTimeFormatted());
			String parameters = "";
			parameters += "Filter Attributes: " + compoundCue.getFilterAttributes();
			parameters += " | ";
			parameters += "Target ID: " + compoundCue.getTargetID();
			lbParameters.setText(parameters);
		} else if (result instanceof SpreadingActivation) {
			SpreadingActivation spreadingActivation = (SpreadingActivation) result;
			lbMechanism.setText(" " + SpreadingActivation.NAME);
			lbTime.setText(spreadingActivation.getTime().getRealTimeFormatted());
			String parameters = "";
			parameters += "Filter Attributes: " + spreadingActivation.getFilterAttributes();
			parameters += " | ";
			parameters += "Target Attribute: " + spreadingActivation.getTargetAttributeName();
			lbParameters.setText(parameters);
		} else if (result instanceof Generalisation) {
			Generalisation generalisation = (Generalisation) result;
			lbMechanism.setText(" " + Generalisation.NAME);
			lbTime.setText(generalisation.getTime().getRealTimeFormatted());
			String parameters = "";
			parameters += "Filter Attributes: " + generalisation.getFilterAttributes();
			parameters += " | ";
			parameters += "Generalisation Attributes: " + generalisation.getAttributeNames();
			parameters += " | ";
			parameters += "Minimum Coverage: " + generalisation.getMinimumCoverage();
			lbParameters.setText(parameters);
		} else {
			lbMechanism.setText("Unknown");
			lbTime.setText("-");
			lbParameters.setText("<>");
		}

		JButton btView = new JButton("View");
		btView.addActionListener(new AlView());
		add(btView);

		JButton btDelete = new JButton("Delete");
		btDelete.addActionListener(new AlDelete());
		add(btDelete);

	}

	private class AlView implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			if (result instanceof CompoundCue) {
				CompoundCuePanel compoundCuePanel = advancedMemoryComponent.getAdvancedMemoryPanel().getCompoundCuePanel();
				compoundCuePanel.setCompoundCue((CompoundCue) result);
				compoundCuePanel.updatePanel();
				advancedMemoryComponent.getAdvancedMemoryPanel().getTpMethods().setSelectedComponent(compoundCuePanel);
			} else if (result instanceof SpreadingActivation) {
				SpreadingActivationPanel spreadingActivationPanel = advancedMemoryComponent.getAdvancedMemoryPanel().getSpreadingActivationPanel();
				spreadingActivationPanel.setSpreadingActivation((SpreadingActivation) result);
				spreadingActivationPanel.updatePanel();
				advancedMemoryComponent.getAdvancedMemoryPanel().getTpMethods().setSelectedComponent(spreadingActivationPanel);
			} else if (result instanceof Generalisation) {
				GeneralisationPanel generalisationPanel = advancedMemoryComponent.getAdvancedMemoryPanel().getGeneralisationPanel();
				generalisationPanel.setGeneralisation((Generalisation) result);
				generalisationPanel.updatePanel();
				advancedMemoryComponent.getAdvancedMemoryPanel().getTpMethods().setSelectedComponent(generalisationPanel);
			}
		}
	}

	private class AlDelete implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			advancedMemoryComponent.getResults().remove(result);
			advancedMemoryComponent.getAdvancedMemoryPanel().getOverviewPanel().updateResultList();
		}
	}

}
