package eu.lirec.myfriend.events;

import org.kxml2.kdom.Element;

import eu.lirec.myfriend.Device;
import eu.lirec.myfriend.synchronization.events.SynchronizationStart;
import ion.Meta.Event;

public class MigrationStart extends Event {
	
	public final Device destination;
	private final SynchronizationStart syncStart;
	
	public MigrationStart(Device destination, SynchronizationStart syncStart) {
		this.destination = destination;
		this.syncStart = syncStart;
	}
	
	public MigrationStart(Device destination) {
		this.destination = destination;
		this.syncStart = null;
	}

	public Element newElement(String namespace, String name){
		if(syncStart == null){
			Element element = new Element();
			element.setName(name);
			element.setNamespace(namespace);
			return element;
		} else {
			return this.syncStart.newElement(namespace, name);
		}
	}
}
