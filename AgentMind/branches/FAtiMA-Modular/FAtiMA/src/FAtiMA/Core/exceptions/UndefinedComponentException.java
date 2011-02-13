package FAtiMA.Core.exceptions;

public class UndefinedComponentException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UndefinedComponentException(String requiredComponent)
	{
		super("Error: Required component " + requiredComponent + " not found!");
	}
}
