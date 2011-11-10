package FAtiMA.DeliberativeComponent2.parsers;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.util.parsers.ReflectXMLHandler2;
import FAtiMA.DeliberativeComponent2.DeliberativeComponent2;

public class DeliberativeLoaderHandler extends ReflectXMLHandler2{
	  
	private DeliberativeComponent2 _deliberativeComponent;
	private AgentModel _am;
	
	public DeliberativeLoaderHandler(AgentModel am, DeliberativeComponent2 deliberativeComponent)
	{
		_am = am;
		_deliberativeComponent = deliberativeComponent;
	}
	
	public void Goal(Attributes attributes) throws UnknownGoalException 
    {
  
      float impOfSuccess = 0;
      float impOfFailure = 0;

      String goalName = attributes.getValue("name");
      String aux = attributes.getValue("importanceOfSuccess");
      if(aux != null)
      {
    	  impOfSuccess = Float.parseFloat(aux);
      }
      
      aux = attributes.getValue("importanceOfFailure");
      if(aux != null)
      {
    	  impOfFailure = Float.parseFloat(attributes.getValue("importanceOfFailure"));
      }
      
      _deliberativeComponent.addGoal(_am,goalName,impOfSuccess,impOfFailure);
			
    }

}
