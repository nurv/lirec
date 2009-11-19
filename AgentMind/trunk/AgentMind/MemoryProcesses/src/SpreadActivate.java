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

import FAtiMA.memory.ActionDetail;
import FAtiMA.memory.autobiographicalMemory.AutobiographicalMemory;
import FAtiMA.memory.autobiographicalMemory.MemoryEpisode;
import FAtiMA.memory.shortTermMemory.STMemoryRecord;
import FAtiMA.memory.shortTermMemory.ShortTermMemory;

public class SpreadActivate implements Serializable {
	
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Singleton pattern 
	 */
	private static SpreadActivate _saInstance;
	
	public static SpreadActivate GetInstance()
	{
		if(_saInstance == null)
		{
			_saInstance = new SpreadActivate();
		}
		
		return _saInstance;
	} 

	private KnowledgeBuilder _kbuilder;	
	private KnowledgeBase _kbase;
	private StatefulKnowledgeSession _ksession;
	
	public SpreadActivate()
	{
		try {
			// load up the knowledge base
			_kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			_kbuilder.add(ResourceFactory.newClassPathResource("rules/SpreadActivate.drl"), ResourceType.DRL);
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
	 * Spread activate through the memory
	 */
	public void Spread(ArrayList<MemoryEpisode> episodes)
	{			
		try {
			MemoryEpisode event;
			ArrayList<ActionDetail> details;
			ActionDetail actionDetail;
			CandidateActionDetail cad; 
			
			System.out.println("Spreading Activation");
			
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
									
			ArrayList<String> knownInfo = new ArrayList<String>();
			knownInfo.add("subject Greta");
			knownInfo.add("target Amy");
			
			SAQuery query = new SAQuery();			
			query.setQuery(knownInfo, "action");
			_ksession.insert(query);
			
			_ksession.fireAllRules();
			
			Hashtable answers = query.getAnswers();
			Iterator it = answers.keySet().iterator();
			while (it.hasNext())
			{
				String answer = (String) it.next();
				System.out.println("Location " + answer + " frequency " + answers.get(answer));
			}
			System.out.println("\n");
			
			SAQuery query2 = new SAQuery();
			knownInfo.clear();
			knownInfo.add("subject Amy");
			knownInfo.add("action Accept");
			query2.setQuery(knownInfo, "target");
			_ksession.insert(query2);
			
			_ksession.fireAllRules();
			
			Hashtable answers2 = query2.getAnswers();
			Iterator it2 = answers2.keySet().iterator();
			while (it2.hasNext())
			{
				String answer = (String) it2.next();
				System.out.println("Target " + answer + " frequency " + answers2.get(answer));
			}			
			System.out.println("\n\n");
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}
}
