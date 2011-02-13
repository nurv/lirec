package FAtiMA.socialRelations.display;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.util.Constants;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.socialRelations.Relation;
import FAtiMA.socialRelations.RespectRelation;

public class SocialRelationsPanel extends AgentDisplayPanel {

	private static final long serialVersionUID = 1L;

	JPanel _relationsPanel;

	protected Hashtable<String, RelationDisplay> _relationsDisplay;

	public SocialRelationsPanel() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		_relationsDisplay = new Hashtable<String, RelationDisplay>();

		_relationsPanel = new JPanel();
		_relationsPanel.setLayout(new BoxLayout(_relationsPanel,
				BoxLayout.Y_AXIS));

		JScrollPane relationsScroll = new JScrollPane(_relationsPanel);
		relationsScroll.setBorder(BorderFactory
				.createTitledBorder("Social Relations"));

		this.add(relationsScroll);
	}
	
	public boolean Update(AgentModel am)
	{
		
		ArrayList<Relation> relations = new ArrayList<Relation>();
		relations.addAll(LikeRelation.getAllRelations(am.getMemory(),Constants.SELF));
		relations.addAll(RespectRelation.getAllRelations(am.getMemory(), Constants.SELF)); 
		
		boolean updated = false;

		// in this case, there's a new relation added (it is not usual for
		// relations to disappear)
		// so we have to clear all relations and start displaying them all again
		if (_relationsDisplay.size() != relations.size()) {
			_relationsPanel.removeAll(); // removes all displayed emotions
											// from the panel
			_relationsDisplay.clear();
			Iterator<Relation> it = relations.iterator();
			while (it.hasNext()) {
				Relation r = (Relation) it.next();
				RelationDisplay display = new RelationDisplay(am.getMemory(), r);
				_relationsPanel.add(display.getPanel());
				_relationsDisplay.put(r.getHashKey(), display);
			}
			updated = true;
		}

		Iterator<Relation> it = relations.iterator();
		while (it.hasNext()) {
			Relation r = (Relation) it.next();
			RelationDisplay display = (RelationDisplay) _relationsDisplay
					.get(r.getHashKey());
			display.setValue(r.getValue(am.getMemory()));
		}

		return updated;
	}

	public boolean Update(AgentCore ag) 
	{
		return Update((AgentModel) ag);
	}
}
