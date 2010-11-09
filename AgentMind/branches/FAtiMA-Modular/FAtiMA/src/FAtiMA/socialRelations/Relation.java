package FAtiMA.socialRelations;

import FAtiMA.Core.memory.Memory;

public abstract class Relation {

	protected String _subj1;

	protected String _subj2;

	private static final long serialVersionUID = 1L;

	public Relation(){}
	
	
	public Relation(String sub1, String sub2) {
		this._subj1 = sub1;
		this._subj2 = sub2;
	}

	public abstract String increment(Memory m, float intensity);

	public abstract String decrement(Memory m, float intensity);
	
	public abstract float getValue(Memory m);
	
	public abstract void setValue(Memory m, float relationValue);
	
	public abstract String getHashKey();
	
	public abstract String getSubject();
	
	public abstract String getTarget();


}
