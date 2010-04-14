package cmion.level2.migration;

import org.w3c.dom.Document;

import cmion.level2.competencies.Migration.Device;
import ion.Meta.Event;

public class MigrationStart extends Event {
	
	public final Device destination;
	public final Document document;
	
	public MigrationStart(Device destination, Document document) {
		this.destination = destination;
		this.document = document;
	}
}
