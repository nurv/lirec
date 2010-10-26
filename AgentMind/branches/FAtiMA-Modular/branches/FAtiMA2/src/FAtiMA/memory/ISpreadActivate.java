package FAtiMA.memory;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.memory.episodicMemory.ActionDetail;
import FAtiMA.memory.episodicMemory.MemoryEpisode;
import FAtiMA.memory.episodicMemory.EpisodicMemory;;

public interface ISpreadActivate {
	
	public void Spread(String question, ArrayList<String> knownInfo, EpisodicMemory episodicMemory);
	//ArrayList<MemoryEpisode> episodes, ArrayList<ActionDetail> records
	public Hashtable<String, Integer> getSAResult();
}
