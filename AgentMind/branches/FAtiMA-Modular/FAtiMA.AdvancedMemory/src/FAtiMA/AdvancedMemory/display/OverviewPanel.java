/** 
 * OverviewPanel.java - Display panel for the Advanced Memory result
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;
import FAtiMA.Core.memory.episodicMemory.Time;

public class OverviewPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private AdvancedMemoryPanel advancedMemoryPanel;

	private JTextField tfFileName;

	private JPanel pnResults;

	private JLabel lbStatus;

	public OverviewPanel(AdvancedMemoryPanel advancedMemoryPanel) {
		super();
		this.advancedMemoryPanel = advancedMemoryPanel;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel pnActions = new JPanel();
		pnActions.setLayout(new BoxLayout(pnActions, BoxLayout.X_AXIS));
		pnActions.setBorder(BorderFactory.createEtchedBorder());
		this.add(pnActions);

		JLabel lbFileName = new JLabel("File Name:");
		pnActions.add(lbFileName);

		String fileName = advancedMemoryPanel.getAdvancedMemoryComponent().getMemory().getSaveDirectory() + AdvancedMemoryComponent.FILENAME;
		tfFileName = new JTextField(fileName);
		tfFileName.setMinimumSize(new Dimension(320, 26));
		tfFileName.setMaximumSize(new Dimension(320, 26));
		pnActions.add(tfFileName);

		JButton btLoad = new JButton("Load Advanced Memory");
		btLoad.addActionListener(new AlLoad());
		pnActions.add(btLoad);

		JButton btSave = new JButton("Save Advanced Memory");
		btSave.addActionListener(new AlSave());
		pnActions.add(btSave);

		pnResults = new JPanel();
		pnResults.setLayout(new BoxLayout(pnResults, BoxLayout.Y_AXIS));

		JScrollPane spResults = new JScrollPane(pnResults);
		this.add(spResults);

		JPanel pnStatus = new JPanel();
		pnStatus.setLayout(new BoxLayout(pnStatus, BoxLayout.X_AXIS));
		this.add(pnStatus);

		lbStatus = new JLabel(" ");
		pnStatus.add(lbStatus);

	}

	public AdvancedMemoryPanel getAdvancedMemoryPanel() {
		return advancedMemoryPanel;
	}

	public void updateResultList() {

		pnResults.removeAll();
		for (Object result : advancedMemoryPanel.getAdvancedMemoryComponent().getResults()) {
			pnResults.add(new ResultPanel(this, result));
		}
		//pnResults.repaint();
		// workaround
		pnResults.updateUI();

		for (Component component : pnResults.getComponents())
			((JComponent) component).setAlignmentX(Component.LEFT_ALIGNMENT);

		//lbStatus.setText("Result list updated at " + new Time().getRealTimeFormatted());

	}

	private class AlLoad implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String fileName = tfFileName.getText();
			Time time = new Time();
			advancedMemoryPanel.getAdvancedMemoryComponent().load(fileName);
			lbStatus.setText("Results loaded from \"" + fileName + "\" at " + time.getRealTimeFormatted());
		}
	}

	private class AlSave implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			String fileName = tfFileName.getText();
			Time time = new Time();
			advancedMemoryPanel.getAdvancedMemoryComponent().save(fileName);
			lbStatus.setText("Results saved to \"" + fileName + "\" at " + time.getRealTimeFormatted());
		}
	}

}
