package FAtiMA.socialRelations;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;

public class RespectRelation extends Relation{

	public RespectRelation(String sub1, String sub2)
	{
		this._subj1 = sub1;
		this._subj2 = sub2;
	}
	
	public String increment(Memory m, float intensity)
	{
		float respect = getValue(m);
		respect+= intensity/2;
		if(respect > 10)
		{
			respect = 10;
		}
		setValue(m, respect);
		return this._subj2 + ": " + respect;
	}
	
	public String decrement(Memory m, float intensity)
	{
		float respect = getValue(m);
		respect-= intensity/2;
		if(respect < -10)
		{
			respect = -10;
		}
		setValue(m, respect);
		return this._subj2 + ": " + respect;
	}
	
	
	public float getValue(Memory m)
	{
		Name respectProperty = Name.ParseName("Respect(" + this._subj1 + "," + this._subj2 + ")");
		Float result = (Float) m.getSemanticMemory().AskProperty(respectProperty);
		//If relation doesn't exists, create it in a neutral state
		if(result == null)
		{
			m.getSemanticMemory().Tell(respectProperty, new Float(0));
			return 0;
		}
		return result.floatValue();
	}
	
	public void setValue(Memory m, float like)
	{
		Name respectProperty = Name.ParseName("Respect(" + this._subj1 + "," + this._subj2 + ")");
		m.getSemanticMemory().Tell(respectProperty, new Float(like));
	}
	
	public String getHashKey() {
		return RelationType.RESPECT + "-" + this._subj1 + this._subj2;
	}
	
	public String getTarget() {
		return _subj2;
	}
	
	public String getSubject() {
		return _subj1;
	}

	public static Relation getRelation(String subject1, String subject2) {
		return new RespectRelation(subject1, subject2);
	}

	public static ArrayList<RespectRelation> getAllRelations(Memory m, String subject1) {
		ArrayList<RespectRelation> relations = new ArrayList<RespectRelation>();

		Name relationProperty = Name.ParseName("Respect(" + subject1 + ",[X])");
		ArrayList<SubstitutionSet> bindingSets = m.getSemanticMemory().GetPossibleBindings(relationProperty);

		if (bindingSets != null) {
			for (ListIterator<SubstitutionSet> li = bindingSets.listIterator(); li.hasNext();) {
				SubstitutionSet subSet = (SubstitutionSet) li.next();
				Substitution sub = (Substitution) subSet.GetSubstitutions()
						.get(0);
				String target = sub.getValue().toString();
				relations.add(new RespectRelation(subject1, target));
			}
		}
		return relations;
	}
}
