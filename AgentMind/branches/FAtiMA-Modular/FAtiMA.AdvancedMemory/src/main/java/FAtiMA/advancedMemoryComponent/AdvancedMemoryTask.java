package FAtiMA.advancedMemoryComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;

import FAtiMA.Core.memory.Memory;

public class AdvancedMemoryTask extends TimerTask {

	private final static String filepath = "data/characters/minds/state/";

	private AdvancedMemoryComponent advancedMemoryComponent;
	private Memory memory;

	public AdvancedMemoryTask(AdvancedMemoryComponent advancedMemoryComponent, Memory memory) {
		this.advancedMemoryComponent = advancedMemoryComponent;
		this.memory = memory;
	}

	public void run() {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String strTime = sdf.format(cal.getTime());

		ArrayList<String> gAttributes;
		AdvancedMemoryWriter advancedMemoryWriter;

		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("action");
		gAttributes.add("target");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubActTar" + ".xml");

		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("action");
		gAttributes.add("location");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubActLoc" + ".xml");

		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("action");
		gAttributes.add("time");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubActTim" + ".xml");

		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("action");
		gAttributes.add("target");
		gAttributes.add("location");
		gAttributes.add("time");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubActTarLocTim" + ".xml");

		System.out.println(strTime + ": performing Generalisation");
		gAttributes = new ArrayList<String>();
		gAttributes.add("subject");
		gAttributes.add("location");
		gAttributes.add("time");
		advancedMemoryComponent.getGeneralisation().generalise(gAttributes, memory.getEpisodicMemory());
		advancedMemoryWriter = new AdvancedMemoryWriter(advancedMemoryComponent.getGeneralisation().getAllGERs());
		System.out.println(strTime + ": saving XML Advanced Memory");
		advancedMemoryWriter.outputGERtoXML(filepath + "XMLMemory_" + strTime + "_Advanced_SubLocTim" + ".xml");

	}

}
