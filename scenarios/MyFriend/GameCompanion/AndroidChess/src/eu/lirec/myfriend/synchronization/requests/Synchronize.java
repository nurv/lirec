package eu.lirec.myfriend.synchronization.requests;

import ion.Meta.Request;

public class Synchronize extends Request {

	public final String host;
	public final int port;
	
	public Synchronize(String host, int port){
		this.host = host;
		this.port = port;
	}
	
}
