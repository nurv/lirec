package eu.lirec.myfriend.synchronization.events;

import org.kxml2.kdom.Element;

import ion.Meta.Event;

public class MessageReceived extends Event {
	
	public final Element message;
	public final String type;
	
	public MessageReceived(Element message){
		this.message = message;
		type = message.getName();
	}

}
