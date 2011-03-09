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

package cmion.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cmion.architecture.IArchitecture;


/** a specific subclass of cmion storage container with convenience methods added 
 * for handling "agent" and "object" sub containers. */
public class WorldModel extends CmionStorageContainer {

	public static String AGENT_TYPE_NAME = "agent";
	public static String OBJECT_TYPE_NAME = "object";
	
	
	public WorldModel(IArchitecture architecture, String name) {
		// this container is of the type "WorldModel"
		super(architecture,name, "WorldModel", null);
	}
	
	/** request adding an agent to the world model */
	public void requestAddAgent(String name)
	{
		this.requestAddSubContainer(name, AGENT_TYPE_NAME);
	}
	
	/** request adding an agent to the world model */
	public void requestAddAgent(String name, HashMap<String,Object> initialProperties)
	{
		this.requestAddSubContainer(name, AGENT_TYPE_NAME, initialProperties);
	}
	
	/** request removing an agent from the world model */
	public void requestRemoveAgent(String name)
	{
		if (this.hasSubContainer(name, AGENT_TYPE_NAME))
			this.requestRemoveSubContainer(name);
	}
	
	/** request adding an object to the world model */
	public void requestAddObject(String name)
	{
		this.requestAddSubContainer(name, OBJECT_TYPE_NAME);
	}
	
	/** request adding an object to the world model */
	public void requestAddObject(String name, HashMap<String,Object> initialProperties)
	{
		this.requestAddSubContainer(name, OBJECT_TYPE_NAME, initialProperties);
	}
	
	/** request removing an object from the world model */
	public void requestRemoveObject(String name)
	{
		if (this.hasSubContainer(name, OBJECT_TYPE_NAME))
			this.requestRemoveSubContainer(name);
	}
	
	/** returns whether the world model has an agent of the specified name*/
	public synchronized boolean hasAgent(String name)
	{
		return this.hasSubContainer(name,AGENT_TYPE_NAME);
	}
	
	/** returns whether the world model has an object of the specified name*/
	public synchronized boolean hasObject(String name)
	{
		return this.hasSubContainer(name,OBJECT_TYPE_NAME);
	}
	
	/** returns a list of the names of all agents */
	public synchronized ArrayList<String> getAgentNames()
	{
		return this.getSubContainerNames(AGENT_TYPE_NAME);
	}
	
	/** returns a list of the names of all objects */
	public synchronized ArrayList<String> getObjectNames()
	{
		return this.getSubContainerNames(OBJECT_TYPE_NAME);
	}
	
	/** returns the agent storage container with the specified name or null if 
	 * it does not exist in this world model */
	public synchronized CmionStorageContainer getAgent(String name)
	{
		if (this.hasAgent(name)) return this.getSubContainer(name);
		else return null;
	}
	
	/** returns the object storage container with the specified name or null if 
	 * it does not exist in this world model */
	public synchronized CmionStorageContainer getObject(String name)
	{
		if (this.hasObject(name)) return this.getSubContainer(name);
		else return null;
	}
	
	protected InputStream openInitFile(String initFileName) throws Exception{
		File initFile = new File(initFileName);

		// check if file exists
		if (! initFile.exists()) throw new Exception("cannot locate world model initialization file " + initFileName );
		
		return new FileInputStream(initFile);
	}
	
	/** Load the initial contents for the world model from the specified file */
	public void loadInitFile(String initFile) throws Exception
	{
		InputStream inStream = openInitFile(initFile);
		
		// parse the file to a dom document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (inStream);

		// normalize text representation
		doc.getDocumentElement().normalize();
		
		ArrayList<String> names = new ArrayList<String>();
		
		// read agents
		readAgentOrObject(doc,"Agent",true,names);		
		// read objects
		readAgentOrObject(doc,"Object",false,names);		
				
	}
	
	private void readAgentOrObject(Document doc, String tag, boolean agent,ArrayList<String> names) throws Exception
	{
		// read <Agent>, or <Object> tags
		NodeList tags = doc.getElementsByTagName(tag);		
		for (int i=0; i<tags.getLength(); i++)
		{
			Node nameAttr = tags.item(i).getAttributes().getNamedItem("Name");

			if (nameAttr==null) throw new Exception(tag + " name not specified in World Model Init File");
			
			String name = nameAttr.getNodeValue().trim();

			if (name.equals("")) throw new Exception(tag + " name not specified in World Model Init File");
			
			if (names.contains(name)) throw new Exception(tag + " name " + name + " may not be used multiple times in World Model Init File");
				
			names.add(name);
			
			HashMap<String,Object> properties = new HashMap<String,Object>();
			
			NodeList propNodes = tags.item(i).getChildNodes();
						
			for (int j=0; j<propNodes.getLength(); j++)
			{
				
				if (propNodes.item(j).getNodeName().equals("Property"))
				{
					Node propNameAttr = propNodes.item(j).getAttributes().getNamedItem("Name");
					
					if (propNameAttr==null) throw new Exception("Property name not specified for "+tag+" "+ name + " in World Model Init File");
					
					String propName = propNameAttr.getNodeValue().trim();

					if (propName.equals("")) throw new Exception("Property name not specified for "+tag+" "+ name + " in World Model Init File");

					Node propValueAttr = propNodes.item(j).getAttributes().getNamedItem("Value");
					
					if (propValueAttr==null) throw new Exception("Property value not specified for "+tag+" "+ name + " in World Model Init File");
					
					String propValue = propValueAttr.getNodeValue().trim();
					
					properties.put(propName, propValue);					
					
				}
			}
			
			if (agent)
				this.requestAddAgent(name, properties);
			else
				this.requestAddObject(name, properties);							
		}				
	}
	
}
