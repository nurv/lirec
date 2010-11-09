package FAtiMA.Core.memory;


import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;

public interface ICompoundCue {
	
	public void Match(ActionDetail queryEvent, EpisodicMemory episodicMemory);
	
	//public Hashtable<Integer, Float> getCCResults();
	
	public ActionDetail getStrongestResult();
	
	public float getEvaluation();

}
