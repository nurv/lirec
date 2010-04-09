package eu.lirec.myfriend.synchronization.events;

import org.kxml2.kdom.Document;

import ion.Meta.Event;

public class MessageDelivered extends Event {

	public final Document message;
	
	public MessageDelivered(Document message) {
		this.message = message;
	}
}
