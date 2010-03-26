package FAtiMA.conditions;

import java.util.ArrayList;

import FAtiMA.AgentModel;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;

/**
 * Context of a Ritual.
 * @author nafonso
 *
 */

public class Context extends Condition{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TimeCondition _timeCondition;
	private PlaceCondition _placeCondition;
	private ArrayList<SocialCondition> _socialConditions;
	
	public Context(){
		_timeCondition = NullTimeCondition.GetInstance();
		_placeCondition = NullPlaceCondition.GetInstance();
		_socialConditions = new ArrayList<SocialCondition>();
	}
	
	public void SetTimeCondition( TimeCondition timeCondition ){
		_timeCondition = timeCondition;
	}
	
	public TimeCondition GetTimeCondition(){
		return _timeCondition;
	}
	
	public void SetPlaceCondition( PlaceCondition placeCondition ){
		_placeCondition = placeCondition;
	}
	
	public PlaceCondition GetPlaceCondition(){
		return _placeCondition;
	}
	
	public void SetSocialConditions( ArrayList<SocialCondition> socialConditions ){
		_socialConditions = socialConditions;
	}
	
	public void AddSocialCondition( SocialCondition socialCondition ){
		_socialConditions.add(socialCondition);
	}

	public boolean CheckCondition(AgentModel am) {
		if( !_timeCondition.CheckCondition(am) || !_placeCondition.CheckCondition(am) )
			return false;
		/*for( int i = 0, limit = _socialConditions.size(); i != limit; ++i ){
			if( !((PropertyCondition)_socialConditions.get(i)).CheckCondition(am) )
				return false;
		}*/
		return true;
	}

	public Object clone() {
		Context aux = new Context();
		aux._timeCondition = (TimeCondition)_timeCondition.clone();
		aux._placeCondition = (PlaceCondition)_placeCondition.clone();
		for( int i = 0, limit = _socialConditions.size(); i != limit; ++i )
			aux._socialConditions.add((SocialCondition)_socialConditions.get(i).clone() );
		return aux;
	}

	/**
	 * @deprecated
	 */
	public Object GenerateName(int id)
	{
		Context aux = (Context) this.clone();
		aux.ReplaceUnboundVariables(id);
		return aux;
	}

	public Object Ground(ArrayList<Substitution> bindingConstraints)
	{
		Context aux = (Context) this.clone();
		aux.MakeGround(bindingConstraints);
		return aux;
	}

	public Object Ground(Substitution subst)
	{
		Context aux = (Context) this.clone();
		aux.MakeGround(subst);
		return aux;
	}
	
	public void MakeGround(ArrayList<Substitution> bindings)
    {
		_timeCondition.MakeGround(bindings);
		_placeCondition.MakeGround(bindings);
		for( int i = 0, limit = _socialConditions.size(); i != limit; ++i ){
			((SocialCondition)_socialConditions.get(i)).MakeGround(bindings);
		}
    	//this._name.MakeGround(bindings);
    	//this._value.MakeGround(bindings);
    }

	public void MakeGround(Substitution subst)
    {
		_timeCondition.MakeGround(subst);
		_placeCondition.MakeGround(subst);
		for( int i = 0, limit = _socialConditions.size(); i != limit; ++i ){
			((SocialCondition)_socialConditions.get(i)).MakeGround(subst);
		}
    }

	public void ReplaceUnboundVariables(int variableID) {
		_timeCondition.ReplaceUnboundVariables(variableID);
		_placeCondition.ReplaceUnboundVariables(variableID);
		for( int i = 0, limit = _socialConditions.size(); i != limit; ++i ){
			((SocialCondition)_socialConditions.get(i)).ReplaceUnboundVariables(variableID);
		}
	}

	public boolean isGrounded() {
		if( !_timeCondition.isGrounded() || !_placeCondition.isGrounded() )
			return false;
		boolean grounded = true;
		for( int i = 0, limit = _socialConditions.size(); i != limit; ++i ){
			if( !((SocialCondition)_socialConditions.get(i)).isGrounded() ){
				grounded = false;
				break;
			}
		}
		
		return grounded;
	}
	
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am)
	{
		ArrayList<Condition> conditions = new ArrayList<Condition>();
		conditions.add(_timeCondition);
		conditions.add(_placeCondition);
		conditions.addAll(_socialConditions);
		
		return Condition.CheckActivation(am, conditions);
	}

	
	public Name GetValue() {
		return null;
	}

	protected ArrayList<Substitution> GetValueBindings(AgentModel am) {
		return null;
	}

}
