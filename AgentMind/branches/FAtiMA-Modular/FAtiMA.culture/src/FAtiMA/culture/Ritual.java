/** 
 * Ritual.java - Represents a cultural ritual
 *  
 * Copyright (C) 2008 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 12/03/2008 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: 12/03/2008 - File created
 * 							
 */


package FAtiMA.culture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.exceptions.InvalidReplaceUnboundVariableException;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.plans.CausalLink;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.IPlanningOperator;
import FAtiMA.Core.plans.OrderingConstraint;
import FAtiMA.Core.plans.Plan;
import FAtiMA.Core.plans.ProtectedCondition;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;

public class Ritual extends ActivePursuitGoal {
	
	/**
	 * 
	 */
	
	//private static final float EXPECTED_AFFILIATION_ONIGNORE_VALUE = -10f;
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Symbol> _roles;
	private Plan _plan;
	private ArrayList<Step> _steps;
	private ArrayList<OrderingConstraint> _links;
	
	
	private Ritual()
	{
		
	}
	
	public Ritual(Name description)
	{
		super(description);
				//this.SetExpectedEffectOnDrive("OnIgnore","Affiliation","[SELF]",EXPECTED_AFFILIATION_ONIGNORE_VALUE);
		_steps = new ArrayList<Step>(5);
		_links = new ArrayList<OrderingConstraint>();
		_roles = new ArrayList<Symbol>(3);
		_appliedSubstitutions = new ArrayList<Substitution>();
	}
	
	public void AddStep(AgentModel am, Name actionName, Name role)
	{
		ArrayList<Substitution> subst;
		Step action =  am.getActionLibrary().getAction(_steps.size(), actionName);
		if(action != null)
		{
			subst = Unifier.Unify(action.getAgent(),role);
			if(subst != null)
			{
				action.MakeGround(subst);
			}
			
			
			
			this._successConditions.add(action.getEffect(new Integer(0)).GetEffect());
			
			//the probability of another agent to execute the steps of ritual needs to be high for 2 main reasons
			// 1) if it is a ritual it really means that it will likely be executed by the other agents
			// 2) if it were low, rituals with several agents would seem very unlikely to the agent, since
			//    he must multiply the probabilities to determine the ritual probability of success
			// However we cannot make it 100%, because we want to assume that is always better to have 
			// a ritual where you execute the actions, than a ritual where someone else does the actions. The rational
			// is that the agent can always trust his decisions, but not other ones. As such, the probability will 
			// be set sligthly smaller than 100%
			action.setProbability(0.9f);
			_steps.add(action);
		}
	}
	
	public void AddLink(int before, int after)
	{
		/*Step beforeStep;
		Step afterStep;
		Effect e;
		CausalLink link;*/
		OrderingConstraint order = new OrderingConstraint(new Integer(before+2),new Integer(after+2));
		
		if(before < 0 || before >= _steps.size()) return;
		if(after < 0 || after >= _steps.size()) return;
		
		/*beforeStep = (Step) _steps.get(before);
		afterStep = (Step) _steps.get(after);
		
		e = beforeStep.getEffect(new Integer(0));
		afterStep.AddPrecondition(e.GetEffect());
		link = new CausalLink(new Integer(before+2),
							new Integer(0),
							new Integer(after+2),
							new Integer(afterStep.getPreconditions().size()-1));*/
		_links.add(order);
		
		//_links.add(link);
	}
	
	public void AddRole(Symbol role)
	{
		if(_roles.size() == 0)
		{
			_agent = role;
		}
		_roles.add(role);
		
		addRoleToRitualKey(role);  //The key of a ritual is composed by the ritual's name plus the name of the roles		
	}
	
	private void addRoleToRitualKey(Symbol role){
		if(_roles.size() == 1){
			this._key = this._key + "(" + role + ")";
		}else{
			this._key = this._key.substring(0,this._key.length()-1) + "," + role + ")"; 
		}
	}
	
	public ArrayList<Symbol> GetRoles()
	{
		return this._roles;
	}
	
	public void BuildPlan(AgentModel am)
	{
		
		Step s;
		OrderingConstraint o;
		_plan = new Plan(new ArrayList<ProtectedCondition>(),_successConditions);
		
		for(int i=0; i < _steps.size(); i++)
		{
			s = (Step) _steps.get(i);
			_plan.AddOperator(s);
			_plan.AddLink(new CausalLink(new Integer(i+2),new Integer(0),new Integer(1),new Integer(i)));
		}
		
		_plan.RemoveOpenPreconditions(_plan.getFinish().getID());
		
		for(int i=0; i < _links.size(); i++)
		{
			o = (OrderingConstraint) _links.get(i);
			_plan.AddOrderingConstraint(o);
		}
		
		
		String name;
		Event e;
		RitualCondition ritualCondition;
		
		for(int j = 0; j < _roles.size(); j++)
		{
			
			Collections.rotate(_roles, 1);
			name = this._name.GetFirstLiteral().toString() + "(";
			
			for(int i=0; i < _roles.size(); i++)
			{
				name+= _roles.get(i).toString();
				name+=",";
			}
			
			if(_roles.size() > 0)
			{
				name = name.substring(0,name.length()-1);
			}
			
			name+= ")";
			
			this._name = Name.ParseName(name);
			
			
			e = this.GetSuccessEvent();
			ritualCondition = new RitualCondition(this._name.GetFirstLiteral(),_roles, new Symbol("*"),true,false);
			this.addEffect(new Effect(am, e.GetTarget(),1.0f,ritualCondition));
		} 
	}
	
	public ArrayList<Plan> getPlans(AgentModel am)
	{
		ArrayList<Plan> plans = new ArrayList<Plan>();
		this._plan.UpdatePlan(am);
		plans.add(this._plan);
		return plans;
	}
	
	public Plan getPlan()
	{
		return this._plan;
	}
	
	public String toString()
	{
		return "Ritual: " + this._name.toString();
	}
	
	/**
	 * Replaces all unbound variables in the object by applying a numeric 
     * identifier to each one. For example, the variable [x] becomes [x4]
     * if the received ID is 4. 
     * Attention, this method modifies the original object.
     * @param variableID - the identifier to be applied
	 * @throws InvalidReplaceUnboundVariableException 
	 */
    public void ReplaceUnboundVariables(int variableID) 
    {
    	
    	ListIterator<Condition> li;
    
    	this._name.ReplaceUnboundVariables(variableID);
    	
    	li = this._preConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).ReplaceUnboundVariables(variableID);
    	}
    	
    	li = this._failureConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).ReplaceUnboundVariables(variableID);
    	}
    	
    	li = this._successConditions.listIterator();
    	while(li.hasNext())
    	{
    		((Condition) li.next()).ReplaceUnboundVariables(variableID);
    	}
    	
    	for(Effect e : this._effects)
    	{
    		e.ReplaceUnboundVariables(variableID);
    	}
    	
    	for(Symbol s : this._roles)
    	{
    		s.ReplaceUnboundVariables(variableID);
    	}
    	
    	
    	this._agent.ReplaceUnboundVariables(variableID);
    	
    	
    	this._plan.ReplaceUnboundVariables(variableID);
    }
   
	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void MakeGround(ArrayList<Substitution> bindings)
    {
    	
    	this._name.MakeGround(bindings);
    	
    	this._appliedSubstitutions.addAll(bindings);

    	for(Condition c : this._preConditions)
    	{
    		c.MakeGround(bindings);
    	}
    	
    	for(Condition c : this._failureConditions)
    	{
    		c.MakeGround(bindings);
    	}
    	
    	for(Condition c : this._successConditions)
    	{
    		c.MakeGround(bindings);
    	}
    	
    	this._plan.AddBindingConstraints(bindings);
    	
    	for(Symbol s : this._roles)
    	{
    		s.MakeGround(bindings);
    	}
    	
    	this._agent.MakeGround(bindings);
    	
    	for(Effect e : this._effects)
    	{
    		e.MakeGround(bindings);
    	}
    }

	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void MakeGround(Substitution subst)
    {
    	this._appliedSubstitutions.add(subst);
    	
    	this._name.MakeGround(subst);

    	for(Condition c : this._preConditions)
    	{
    		c.MakeGround(subst);
    	}
    	
    	for(Condition c : this._failureConditions)
    	{
    		c.MakeGround(subst);
    	}
    	
    	for(Condition c : this._successConditions)
    	{
    		c.MakeGround(subst);
    	}
    	
    	this._plan.AddBindingConstraint(subst);
    	
    	for(Symbol s : this._roles)
    	{
    		s.MakeGround(subst);
    	}
    	
    	this._agent.MakeGround(subst);
    	
    	for(Effect e : this._effects)
    	{
    		e.MakeGround(subst);
    	}	
    }
	
	/**
	 * Clones this ActivePursuitGoal, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The Goal's copy.
	 */
	public Object clone()
	{
		Ritual r = new Ritual();
		r._goalID = this._goalID;
		r._id = this._id;
		r._active = this._active;
		r._name = (Name) this._name.clone();
		r._baseIOF = this._baseIOF;
		r._baseIOS = this._baseIOS;
		r._dynamicIOF = (Name) this._dynamicIOF.clone();
		r._dynamicIOS = (Name) this._dynamicIOS.clone();
		
		r._numberOfTries = this._numberOfTries;
		
		r._key = this._key;
		
		r._appliedSubstitutions = new ArrayList<Substitution>(this._appliedSubstitutions.size());
		for(Substitution s : this._appliedSubstitutions)
		{
			r._appliedSubstitutions.add((Substitution) s.clone());
		}
		
		if(this._preConditions != null)
		{
			r._preConditions = new ArrayList<Condition>(this._preConditions.size());
			for(Condition c : this._preConditions)
			{
				r._preConditions.add((Condition) c.clone());
			}
		}
		
		if(this._failureConditions != null)
		{
			r._failureConditions = new ArrayList<Condition>(this._failureConditions.size());
			
			for(Condition c : this._failureConditions)
			{
				r._failureConditions.add((Condition) c.clone());
			}
		}
		
		if(this._successConditions != null)
		{
			r._successConditions = new ArrayList<Condition>(this._successConditions.size());
			
			for(Condition c : this._successConditions)
			{
				r._successConditions.add((Condition) c.clone());
			}
		}
		
		if(this._roles != null)
		{
			r._roles = new ArrayList<Symbol>(this._roles.size());
			for(Symbol s : this._roles)
			{
				r._roles.add((Symbol) s.clone());
			}
		}
		
		r._plan = (Plan) this._plan.clone();
		
		r._probability = this._probability;
		r._probabilityDetermined = this._probabilityDetermined;
		r._familiarity = this._familiarity;
		r._urgency = this._urgency;
		
		//IPlanningOperators attributes
		r._agent = (Symbol) this._agent.clone();
		r._id = this._id;
		
		if(this._effects != null)
		{
			r._effects = new ArrayList<Effect>(this._effects.size());
			for(Effect e : this._effects)
			{
				r._effects.add((Effect) e.clone());
			}
		}
		
		return r;
		
	}
	
	public ArrayList<SubstitutionSet> findMatchWithStep(Symbol agent, Name stepName)
	{
		ArrayList<SubstitutionSet> substitutions = new ArrayList<SubstitutionSet>();
		SubstitutionSet subSet;
		ArrayList<Substitution> subst;
		IPlanningOperator ip;
		
		for(ListIterator<IPlanningOperator> li = this._plan.GetFirstActions().listIterator(); li.hasNext();)
		{
			ip = li.next();
			
			subst = Unifier.Unify(ip.getName(), stepName);
			if(subst != null)
			{
				if(Unifier.Unify(ip.getAgent(), agent)!=null)
				{
					subSet = new SubstitutionSet(subst);
					subSet.AddSubstitution(new Substitution(ip.getAgent(),agent));
					substitutions.add(subSet);
				}
			}
		}
		
		return substitutions;
	}
	
	//IPlanningOperator methods
	
	public float getProbability(AgentModel am)
	{
		Float f = this.GetProbability(am);
		if(f != null)
		{
			return f.floatValue();
		}
		else return 1.0f;
	}
	
	@Override
	protected Event generateEventDescription(short goalEventType)
	{
		String target = "";
		String name;
		ListIterator<Symbol> li = this._name.GetLiteralList().listIterator();
		
		name = li.next().toString();
		if(li.hasNext())
		{
			target = li.next().toString();
		}
		
		Event e = new Event("SELF",name,target,EventType.GOAL,goalEventType);
		
		
		for(Symbol role : _roles){
			e.AddParameter(new Parameter("param",role.toString()));
		}
		
	    return e;
	}
	
}
