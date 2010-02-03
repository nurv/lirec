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

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.Simulation;
import ion.Meta.Events.IAdded;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cmion.architecture.IArchitecture;
import cmion.architecture.CmionComponent;


/** the competency library is the component that registers all components */
public class CompetencyLibrary extends CmionComponent {

	/** the list of competencies that are available for execution by the competency
	 *  execution system */
	private ArrayList<Competency> competencies;
	
	/** the list of competencies that should run in the background constantly, started
	 *  up when the architecture is started */
	private ArrayList<Competency> backgroundCompetencies;

	/** a list of Samgar competency info objects for all Samgar competencies 
	 *  available in this scenario. */
	private ArrayList<SamgarCompetencyInfo> samgarCompetencyInfos;
	
	/** list of competencies that have to be started when we receive a message that 
		they have been created */
	private ArrayList<Competency> competenciesToStart;
	
	
	/** a map of all constructed Samgar competencies indexed by the info object
	 *  from which they were constructed (i.e. this is initially always empty and
	 *  remembers which samgar competencies we have already constructed at runtime
	 *  and from which info object, for the purpose of avoiding to reconstruct them
	 *  when they briefly disconnect and then reconnect). Note that every competency
	 *  listed in here will also be listed in either competencies or backgroundCompetencies */
	private HashMap<SamgarCompetencyInfo,Competency> samgarCompetencies;
	
	/** Create a new competency library */
	public CompetencyLibrary(IArchitecture architecture, String competencyLibraryFile) throws Exception 
	{
		super(architecture);
		
		// create competencies array list
		competencies = new ArrayList<Competency>();
		
		// create background competencies array list
		backgroundCompetencies = new ArrayList<Competency>();
		
		// create samgarCompetencyInfos list
		samgarCompetencyInfos = new ArrayList<SamgarCompetencyInfo>();
		
		// create samgar competencies list
		samgarCompetencies = new HashMap<SamgarCompetencyInfo,Competency>();
		
		// create to start list
		competenciesToStart = new ArrayList<Competency>();
		
		// load competency library file (in this function competencies that are 
		// specified in the file, will be built
		loadConfigurationFile(competencyLibraryFile);			
		
		// add all competencies that were loaded to ION and initialise
		for (Competency competency : competencies)
		{	
			Simulation.instance.getElements().add(competency);
			competency.initialize();
		}

		// same for background competencies
		for (Competency competency : backgroundCompetencies)
		{	
			Simulation.instance.getElements().add(competency);
			competency.initialize();
		}	
	}

	/** load the competency library configuration file that specifies which competencies to load*/
	private void loadConfigurationFile(String competencyLibraryFile) throws Exception
	{
		File configFile = new File(competencyLibraryFile);
		if (!configFile.exists()) 
			throw new Exception("Could not locate competency library configuration file " + competencyLibraryFile);
		
		// parse the file to a dom document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse (configFile);

		// normalize text representation
		doc.getDocumentElement().normalize();
			
		// read <CompetencyLibrary> tags
		NodeList libTags = doc.getElementsByTagName("CompetencyLibrary");		
		if (libTags.getLength() != 1) throw new Exception("Error in Competency Library Configuration File: xml file needs exactly 1 tag CompetencyLibrary");
		
		// load competencies and create them
		
		// search for all Competency tags
		NodeList allChildren = libTags.item(0).getChildNodes();
		for (int i=0; i<allChildren.getLength(); i++)
		{
			Node node = allChildren.item(i);
			if (node.getNodeName().equals("Competency") || node.getNodeName().equals("SamgarCompetency"))
			{
				// check whether this is a samgar competency or a regular competency
				boolean samgarCompetency = false;
				if (node.getNodeName().equals("SamgarCompetency"))
					samgarCompetency = true;
				
				NamedNodeMap attribs = node.getAttributes();
				
				// read attribute ClassName
				String className;
				Node atrClassName = attribs.getNamedItem("ClassName");
				if (atrClassName!=null) 
					className = atrClassName.getNodeValue();
				else
					throw new Exception("No class name specified for a competency in competency library configuration file.");
				
				// read attribute ConstructorParameters: default none
				String constructorParametersStr = "";
				Node atrConstrPars = attribs.getNamedItem("ConstructorParameters");
				if (atrConstrPars!=null)
					constructorParametersStr= atrConstrPars.getNodeValue();
				
				// create array to store values of constructor parameters
				ArrayList<Object> constructorParameters = new ArrayList<Object>();
				// create array that specifies the classes of the parameters of the constructor
				ArrayList<Class<?>> constructorClasses = new ArrayList<Class<?>>();

				// the first parameter to the constructor is always a reference to the architecture for all cmion components				
				constructorParameters.add(architecture);
				// it's class is IArchitecture
				constructorClasses.add(IArchitecture.class);

				// di-sect constructor parameters (in the string all parameters are seperated by a comma)
				StringTokenizer st = new StringTokenizer(constructorParametersStr,",");
				while (st.hasMoreTokens())
				{
					String token = st.nextToken().trim();
					if (!token.equals(""))
					{
						constructorParameters.add(token);
						constructorClasses.add(String.class);
					}
				}

				
				
				// if this is a samgar competency also read attributes category and sub category
				// and then store information about the competency (without creating it yet) in 
				// the respective array list
				if (samgarCompetency)
				{
					String catName;
					Node atrCatName = attribs.getNamedItem("Category");
					if (atrCatName!=null) 
						catName = atrCatName.getNodeValue();
					else
						throw new Exception("No category specified for a Samgar competency in competency library configuration file.");

					String subCatName;
					Node atrSubCatName = attribs.getNamedItem("SubCategory");
					if (atrSubCatName!=null) 
						subCatName = atrSubCatName.getNodeValue();
					else
						throw new Exception("No sub-category specified for a Samgar competency in competency library configuration file.");
					
					samgarCompetencyInfos.add(new SamgarCompetencyInfo(className,constructorParameters,
												constructorClasses,catName,subCatName));
					
					
					
				}
				else // otherwise directly construct the competency and add it to the library
				{
					
					// dynamically construct custom competency

					// obtain class 
					Class<?> cls = Class.forName(className);

					// obtain the constructor 
					Constructor<?> constructor = cls.getConstructor(constructorClasses.toArray(new Class[constructorClasses.size()]));

					// construct an instance from the constructor
					Object instance = constructor.newInstance(constructorParameters.toArray());

					// check if instance is of the right type
					if (!(instance instanceof Competency)) throw new Exception("Competency could not be loaded because "+ className+ " is not a subclass of Competency");
					Competency competency = (Competency) instance;

					// add to our list of competencies
					if (competency.runsInBackground())
						backgroundCompetencies.add(competency);
					else	
						competencies.add(competency);	
				}
			}
		}
	}

	@Override
	public final void registerHandlers() 
	{
		Simulation.instance.getEventHandlers().add(new HandleSamgarModuleAdded());
		Simulation.instance.getEventHandlers().add(new HandleSamgarModuleRemoved());
		Simulation.instance.getEventHandlers().add(new HandleAddedElement());
	}
	
	/** in this function all competencies that are continuously running in the background are started */
	public void startBackgroundCompetencies()
	{
		// if we have any competencies to start do the starting in a separate thread
		if (backgroundCompetencies.size()>0) new BackgroundCompetencyStarter().start();	
	}
	
	
	/** return a List of competencies with the given type, the list will be empty
	 *  if no such competency is in the library */
	public ArrayList<Competency> getCompetencies(String type)
	{
		ArrayList<Competency> returnList = new ArrayList<Competency>();
		for (Competency competency : competencies)
			if (competency.getCompetencyType().equals(type))
				returnList.add(competency);

		return returnList;
	}
	
	/** thread for starting the background competencies. this might look at first
	 *  like an over complication, but the reason for handling the starting like here
	 *  is that we cannot be sure when exactly the background competencies will be available,
	 *  e.g. if an external client program needs to connect first, that's why we 
	 *  have to keep trying */
	private class BackgroundCompetencyStarter extends Thread
	{
		@Override
		public void run()
		{
			// create list for storing the competencies that we have already 
			// started, initially empty
			ArrayList<Competency> alreadyStartedCompetencies = new ArrayList<Competency>();
			
			// initially of course not all competencies are started
			boolean allStarted = false;
	
			while (!allStarted)
			{
				// assume all are started
				allStarted = true;

				// iterate over background competencies, attempt to start them
				for (Competency competency : backgroundCompetencies)
					if (!alreadyStartedCompetencies.contains(competency))
					{
						// this competency is not started yet, see if we can start it now & here						
						if (competency.isAvailable()) 
						{
							// it is available, so we can start it and add it to the started competencies list
							// note: competency start parameters are empty, if something needs to be passed to
							// the competency, the constructor can be used
							competency.requestStartCompetency(new HashMap<String,String>());
							alreadyStartedCompetencies.add(competency);
						}
						else allStarted = false;
					}
				if (!allStarted) Thread.yield();
			}
			
			// all competencies are started: print out a message
			System.out.println("All background competencies are started");
		}
	}
	
	/** retrieves a matching samgar competency info for a samgar module*/
	private SamgarCompetencyInfo findCompetencyForModule(SamgarModuleInfo modInfo)
	{
		/** pick the first samgar competency info that matches that we encounter,
		 *  there should be never more than one */
    	for (SamgarCompetencyInfo samgarCompInfo:samgarCompetencyInfos)
    		if (samgarCompInfo.canConnectToModule(modInfo))
    			return samgarCompInfo;
    	return null;
	}
	
	/** internal event handler class for listening to samgar module added events */
	private class HandleSamgarModuleAdded extends EventHandler {

	    public HandleSamgarModuleAdded() {
	        super(EventSamgarModuleAdded.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type EventSamgarModuleAdded,
	    	// the following casts always work
	    	SamgarModuleInfo modInfo = ((EventSamgarModuleAdded)evt).getModuleInfo();
	    	SamgarCompetencyInfo samgarCompInfo = findCompetencyForModule(modInfo);
	    	if (samgarCompInfo!=null) 
	    	{   // we have information about a competency suitable for this module
	    		// check if the competency was already constructed before at some point
	    		Competency comp = samgarCompetencies.get(samgarCompInfo);
	    		if (comp!=null) // if yes, enable it by setting available true
	    			comp.available = true;
	    		else
	    		{	// otherwise: construct competency, initialise it and make it available
	    			// or start it up if it runs in background

	    			try {
	    				// construct competency
	    				comp = samgarCompInfo.construct();
	    				// add to ION
	    				Simulation.instance.getElements().add(comp);	    				
	    				// initialise
	    				comp.initialize();
	    				// register handlers
	    				comp.registerHandlers();
	    				// add it to the samgar competencies list
	    				samgarCompetencies.put(samgarCompInfo, comp);

	    				// what we do now depends on whether the competency runs in the background or not
	    				if (comp.runsInBackground())
	    				{
	    					// add it to background competencies
	    					backgroundCompetencies.add(comp);
	    					
	    					// and add it to the to-start competencies
	    					competenciesToStart.add(comp);
	    				
	    				}
	    				else
	    				{
	    					// add it to the list of startable competencies so it is available to the
	    					// competency manager
	    					competencies.add(comp);
	    				}
	    			} catch (Exception e) 
	    			{
	    				System.out.println("Error: Samgar Competency "+samgarCompInfo.getClassName()+" could not be constructed");
	    				e.printStackTrace();
	    			}
	    		}
	    	}
	    }
	}
	
	/** internal event handler class for listening to samgar module added events */
	private class HandleSamgarModuleRemoved extends EventHandler {

	    public HandleSamgarModuleRemoved() {
	        super(EventSamgarModuleRemoved.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type EventSamgarModuleRemoved,
	    	// the following casts always work
	    	SamgarModuleInfo modInfo = ((EventSamgarModuleRemoved)evt).getModuleInfo();
	    	SamgarCompetencyInfo samgarCompInfo = findCompetencyForModule(modInfo);
	    	if (samgarCompInfo !=null)
	    	{
	    		// check if the competency was already constructed before at some point
	    		Competency comp = samgarCompetencies.get(samgarCompInfo);
	    		if (comp!=null) // if yes, disable it by setting available false
	    			comp.available = false;
	    	}
	    }
	}	

	/** internal event handler class for listening to ion element added events,
	 * 	we want to react to them if the element added was a competency that we need
	 *  to start */
	private class HandleAddedElement extends EventHandler {

	    public HandleAddedElement() {
	        super(IAdded.class);
	    }

	    @Override
	    public void invoke(IEvent evt) {
	        // since this is an event handler only for type IAdded,
	    	// the following casts always work
	    	Object whatAdded = ((IAdded<?, ?>)evt).getItem();
	    	// check if the object that was added is one that we are trying to start
	    	if (competenciesToStart.contains(whatAdded))
	    	{
		    	// safe cast because competencies to start only contains competency objects
	    		Competency comp = (Competency) whatAdded;
	    		// request start now
	    		comp.requestStartCompetency(new HashMap<String,String>());
	    		// remove object from list
	    		competenciesToStart.remove(whatAdded);
	    	}
	    }
	}	

}
