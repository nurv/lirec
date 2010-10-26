package FAtiMA.conditions;

import java.util.ArrayList;

import FAtiMA.AgentModel;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;

public class NullTimeCondition extends TimeCondition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static NullTimeCondition _singleton = new NullTimeCondition();
	
	public static NullTimeCondition GetInstance(){
		return _singleton;
	}
	
	private NullTimeCondition(){
	}
	
	public boolean CheckCondition(){
		return true;
	}
	
	public Object clone()
	{
		return this;
	}
	
	public void MakeGround(ArrayList<Substitution> bindings)
    {
		return;
    }

	public void MakeGround(Substitution subst)
    {
		return;
    }

	public void ReplaceUnboundVariables(int variableID) {
		return;
	}

	public boolean isGrounded() {
		return true;
	}
	
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		ArrayList<SubstitutionSet> list = new ArrayList<SubstitutionSet>();
		list.add(new SubstitutionSet());
		return list;
	}
}
