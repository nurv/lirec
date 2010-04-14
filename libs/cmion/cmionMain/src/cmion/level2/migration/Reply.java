package cmion.level2.migration;

import org.w3c.dom.Document;

import ion.Meta.Request;

public class Reply extends Request {

	public final Document message;
	
	public Reply(Document message) {
		this.message = message;
	}
}
