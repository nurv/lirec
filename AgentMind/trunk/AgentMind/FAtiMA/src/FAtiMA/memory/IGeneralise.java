package FAtiMA.memory;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.memory.episodicMemory.ActionDetail;

public interface IGeneralise {
	
	public void GeneraliseEvents(ArrayList<ActionDetail> actionDetails);
	public Hashtable<ArrayList<Integer>, Hashtable<String, String>> getMatch();
}
