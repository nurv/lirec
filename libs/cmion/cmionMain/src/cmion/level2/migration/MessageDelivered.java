package cmion.level2.migration;

import org.w3c.dom.Document;

import ion.Meta.Event;

public class MessageDelivered extends Event {

	public final Document message;
	
	public MessageDelivered(Document message) {
		this.message = message;
	}
}
