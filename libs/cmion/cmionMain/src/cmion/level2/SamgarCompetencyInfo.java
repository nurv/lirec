package cmion.level2;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/** stores information about a Samgar competency that is necessary to build the competency
 *  dynamically and to chose it (e.g. class name, constructor parameters, 
 *  category and sub category)*/
public class SamgarCompetencyInfo 
{

	/** the full name of the class */
	private String className;
	
	/** the parameters for the constructor */
	private ArrayList<Object> constructorParameters;

	/** the classes for the constructor parameters */
	private ArrayList<Class<?>> constructorParameterClasses;
	
	/** the category of the Samgar module */
	private String category;
	
	/** the sub-category of the Samgar module */
	private String subCategory;
	
	

	public SamgarCompetencyInfo(String className, 
								ArrayList<Object> constructorParameters,
								ArrayList<Class<?>> constructorParameterClasses,
								String category, String subCategory)
	{
		this.className = className;
		this.constructorParameters = constructorParameters;
		this.constructorParameterClasses = constructorParameterClasses;
		this.category = category;
		this.subCategory = subCategory;
	}
	
	
	/** return whether the samgar competency represented by this info can be used to connect
	 *  to the module passed */
	public boolean canConnectToModule(SamgarModuleInfo modInfo)
	{
		if (category.equals(modInfo.getCategory())
			&& subCategory.equals(modInfo.getSubCategory()))
			return true;
		else 
			return false;
	}
	
	/** constructs the competency that this info represents and returns it, throws 
	 *  an exception if class does not exist, parameters do not match, constructor does not
	 *  exist or class is not subclass of Competency */
	public Competency construct() throws Exception
	{
		// obtain class 
		Class<?> cls = Class.forName(className);

		// obtain the constructor 
		Constructor<?> constructor = cls.getConstructor(constructorParameterClasses.toArray(new Class[constructorParameterClasses.size()]));

		// construct an instance from the constructor
		Object instance = constructor.newInstance(constructorParameters.toArray());

		// check if instance is of the right type
		if (!(instance instanceof Competency)) throw new Exception("Competency could not be constructed because "+ className+ " is not a subclass of Competency");
		Competency competency = (Competency) instance;

		return competency;
	}


	/** returns the class name of the competency this info represents*/
	public String getClassName() {
		return className;
	}

}
