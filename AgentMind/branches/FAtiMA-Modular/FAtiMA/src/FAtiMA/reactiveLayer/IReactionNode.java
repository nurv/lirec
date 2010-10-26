package FAtiMA.reactiveLayer;

import FAtiMA.IIntegrityTester;
import FAtiMA.sensorEffector.Event;

public interface IReactionNode extends Cloneable, IIntegrityTester{
	
	public Reaction getReaction(Event e);
	
	public Object clone();

}
