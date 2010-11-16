package FAtiMA.advancedMemoryComponent;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;

public interface ICommonalities {
	
	public void eventCommonalities(ArrayList<ActionDetail> actionDetails);
	public Hashtable<ArrayList<Integer>, Hashtable<String, String>> getMatch();
}
