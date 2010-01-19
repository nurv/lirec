package cmion.addOns.samgar;

/** a class storing information about a Samgar module */
public final class ModuleInfo 
{
	/** name of the module */
	private final String name;
	
	/** category of the module */
	private final String category;
	
	/** sub category of the module */
	private final String subCategory;
	
	public ModuleInfo(String name, String category, String subCategory)
	{
		this.name = name;
		this.category = category;
		this.subCategory = subCategory;
	}
	
	@Override 
	public int hashCode()
	{
		//TODO
		return 0;
	}
	
	
	/** returns the name of the module */
	public String getName()
	{
		return name;
	}
	
	/** returns the category of the module */
	public String getCategory()
	{
		return category;
	}
	
	/** returns the sub category of the module */
	public String getSubCategory()
	{
		return subCategory;
	}
}
