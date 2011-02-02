package cmion.addOns.samgar.competencies.player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import yarp.Bottle;
import cmion.addOns.samgar.SamgarCompetency;
import cmion.architecture.IArchitecture;
import cmion.addOns.samgar.playerProxy.*;

public class NavigationSamgarCompetency extends SamgarCompetency {

	private boolean active=false; 
	private boolean result=false;
	
	PlayerPlanner planner;
	
	private String configFile;
	private DocumentBuilder docBuilder; 
	private HashMap<String, Coordinate> positionList;
	private HashMap<String, Rectangle> locationList;
	
	public NavigationSamgarCompetency(IArchitecture architecture, String configFile) 
	{
		super(architecture);
		planner = new PlayerPlanner();
		
		this.competencyName ="NavigationSamgarCompetency";
		this.competencyType ="DefaultNavigation";
		this.configFile = configFile;
		try {
			this.docBuilder = createDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		
		this.positionList = new HashMap<String, Coordinate>();
		this.locationList = new HashMap<String, Rectangle>();
	}
		
	void prepare()
	{		
		Document doc = null;
		try {
			doc = docBuilder.parse(openConfigFile(configFile));
		} catch (SAXException e1) {
			System.err.println("Could not initialize migration module.");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.err.println("Could not initialize migration module.");
			e1.printStackTrace();
		}

		importConfig(doc);
		
		available = true;
		// regularly send a bottle with "test cmion" inside
	}
	
	void newDataRequest()
	{
		Bottle b = this.prepareBottle();
		planner.setReq(b, false, false, false, true, false);
		this.sendBottle();
	}
	
	void updateData()
	{
		if (planner.updated) 
		{
			if (planner.donePath)
			{
				active=false;
				result=true;
			}
			if (!planner.validPath)
			{
				active=false;
				result=false;
			}
			planner.updated=false;
		}
	}
		
	@Override
	public void onRead(Bottle b) 
	{
		//System.out.println("received bottle :"+b);
		if(b.get(0).asInt() == PlayerProxy.Types.Planner.ordinal())
		{
			planner.update(b);
		}
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		prepare();
		Bottle b = this.prepareBottle();
		planner.enable(b);
		this.sendBottle();
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
		
		String goalPosition = parameters.get("Position");
		Coordinate goal = positionList.get(goalPosition);
		if (goal == null)
			return false;
		b = this.prepareBottle();
		planner.setGoal(b, goal);
		this.sendBottle();
		System.out.println("Navigation: goal  name="+goalPosition+" ("+goal.x+", "+goal.y+")");
		try { Thread.sleep(2000); } catch (InterruptedException e) {}
		active = true;
		while (active)
		{
			// send data request
			newDataRequest();
			try { Thread.sleep(100); } catch (InterruptedException e) {} // 100ms interval
			updateData();
			try { Thread.sleep(100); } catch (InterruptedException e) {} // 1s interval
		}
		return result;
	}

	// this test competency runs in the background
	@Override
	public boolean runsInBackground() {
		return false;
	}
	
	private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		return builder;
	}
	
	/**
	 * This method is responsible for opening the configuration file correctly.
	 * It should be different depending on the platform and the way it provides
	 * access to resources. 
	 * 
	 * @return InputStream for the configuration file of the migration competency.
	 */
	protected InputStream openConfigFile(String configFile) {
		InputStream inStream = null;
		
		try {
			inStream = new FileInputStream(configFile);
		} catch (FileNotFoundException e) {
			System.err.println("Could not find the migration configuration file.");
			System.err.println(e);
		} 
		
		return inStream;
	}
	
	public void importConfig(Document doc){
		Element root = (Element) doc.getElementsByTagName("map").item(0);
		if(root == null){
			return;
		}
	
		Element locations = (Element) root.getElementsByTagName("locations").item(0);
		NodeList locationList = locations.getElementsByTagName("location");
		
		System.out.println("Importing "+locationList.getLength()+" locations.");
		
		for(int i=0 ; i < locationList.getLength() ; i++){
			Element location = (Element) locationList.item(i);
			
			String name;
			Double x_min;
			Double x_max;
			Double y_min;
			Double y_max;
			
			name = location.getAttribute("name");
			x_min = new Double(location.getAttribute("x_min"));
			x_max = new Double(location.getAttribute("x_max"));
			y_min = new Double(location.getAttribute("y_min"));
			y_max = new Double(location.getAttribute("y_max"));
			
			this.locationList.put(name, new Rectangle(x_min, x_max, y_min, y_max));
		}
		
		Element positions = (Element) root.getElementsByTagName("positions").item(0);
		NodeList positionList = positions.getElementsByTagName("position");
		
		System.out.println("Importing "+positionList.getLength()+" positions.");
		
		for(int i=0 ; i < positionList.getLength() ; i++){
			Element position = (Element) positionList.item(i);
			
			String name;
			Double x;
			Double y;
			Double a;
			
			name = position.getAttribute("name");
			x = new Double(position.getAttribute("x"));
			y = new Double(position.getAttribute("y"));
			a = new Double(position.getAttribute("a"));
			
			this.positionList.put(name, new Coordinate(x, y, a));
		}
	}
}
