package FAtiMA.memory;


import FAtiMA.memory.episodicMemory.ActionDetail;
import FAtiMA.memory.episodicMemory.EpisodicMemory;

public interface ICompoundCue {
	
	public void Match(ActionDetail queryEvent, EpisodicMemory episodicMemory);
	
	//public Hashtable<Integer, Float> getCCResults();
	
	public ActionDetail getStrongestResult();
	
	public float getEvaluation();

}
