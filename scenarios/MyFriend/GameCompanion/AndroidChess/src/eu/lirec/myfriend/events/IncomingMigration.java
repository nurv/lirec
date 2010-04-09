package eu.lirec.myfriend.events;

import org.kxml2.kdom.Element;

import ion.Meta.Event;

public class IncomingMigration extends Event {

	public final Element fullMessage;
	
	public IncomingMigration(Element fullMessage) {
		this.fullMessage = fullMessage;
	}
	
}
