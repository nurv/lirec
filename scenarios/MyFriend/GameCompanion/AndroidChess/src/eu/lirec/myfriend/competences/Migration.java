package eu.lirec.myfriend.competences;

import ion.Meta.EventHandler;
import ion.Meta.IEvent;
import ion.Meta.IReadOnlyQueueSet;
import ion.Meta.Request;
import ion.Meta.RequestHandler;
import ion.Meta.TypeSet;
import ion.Meta.Events.IAdded;
import ion.Meta.Events.IRemoved;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import eu.lirec.myfriend.Device;
import eu.lirec.myfriend.events.IncomingMigration;
import eu.lirec.myfriend.events.MigrationStart;
import eu.lirec.myfriend.requests.Migrate;
import eu.lirec.myfriend.synchronization.Synchronizer;
import eu.lirec.myfriend.synchronization.events.MessageDelivered;
import eu.lirec.myfriend.synchronization.events.MessageReceived;
import eu.lirec.myfriend.synchronization.events.SynchronizationFailed;
import eu.lirec.myfriend.synchronization.events.SynchronizationStart;
import eu.lirec.myfriend.synchronization.requests.Reply;
import eu.lirec.myfriend.synchronization.requests.Synchronize;

public class Migration extends Competence {
	
	private HashMap<String, Device> deviceList;
	private Synchronizer sync;
	private int listenPort;
	private List<Element> migrationElements;
	private Device destination;
	private boolean occupied;
	private View agent;
	private Handler handlerUI;
	private Animation in;
	private Animation out;
	
	public Migration(XmlPullParser parser) throws IOException, XmlPullParserException{
		
		this.deviceList = new HashMap<String, Device>();
		this.occupied = true;
		this.getRequestHandlers().add(new MigrationManager());
		this.getEventHandlers().add(new SimulationAddedHandler());
		this.getEventHandlers().add(new SimulationRemovedHandler());
		
		this.getRequestHandlers().add(new MigrationExecuter());
		
		importConfig(parser);
		
		//Set up the synchronization element
		this.sync = new Synchronizer(listenPort);
		this.sync.getEventHandlers().add(new ReceiveMessages());
		this.sync.getEventHandlers().add(new MessageDelivery());
		this.sync.getEventHandlers().add(new MessageFailure());
		this.sync.getEventHandlers().add(new SynchronizationStartRelay());
	}
	
	@Override
	public void onDestroy() {
		this.sync.onDestroy();
	}
	
	public void enableAnimations(View agent, Context context, Handler handlerUI){
		this.agent = agent;
		this.handlerUI = handlerUI;
		in = AnimationUtils.makeInAnimation(context, false);
		out = AnimationUtils.makeOutAnimation(context, true);
		in.setFillAfter(true);
		out.setFillAfter(true);
	}
	
	public void addMigrationData(Element xmlElement){
//		sync.packMessage(xmlElement);
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
	
	private Device getDevice(String host, int port){
		
		for (Device device : deviceList.values()) {
			if(device.getHost().equals(host) && device.getPort() == port){
				return device;
			}
		}
		
		return null;
	}
	
	public void importConfig(XmlPullParser parser) throws IOException, XmlPullParserException{
		Document document = new Document();
		document.parse(parser);
		
		org.kxml2.kdom.Element root = document.getRootElement();

		org.kxml2.kdom.Element config = root.getElement("", "configuration");
		this.listenPort = Integer.parseInt(config.getElement("", "listenport").getAttributeValue("", "value"));
		
		org.kxml2.kdom.Element devices = root.getElement("", "devices");
		
		for(int i=0 ; i < devices.getChildCount() ; i++){
			org.kxml2.kdom.Element device = devices.getElement(i);
			
			if (device != null){
				String name;
				String host;
				Integer port;
				
				name = device.getAttributeValue("", "name");
				host = device.getAttributeValue("", "host");
				port = new Integer(device.getAttributeValue("", "port"));
				
				this.deviceList.put(name, new Device(name, host, port));
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

			System.out.println("Receiving Messages");
			if(messageReceived.type.equals("status")){
				//TODO send back message indicating if device is occupied by another agent.
			}
			
			if(messageReceived.type.equals("migration")){
				if(!occupied){
					raise(new IncomingMigration(messageReceived.message));
					raiseMessages(messageReceived.message);
					occupied = true;
					System.out.println("Occupied");
					sync.schedule(new Reply(replySuccess()));
					
					if(agent != null){
						handlerUI.post(new AnimateIn());
						System.out.println("Just came in.");
					}
				} else {
					sync.schedule(new Reply(replyFailure()));
				}
			}
		}
		
		private void raiseMessages(Element message){
			
			for(int i=0 ; i < message.getChildCount() ; i++){
				org.kxml2.kdom.Element element = message.getElement(i);
				
				if (element != null){
					raise(new MessageReceived(element));
				}
			}
		}
		
		private Document replySuccess(){
			Document reply = new Document();
			
			Element success = reply.createElement("", "success");
			reply.addChild(Node.ELEMENT, success);
			
			return reply;
		}
		
		private Document replyFailure(){
			Document reply = new Document();
			
			Element failure = reply.createElement("", "failure");
			failure.setAttribute("", "reason", "Occupied");
			reply.addChild(Node.ELEMENT, failure);
			
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
			
			if(delivered.message.getRootElement().getName().equals("success")){
				raise(new MessageDelivered(delivered.message));
				occupied = false;
				System.out.println("Vacant");
			} else {
				if(agent != null){
					handlerUI.post(new AnimateIn());
					System.out.println("I'm back. Couldn't migrate");
				}
			}
			
		}
	}
	
	private class MessageFailure extends EventHandler{
		public MessageFailure() {
			super(SynchronizationFailed.class);
		}
		
		@Override
		public void invoke(IEvent evt) {
			raise((SynchronizationFailed) evt);
			if(agent != null){
				handlerUI.post(new AnimateIn());
				System.out.println("I'm back. It's possible I've been cloned.");
			}
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
			
			Element migration = synchStart.newElement("", "migration");
			for (Element element : migrationElements) {
				migration.addChild(Node.ELEMENT, element);
			}
			
			sync.packMessage(migration);
//			raise(new MigrationStart(getDevice(synchStart.host, synchStart.port), synchStart));			
		}
	}
	
	private class MigrationManager extends RequestHandler{
		
		public MigrationManager() {
			super(new TypeSet(Migrate.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			if(!occupied){
				raise(new SynchronizationFailed());
				return;
			}
			
			for (Migrate request : requests.get(Migrate.class)) {
				System.out.println("Migration Manager");
				migrationElements = new ArrayList<Element>();
				destination = deviceList.get(request.deviceName);
				raise(new MigrationStart(destination));
				schedule(new ExecuteMigration());
				if(agent != null){
					handlerUI.post(new AnimateOut());
					System.out.println("Going away to another place");
				}
//				Device destination = deviceList.get(request.deviceName);
//				sync.schedule(new Synchronize(destination.getHost(), destination.getPort()));
			}
		}
	}
	
	private class MigrationExecuter extends RequestHandler{
		
		public MigrationExecuter() {
			super(new TypeSet(ExecuteMigration.class));
		}
		
		@Override
		public void invoke(IReadOnlyQueueSet<Request> requests) {
			
			try {
				sync.schedule(new Synchronize(destination.getHost(), destination.getPort()));
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Migration Executer");
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
	
	private class AnimateIn implements Runnable {
		
		@Override
		public void run() {
			agent.startAnimation(in);
		}
	}
	
	private class AnimateOut implements Runnable {
		
		@Override
		public void run() {
			agent.startAnimation(out);
		}
	}
}
