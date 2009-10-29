package FAtiMA.conditions;

import java.util.ArrayList;

import FAtiMA.AgentModel;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;

public class NullPlaceCondition extends PlaceCondition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static NullPlaceCondition _singleton = new NullPlaceCondition();
	
	public static NullPlaceCondition GetInstance(){
		return _singleton;
	}
	
	private NullPlaceCondition(){
	}
	
	public boolean CheckCondition(){
		return true;
	}
	
	public Object clone()
	{
		return this;
	}
	
	public void MakeGround(ArrayList bindings)
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
	
	public ArrayList GetValidBindings(AgentModel am) {
		ArrayList list = new ArrayList();
		list.add(new SubstitutionSet());
		return list;
	}

}
