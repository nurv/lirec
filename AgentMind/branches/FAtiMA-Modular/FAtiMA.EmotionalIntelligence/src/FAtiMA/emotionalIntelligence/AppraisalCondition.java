package FAtiMA.emotionalIntelligence;

import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.emotionalState.EmotionalPameters;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.advancedMemoryComponent.AdvancedMemoryComponent;
import FAtiMA.motivationalSystem.MotivationalComponent;

public class AppraisalCondition extends PastEventCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final float K = 0.1f; 
	
	private String _appraisalVariable;
	private Symbol _value;
	private short _test;
	private int _threshold;
	
	
	private AppraisalCondition()
	{
	}
	
	public AppraisalCondition(Symbol agent, String appraisalVariable, Symbol value, int threshold, short test, Symbol subject, Symbol action, Symbol target, ArrayList<Symbol> parameters)
	{
		//this._type = type;
		//this._status = status;
		this._ToM = agent;
		
		this._positive = true;
		this._appraisalVariable = appraisalVariable;
		this._value = value;
		this._threshold = threshold;
		this._test = test;
		
		this._subject = subject;
		this._action = action;
		this._target = target;
		
		this._parameters = parameters;
		
		String aux = this._appraisalVariable + "," + this._ToM + "," + this._subject + "," + this._action;
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
	
	public boolean CheckCondition(AgentModel am) {
		return this._action.isGrounded();
	}
	
	public Object clone()
	{
		AppraisalCondition newCondition = new AppraisalCondition();
		
		// Meiyii
		//newCondition._type = this._type;
		//newCondition._status = this._status;
		newCondition._positive = this._positive;
		
		newCondition._appraisalVariable = this._appraisalVariable;
		newCondition._value = (Symbol) this._value.clone();
		newCondition._ToM = (Symbol) this._ToM.clone();
		newCondition._threshold = this._threshold;
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

	public ArrayList<SubstitutionSet> GetValidBindings(AgentModel am) {
		
		float finalemotionvalue;
		float appraisalVariableValue;
		ArrayList<SubstitutionSet> subs;
		float mood;
	
		AgentModel modelToTest = am.getModelToTest(this._ToM);
	 	
		if(!this._value.isGrounded()) return null;
		
		mood = modelToTest.getEmotionalState().GetMood();
		
		finalemotionvalue = Float.parseFloat(this._value.toString()) + _threshold + K;
		
		//meaning that the valence of the corresponding emotion is positive
		if(_test == 0){
			
			appraisalVariableValue = finalemotionvalue - (mood * EmotionalPameters.MoodInfluenceOnEmotion);
			if(appraisalVariableValue < 0)
			{
				appraisalVariableValue = 0;
			}
		}
		else
		{
			//meaning that the valence of the corresponding emotion is negative
			appraisalVariableValue = - finalemotionvalue + (mood * EmotionalPameters.MoodInfluenceOnEmotion);
			if(appraisalVariableValue > 0)
			{
				appraisalVariableValue = 0;
			}
		}

	
		
		//subs = searchMemoryAppraisals(modelToTest, appraisalVariableValue);
		subs = new ArrayList<SubstitutionSet>();
		
		subs.addAll(searchDrivesAppraisals(modelToTest, appraisalVariableValue));
	
		if(subs.size() > 0)
		{
			return subs;
		}
		else return null;
		
	}
	
	private ArrayList<SubstitutionSet> searchMemoryAppraisals(AgentModel am, float desirability)
	{
		ArrayList<SubstitutionSet> subs = new ArrayList<SubstitutionSet>();
		SubstitutionSet sset;
		Symbol target;
		AdvancedMemoryComponent advMem;
		
		ArrayList<String> knownInfo = new ArrayList<String>();
		knownInfo.add("desirability " + desirability);
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
		
		advMem = (AdvancedMemoryComponent) am.getComponent(AdvancedMemoryComponent.NAME);
		
		advMem.getSpreadActivate().Spread(question, knownInfo, am.getMemory().getEpisodicMemory());
		
		ArrayList<ActionDetail> details = advMem.getSpreadActivate().getDetails();
		
		if(details.size() > 0)
		{
			for(ActionDetail ad : details)
			{
				if(ad.getTarget().equals(Constants.SELF))
				{
					target = this._ToM;
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
		}
		
		return subs;
	}
	
	private ArrayList<SubstitutionSet> searchDrivesAppraisals(AgentModel am, float desirability)
	{
		MotivationalComponent motivationalComponent = (MotivationalComponent) am.getComponent(MotivationalComponent.NAME);
		
		return motivationalComponent.searchEventsWithAppraisal(am, _subject, _action, _target, _parameters.get(0), desirability);	
	}

	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		
		AppraisalCondition c = (AppraisalCondition) this.clone();
		c.MakeGround(bindingConstraints);
		return c;
	}

	public Object Ground(Substitution subst) {
		AppraisalCondition c = (AppraisalCondition) this.clone();
		c.MakeGround(subst);
		return c;
	}

	public void MakeGround(ArrayList<Substitution> bindings) {
		this._value.MakeGround(bindings);
		
		this._name.MakeGround(bindings);
		this._ToM.MakeGround(bindings);
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
	
	public void MakeGround(Substitution subst) {
		this._value.MakeGround(subst);
		
		this._name.MakeGround(subst);
		this._ToM.MakeGround(subst);
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
	
	public void ReplaceUnboundVariables(int variableID) {
		this._value.ReplaceUnboundVariables(variableID);
		this._ToM.ReplaceUnboundVariables(variableID);
		
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
}
