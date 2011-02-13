package FAtiMA.ReactiveComponent;

import FAtiMA.Core.IIntegrityTester;
import FAtiMA.Core.sensorEffector.Event;

public interface IReactionNode extends Cloneable, IIntegrityTester{
	
	public Reaction getReaction(Event e);
	
	public Object clone();

}
