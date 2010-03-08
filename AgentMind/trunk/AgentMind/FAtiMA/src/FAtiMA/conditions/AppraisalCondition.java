package FAtiMA.conditions;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;

import FAtiMA.AgentModel;
import FAtiMA.memory.episodicMemory.ActionDetail;
import FAtiMA.util.Constants;
import FAtiMA.wellFormedNames.Name;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.SubstitutionSet;
import FAtiMA.wellFormedNames.Symbol;

public class AppraisalCondition extends PastEventCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Symbol _agent;
	private String _appraisalVariable;
	private Symbol _value;
	private short _test;
	
	private AppraisalCondition()
	{
	}
	
	public AppraisalCondition(Symbol agent, String appraisalVariable, Symbol value, short test, Symbol subject, Symbol action, Symbol target, ArrayList<Symbol> parameters)
	{
		//this._type = type;
		//this._status = status;
		
		this._positive = true;
		this._agent = agent;
		this._appraisalVariable = appraisalVariable;
		this._value = value;
		this._test = test;
		
		this._subject = subject;
		this._action = action;
		this._target = target;
		
		this._parameters = parameters;
		
		String aux = this._appraisalVariable + "," + this._agent + "," + this._subject + "," + this._action;
		if(this._target != null)
		{
			aux = aux + "," + this._target;
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			aux = aux + "," + li.next();
		}
		
		this._name = Name.ParseName("Appraisal(" + aux + ")");
	}
	
	public Object clone()
	{
		AppraisalCondition newCondition = new AppraisalCondition();
		
		// Meiyii
		//newCondition._type = this._type;
		//newCondition._status = this._status;
		newCondition._positive = this._positive;
		
		newCondition._agent = (Symbol) this._agent.clone();
		newCondition._appraisalVariable = this._appraisalVariable;
		newCondition._value = (Symbol) this._value.clone();
		newCondition._test = this._test;
		
		newCondition._name = (Name) this._name.clone();
		newCondition._subject = (Symbol) this._subject.clone();
		newCondition._action = (Symbol) this._action.clone();
		if(this._target != null)
		{
			newCondition._target = (Symbol) this._target.clone();
		}
		
		newCondition._parameters = new ArrayList<Symbol>(this._parameters.size());
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		
		while(li.hasNext())
		{
			newCondition._parameters.add((Symbol)li.next().clone());
		}
		
		return newCondition;	
	}
	
	public Object GenerateName(int id) {
		AppraisalCondition c = (AppraisalCondition) this.clone();
		c.ReplaceUnboundVariables(id);
		return c;
	}

	public void ReplaceUnboundVariables(int variableID) {
		this._agent.ReplaceUnboundVariables(variableID);
		this._value.ReplaceUnboundVariables(variableID);
		
		this._name.ReplaceUnboundVariables(variableID);
		this._subject.ReplaceUnboundVariables(variableID);
		this._action.ReplaceUnboundVariables(variableID);
		
		if(this._target != null)
		{
			this._target.ReplaceUnboundVariables(variableID);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().ReplaceUnboundVariables(variableID);
		}
	}

	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		
		AppraisalCondition c = (AppraisalCondition) this.clone();
		c.MakeGround(bindingConstraints);
		return c;
	}

	public void MakeGround(ArrayList<Substitution> bindings) {
		this._agent.MakeGround(bindings);
		this._value.MakeGround(bindings);
		
		this._name.MakeGround(bindings);
		this._subject.MakeGround(bindings);
		this._action.MakeGround(bindings);
		if(this._target != null)
		{
			this._target.MakeGround(bindings);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().MakeGround(bindings);
		}
	}

	public Object Ground(Substitution subst) {
		AppraisalCondition c = (AppraisalCondition) this.clone();
		c.MakeGround(subst);
		return c;
	}

	public void MakeGround(Substitution subst) {
		this._agent.MakeGround(subst);
		this._value.MakeGround(subst);
		
		this._name.MakeGround(subst);
		this._subject.MakeGround(subst);
		this._action.MakeGround(subst);
		if(this._target != null)
		{
			this._target.MakeGround(subst);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().MakeGround(subst);
		}
	}
	
	public boolean CheckCondition(AgentModel am) {
		//TODO finish this method
		return this._action.isGrounded();		 
	}
	
	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		
		Symbol target;
		ArrayList<SubstitutionSet> subs = new ArrayList<SubstitutionSet>();
		SubstitutionSet sset;
	
		AgentModel modelToTest = am;
		
		if(!this._agent.isGrounded()) return null;
	 	
		if(!this._value.isGrounded()) return null;
		
		if(!this._agent.toString().equals(Constants.SELF))
		{
			modelToTest = am.getToM().get(this._agent.toString());
		}
		
		ArrayList<String> knownInfo = new ArrayList<String>();
		knownInfo.add("desirability " + this._value.toString());
		//float desirability = Float.parseFloat(this._value.toString());
		/*if(desirability >= 0)
		{
			knownInfo.add("positive");
		}
		else
		{
			knownInfo.add("negative");
		}*/
		
		String question = "action";
		
		 
		am.getSpreadActivate().Spread(question, knownInfo, modelToTest.getMemory().getEpisodicMemory());
		
		ArrayList<ActionDetail> details = am.getSpreadActivate().getDetails();
		
		if(details.size() > 0)
		{
			for(ActionDetail ad : details)
			{
				if(ad.getTarget().equals(Constants.SELF))
				{
					target = this._agent;
				}
				else
				{
					target = new Symbol(ad.getTarget()); 
				}
				sset = new SubstitutionSet();
				sset.AddSubstitution(new Substitution(this._action,new Symbol(ad.getAction())));
				sset.AddSubstitution(new Substitution(this._target,target));
				subs.add(sset);
			}
			
			return subs;
		}
		
		//Hashtable<String, Integer> saResult = am.getSpreadActivate().getSAResult();
		/*Iterator it = saResult.keySet().iterator();
		while (it.hasNext())
		{
			//talk to Mei Yii, preciso de mais informação aqui, isto deve retornar o action detail completo se possivel
			String result = (String) it.next();
			System.out.println(question + " " + result + " frequency " + saResult.get(result));
		}*/
	
		return null;
	}
}
