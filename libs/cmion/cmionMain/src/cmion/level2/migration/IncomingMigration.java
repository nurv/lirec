package cmion.level2.migration;


import org.w3c.dom.Element;

import ion.Meta.Event;

public class IncomingMigration extends Event {

	public final Element fullMessage;
	
	public IncomingMigration(Element fullMessage) {
		this.fullMessage = fullMessage;
	}
	
}
