package lirec.storage;

import ion.Meta.Request;

/** a request that can be made to any Lirec Storage Container for removing an
 *  existing sub container (does not remove sub sub containers)*/
public class RequestRemoveSubContainer extends Request {

	/** the name of the sub container to be removed */
	private String name;
	
	public RequestRemoveSubContainer(String name)
	{
		this.name = name;
	}

	/** returns the name of the sub container to be removed */
	public String getName()
	{
		return name;
	}	
	
}
