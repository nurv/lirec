package FAtiMA.ToM;

import javax.swing.BoxLayout;
import javax.swing.JTabbedPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.Display.EmotionalStatePanel;
import FAtiMA.Core.Display.KnowledgeBasePanel;
import FAtiMA.Core.Display.ShortTermMemoryPanel;
import FAtiMA.Core.componentTypes.IComponent;

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
	     
	     for(IComponent c: m.getComponents())
	     {
	    	 panel = c.createDisplayPanel(m);
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
