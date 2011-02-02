package cmion.addOns.samgar.competencies.player;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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
import cmion.storage.CmionStorageContainer;


public class LocalizeSamgarCompetency extends SamgarCompetency {

	private boolean active=false; 
	private boolean result=false;
	
	private PlayerLocalize localize;
	private PlayerPosition2d position2d;
	
	private String configFile;
	private DocumentBuilder docBuilder; 
	private HashMap<String, Coordinate> positionList;
	private HashMap<String, Rectangle> locationList;
	
	boolean readyForNewData=true;
	
	private String agentName = "SpiritOfTheBuilding";
	
	public LocalizeSamgarCompetency(IArchitecture architecture, String configFile, String activeAgentName) 
	{
		super(architecture);
		this.competencyName ="LocalizeSamgarCompetency";
		this.competencyType ="DefaultLocalization";
		
		agentName = activeAgentName;
		
		localize = new PlayerLocalize();
		position2d = new PlayerPosition2d();
		
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
	}
	
	void newDataRequest()
	{
		Bottle b = this.prepareBottle();
		localize.setReq(b, false, false);
		this.sendBottle();
		b = this.prepareBottle();
		try { Thread.sleep(200); } catch (InterruptedException e) {}
		position2d.setReq(b, false, true, false, false);
		this.sendBottle();
		readyForNewData=false;
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
	}
	
	void updateData()
	{
		if (position2d.updated) 
		{
			updateLocation();
		}
	}
		
	void updateLocation()
	{
	    for (Map.Entry<String, Rectangle> e : locationList.entrySet()){
	    	if (e.getValue().contains(position2d.pose))
		    {
	    		//System.out.println(e.getKey());
		    	CmionStorageContainer user = architecture.getWorldModel().getAgent(agentName);
		    	if (user!=null) 
		    		user.requestSetProperty("location", e.getKey());
				break;
		    }
	    }
	}
	
	@Override
	public void onRead(Bottle b) 
	{
		//System.out.println("received bottle :"+b);
		if(b.get(0).asInt() == PlayerProxy.Types.Localize.ordinal())
		{
			localize.update(b);
		}
		if(b.get(0).asInt() == PlayerProxy.Types.Position2d.ordinal())
		{
			position2d.update(b);
		}
	}

	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) 
	{
		prepare();
		Bottle b = this.prepareBottle();
		this.position2d.enable(b);
		this.sendBottle();
		try { Thread.sleep(1000); } catch (InterruptedException e) {}
		active = true;
		
		while (active)
		{
			// send data request
			newDataRequest();
			updateData();
		}
		return result;
	}

	// this test competency runs in the background
	@Override
	public boolean runsInBackground() {
		return true;
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
