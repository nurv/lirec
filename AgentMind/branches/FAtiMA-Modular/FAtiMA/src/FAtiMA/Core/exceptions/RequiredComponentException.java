package FAtiMA.Core.exceptions;

public class RequiredComponentException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RequiredComponentException(String requesterComponent,String requiredComponent)
	{
		super("Dependency Error: component " + requesterComponent + 
				" requires component " + requiredComponent + " to be added first!");
	}

}
