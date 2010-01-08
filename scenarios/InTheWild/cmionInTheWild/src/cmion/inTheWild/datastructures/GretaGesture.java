package cmion.inTheWild.datastructures;

/** a class storing information related to a gesture */
public class GretaGesture 
{
	/** defines what type a gesture could have*/
	public enum GestureType {HEAD_GESTURE, BODY_GESTURE}

	/** the name of the gesture */
	private String gestureName;
	
	/** the class of the gesture */
	private String gestureClass;
	
	/** the type of the gesture (body, head) */
	private GestureType gestureType;
	
	public GretaGesture(String gestureName, String gestureClass, GestureType gestureType)
	{
		this.gestureName = gestureName;
		this.gestureClass = gestureClass;
		this.gestureType = gestureType;
	}
	
	@Override
	public String toString()
	{
		return gestureName;
	}

	/** returns the name of the gesture */
	public String getGestureName() {
		return gestureName;
	}

	/** returns the class of the gesture, e.g for body gestures deitic, adjectival, etc. */
	public String getGestureClass() {
		return gestureClass;
	}

	/** returns the type of the gesture (e.g. body/head) */
	public GestureType getGestureType() {
		return gestureType;
	}
	
}
