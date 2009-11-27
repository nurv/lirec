/*	
        Lirec Architecture
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
  ---  
*/

package lirec.level3;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** the base class for mind actions, different minds (e.g. fatima) can implement different subclasses of this*/
public class MindAction
{

	/** create a new mind action with the specified name and parameters, if the action has
	 * no parameters, the second parameter of this constructor may be null*/
	public MindAction(String subject, String name, ArrayList<String> parameters)
	{
		this.subject = subject;
		this.name = name;
		this.parameters = parameters;
		if (this.parameters == null) this.parameters = new ArrayList<String>();
	}
	
	/** protected constructor for subclasses to use */
	protected MindAction()
	{
		this.parameters = new ArrayList<String>();
	}
	
	/** create a mindAction from a DOM Node (used when parsing rules file) */
	public MindAction(Node domNode) throws Exception
	{
		// call basic constructor
		this();
		
		// read name from attribute
		NamedNodeMap attribs = domNode.getAttributes();
		Node nameAttr = attribs.getNamedItem("Name");
		if (nameAttr!=null) name = nameAttr.getNodeValue(); 
			else throw new Exception("No name defined for a mind action");  

		// create a hashmap to store read parameters temporarily
		HashMap<Integer, String> tempParameters = new HashMap<Integer,String>();
	
		// read parameters
		NodeList children = domNode.getChildNodes();
		
		for (int i=0; i<children.getLength(); i++)
		{
			if (children.item(i).getNodeName().equals("Parameter"))
			{
				String parameterNo;
				int no;
				String parameterValue;
				
				NamedNodeMap parameterAttribs = children.item(i).getAttributes();
				Node noAttr = parameterAttribs.getNamedItem("No");
				if (noAttr!=null) parameterNo = noAttr.getNodeValue(); 
					else throw new Exception("No number defined for a parameter of action " + name);  
				
				Node valueAttr = parameterAttribs.getNamedItem("Value");
				if (valueAttr!=null) parameterValue = valueAttr.getNodeValue(); 
					else throw new Exception("No value defined for parameter " + parameterNo + " of action " + name);  
				
				try
				{
					no = Integer.parseInt(parameterNo);
				}
				catch (NumberFormatException e)
				{
					throw new Exception("Parameter No of action "+name+" is not a number");
				}
				
				tempParameters.put(no, parameterValue);				
			}
		}
		
		// finally perform a check whether the read parameters numbering is correct and if yes copy them over to parameters arraylist
		for (int i = 1; i<=tempParameters.size(); i++)
		{
			// every i should exactly appear once as key, if not something is wrong with the numbering
			if (! tempParameters.containsKey(i)) throw new Exception("Wrong Parameter numbering for action "+ name);
			parameters.add(tempParameters.get(i));
		}		
		
	}
	
	/** compares whether this mindAction (part of a rule with "*" allowed as name or parameter) produces
	 *  a match with the mind action that is passed to this function. A match occurs, when all of the following
	 *  3 conditions are fulfilled: 1) the names are equal, or this.name == "*", 2.) both actions have the same
	 *  number of parameters. 3) all parameters have either the same value or the value of the parameter in this
	 *  is "*" */  
	public boolean compareMatch(MindAction matchAction)
	{
		// check condition 1, same names or this name is "*"
		if (! name.equals("*")) 
			if (! name.equals(matchAction.name)) return false;
		
		// check condition 2, same number of parameters 
		if (! (parameters.size() == matchAction.parameters.size())) return false;
		
		// check condition 3
		for (int i=0; i<parameters.size(); i++)
		{
			if (! parameters.get(i).equals("*"))
				if (! parameters.get(i).equals(matchAction.parameters.get(i))) return false;
		}
		
		// all checks passed: match
		return true;
	}
	
	
	/** the name/identifier of the action */
	protected String name;
	
	/** the name/identifier of the agent/ user who has performed this action*/
	protected String subject;
	
	/** the parameters of this action */
	protected ArrayList<String> parameters;
	
	/** returns the name/identifier of this action */
	public String getName()
	{
		return name;
	}
	
	/** returns the name/identifier of the agent/ user who has performed this action*/
	public String getSubject()
	{
		return subject;
	}

	/** returns the parameters of this action */
	public ArrayList<String> getParameters()
	{
		return parameters;
	}

	/** returns the name, subject and parameters of this action in a map, indexed by "$subject" "$action" "$parameter1", "$parameter2", etc */
	public HashMap<String, String> getMappings() 
	{
		HashMap<String, String> mappings = new HashMap<String, String>();
		mappings.put("$subject", subject);
		mappings.put("$action", name);
		for (int i=1; i<=parameters.size(); i++ )
		{
			String parameterKey = "$parameter" + i; 
			mappings.put(parameterKey, parameters.get(i-1));
		}
		return mappings;
	}
}
