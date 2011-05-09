/*	
    CMION
	Copyright(C) 2009 Heriot Watt University

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

	Authors:  Michael Kriegel 

	Revision History:
  ---
  09/10/2009      Michael Kriegel <mk95@hw.ac.uk>
  First version.
  27/11/2009      Michael Kriegel <mk95@hw.ac.uk>
  Renamed to CMION
  ---  
*/

package cmion.level2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cmion.architecture.IArchitecture;
import cmion.storage.CmionStorageContainer;

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
	
	/** refernce to cmion architecture */
	private IArchitecture architecture;
	
	/** private constructor only used from within this class */
	private CompetencyExecutionPlanStep(IArchitecture architecture)
	{
		this.architecture = architecture;
		preconditions = new ArrayList<String>();
		competencyParameters = new HashMap<String,String>();
	}
	
	/** create a new plan step from a DOM node */
	public CompetencyExecutionPlanStep(Node domNode,IArchitecture architecture) throws Exception
	{
		this(architecture);

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
		
		CompetencyExecutionPlanStep returnStep = new CompetencyExecutionPlanStep(architecture);
		
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
			
			// replace all occurrences of all mapping variables
			for (String key: mappings.keySet())
			{
				value = value.replace(key, mappings.get(key));
			}
			
			// after replacing all variables, check if there are any blackboard variables to replace
			// this means search for $BB, variables look like this $BB(utterance) reads the value
			// of the blackboard property utterance, $BB(messages.1) reads the value of the property 1
			// in the sub container 1, etc.
			while (value.contains("$BB"))
			{
				int idx = value.indexOf("$BB");
				String substring = "";
				for (int i=idx; value.charAt(i)!=')'; i++)
					substring += value.charAt(i);
				
				// substring should now contain the $BB expression, excluding the closing bracket,
				// next we split on the '.' and '(' characters
				StringTokenizer st = new StringTokenizer(substring,".(");
				ArrayList<String> tokens = new ArrayList<String>();
				if (st.hasMoreTokens()) st.nextToken(); // discard the first token
				while (st.hasMoreTokens()) tokens.add(st.nextToken()); // store the rest in the array list
				
				// if there isnt at least one token this variable is malformed, remove from string
				if (tokens.size()<1) 
					value = value.replace(substring, "");
				else
				{
					// last token is the name of the property
					String propName = tokens.remove(tokens.size()-1);
					CmionStorageContainer csc = architecture.getBlackBoard();
					while (tokens.size()>0)
					{
						String containerName = tokens.remove(tokens.size()-1);
						if (csc!=null)
							csc = csc.getSubContainer(containerName);
					}
					if (csc==null)
						value = value.replace(substring, "");
					else
					{
						Object propValue = csc.getPropertyValue(propName);
						if (propValue==null)
							value = value.replace(substring, "");
						else
							value = value.replace(substring, propValue.toString());
					}
				}
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
