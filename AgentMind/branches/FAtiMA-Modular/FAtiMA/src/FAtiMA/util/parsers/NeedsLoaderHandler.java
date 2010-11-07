package FAtiMA.util.parsers;

import org.xml.sax.Attributes;

import FAtiMA.AgentModel;
import FAtiMA.deliberativeLayer.DeliberativeProcess;
import FAtiMA.emotionalState.EmotionalState;
import FAtiMA.motivationalSystem.InvalidMotivatorTypeException;
import FAtiMA.motivationalSystem.Motivator;
import FAtiMA.motivationalSystem.MotivatorType;
import FAtiMA.reactiveLayer.ReactiveProcess;
import FAtiMA.util.AgentLogger;
import FAtiMA.util.Constants;
import FAtiMA.wellFormedNames.Substitution;
import FAtiMA.wellFormedNames.Symbol;

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
