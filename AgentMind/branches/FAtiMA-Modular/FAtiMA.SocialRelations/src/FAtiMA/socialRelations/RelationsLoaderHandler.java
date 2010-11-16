package FAtiMA.socialRelations;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.deliberativeLayer.plan.Effect;
import FAtiMA.Core.exceptions.ContextParsingException;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.LikeCondition;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;

public class RelationsLoaderHandler  extends ReflectXMLHandler{
	
	private AgentCore _ag;
	
	public RelationsLoaderHandler(AgentCore ag)
	{
		this._ag = ag;
	}

	//parsed from the role file
	public void Relation(Attributes attributes)
	{
		float respect;
		String target = attributes.getValue("target");
		float like = Float.parseFloat(attributes.getValue("like"));
		LikeRelation.getRelation(Constants.SELF, target).setValue(_ag.getMemory(),like);

		String auxRespect = attributes.getValue("respect");
		if(auxRespect == null)
		{
			respect = 0;
		}
		else 
		{
			respect = Float.parseFloat(auxRespect);
		}
		RespectRelation.getRelation(Constants.SELF, target).setValue(_ag.getMemory(),respect);
	}


	//used in goals
	public void LikeRelation(Attributes attributes)
	{
		LikeCondition lc;

		try
		{
			lc = LikeCondition.ParseSocialCondition(attributes);

			lc.MakeGround(_self);
			_currentGoal.AddCondition(_conditionType, lc);
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
		//l.MakeGround(_self);

		if(_precondition) 
			_currentOperator.AddPrecondition(l);
		else {
			String operatorName = _currentOperator.getName().GetFirstLiteral().toString();
			_currentOperator.AddEffect(new Effect(_am, operatorName,_probability, l));	
		}
	}
}

