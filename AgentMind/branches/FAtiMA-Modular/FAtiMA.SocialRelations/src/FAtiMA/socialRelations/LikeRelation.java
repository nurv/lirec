package FAtiMA.socialRelations;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;

public class LikeRelation extends Relation {

	public LikeRelation(String sub1, String sub2) {
		this._subj1 = sub1;
		this._subj2 = sub2;
	}

	public String increment(Memory m, float intensity) {
		float like = getValue(m);
		like += intensity / 2;
		if (like > 10) {
			like = 10;
		}
		setValue(m, like);
		return _subj2 + ": " + like;
	}

	public String decrement(Memory m, float intensity) {
		float like = getValue(m);
		like -= intensity / 2;
		if (like < -10) {
			like = -10;
		}
		setValue(m, like);
		return this._subj2 + ": " + like;
	}

	public float getValue(Memory m) {
		Name relationProperty = Name.ParseName("Like(" + this._subj1 + ","
				+ this._subj2 + ")");
		Float result = (Float) m.getSemanticMemory().AskProperty(
				relationProperty);
		//If relation doesn't exists, create it in a neutral state
		if (result == null) {
			m.getSemanticMemory().Tell(relationProperty, new Float(0));
			return 0;
		}
		return result.floatValue();
	}

	public void setValue(Memory m, float like) {
		Name relationProperty = Name.ParseName("Like(" + this._subj1 + ","
				+ this._subj2 + ")");
		m.getSemanticMemory().Tell(relationProperty, new Float(like));
	}

	public String getHashKey() {
		return RelationType.LIKE + "-" + this._subj1 + this._subj2;
	}
	
	public String getSubject() {
		return _subj1;
	}
	
	public String getTarget() {
		return _subj2;
	}

	public static Relation getRelation(String subject1, String subject2) {
		return new LikeRelation(subject1, subject2);
	}

	public static ArrayList<LikeRelation> getAllRelations(Memory m, String subject1) {
		ArrayList<LikeRelation> relations = new ArrayList<LikeRelation>();

		Name relationProperty = Name.ParseName("Like(" + subject1 + ",[X])");
		ArrayList<SubstitutionSet> bindingSets = m.getSemanticMemory().GetPossibleBindings(relationProperty);

		if (bindingSets != null) {
			for (ListIterator<SubstitutionSet> li = bindingSets.listIterator(); li.hasNext();) {
				SubstitutionSet subSet =  li.next();
				Substitution sub = (Substitution) subSet.GetSubstitutions()
						.get(0);
				String target = sub.getValue().toString();
				relations.add(new LikeRelation(subject1, target));
			}
		}

		return relations;
	}

}
