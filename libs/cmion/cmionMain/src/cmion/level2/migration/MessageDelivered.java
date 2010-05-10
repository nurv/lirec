package cmion.level2.migration;

import org.w3c.dom.Element;

import ion.Meta.Event;

public class MessageDelivered extends Event {

	public final Element message;
	
	public MessageDelivered(Element message) {
		this.message = message;
	}
}
