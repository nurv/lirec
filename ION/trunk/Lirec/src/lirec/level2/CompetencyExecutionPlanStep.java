package lirec.level2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** represents a step in a competency execution plan, can be instantiated or uninstantiated,
 *  see comments in class CompetencyExecutionPlan for more on this distinction. */
public class CompetencyExecutionPlanStep 
{

	/** the ID of this plan step */
	private String ID;
	
	/** the IDs of other plan steps within the same execution plan that have
	 *  to be completed before this step can be carried out (pre conditions) */
	private ArrayList<String> preconditions;
	
	/** the type of competency we want to invoke */
	private String competencyType;
	
	/** parameters for the competency to invoke */
	private HashMap<String,String> competencyParameters;

	/** during execution of a plan this stores which competencies have already 
	 *  been tried to realize this plan step (this becomes only important if there
	 *  are several competencies with the same type in the library, e.g. several 
	 *  alternate move competencies) */
	private ArrayList<Competency> competenciesAlreadyTried;
	
	/** private constructor only used from within this class */
	private CompetencyExecutionPlanStep()
	{
		preconditions = new ArrayList<String>();
		competencyParameters = new HashMap<String,String>();
	}
	
	/** create a new plan step from a DOM node */
	public CompetencyExecutionPlanStep(Node domNode) throws Exception
	{
		this();

		NamedNodeMap attribs = domNode.getAttributes();
		
		// read ID
		Node idAttr = attribs.getNamedItem("ID");
		if (idAttr!=null) ID = idAttr.getNodeValue(); 
			else throw new Exception("No ID defined for a competency execution plan step");
		
		// read competency type
		Node typeAttr = attribs.getNamedItem("Type");
		if (typeAttr!=null) competencyType = typeAttr.getNodeValue(); 
			else throw new Exception("No type defined for a competency execution plan step");

		// read preconditions (dependency)
		String dependency = "";
		Node depAttr = attribs.getNamedItem("Dependency");
			if (depAttr!=null) dependency = depAttr.getNodeValue(); 
		
		// parse dependency String
		if(!dependency.trim().equals(""))
		{
			StringTokenizer st = new StringTokenizer(dependency,",");
			while (st.hasMoreTokens()) preconditions.add(st.nextToken().trim());	
		}
		
		// read competency parameters
		NodeList children = domNode.getChildNodes();
		
		// iterate over all CompetencyParameter nodes
		for (int i=0; i<children.getLength(); i++)
		{
			if (children.item(i).getNodeName().equals("CompetencyParameter"))
			{	
				attribs = children.item(i).getAttributes();
				// read attributes name and value
				String name;
				String value;
			
				Node nameAttr = attribs.getNamedItem("Name");
				if (nameAttr!=null) name = nameAttr.getNodeValue(); 
					else throw new Exception("No name defined for a competency parameter ");
			
				Node valueAttr = attribs.getNamedItem("Value");
				if (valueAttr!=null) value = valueAttr.getNodeValue(); 
					else throw new Exception("No value defined for competency parameter " + name);			
		
				competencyParameters.put(name, value);
			}
		}
		
		
	}	
	
	/** returns an instantiated copy of this plan step. Concrete this means all variables
	 * in competency parameters values (indicated by starting with $) will be replaced with
	 * a concrete value for that variable, if this value is provided by the mappings given */
	public CompetencyExecutionPlanStep getInstantiatedCopy(HashMap<String, String> mappings) {
		
		CompetencyExecutionPlanStep returnStep = new CompetencyExecutionPlanStep();
		
		// copy basic fields
		returnStep.ID = ID;
		returnStep.competencyType = competencyType;
		
		// copy preconditions
		for (String precondition : preconditions)
			returnStep.preconditions.add(precondition);
		
		// copy parameters, while instantiating
		for (String parameterName : competencyParameters.keySet())
		{
			// retrieve original value
			String value = competencyParameters.get(parameterName);
			
			// check if this is a variable and if we have a mapping for it
			if ((value.startsWith("$")) && (mappings.containsKey(value)))
			{
				// if yes, replace value, by mapping
				value = mappings.get(value);
			}
			
			// add parameter and value
			returnStep.competencyParameters.put(parameterName, value);			
		}
		
		// initialise variables needed for instantiated plan steps
		returnStep.competenciesAlreadyTried = new ArrayList<Competency>();
		
		return returnStep;
	}

	/** returns the ID of this plan step */
	public String getID() 
	{
		return ID;
	}

	/** returns the type/identifier of the competency to invoke in this step */
	public String getCompetencyType()
	{
		return competencyType;
	}
	
	/** returns the pre conditions (list of ids of other plan steps) for this plan steps,
	 *  should only be read and not modified externally */
	public ArrayList<String> getPreconditions()
	{
		return preconditions;
	}
	
	/** returns the parameters for the competency to invoke in this step */
	public HashMap<String,String> getCompetencyParameters()
	{
		return competencyParameters;
	}
	
	/** during execution of a plan this returns a list of competencies that have already 
	 *  been tried to realize this plan step. the returned list may be modified externally */
	public ArrayList<Competency> getCompetenciesAlreadyTried()
	{
		return competenciesAlreadyTried;
	}

}
