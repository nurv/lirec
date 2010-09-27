package RetrievalProcesses;
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
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import FAtiMA.memory.episodicMemory.ActionDetail;
import FAtiMA.memory.episodicMemory.EpisodicMemory;
import FAtiMA.memory.episodicMemory.MemoryEpisode;


public class RuleEngine implements Serializable {
	
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	protected KnowledgeBuilder _kbuilder;	
	protected KnowledgeBase _kbase;
	protected StatefulKnowledgeSession _ksession;
	
	public RuleEngine(String rulePath)
	{
		try {
			// load up the knowledge base
			_kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			_kbuilder.add(ResourceFactory.newClassPathResource(rulePath), ResourceType.DRL);
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
	
	protected void AssertData(EpisodicMemory episodicMemory)
	{
		try {		
			MemoryEpisode event;
			ArrayList<ActionDetail> details;
			ActionDetail actionDetail;
			CandidateEvent cad; 
			
			_ksession = _kbase.newStatefulKnowledgeSession();
		
			ArrayList<MemoryEpisode> episodes = episodicMemory.GetAllEpisodes(); 
			for (int i = 0; i < episodes.size(); i++)
			{
				event = (MemoryEpisode) episodes.get(i);
				details = event.getDetails();
				for (int j = 0; j < details.size(); j++)
				{
					actionDetail = (ActionDetail) details.get(j);
					_ksession.insert(actionDetail);
					//cad = new CandidateEvent(actionDetail);
					//_ksession.insert(cad);	
				}				
			}
			
			ArrayList<ActionDetail> records = episodicMemory.getDetails();
			for (int j = 0; j < records.size(); j++)
			{
				actionDetail = (ActionDetail) records.get(j);
				_ksession.insert(actionDetail);
				//cad = new CandidateEvent(actionDetail);
				//_ksession.insert(cad);	
			}	
		} catch (Throwable t) {
			t.printStackTrace();
		}		
	}	
}
