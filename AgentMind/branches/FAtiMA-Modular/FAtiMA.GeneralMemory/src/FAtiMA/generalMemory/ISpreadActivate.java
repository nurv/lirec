package FAtiMA.generalMemory;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;

public interface ISpreadActivate {
	
	public void Spread(String question, ArrayList<String> knownInfo, EpisodicMemory episodicMemory);
	//ArrayList<MemoryEpisode> episodes, ArrayList<ActionDetail> records
	public Hashtable<String, Integer> getSAResults();
	public String getSABestResult();
	public ArrayList<ActionDetail> getDetails();
}
