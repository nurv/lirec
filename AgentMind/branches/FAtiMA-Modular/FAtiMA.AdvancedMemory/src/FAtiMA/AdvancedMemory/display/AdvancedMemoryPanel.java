/** 
 * AdvancedMemoryPanel.java - Display panel containing the tabs for the
 * results overview panel and the different mechanism panels.
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

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

import FAtiMA.AdvancedMemory.AdvancedMemoryComponent;
import FAtiMA.AdvancedMemory.CompoundCue;
import FAtiMA.AdvancedMemory.Generalisation;
import FAtiMA.AdvancedMemory.SpreadingActivation;
import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;

public class AdvancedMemoryPanel extends AgentDisplayPanel {

	private static final long serialVersionUID = 1L;

	private OverviewPanel overviewPanel;
	private CompoundCuePanel compoundCuePanel;
	private SpreadingActivationPanel spreadingActivationPanel;
	private GeneralisationPanel generalisationPanel;

	private JTabbedPane tpMethods;

	public AdvancedMemoryPanel(AdvancedMemoryComponent advancedMemoryComponent) {
		super();

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		tpMethods = new JTabbedPane();
		this.add(tpMethods);

		overviewPanel = new OverviewPanel(advancedMemoryComponent);
		tpMethods.addTab("Overview", overviewPanel);
		compoundCuePanel = new CompoundCuePanel(advancedMemoryComponent);
		tpMethods.addTab(CompoundCue.NAME, compoundCuePanel);
		spreadingActivationPanel = new SpreadingActivationPanel(advancedMemoryComponent);
		tpMethods.addTab(SpreadingActivation.NAME, spreadingActivationPanel);
		generalisationPanel = new GeneralisationPanel(advancedMemoryComponent);
		tpMethods.addTab(Generalisation.NAME, generalisationPanel);
	}

	public OverviewPanel getOverviewPanel() {
		return overviewPanel;
	}

	public CompoundCuePanel getCompoundCuePanel() {
		return compoundCuePanel;
	}

	public SpreadingActivationPanel getSpreadingActivationPanel() {
		return spreadingActivationPanel;
	}

	public GeneralisationPanel getGeneralisationPanel() {
		return generalisationPanel;
	}

	public JTabbedPane getTpMethods() {
		return tpMethods;
	}

	@Override
	public boolean Update(AgentCore ag) {
		return false;
	}

	@Override
	public boolean Update(AgentModel am) {
		return false;
	}

}
