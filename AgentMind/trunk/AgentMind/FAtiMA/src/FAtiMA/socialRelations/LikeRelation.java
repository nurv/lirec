package FAtiMA.socialRelations;

import java.util.ArrayList;
import java.util.ListIterator;

//import FAtiMA.knowledgeBase.KnowledgeBase;
import FAtiMA.util.enumerables.RelationType;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.memory.shortTermMemory.WorkingMemory;
import FAtiMA.memory.Memory;

public class LikeRelation extends Relation {

	public LikeRelation(String sub1, String sub2) {
		this._subj1 = sub1;
		this._subj2 = sub2;
	}

	public String increment(float intensity) {
		float like = getValue();
		like += intensity / 5;
		if (like > 10) {
			like = 10;
		}
		setValue(like);
		return _subj2 + ": " + like;
	}

	public String decrement(float intensity) {
		float like = getValue();
		like -= intensity / 5;
		if (like < -10) {
			like = -10;
		}
		setValue(like);
		return this._subj2 + ": " + like;
	}

	public float getValue() {
		Name relationProperty = Name.ParseName("Like(" + this._subj1 + ","
				+ this._subj2 + ")");
		Float result = (Float) Memory.GetInstance().AskProperty(relationProperty);
		//If relation doesn't exists, create it in a neutral state
		if (result == null) {
			WorkingMemory.GetInstance().Tell(relationProperty, new Float(0));
			//System.out.println("get value LikeRelation");
			return 0;
		}
		return result.floatValue();
	}

	public void setValue(float like) {
		Name relationProperty = Name.ParseName("Like(" + this._subj1 + ","
				+ this._subj2 + ")");
		WorkingMemory.GetInstance().Tell(relationProperty, new Float(like));
		//System.out.println("Set value LikeRelation");
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

	public static ArrayList getAllRelations(String subject1) {
		ArrayList relations = new ArrayList();

		Name relationProperty = Name.ParseName("Like(" + subject1 + ",[X])");
		ArrayList bindingSets = Memory.GetInstance()
				.GetPossibleBindings(relationProperty);

		if (bindingSets != null) {
			for (ListIterator li = bindingSets.listIterator(); li.hasNext();) {
				SubstitutionSet subSet = (SubstitutionSet) li.next();
				Substitution sub = (Substitution) subSet.GetSubstitutions()
						.get(0);
				String target = sub.getValue().toString();
				relations.add(new LikeRelation(subject1, target));
			}
		}

		return relations;
	}

}
