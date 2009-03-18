package FAtiMA.Display;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Agent;
import FAtiMA.memory.Memory;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.socialRelations.Relation;
import FAtiMA.socialRelations.RespectRelation;

public class SocialRelationsPanel extends AgentDisplayPanel {

	private static final long serialVersionUID = 1L;

	JPanel _relationsPanel;

	protected Hashtable _relationsDisplay;

	public SocialRelationsPanel() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		_relationsDisplay = new Hashtable();

		_relationsPanel = new JPanel();
		_relationsPanel.setLayout(new BoxLayout(_relationsPanel,
				BoxLayout.Y_AXIS));

		JScrollPane relationsScroll = new JScrollPane(_relationsPanel);
		relationsScroll.setBorder(BorderFactory
				.createTitledBorder("Social Relations"));

		this.add(relationsScroll);
	}

	public boolean Update(Agent ag) {
		ArrayList relations = LikeRelation.getAllRelations(Memory.GetInstance().getSelf());
		relations.addAll(RespectRelation.getAllRelations(Memory.GetInstance().getSelf()));
		boolean updated = false;

		// in this case, there's a new relation added (it is not usual for
		// relations to disapear)
		// so we have to clear all relations and start displaying them all again
		if (_relationsDisplay.size() != relations.size()) {
			_relationsPanel.removeAll(); // removes all displayed emotions
											// from the panel
			_relationsDisplay.clear();
			Iterator it = relations.iterator();
			while (it.hasNext()) {
				Relation r = (Relation) it.next();
				RelationDisplay display = new RelationDisplay(r);
				_relationsPanel.add(display.getPanel());
				_relationsDisplay.put(r.getHashKey(), display);
			}
			updated = true;
		}

		Iterator it = relations.iterator();
		while (it.hasNext()) {
			Relation r = (Relation) it.next();
			RelationDisplay display = (RelationDisplay) _relationsDisplay
					.get(r.getHashKey());
			display.setValue(r.getValue());
		}

		return updated;
	}
}
