package FAtiMA.memory.eventQuery;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.event.rule.DebugAgendaEventListener;
import org.drools.event.rule.DebugWorkingMemoryEventListener;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import FAtiMA.emotionalState.BaseEmotion;
import FAtiMA.memory.ActionDetail;
import FAtiMA.memory.Time;
import FAtiMA.memory.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.memory.autobiographicalMemory.MemoryEpisode;
import FAtiMA.memory.shortTermMemory.STMemoryRecord;
import FAtiMA.memory.shortTermMemory.ShortTermMemory;

public class CompoundCue implements Serializable {
	
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Singleton pattern 
	 */
	private static CompoundCue _ccInstance;
	
	public static CompoundCue GetInstance()
	{
		if(_ccInstance == null)
		{
			_ccInstance = new CompoundCue();
		}
		
		return _ccInstance;
	} 

	private KnowledgeBuilder _kbuilder;	
	private KnowledgeBase _kbase;
	private StatefulKnowledgeSession _ksession;
	
	public CompoundCue()
	{
		try {
			// load up the knowledge base
			_kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			_kbuilder.add(ResourceFactory.newClassPathResource("rules/CompoundCue.drl"), ResourceType.DRL);
			KnowledgeBuilderErrors errors = _kbuilder.getErrors();
			if (errors.size() > 0) {
				for (KnowledgeBuilderError error: errors) {
					System.err.println(error);
				}
				throw new IllegalArgumentException("Could not parse knowledge.");
			}
			_kbase = KnowledgeBaseFactory.newKnowledgeBase();
			_kbase.addKnowledgePackages(_kbuilder.getKnowledgePackages());
			_ksession = _kbase.newStatefulKnowledgeSession();
			
			//_ksession.addEventListener(new DebugAgendaEventListener());
			//_ksession.addEventListener(new DebugWorkingMemoryEventListener());
			// setup the audit logging
			//KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory
			//		.newFileLogger(ksession, "event");
			//logger.close();		
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
	
	/**
	 * Match current entry with events in memory
	 */
	public void Match()
	{			
		try {		
			MemoryEpisode event;
			ArrayList<ActionDetail> details;
			ActionDetail actionDetail;
			CandidateActionDetail cad; 
			
			System.out.println("Compound Cue");
			
			ArrayList<MemoryEpisode> episodes = AutobiographicalMemory.GetInstance().GetAllEpisodes();
			for (int i = 0; i < episodes.size(); i++)
			{
				event = (MemoryEpisode) episodes.get(i);
				details = event.getDetails();
				for (int j = 0; j < details.size(); j++)
				{
					actionDetail = (ActionDetail) details.get(j);
					cad = new CandidateActionDetail(actionDetail);
					_ksession.insert(cad);	
				}
				
			}
			STMemoryRecord records = ShortTermMemory.GetInstance().GetAllRecords();				
			details = records.getDetails();
			
			for (int j = 0; j < details.size(); j++)
			{
				actionDetail = (ActionDetail) details.get(j);
				cad = new CandidateActionDetail(actionDetail);
				_ksession.insert(cad);	
			}	
			
			ActionDetail ad = new ActionDetail(2, "Greta", "SpeechAct", "Amy", null, null, null, "LivingRoom", null);
			CCQuery query = new CCQuery();
			
			query.setQuery(ad);
			_ksession.insert(query);
			
			_ksession.fireAllRules();
			
			Hashtable evaluations = query.getEvaluation();
			Iterator it = evaluations.keySet().iterator();
			while (it.hasNext())
			{
				int id = (Integer) it.next();
				System.out.println("ID " + id + " evaluation " + evaluations.get(id));
			}
			System.out.println("\n\n");
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}

}
