package cmion.level2;

/** a class storing information about a Samgar module (distinct from a Samgar competency,
 *  a Samgar competency is used to connect to a Samgar module). I.e competency on the cmion
 *  side, module on the Samgar side  */
public final class SamgarModuleInfo 
{
	/** name of the module */
	private final String name;
	
	/** category of the module */
	private final String category;
	
	/** sub category of the module */
	private final String subCategory;
	
	public SamgarModuleInfo(String name, String category, String subCategory)
	{
		this.name = name;
		this.category = category;
		this.subCategory = subCategory;
	}
	
	/** override equality method so we can compare moduleinfos easily*/
	@Override
	public boolean equals(Object anotherObject)
	{
		if (anotherObject==null) return false;
		if (this == anotherObject) return true;
		if (!(anotherObject instanceof SamgarModuleInfo)) return false;
		SamgarModuleInfo anotherModInfo = (SamgarModuleInfo) anotherObject;
		if (anotherModInfo.name.equals(name) &&
			anotherModInfo.category.equals(category) &&
			anotherModInfo.subCategory.equals(subCategory))
			return true;
		else
			return false;
	}
	
	/** it is good practise to override this when we override equals (see above) */
	@Override 
	public int hashCode()
	{
		//generate a very simple hashcode by adding the length of the 3 strings
		return name.length() + 10 * category.length() + 100 * subCategory.length();
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
