package FAtiMA.Core.util.parsers;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.deliberativeLayer.DeliberativeProcess;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.reactiveLayer.ReactiveProcess;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.motivationalSystem.InvalidMotivatorTypeException;
import FAtiMA.motivationalSystem.Motivator;
import FAtiMA.motivationalSystem.MotivatorType;

public class NeedsLoaderHandler  extends ReflectXMLHandler {
	AgentModel _am;


	public NeedsLoaderHandler(AgentModel am){
		this._am = am;
	}

	public void MotivationalParameter(Attributes attributes) throws InvalidMotivatorTypeException {
		String motivatorName;
		short type;

		motivatorName = attributes.getValue("motivator");
		type = MotivatorType.ParseType(motivatorName);
		_am.getMotivationalState().AddMotivator(new Motivator(type,
				new Float(attributes.getValue("decayFactor")).floatValue(),
				new Float(attributes.getValue("weight")).floatValue(),
				new Float(attributes.getValue("intensity")).floatValue()));
		AgentLogger.GetInstance().logAndPrint("Motivator found: " + type);
	}
}
