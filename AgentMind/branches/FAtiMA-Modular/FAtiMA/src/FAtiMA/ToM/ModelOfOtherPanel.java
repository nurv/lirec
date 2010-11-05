package FAtiMA.ToM;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

import FAtiMA.Agent;
import FAtiMA.AgentCore;
import FAtiMA.AgentModel;
import FAtiMA.IComponent;
import FAtiMA.Display.ActionTendenciesPanel;
import FAtiMA.Display.AgentDisplayPanel;
import FAtiMA.Display.EmotionalStatePanel;
import FAtiMA.Display.KnowledgeBasePanel;
import FAtiMA.Display.ShortTermMemoryPanel;
import FAtiMA.motivationalSystem.NeedsPanel;
import FAtiMA.socialRelations.SocialRelationsPanel;

public class ModelOfOtherPanel extends AgentDisplayPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ModelOfOther _model;
	private JTabbedPane _components;
	
	public ModelOfOtherPanel(ModelOfOther m)
	{
		super();
		AgentDisplayPanel panel;
		
	     this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	     _components = new JTabbedPane();
	     
	     _components.addTab("Knowledge Base", null, new KnowledgeBasePanel());
	     
	     _components.addTab("Emotional State", null, new EmotionalStatePanel());
	     
	     _components.addTab("ShortTermMemory", null, new ShortTermMemoryPanel());
	     
	     _components.addTab("ActionTendencies", null, new ActionTendenciesPanel());
	     
	     
	     for(IComponent c: m.getComponents())
	     {
	    	 panel = c.createComponentDisplayPanel(m);
	    	 if(panel != null)
	    	 {
	    		 _components.addTab(c.name(),null,panel);
	    	 }
	     }
	     
	     
	     this.add(_components);
		_model = m;
	}
	
	public boolean Update(AgentCore ag)
	{
		return Update((AgentModel) ag);
	}

	@Override
	public boolean Update(AgentModel am) {
		AgentDisplayPanel pnl = (AgentDisplayPanel) _components.getSelectedComponent();
		return pnl.Update(_model);
	}

}
