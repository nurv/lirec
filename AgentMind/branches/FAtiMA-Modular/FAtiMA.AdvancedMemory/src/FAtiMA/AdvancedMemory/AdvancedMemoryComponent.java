/** 
 * AdvancedMemoryComponent.java - FAtiMA component handling Advanced Memory mechanisms like
 * Compound Cue, Spreading Activation and Generalisation.
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

package FAtiMA.AdvancedMemory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.AdvancedMemory.display.AdvancedMemoryPanel;
import FAtiMA.AdvancedMemory.parsers.AdvancedMemoryHandler;
import FAtiMA.AdvancedMemory.writers.AdvancedMemoryWriter;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IProcessExternalRequestComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;

public class AdvancedMemoryComponent implements Serializable, IProcessExternalRequestComponent, IAppraisalDerivationComponent {

	private static final long serialVersionUID = 1;

	public static final String FILENAME = "XMLMemoryAdvanced";

	private static final String NAME = "AdvancedMemory";

	private static final String SA_MEMORY = "SA-MEMORY";
	private static final String CC_MEMORY = "CC-MEMORY";
	private static final String G_MEMORY = "G-MEMORY";
	private static final String SAVE_ADV_MEMORY = "SAVE_ADV_MEMORY";
	private static final String LOAD_ADV_MEMORY = "LOAD_ADV_MEMORY";

	private Memory memory;

	private ArrayList<Object> results; // CompoundCue, SpreadingActivation, Generalisation

	private AdvancedMemoryPanel advancedMemoryPanel;

	public AdvancedMemoryComponent() {
		results = new ArrayList<Object>();
	}

	public Memory getMemory() {
		return memory;
	}

	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	public ArrayList<Object> getResults() {
		return results;
	}

	public void setResults(ArrayList<Object> results) {
		this.results = results;
	}

	public AdvancedMemoryPanel getAdvancedMemoryPanel() {
		return advancedMemoryPanel;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public void initialize(AgentModel am) {
		memory = am.getMemory();
		if (ConfigurationManager.getMemoryLoad()) {
			load(memory.getSaveDirectory() + FILENAME);
		}
	}

	@Override
	public void reset() {
	}

	@Override
	public void update(AgentModel am, long time) {
	}

	@Override
	public void update(AgentModel am, Event e) {
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		advancedMemoryPanel = new AdvancedMemoryPanel(this);
		return advancedMemoryPanel;
	}

	@Override
	public ReflectXMLHandler getActionsParser(AgentModel am) {
		return null;
	}

	@Override
	public ReflectXMLHandler getGoalsParser(AgentModel am) {
		return null;
	}

	@Override
	public ReflectXMLHandler getPersonalityParser(AgentModel am) {
		return null;
	}

	@Override
	public void parseAdditionalFiles(AgentModel am) {
	}

	@Override
	public String[] getComponentDependencies() {
		return new String[0];
	}

	@Override
	public void processExternalRequest(AgentModel am, String msgType, String perception) {

		if (msgType.equals(SA_MEMORY)) {

			// parse perception
			StringTokenizer stringTokenizer = new StringTokenizer(perception, "$");
			String targetAttribute = stringTokenizer.nextToken();
			String filterAttributesStr = "";
			while (stringTokenizer.hasMoreTokens()) {
				filterAttributesStr += stringTokenizer.nextToken();
			}

			// execute Spreading Activation mechanism
			SpreadingActivation spreadingActivation = new SpreadingActivation();
			spreadingActivation.spreadActivation(memory.getEpisodicMemory(), filterAttributesStr, targetAttribute);

			// add to results
			results.add(spreadingActivation);
			advancedMemoryPanel.getOverviewPanel().updateResultList();

		} else if (msgType.equals(CC_MEMORY)) {

			// choose target action detail
			ActionDetail actionDetailTarget = memory.getEpisodicMemory().getSTEM().getDetails().get(0);

			// execute Spreading Activation mechanism
			CompoundCue compoundCue = new CompoundCue();
			compoundCue.execute(memory.getEpisodicMemory(), actionDetailTarget);

			// add to results
			results.add(compoundCue);
			advancedMemoryPanel.getOverviewPanel().updateResultList();

		} else if (msgType.equals(G_MEMORY)) {

			// parse perception
			ArrayList<String> attributeNames = new ArrayList<String>();
			StringTokenizer stringTokenizer = new StringTokenizer(perception, "*");
			while (stringTokenizer.hasMoreTokens()) {
				attributeNames.add(stringTokenizer.nextToken());
			}

			// execute Generalisation mechanism
			Generalisation generalisation = new Generalisation();
			generalisation.generalise(memory.getEpisodicMemory(), attributeNames, 1);

			// add to results
			results.add(generalisation);
			advancedMemoryPanel.getOverviewPanel().updateResultList();

		} else if (msgType.equals(SAVE_ADV_MEMORY)) {
			save(memory.getSaveDirectory() + FILENAME);

		} else if (msgType.equals(LOAD_ADV_MEMORY)) {
			load(memory.getSaveDirectory() + FILENAME);

		}

	}

	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame af) {

		ActionDetail actionDetailTarget = new ActionDetail(0, e.GetSubject(), e.GetAction(), e.GetTarget(), e.GetParameters(), null, null, null);
		CompoundCue compoundCue = new CompoundCue();
		ActionDetail actionDetailMax = compoundCue.execute(memory.getEpisodicMemory(), actionDetailTarget);

		if (actionDetailMax != null) {
			float desirability = actionDetailMax.getDesirability();
			if (desirability != 0) {
				af.SetAppraisalVariable(AdvancedMemoryComponent.NAME, (short) 3, OCCAppraisalVariables.DESIRABILITY.name(), desirability);
			}
		}

	}

	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
	}

	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
	}

	public void load(String fileName) {
		AgentLogger.GetInstance().log("LOADING Advanced Memory: " + fileName);
		try {
			AdvancedMemoryHandler advancedMemoryHandler = new AdvancedMemoryHandler(this);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			File file = new File(fileName);
			if (file.exists()) {
				parser.parse(new File(fileName), advancedMemoryHandler);
			} else {
				AgentLogger.GetInstance().log("File does not exist: " + fileName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (advancedMemoryPanel != null) {
			advancedMemoryPanel.getOverviewPanel().updateResultList();
		}
	}

	public void save(String fileName) {
		AdvancedMemoryWriter advancedMemoryWriter = new AdvancedMemoryWriter();
		advancedMemoryWriter.write(results, fileName);
	}

}
