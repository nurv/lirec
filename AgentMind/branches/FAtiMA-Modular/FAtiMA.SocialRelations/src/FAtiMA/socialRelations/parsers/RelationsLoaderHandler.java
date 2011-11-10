package FAtiMA.socialRelations.parsers;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.exceptions.ContextParsingException;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.socialRelations.LikeCondition;
import FAtiMA.socialRelations.LikeRelation;
import FAtiMA.socialRelations.RespectRelation;

public class RelationsLoaderHandler  extends ReflectXMLHandler{

	private String _currentGoalKey;
	private String _currentActionKey;
	private String _conditionType;
	private boolean _precondition;
	private AgentModel _aM;
	private float _probability;
    private Substitution _self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF));
	
	public RelationsLoaderHandler(AgentModel aM){
		_aM = aM;
	}
	
	
	//parsed from the role file
	public void Relation(Attributes attributes)
	{
		float respect;
		String subject = attributes.getValue("subject");
		String target = attributes.getValue("target");
		float like = Float.parseFloat(attributes.getValue("like"));
		String auxRespect = attributes.getValue("respect");
		
		if(subject == null || subject.equals(_aM.getName()))
		{
			subject = Constants.SELF;
		}
		else 
		{
			return;
		}
		
		
		LikeRelation.getRelation(subject, target).setValue(_aM.getMemory(),like);

		
		if(auxRespect == null)
		{
			respect = 0;
		}
		else 
		{
			respect = Float.parseFloat(auxRespect);
		}
		
		RespectRelation.getRelation(subject, target).setValue(_aM.getMemory(),respect);
	}
	
	

	//used in goals
	public void LikeRelation(Attributes attributes)
	{
		LikeCondition lc;
		Goal g;
		
		try
		{
			lc = LikeCondition.ParseSocialCondition(attributes);
			lc.MakeGround(_self);
			g = _aM.getGoalLibrary().GetGoal(Name.ParseName(_currentGoalKey));
			g.AddCondition(_conditionType, lc);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}


	//used in actions
	public void LikeCondition(Attributes attributes) throws InvalidEmotionTypeException, ContextParsingException {
		LikeCondition l;
		l = LikeCondition.ParseSocialCondition(attributes);
		l.MakeGround(_self);

		Step action = _aM.getActionLibrary().getAction(_currentActionKey);
		if(_precondition)
			action.AddPrecondition(l);
		else {
			String operatorName = action.getName().GetFirstLiteral().toString();
			action.AddEffect(new Effect(_aM, operatorName,_probability, l));	
		}
	}
	
	

	public void ActivePursuitGoal(Attributes attributes) {
    	_currentGoalKey = attributes.getValue("name");
    }
	
	public void Action(Attributes attributes){
	 	_currentActionKey = attributes.getValue("name");	
	}
	
	public void SuccessConditions(Attributes attributes) {
	    _conditionType = "SuccessConditions";
	}
	    
	 public void FailureConditions(Attributes attributes) {
	    _conditionType = "FailureConditions";
	 }
	 
	 public void PreConditions(Attributes attributes) {
		 _conditionType = "PreConditions";
		 _precondition = true;
	 }
	 
	 public void Effects(Attributes attributes) {
	    _precondition = false;
	}
	 
	 public void Effect(Attributes attributes) {
		_probability = Float.parseFloat(attributes.getValue("probability"));
	 }
		
	 
}

