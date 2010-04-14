package cmion.level2.migration;

import org.w3c.dom.Element;

import ion.Meta.Event;

public class MessageReceived extends Event {
	
	public final Element message;
	public final String type;
	
	public MessageReceived(Element message){
		this.message = message;
		type = message.getTagName();
	}

}
