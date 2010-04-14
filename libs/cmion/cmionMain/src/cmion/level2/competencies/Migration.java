package cmion.level2.competencies;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;
import ion.Meta.Events.IAdded;
import ion.Meta.Events.IRemoved;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cmion.architecture.IArchitecture;
import cmion.level2.Competency;
import cmion.level2.migration.IncomingMigration;
import cmion.level2.migration.MessageDelivered;
import cmion.level2.migration.MessageReceived;
import cmion.level2.migration.MigrationComplete;
import cmion.level2.migration.MigrationFailed;
import cmion.level2.migration.MigrationStart;
import cmion.level2.migration.Reply;
import cmion.level2.migration.SynchronizationFailed;
import cmion.level2.migration.SynchronizationStart;
import cmion.level2.migration.Synchronize;
import cmion.level2.migration.Synchronizer;


public class Migration extends Competency {

	private HashMap<String, Device> deviceList;
	private Synchronizer sync;
	private int listenPort;
	private List<Element> migrationElements;
	private Document migrationDocument;
	private Device destination;
	private boolean occupied;
	private boolean finishedMigration;
	private boolean migrationSucceeded;
	private String configFile;
	private DocumentBuilder docBuilder; 
	
	/* This class represents a Device to which the agent is able
	 * to migrate to.
	 * It contains relevant information regarding that Device.
	 */
	public class Device {

		private String name;
		private String host;
		private int port;
		private boolean isAvailable;
		
		public Device(String name, String host, int port){
			this.name = name;
			this.host = host;
			this.port = port;
		}
		
		public boolean isAvailable(){
			return isAvailable;
		}

		public String getName() {
			return name;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}
	}
	
	public Migration(IArchitecture architecture, String configFile) {

		super(architecture);
		competencyName = "MigrationCompetency";
		competencyType = "Migration";
		this.configFile = configFile;
		
		try {
			this.docBuilder = createDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is responsible for opening the configuration file correctly.
	 * It should be different depending on the platform and the way it provides
	 * access to resources. 
	 * 
	 * @return InputStream for the conifguration file of the migration competency.
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

	@Override
	public void initialize() {
		this.deviceList = new HashMap<String, Device>();
		this.occupied = false;
		this.finishedMigration = false;
		
		// These handlers detect when the MigrationCompetency is added or
		// removed from the simulation and add/remove Synchronization
		// element accordingly.
		this.getEventHandlers().add(new SimulationAddedHandler());
		this.getEventHandlers().add(new SimulationRemovedHandler());
		
		this.getRequestHandlers().add(new MigrationExecuter());
		
		//Get and parse the initialization file
		
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


		//Set up the synchronization element
		try {
			this.sync = new Synchronizer(listenPort);
		} catch (IOException e) {
			System.out.println("Could not create migration listening socket.");
			e.printStackTrace();
			return;
		} catch (ParserConfigurationException e) {
			System.out.println("Could not create XML parser.");
			e.printStackTrace();
			return;
		}
		this.sync.getEventHandlers().add(new ReceiveMessages());
		this.sync.getEventHandlers().add(new MessageDelivery());
		this.sync.getEventHandlers().add(new MessageFailure());
		this.sync.getEventHandlers().add(new SynchronizationStartRelay());
		
		available = true;
	}

	@Override
	public boolean runsInBackground() {
		return false;
	}
	
	@Override
	protected boolean competencyCode(HashMap<String, String> parameters) {		
		
		if(!occupied){
			System.out.println("There is no one here to migrate.");
			raise(new SynchronizationFailed());
			return false;
		}
		
		migrationElements = new ArrayList<Element>();
		destination = deviceList.get(parameters.get("DeviceName"));
		migrationDocument = docBuilder.newDocument();
		raise(new MigrationStart(destination, migrationDocument));
		schedule(new ExecuteMigration());
		
		while(!finishedMigration){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		finishedMigration = false;

		return migrationSucceeded;
	}
	
	private DocumentBuilder createDocumentBuilder() throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		return builder;
	}
	
	private void migrationSucceeded(){
		migrationSucceeded = true;
		finishedMigration = true;
	}
	
	private void migrationFailed(){
		migrationSucceeded = false;
		finishedMigration = true;
	}
	
	public void addMigrationData(Element xmlElement){
		System.out.println("Adding Migration Data");
		migrationElements.add(xmlElement);
	}
	
	public Map<String, Device> getDeviceList(){
		return this.deviceList;
	}
	
	public Set<Device> getAllDevices(){
		return new HashSet<Device>(this.deviceList.values());
	}
	
	public Set<Device> getAvailableDevices(){
		HashSet<Device> availableDevices = new HashSet<Device>();
		
		for (Device device : this.deviceList.values()) {
			if(device.isAvailable()){
				availableDevices.add(device);
			}
		}
		
		return availableDevices;
	}
	
	public void importConfig(Document doc){
		Element root = (Element) doc.getElementsByTagName("migrationconfig").item(0);
		if(root == null){
			return;
		}

		Element config = (Element) root.getElementsByTagName("configuration").item(0);
		Element listenPort = (Element) config.getElementsByTagName("listenport").item(0);
		this.listenPort = Integer.parseInt(listenPort.getAttribute("value"));
		
		Element agent = (Element) config.getElementsByTagName("agent").item(0);
		this.occupied = Boolean.parseBoolean(agent.getAttribute("active"));
		
		Element devices = (Element) root.getElementsByTagName("devices").item(0);
		NodeList deviceList = devices.getElementsByTagName("device");
		
		System.out.println("Importing "+deviceList.getLength()+" devices.");
		
		for(int i=0 ; i < deviceList.getLength() ; i++){
			Element device = (Element) deviceList.item(i);
			
			String name;
			String host;
			Integer port;
			
			name = device.getAttribute("name");
			host = device.getAttribute("host");
			port = new Integer(device.getAttribute("port"));
			
			this.deviceList.put(name, new Device(name, host, port));
		}
	}
	
	private class ReceiveMessages extends EventHandler{
		
		public ReceiveMessages() {
			super(MessageReceived.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			MessageReceived messageReceived = (MessageReceived) evt;

			if(messageReceived.type.equals("status")){
				//TODO send back message indicating if device is occupied by another agent.
			}
			
			if(messageReceived.type.equals("success")){
				raise(new MigrationComplete());
			}
			
			if(messageReceived.type.equals("failure")){
				raise(new MigrationFailed());
			}
			
			if(messageReceived.type.equals("migration")){
				if(!occupied){
					raise(new IncomingMigration(messageReceived.message));
					raiseMessages(messageReceived.message);
					occupied = true;
					sync.schedule(new Reply(replySuccess()));
				} else {
					sync.schedule(new Reply(replyFailure()));
				}
			}
		}
		
		private void raiseMessages(Element message){
			
			NodeList nodes = message.getChildNodes();
			
			for(int i=0 ; i < nodes.getLength() ; i++){
				Node node = nodes.item(i);
				
				if (node.getNodeType() == Node.ELEMENT_NODE){
					raise(new MessageReceived((Element) node));
				}
			}
		}
		
		private Document replySuccess(){
			Document reply = docBuilder.newDocument();
			
			Element success = reply.createElement("success");
			reply.appendChild(success);
			
			return reply;
		}
		
		private Document replyFailure(){
			Document reply = docBuilder.newDocument();
			
			Element failure = reply.createElement("failure");
			failure.setAttribute("reason", "Occupied");
			reply.appendChild(failure);
			
			return reply;
		}
	}
	
	private class MessageDelivery extends EventHandler{
		public MessageDelivery() {
			super(MessageDelivered.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			MessageDelivered delivered = (MessageDelivered) evt;
			
			Element root = (Element) delivered.message.getElementsByTagName("success").item(0);
			
			if(root != null){
				raise(new MessageDelivered(delivered.message));
				occupied = false;
				migrationSucceeded();
			} else {
				migrationFailed();
			}
		}
	}
	
	private class MessageFailure extends EventHandler{
		public MessageFailure() {
			super(SynchronizationFailed.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			raise(new MigrationFailed());
			migrationFailed();
		}
	}
	
	private class SynchronizationStartRelay extends EventHandler {
		
		public SynchronizationStartRelay(){
			super(SynchronizationStart.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			System.out.println("Synchronization Start");
			SynchronizationStart synchStart = (SynchronizationStart) evt;
			
			Element migration = migrationDocument.createElement("migration");
			for (Element element : migrationElements) {
				Node adoptedNode = migrationDocument.adoptNode(element);
				migration.appendChild(adoptedNode);
			}
			
			sync.packMessage(migration);
		}
	}
	
	private class MigrationExecuter extends RequestHandler{
		
		public MigrationExecuter() {
			super(new TypeSet(ExecuteMigration.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			sync.schedule(new Synchronize(destination.getHost(), destination.getPort()));
		}
	}
	
	private class SimulationAddedHandler extends EventHandler  {
		
		public SimulationAddedHandler() {
			super(IAdded.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			IAdded added = (IAdded)evt;
			
			if (added.getItem().equals(Migration.this)){
				System.out.println("Adding Synchronizer to Simulation.\n"+sync);
				System.out.println(added.getItem() +" - "+ added.getElement());
				
				getSimulation().getElements().add(sync);
			}
		}
	}
	
	private class SimulationRemovedHandler extends EventHandler {
		
		public SimulationRemovedHandler() {
			super(IRemoved.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			IRemoved removed = (IRemoved) evt;
			
			if (removed.getItem().equals(Migration.this)){
				System.out.println("Removing Synchronizer of Simulation.");
				
				getSimulation().getElements().remove(sync);
			}
		}
	}

	private class ExecuteMigration extends Request{
	}
}
