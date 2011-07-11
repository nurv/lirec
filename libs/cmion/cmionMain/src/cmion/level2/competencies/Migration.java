package cmion.level2.competencies;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.Simulation;
import ion.Meta.TypeSet;
import ion.Meta.Events.IAdded;
import ion.Meta.Events.IRemoved;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
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
import cmion.level2.migration.HaltMigration;
import cmion.level2.migration.IncomingInvite;
import cmion.level2.migration.IncomingMigration;
import cmion.level2.migration.InviteListener;
import cmion.level2.migration.InviteMigration;
import cmion.level2.migration.MessageDelivered;
import cmion.level2.migration.MessageReceived;
import cmion.level2.migration.MigrationComplete;
import cmion.level2.migration.MigrationFailed;
import cmion.level2.migration.MigrationStart;
import cmion.level2.migration.MigrationUtils;
import cmion.level2.migration.Reply;
import cmion.level2.migration.ResumeMigration;
import cmion.level2.migration.SynchronizationFailed;
import cmion.level2.migration.SynchronizationStart;
import cmion.level2.migration.Synchronize;
import cmion.level2.migration.Synchronizer;
import cmion.level2.migration.SynchronizerImpl;
import cmion.level3.EventRemoteAction;
import cmion.level3.MindAction;


public class Migration extends Competency {
	
	public static final String SUCCESS_TAG = "success";

	private HashMap<String, Device> deviceList;
	private Synchronizer sync;
	private InviteListener inviteListener;
	private String devicename;
	private int listenPort;
	private int listenInvitePort;
	private List<Element> migrationElements;
	private Document migrationDocument;
	private Device destination;
	private boolean occupied;
	private boolean finishedMigration;
	private boolean migrationSucceeded;
	private String configFile;
	private DocumentBuilder docBuilder; 
	private Set<Object> lockingObjects;
	private boolean migrationOnHalt;

	
	/* This class represents a Device to which the agent is able
	 * to migrate to.
	 * It contains relevant information regarding that Device.
	 */
	public class Device {

		private String name;
		private String host;
		private int port;
		private int inviteport;
		private boolean isAvailable;
		
		public Device(String name, String host, int port){
			this.name = name;
			this.host = host;
			this.port = port;
			this.inviteport = -1;
		}

		public Device(String name, String host, int port, int inviteport){
			this(name,host,port);
			this.inviteport = inviteport;
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
		
		public int getInvitePort() {
			return inviteport;
		}
	}
	
	public Migration(IArchitecture architecture, String configFile) {

		super(architecture);
		this.competencyName = "MigrationCompetency";
		this.competencyType = "Migration";
		
		this.configFile = configFile;
		this.lockingObjects = new HashSet<Object>();
		this.migrationOnHalt = false;
		
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

	@Override
	public void initialize() {
		this.deviceList = new HashMap<String, Device>();
		this.occupied = false;
		this.finishedMigration = false;
				
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
			this.sync = getNewSynchronizer(listenPort);
		} catch (IOException e) {
			System.out.println("Could not create migration listening socket.");
			e.printStackTrace();
			return;
		} catch (ParserConfigurationException e) {
			System.out.println("Could not create XML parser.");
			e.printStackTrace();
			return;
		}
		
		// set up the invite listener
		this.inviteListener = new InviteListener(listenInvitePort);
		inviteListener.start();
	
		available = true;
	}
	
	protected Synchronizer getNewSynchronizer(int listenPort) throws IOException, ParserConfigurationException{
		return new SynchronizerImpl(listenPort);
	}

	@Override
	public final void registerHandlers() 
	{
		super.registerHandlers();
		// registering all migration related handlers for all migrating and migration aware components
		MigrationUtils.registerAllComponents(Simulation.instance);
		
		// These handlers detect when the MigrationCompetency is added or
		// removed from the simulation and add/remove Synchronizer
		// element accordingly.
		this.getEventHandlers().add(new SimulationAddedHandler());
		this.getEventHandlers().add(new SimulationRemovedHandler());
		
		// Check if the Migration was added before the handlers were set
		// and add the Synchronizer if necessary.
		if(getSimulation() != null){
			getSimulation().getElements().add(sync);
			getSimulation().getEventHandlers().add(new InviteMigrationHandler());
			getSimulation().getEventHandlers().add(new IncomingInviteHandler());
		}
		
		this.getRequestHandlers().add(new MigrationExecuter());
		this.getEventHandlers().add(new MigrationStartHandler());
		
		this.sync.getEventHandlers().add(new ReceiveMessages());
		this.sync.getEventHandlers().add(new MessageDelivery());
		this.sync.getEventHandlers().add(new MessageFailure());
		this.sync.getEventHandlers().add(new SynchronizationStartRelay());		
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
		
		if(destination == null){
			System.out.println("Unknown Destination: "+parameters.get("DeviceName"));
			raise(new SynchronizationFailed());
			return false;
		}
		
		migrationDocument = docBuilder.newDocument();
		raise(new MigrationStart(destination, migrationDocument));
		
		while(!finishedMigration){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		finishedMigration = false;
		
		// free memory
		migrationElements = null;
		migrationDocument = null;
		
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
		
		// try to read inviteport
		if (config.getElementsByTagName("inviteport").getLength()>0)
		{
			Element invitePort = (Element) config.getElementsByTagName("inviteport").item(0);
			this.listenInvitePort = Integer.parseInt(invitePort.getAttribute("value"));
		}
		else listenInvitePort = -1;

		// try to read devicename
		if (config.getElementsByTagName("devicename").getLength()>0)
		{
			Element deviceName = (Element) config.getElementsByTagName("devicename").item(0);
			this.devicename = deviceName.getAttribute("value");
		}
		else devicename = "";

		
		Element agent = (Element) config.getElementsByTagName("agent").item(0);
		this.occupied = Boolean.parseBoolean(agent.getAttribute("active"));
		
		Element devices = (Element) root.getElementsByTagName("devices").item(0);
		NodeList deviceList = devices.getElementsByTagName("device");
		
		System.out.println("Importing "+deviceList.getLength()+" devices.");
		
		for(int i=0 ; i < deviceList.getLength() ; i++){
			Element device = (Element) deviceList.item(i);
						
			String name = device.getAttribute("name");
			String host = device.getAttribute("host");
			Integer port = new Integer(device.getAttribute("port"));
			if (device.getAttribute("inviteport").equals(""))
				this.deviceList.put(name, new Device(name, host, port));
			else
			{
				Integer inviteport = new Integer(device.getAttribute("inviteport"));
				this.deviceList.put(name, new Device(name, host, port, inviteport));				
			}			
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
			
			if(messageReceived.type.equals("migration")){
				if(!occupied){
					raise(new IncomingMigration(messageReceived.message));
					raiseMessages(messageReceived.message);
					occupied = true;
					sync.schedule(new Reply(replySuccess()));
				} else {
					System.out.println("Refusing agent because device is occupied.");
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
			Element success = (Element) delivered.message;
			
			if(success.getTagName().equals(SUCCESS_TAG)){
				raise(new MigrationComplete());
				occupied = false;
				migrationSucceeded();
			} else {
				raise(new MigrationFailed());
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
			SynchronizationStart synchStart = (SynchronizationStart) evt;
			
			Element syncElement = migrationDocument.createElement(Synchronizer.SYNC_TAG);
			Element migration = migrationDocument.createElement("migration");
			migrationDocument.appendChild(syncElement);
			syncElement.appendChild(migration);

			for (Element element : migrationElements) {
				migration.appendChild(element);
			}

			sync.replaceMessage(migrationDocument);

		}
	}
	
	private class MigrationExecuter extends RequestHandler{
		
		public MigrationExecuter() {
			super(new TypeSet(ExecuteMigration.class, HaltMigration.class, ResumeMigration.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			for(HaltMigration request : requests.get(HaltMigration.class)){
				lockingObjects.add(request.lockingObject);
			}
			
			for(ResumeMigration request : requests.get(ResumeMigration.class)){
				lockingObjects.remove(request.lockingObject);
			}
			
			if(!requests.get(ExecuteMigration.class).isEmpty() || migrationOnHalt){
				
				if(lockingObjects.isEmpty()){
					migrationOnHalt = false;
					sync.schedule(new Synchronize(destination.getHost(), destination.getPort()));
				} else {
					migrationOnHalt = true;
				}
			}
		}
	}
	
	private class MigrationStartHandler extends EventHandler{
		
		public MigrationStartHandler(){
			super(MigrationStart.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			schedule(new ExecuteMigration());
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
				getSimulation().getElements().remove(sync);
			}
		}
	}

	private class ExecuteMigration extends Request{
	}
	
	/** Event Handler for handling InviteMigration events, an event telling us we would like to invite an agent
	 *  to migrate into this embodiment. In this class the concrete invitation issuing is handled */
	private class InviteMigrationHandler extends EventHandler{
		
		public InviteMigrationHandler(){
			super(InviteMigration.class);
		}
		
		@Override
		public void invoke(IEvent evt) 
		{
			// check if we are occupied already, if so, there's no point inviting someone
			if (occupied) return;
			
			// check if we know our own device name, if not we cannot send an invitation
			if (devicename.equals("")) 
			{
				System.out.println("Cannot send invite, don't know my own device name");
				return;
			}
			
			// send an invite to all known devices (because we don't know where an agent is) to migrate over here
			for (Device d : deviceList.values())
			{
				try {
					// the invite is sent to the inviteport of the device, check if one was specified, otherwise skip device
					if (d.inviteport<0)	continue;
					Socket s = new Socket(d.host,d.inviteport);
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
					// the invite simply consists of our device name followed by a new line, afterwards we can clos the socket 
					bw.write(devicename);
					bw.newLine();
					bw.close();
					s.close();
					
				} 
				catch (UnknownHostException e) {} 
				catch (IOException e) {}
			}
		}
	}

	/** Event Handler for handling IncomingInvite events, an event telling us some embodiment would like to invite the agent
	 *  we are currently holding to migrate over. In this class the invitation is checked for validity and if it applies,
	 *  a remote action is raised so that the agent mind can decide on whether to follow the invitation */	
	private class IncomingInviteHandler extends EventHandler{
		
		public IncomingInviteHandler(){
			super(IncomingInvite.class);
		}
		
		@Override
		public void invoke(IEvent evt) 
		{
			// ignore the invitation if there is no agent here, to be invited
			if (!occupied) return;
			
			String inviter = ((IncomingInvite)evt).getInviter();
			
			// check if the inviter exists in our device list
			if (!deviceList.containsKey(inviter))
			{
				System.out.println("We have been invited to migrate to "+inviter+". Error: No such device known!");
				return;
			}
			System.out.println("We have been invited to migrate to "+inviter+".");

			
			// if we got this far, raise a remote action, telling the mind we've been invited
			// if the mind is interested, it can then trigger a migration
			MindAction ma = new MindAction(inviter,"MigrationInvitation",null);
			Migration.this.raise(new EventRemoteAction(ma));
		}
	}
		
}
