package FAtiMA.advancedMemoryComponent;


import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;


import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;

import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * @author João Dias
 *
 */
public class ActionsLoaderHandler extends ReflectXMLHandler {
	private String  _operatorKey;
	 
	private boolean _precondition;
	
	private Substitution _self;
	private AgentModel _am;
	private SACondition _sac;
	private float _probability;
	
	public ActionsLoaderHandler(AgentModel am) {
		_precondition = true;
		_am = am;
		_self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF));
	}
	
	public void Action(Attributes attributes) {
		_operatorKey = attributes.getValue("name");
	}

	public void Effects(Attributes attributes) {
		  _precondition = false;
	}
	
	public void Effect(Attributes attributes) {
		_probability = Float.parseFloat(attributes.getValue("probability"));
	}
	
	public void PreConditions(Attributes attributes) {
		  _precondition = true;
	}
    
	
	public void SACondition(Attributes attributes) 
	{	
		Step operator;
		try
    	{
    		_sac = SACondition.ParseSA(attributes);
    		operator = 
    		
    		_am.getActionLibrary().getAction(_operatorKey);
  
    		_sac.MakeGround(_self);
    		if(_precondition) 
    		  	operator.AddPrecondition(_sac);
    		else {
    		  	String operatorName = operator.getName().GetFirstLiteral().toString();
    		  	operator.AddEffect(new Effect(_am, operatorName,_probability, _sac));	
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
	}
	
	public void SAKnown(Attributes attributes) {
		String name;
		Symbol value;

		try
    	{
    		name = attributes.getValue("name");		
    		value = new Symbol(attributes.getValue("value"));
    		System.out.println("known " + name + " " + value);
    		_sac.AddKnownVariables(name, value);
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}		
	}
}