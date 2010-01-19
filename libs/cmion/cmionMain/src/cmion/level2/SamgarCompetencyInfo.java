package cmion.level2;

import java.util.ArrayList;

/** stores information about a Samgar competency that is necessary to build the competency
 *  dynamically and to chose it (e.g. class name, constructor parameters, 
 *  category and sub category)*/
public class SamgarCompetencyInfo 
{

	/** the full name of the class */
	private String className;
	
	/** additional parameters for the constructor */
	private ArrayList<String> constructorParameters;
	
	/** the category of the Samgar module */
	private String category;
	
	/** the sub-category of the Samgar module */
	private String subCategory;


}
