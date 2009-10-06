package lirec.storage;

import ion.Meta.Request;

import java.util.HashMap;

/** a request that can be made to any Lirec Storage Container for adding a new
 *  sub container*/
public class RequestAddSubContainer extends Request 
{
	
	/** the name of the sub container to be added */
	private String newContainerName;
	
	/** the type of the sub container to be added */
	private String newContainerType;
	
	/** the initial properties of the sub container */
	private HashMap<String,Object> initialProperties;
	
	public RequestAddSubContainer(String name, String type)
	{
		this.newContainerName = name;
		this.newContainerType = type;
	}

	public RequestAddSubContainer(String name, String type, HashMap<String,Object> properties)
	{
		this.newContainerName = name;
		this.newContainerType = type;
		this.initialProperties = properties;
	}
	
	/** returns the name of the sub container to be added */
	public String getNewContainerName()
	{
		return newContainerName;
	}
	
	/** returns the initial properties of the sub container to be added */
	public HashMap<String,Object> getInitialProperties()
	{
		return initialProperties;
	}
	
	/** returns the name of the sub container to be added */
	public String getNewContainerType()
	{
		return newContainerType;
	}

}
