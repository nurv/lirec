package eu.lirec.myfriend.synchronization.requests;

import org.kxml2.kdom.Document;

import ion.Meta.Request;

public class Reply extends Request {

	public final Document message;
	
	public Reply(Document message) {
		this.message = message;
	}
}
