package FAtiMA.Display;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

import FAtiMA.Agent;
import FAtiMA.AgentModel;
import FAtiMA.ModelOfOther;

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
	     this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
	     _components = new JTabbedPane();
	     
	     _components.addTab("Knowledge Base", null, new KnowledgeBasePanel());
	     
	     _components.addTab("Emotional State", null, new EmotionalStatePanel());
	     
	     _components.addTab("ShortTermMemory", null, new ShortTermMemoryPanel());
	     
	     
	     this.add(_components);
		_model = m;
	}
	
	public boolean Update(Agent ag)
	{
		return Update((AgentModel) ag);
	}

	@Override
	public boolean Update(AgentModel am) {
		AgentDisplayPanel pnl = (AgentDisplayPanel) _components.getSelectedComponent();
		return pnl.Update(_model);
	}

}
