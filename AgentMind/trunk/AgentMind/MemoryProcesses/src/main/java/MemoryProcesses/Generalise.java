package MemoryProcesses;

import java.util.ArrayList;
import java.util.Hashtable;

import FAtiMA.memory.IGeneralise;
import FAtiMA.memory.episodicMemory.ActionDetail;
import FAtiMA.memory.episodicMemory.EpisodicMemory;

public class Generalise extends RuleEngine implements IGeneralise {
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	private static final String _gRulePath = "Generalise.drl";
	
	private Hashtable<String, Float> _results;
	private GQuery _gQuery;
	
	public Generalise()
	{
		super(_gRulePath);
		_gQuery = new GQuery();		
	}
	
	/**
	 * Generalise on a set of events
	 */
	//public void Match(ActionDetail queryEvent, ArrayList<MemoryEpisode> episodes, ArrayList<ActionDetail> records)
	public void GeneraliseEvents(ArrayList<ActionDetail> actionDetails)
	{			
		ActionDetail actionDetail;
		try {		
			System.out.println("Generalise");
			
			// assert events from memory
			for (int j = 0; j < actionDetails.size(); j++)
			{
				actionDetail = (ActionDetail) actionDetails.get(j);
				System.out.println("ID" + actionDetail.getID());
				_ksession.insert(actionDetail);
			}				
			
			_ksession.insert(_gQuery);
			// fire all CC rules
			_ksession.fireAllRules();	
			//_ksession.retract(queryHandle);
			
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
	
	public Hashtable<ArrayList<Integer>, Hashtable<String, String>> getMatch()
	{
		return _gQuery.getMatch();
	}
}
