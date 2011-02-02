package FAtiMA.advancedMemoryComponent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.EpisodicMemory;
import FAtiMA.Core.memory.episodicMemory.MemoryEpisode;


public class RuleEngine implements Serializable {
	
	/**
	 * for serialization purposes
	 */
	private static final long serialVersionUID = 1L;
	
	protected KnowledgeBuilder _kbuilder;
	protected KnowledgeBase _kbase;
	protected StatefulKnowledgeSession _ksession;
	protected String _rulePath;
	
	public RuleEngine(String rulePath)
	{		
		_rulePath = rulePath;
		createKSession();
	}
	
	private void createKSession()
	{	
		try {
			// load up the knowledge base
			_kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			_kbuilder.add(ResourceFactory.newClassPathResource(_rulePath), ResourceType.DRL);
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
			
		} catch (Throwable t) {
			t.printStackTrace();
		}			
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException
	{
		out.writeObject(_rulePath);
		
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		_rulePath = (String) in.readObject();
		createKSession();
	}
	
	protected void AssertData(EpisodicMemory episodicMemory)
	{
		try {		
			MemoryEpisode event;
			ArrayList<ActionDetail> details;
			ActionDetail actionDetail;
			
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
