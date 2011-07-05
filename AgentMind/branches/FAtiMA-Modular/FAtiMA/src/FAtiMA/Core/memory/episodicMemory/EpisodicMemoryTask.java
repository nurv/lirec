package FAtiMA.Core.memory.episodicMemory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;

import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.util.writers.MemoryWriter;

public class EpisodicMemoryTask extends TimerTask {

	private final static String filepath = "data/characters/minds/state/";

	private Memory memory;

	public EpisodicMemoryTask(Memory memory) {
		this.memory = memory;
	}

	public void run() {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String strTime = sdf.format(cal.getTime());

		System.out.println(strTime + ": saving XML Memory");
		MemoryWriter memoryWriter = new MemoryWriter(memory);
		memoryWriter.outputMemorytoXML(filepath + "XMLMemory_" + strTime + ".xml");

		System.out.println(strTime + ": performing AB Forgetting (20%)");
		memory.getEpisodicMemory().calculateActivationValues();
		ArrayList<ActionDetail> selected = memory.getEpisodicMemory().activationBasedSelectionByAmount(0.8);
		ArrayList<Integer> selectedIDs = new ArrayList<Integer>();
		for (ActionDetail actionDetail : selected) {
			selectedIDs.add(new Integer(actionDetail.getID()));
		}
		memory.getEpisodicMemory().activationBasedForgetting(selectedIDs);

		System.out.println(strTime + ": saving XML Memory");
		memoryWriter.outputMemorytoXML(filepath + "XMLMemory_" + strTime + "_AfterABForgetting" + ".xml");

	}

}
