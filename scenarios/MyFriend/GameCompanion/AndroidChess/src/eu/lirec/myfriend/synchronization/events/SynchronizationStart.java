package eu.lirec.myfriend.synchronization.events;

import org.kxml2.kdom.Element;

import ion.Meta.Event;

public class SynchronizationStart extends Event {

	public final String host;
	public final int port;
	private final Element element;
	
	public SynchronizationStart(String host, int port, Element element) {
		this.host = host;
		this.port = port;
		this.element = element;
	}
	
	public Element newElement(String namespace, String name){
		return this.element.createElement(namespace, name);
	}
}
