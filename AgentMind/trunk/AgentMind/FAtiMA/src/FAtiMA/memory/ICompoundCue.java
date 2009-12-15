package FAtiMA.memory;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.memory.episodicMemory.ActionDetail;
import FAtiMA.memory.episodicMemory.EpisodicMemory;
import FAtiMA.memory.episodicMemory.MemoryEpisode;

public interface ICompoundCue {
	
	public void Match(ActionDetail queryEvent, EpisodicMemory episodicMemory);
	
	public Hashtable<Integer, Float> getCCResults();

}
