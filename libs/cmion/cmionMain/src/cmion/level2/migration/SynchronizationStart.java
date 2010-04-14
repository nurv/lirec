package cmion.level2.migration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ion.Meta.Event;

public class SynchronizationStart extends Event {

	public final String host;
	public final int port;
	private final Document doc;
	
	public SynchronizationStart(String host, int port, Document doc) {
		this.host = host;
		this.port = port;
		this.doc = doc;
	}
	
	public Element newElement(String name){
		return this.doc.createElement(name);
	}
}
